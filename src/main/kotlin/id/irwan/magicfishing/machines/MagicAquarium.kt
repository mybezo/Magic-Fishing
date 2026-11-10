package id.irwan.magicfishing.machines

import id.irwan.magicfishing.Items
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import org.bukkit.inventory.ItemStack

class MagicAquarium(
    group: ItemGroup,
    item: SlimefunItemStack,
    recipe: Array<ItemStack?>,
    energy: Int,
    seconds: Int,
    private val pelletsPerFish: Int
) : MagicMachine(group, item, recipe, energy, seconds, LAYOUT) {

    /** Pellets already used for a progress of [ticks] out of [needed]. */
    private fun pelletsUsed(ticks: Int, needed: Int): Int = (ticks.toLong() * pelletsPerFish / needed).toInt()

    /** Pellets are consumed gradually: 1 pellet every (100 / pelletsPerFish)% of progress. */
    override fun consumeProgress(menu: BlockMenu, before: Int, after: Int, needed: Int): String? {
        val slot = layout.inputs[2]
        val stack = menu.getItemInSlot(slot)
        val hasPellet = stack != null && Items.isPellet(stack)
        val amount = pelletsUsed(after, needed) - pelletsUsed(before, needed)
        if (amount <= 0) {
            // Do not start a new fish without pellets; running out mid-way only pauses (progress is kept).
            return if (before == 0 && !hasPellet) "Needs Fish Pellet" else null
        }
        if (!hasPellet || stack!!.amount < amount) return "Needs Fish Pellet"
        menu.consumeItem(slot, amount)
        return null
    }

    override fun statusLore(progress: Int, needed: Int): Array<String> =
        arrayOf("&7Pellet used: &6${pelletsUsed(progress, needed)}&7/&6$pelletsPerFish")

    override fun findOutput(menu: BlockMenu): WorkResult {
        val a = Items.fishOf(menu.getItemInSlot(layout.inputs[0]))
        val b = Items.fishOf(menu.getItemInSlot(layout.inputs[1]))
        if (a == null || b == null) return WorkResult(null, "Needs 2 parent fish")
        if (a.id != b.id) return WorkResult(null, "Parent fish must be the same type")
        return WorkResult(Items.fishItem(a), "")
    }

    companion object {
        /*
         * 6-row layout:
         *  B B B P P P B B B
         *  B F B P p P B F B
         *  B B B P P P B B B
         *  O O O o o o o o o
         *  O S O o o o o o o
         *  O O O o o o o o o
         * B = fish border, F = fish, P = pellet border, p = pellet,
         * O = status border, S = status, o = output
         */
        val LAYOUT = MachineLayout(
            inputs = intArrayOf(10, 16, 13),
            outputs = intArrayOf(30, 31, 32, 33, 34, 35, 39, 40, 41, 42, 43, 44, 48, 49, 50, 51, 52, 53),
            statusSlot = 37,
            decorations = MachineLayout.decorations(
                MachineLayout.ring(10) to MachineLayout.FISH_BORDER,
                MachineLayout.ring(16) to MachineLayout.FISH_BORDER,
                MachineLayout.ring(13) to MachineLayout.PELLET_BORDER,
                MachineLayout.ring(37) to MachineLayout.STATUS_BORDER
            )
        )
    }
}
