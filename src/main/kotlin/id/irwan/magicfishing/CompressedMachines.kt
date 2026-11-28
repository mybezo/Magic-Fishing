package id.irwan.magicfishing

import id.irwan.magicfishing.machines.FishRefinery
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

/** An item group that never shows in the guide; used for the lower compressed tiers. */
class HiddenGroup(key: NamespacedKey, item: ItemStack) : ItemGroup(key, item) {
    override fun isVisible(p: Player): Boolean = false
}

/**
 * Compressed machines: N machines combined into one (x8, x16, x32, x64), configured in compressed.yml.
 *
 * Recipes use the Compressor: x8 = 8 of the original, every next tier = 2 of the previous one.
 * Only the highest enabled tier is listed in the guide; lower tiers stay craftable in a hidden group.
 * A compressed refinery keeps the original speed; one cycle does up to N operations at once.
 */
object CompressedMachines {

    val MULTIPLIERS = intArrayOf(8, 16, 32, 64)

    /** What is needed to build a compressed version of one Magic Fish Refinery tier. */
    class RefinerySpec(
        val id: String,
        val tier: Int,
        val block: Material,
        val energy: Int,
        val seconds: Double
    )

    /** Loads compressed.yml, adding missing options without touching existing values. */
    private fun loadConfig(plugin: MagicFishing, refineries: List<RefinerySpec>): YamlConfiguration {
        val file = File(plugin.dataFolder, "compressed.yml")
        if (!file.exists()) plugin.saveResource("compressed.yml", false)
        val cfg = YamlConfiguration.loadConfiguration(file)

        var changed = false
        fun default(path: String, value: Any) {
            if (!cfg.contains(path)) {
                cfg.set(path, value)
                changed = true
            }
        }
        default("enabled", true)
        MULTIPLIERS.forEach { default("multipliers.x$it", true) }
        refineries.forEach { default("machines.${it.id}", true) }
        if (changed) cfg.save(file)
        return cfg
    }

    /**
     * Registers the compressed refineries. Returns the number of registered items.
     * [visibleGroup] gets the highest tier, [hiddenGroup] the lower ones.
     */
    fun register(
        plugin: MagicFishing,
        refineries: List<RefinerySpec>,
        visibleGroup: ItemGroup,
        hiddenGroup: ItemGroup,
        lore: (energy: Int, timing: MachineSpeed.Timing) -> Array<String>
    ): Int {
        val cfg = loadConfig(plugin, refineries)
        if (!cfg.getBoolean("enabled", true)) return 0

        // A tier needs every lower tier (its recipe uses the previous tier).
        val tiers = MULTIPLIERS.takeWhile { cfg.getBoolean("multipliers.x$it", true) }
        if (tiers.isEmpty()) return 0

        var count = 0
        for (spec in refineries) {
            if (!cfg.getBoolean("machines.${spec.id}", true)) continue
            val base = SlimefunItem.getById(spec.id) ?: continue
            if (base.isDisabled) continue

            var previous: ItemStack = base.item
            var previousMultiplier = 1
            for (multiplier in tiers) {
                val id = "${spec.id}_X$multiplier"
                // Energy per tick is multiplied (Long, clamped); the speed stays the same.
                val energy = (spec.energy.toLong() * multiplier).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                val stack = SlimefunItemStack(
                    id, spec.block, "&aMagic Fish Refinery ${roman(spec.tier)} ($multiplier)",
                    *lore(energy, MachineSpeed.get(spec.id)),
                    "",
                    "&7Combines &a$multiplier &7machines into one."
                )
                val input = previous.clone().apply { amount = multiplier / previousMultiplier }
                val recipe = arrayOfNulls<ItemStack>(9).apply { this[0] = input }
                val group = if (multiplier == tiers.last()) visibleGroup else hiddenGroup

                FishRefinery(group, stack, RecipeType.COMPRESSOR, recipe, spec.energy, spec.seconds, multiplier)
                    .register(Addon)
                count++

                previous = stack
                previousMultiplier = multiplier
            }
        }
        return count
    }
}
