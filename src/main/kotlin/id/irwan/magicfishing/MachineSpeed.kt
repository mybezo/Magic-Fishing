package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.math.abs

/**
 * Machine speed from speed.yml: every machine has a fixed base time and a speed multiplier
 * (default 1). Real time per output = base time / speed, rounded to Slimefun ticks
 * (default ticker: 0.5 s), never faster than one tick.
 */
object MachineSpeed {

    /** Base time in real seconds at speed 1, per machine id. */
    val BASE_SECONDS: Map<String, Double> = linkedMapOf(
        "MF_FISH_REFINERY_1" to 15.0,
        "MF_FISH_REFINERY_2" to 8.0,
        "MF_FISH_REFINERY_3" to 2.0,
        "MF_MAGIC_AQUARIUM" to 180.0
    )

    /** Speed multiplier and the resulting real seconds per output. */
    class Timing(val speed: Double, val seconds: Double)

    private val timings = HashMap<String, Timing>()

    fun get(id: String): Timing = timings[id] ?: Timing(1.0, BASE_SECONDS[id] ?: 1.0)

    /**
     * Loads speed.yml (created from the jar if missing), adds missing machines with speed 1 and keeps
     * existing values. On first creation, custom times from the old config.yml option
     * machines.<id>.seconds-per-output are converted to speeds; that old option is then removed.
     */
    fun load(plugin: MagicFishing, mainConfig: FileConfiguration) {
        val file = File(plugin.dataFolder, "speed.yml")
        val firstTime = !file.exists()
        if (firstTime) plugin.saveResource("speed.yml", false)
        val cfg = YamlConfiguration.loadConfiguration(file)
        var changed = false

        for ((id, base) in BASE_SECONDS) {
            val oldPath = "machines.$id.seconds-per-output"
            if (mainConfig.isSet(oldPath)) {
                val old = mainConfig.getDouble(oldPath)
                if (firstTime && old > 0 && abs(old - base) > 1e-9) {
                    val speed = Math.round(base / old * 100) / 100.0
                    cfg.set(id, number(speed))
                    plugin.logger.info("Moved $oldPath ($old s) to speed.yml: $id: ${format(speed)}")
                    changed = true
                }
                mainConfig.set(oldPath, null)
            }
            if (!cfg.isSet(id)) {
                cfg.set(id, 1)
                changed = true
            }
        }
        if (changed) cfg.save(file)

        val rate = maxOf(1, Slimefun.getTickerTask().tickRate)
        val tick = rate / 20.0
        timings.clear()
        for ((id, base) in BASE_SECONDS) {
            val raw = cfg.get(id)
            var speed = (raw as? Number)?.toDouble() ?: raw?.toString()?.toDoubleOrNull()
            if (speed == null || speed <= 0.0 || speed.isNaN() || speed.isInfinite()) {
                plugin.logger.warning("speed.yml: invalid $id: $raw (must be above 0). Using 1.")
                speed = 1.0
            }
            // Never faster than one Slimefun tick per output.
            val maxSpeed = base / tick
            if (speed > maxSpeed) {
                plugin.logger.warning("speed.yml: $id: ${format(speed)} is above the maximum ${format(maxSpeed)}, using the maximum.")
                speed = maxSpeed
            }
            val ticks = maxOf(1L, Math.round(base / speed / tick))
            timings[id] = Timing(speed, ticks * tick)
        }
    }

    /** "2" for whole numbers, "1.5" / "2.25" otherwise. */
    fun format(value: Double): String =
        if (value == Math.rint(value)) value.toLong().toString()
        else String.format(java.util.Locale.ROOT, "%.2f", value).trimEnd('0').trimEnd('.')

    private fun number(value: Double): Any = if (value == Math.rint(value)) value.toLong().toInt() else value
}
