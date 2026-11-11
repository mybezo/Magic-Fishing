package id.irwan.magicfishing.machines

import id.irwan.magicfishing.icon
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class MachineLayout(
    val inputs: IntArray,
    val outputs: IntArray,
    val statusSlot: Int,
    val decorations: Map<Int, ItemStack>,
    /** Slot that cargo may insert Fish Pellet into (-1 = none). */
    val pelletSlot: Int = -1
) {
    companion object {
        /** Filled before the menu is created, because BlockMenuPreset calls init() inside its constructor. */
        val registry = ConcurrentHashMap<String, MachineLayout>()

        /** The 8 slots around [center] in a 9-wide chest. */
        fun ring(center: Int): IntArray =
            intArrayOf(center - 10, center - 9, center - 8, center - 1, center + 1, center + 8, center + 9, center + 10)

        fun decorations(vararg groups: Pair<IntArray, ItemStack>): Map<Int, ItemStack> {
            val map = HashMap<Int, ItemStack>()
            for ((slots, item) in groups) slots.forEach { map[it] = item }
            return map
        }

        val FISH_BORDER: ItemStack = icon(Material.BLUE_STAINED_GLASS_PANE, "&bFish Input")
        val PELLET_BORDER: ItemStack = icon(Material.BROWN_STAINED_GLASS_PANE, "&6Fish Pellet Input")
        val STATUS_BORDER: ItemStack = icon(Material.ORANGE_STAINED_GLASS_PANE, "&6Status")
        val OUTPUT_BORDER: ItemStack = icon(Material.GRAY_STAINED_GLASS_PANE, "&7Output")
    }
}
