package id.irwan.magicfishing.machines

import id.irwan.magicfishing.Fishes
import id.irwan.magicfishing.Items
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import org.bukkit.inventory.ItemStack

class FishRefinery(
    group: ItemGroup,
    item: SlimefunItemStack,
    recipe: Array<ItemStack?>,
    energy: Int,
    seconds: Int
) : MagicMachine(group, item, recipe, energy, seconds, LAYOUT), RecipeDisplayItem {

    override fun findOutput(menu: BlockMenu): WorkResult {
        val fish = Items.fishOf(menu.getItemInSlot(layout.inputs[0]))
            ?: return WorkResult(null, "Insert a magic fish")
        val out = fish.output() ?: return WorkResult(null, "Output for this fish not found")
        return WorkResult(out, "")
    }

    override fun getDisplayRecipes(): List<ItemStack> =
        Fishes.all.flatMap { listOf(Items.fishItem(it), it.outputDisplay()) }

    companion object {
        /*
         * 6-row layout:
         *  B B B G G G G G G
         *  B F B G o o o o G
         *  B B B G o o o o G
         *  O O O G o o o o G
         *  O S O G o o o o G
         *  O O O G G G G G G
         * B = fish border, F = fish, O = status border, S = status, G = output border, o = output
         */
        val LAYOUT = MachineLayout(
            inputs = intArrayOf(10),
            outputs = intArrayOf(13, 14, 15, 16, 22, 23, 24, 25, 31, 32, 33, 34, 40, 41, 42, 43),
            statusSlot = 37,
            decorations = MachineLayout.decorations(
                MachineLayout.ring(10) to MachineLayout.FISH_BORDER,
                MachineLayout.ring(37) to MachineLayout.STATUS_BORDER,
                intArrayOf(3, 4, 5, 6, 7, 8, 12, 17, 21, 26, 30, 35, 39, 44, 48, 49, 50, 51, 52, 53) to MachineLayout.OUTPUT_BORDER
            )
        )
    }
}
