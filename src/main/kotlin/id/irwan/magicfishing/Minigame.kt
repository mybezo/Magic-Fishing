package id.irwan.magicfishing

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Anti auto-fish minigame: after a magic fish bites, a boss bar shows a track with a green zone
 * and a moving marker. The player must sneak (any sneak toggle, so Bedrock touch works too)
 * while the marker is inside the zone. Missing or running out of time loses the fish.
 */
object Minigame {

    /** Track length in segments. */
    private const val TRACK = 30

    private class Difficulty(var zone: Int, var speed: Double)

    private val difficulty = mapOf(
        Rarity.COMMON to Difficulty(8, 0.6),
        Rarity.UNCOMMON to Difficulty(7, 0.7),
        Rarity.RARE to Difficulty(6, 0.9),
        Rarity.EPIC to Difficulty(4, 1.1),
        Rarity.LEGENDARY to Difficulty(3, 1.3)
    )

    var enabled = false
        private set
    private var timeTicks = 100
    private var graceTicks = 5
    private var tolerance = 1

    private class Session(
        val player: Player,
        val fish: FishType,
        val bar: BossBar,
        val zoneStart: Int,
        val zoneSize: Int,
        val speed: Double
    ) {
        var pos = Random.nextDouble(0.0, (TRACK - 1).toDouble())
        var dir = if (Random.nextBoolean()) 1 else -1
        var age = 0
    }

    private val sessions = HashMap<UUID, Session>()
    private var task: BukkitTask? = null

    fun load(cfg: FileConfiguration) {
        fun int(path: String, def: Int): Int { cfg.addDefault(path, def); return cfg.getInt(path, def) }
        fun dbl(path: String, def: Double): Double { cfg.addDefault(path, def); return cfg.getDouble(path, def) }
        cfg.addDefault("minigame.enabled", false)
        enabled = cfg.getBoolean("minigame.enabled", false)
        timeTicks = (dbl("minigame.seconds", 5.0) * 20).roundToInt().coerceAtLeast(20)
        graceTicks = int("minigame.grace-ticks", 5).coerceAtLeast(0)
        tolerance = int("minigame.tolerance", 1).coerceAtLeast(0)
        for ((rarity, d) in difficulty) {
            val path = "minigame.difficulty.${rarity.name}"
            d.zone = int("$path.zone-size", d.zone).coerceIn(1, TRACK)
            d.speed = dbl("$path.speed", d.speed).coerceIn(0.05, TRACK.toDouble())
        }
    }

    fun isPlaying(player: Player): Boolean = sessions.containsKey(player.uniqueId)

    fun start(player: Player, fish: FishType) {
        stop(player, success = false, silent = true)
        val d = difficulty.getValue(fish.rarity)
        val zoneStart = Random.nextInt(0, TRACK - d.zone + 1)
        val bar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID)
        bar.progress = 1.0
        bar.addPlayer(player)
        val session = Session(player, fish, bar, zoneStart, d.zone, d.speed)
        sessions[player.uniqueId] = session
        render(session)
        actionBar(player, "&bA magic fish is biting! &7Sneak when the &f&l| &7is in the &agreen &7zone")
        player.playSound(player.location, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1f, 1.2f)
        ensureTask()
    }

    /** Called on any sneak toggle (press or release). */
    fun input(player: Player) {
        val s = sessions[player.uniqueId] ?: return
        if (s.age < graceTicks) return
        val idx = s.pos.roundToInt()
        val hit = idx >= s.zoneStart - tolerance && idx <= s.zoneStart + s.zoneSize - 1 + tolerance
        stop(player, hit, silent = false)
    }

    fun stop(player: Player, success: Boolean, silent: Boolean) {
        val s = sessions.remove(player.uniqueId) ?: return
        s.bar.removeAll()
        if (silent) return
        if (success) {
            val item: ItemStack = Items.fishItem(s.fish)
            player.inventory.addItem(item).values.forEach { player.world.dropItemNaturally(player.location, it) }
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            actionBar(player, "&aCaught! &r${s.fish.name}")
            if (s.fish.rarity >= Rarity.EPIC) {
                player.sendMessage(color("&7You caught ${s.fish.rarity.display} &7fish: &r${s.fish.name}&7!"))
            }
        } else {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f)
            actionBar(player, "&cThe fish got away!")
        }
    }

    fun stopAll() {
        sessions.values.toList().forEach { stop(it.player, success = false, silent = true) }
        task?.cancel()
        task = null
    }

    private fun ensureTask() {
        if (task != null) return
        task = Bukkit.getScheduler().runTaskTimer(MagicFishing.instance, Runnable { tick() }, 1L, 1L)
    }

    private fun tick() {
        for (s in sessions.values.toList()) {
            if (!s.player.isOnline) { stop(s.player, success = false, silent = true); continue }
            s.age++
            if (s.age >= timeTicks) { stop(s.player, success = false, silent = false); continue }
            s.pos += s.dir * s.speed
            val max = (TRACK - 1).toDouble()
            if (s.pos >= max) { s.pos = max - (s.pos - max); s.dir = -1 }
            if (s.pos <= 0.0) { s.pos = -s.pos; s.dir = 1 }
            s.pos = s.pos.coerceIn(0.0, max)
            render(s)
        }
        if (sessions.isEmpty()) {
            task?.cancel()
            task = null
        }
    }

    private fun render(s: Session) {
        val idx = s.pos.roundToInt()
        val sb = StringBuilder()
        for (i in 0 until TRACK) {
            sb.append(
                when {
                    i == idx -> "&f&l|&r"
                    i >= s.zoneStart && i < s.zoneStart + s.zoneSize -> "&a|"
                    else -> "&8|"
                }
            )
        }
        s.bar.setTitle(color(sb.toString()))
        val left = 1.0 - s.age.toDouble() / timeTicks
        s.bar.progress = left.coerceIn(0.0, 1.0)
        s.bar.color = when {
            left > 0.5 -> BarColor.GREEN
            left > 0.25 -> BarColor.YELLOW
            else -> BarColor.RED
        }
    }

    private fun actionBar(player: Player, text: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(color(text)))
    }
}
