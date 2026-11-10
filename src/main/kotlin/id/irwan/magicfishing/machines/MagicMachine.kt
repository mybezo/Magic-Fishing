package id.irwan.magicfishing.machines

import id.irwan.magicfishing.icon
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import me.mrCookieSlime.Slimefun.api.BlockStorage
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.roundToInt

class WorkResult(val output: ItemStack?, val message: String)

/**
 * Electric machine whose inputs are NEVER consumed.
 * Uses [energy] J per Slimefun tick and produces 1 output every [seconds] seconds.
 */
abstract class MagicMachine(
    group: ItemGroup,
    item: SlimefunItemStack,
    recipe: Array<ItemStack?>,
    private val energy: Int,
    private val seconds: Int,
    protected val layout: MachineLayout
) : SlimefunItem(group, item, RecipeType.ENHANCED_CRAFTING_TABLE, recipe), EnergyNetComponent {

    private val progress = ConcurrentHashMap<Location, Int>()

    init {
        MachineLayout.registry[id] = layout
        MachineMenu(id, itemName)

        addItemHandler(
            object : BlockTicker() {
                override fun isSynchronized(): Boolean = false
                override fun tick(b: Block, item: SlimefunItem, data: Config) {
                    process(b)
                }
            },
            object : BlockBreakHandler(false, false) {
                override fun onPlayerBreak(e: BlockBreakEvent, item: ItemStack, drops: MutableList<ItemStack>) {
                    val loc = e.block.location
                    BlockStorage.getInventory(e.block)?.dropItems(loc, *layout.inputs, *layout.outputs)
                    progress.remove(loc)
                }
            }
        )
    }

    protected abstract fun findOutput(menu: BlockMenu): WorkResult

    override fun getEnergyComponentType(): EnergyNetComponentType = EnergyNetComponentType.CONSUMER

    override fun getCapacity(): Int = energy * 2

    private fun ticksNeeded(): Int {
        val rate = max(1, Slimefun.getTickerTask().tickRate)
        return max(1, (seconds * 20.0 / rate).roundToInt())
    }

    private fun setStatus(menu: BlockMenu, material: Material, text: String, vararg lore: String) {
        if (menu.hasViewer()) menu.replaceExistingItem(layout.statusSlot, icon(material, text, *lore))
    }

    private fun process(b: Block) {
        val menu = BlockStorage.getInventory(b) ?: return
        val loc = b.location
        val needed = ticksNeeded()

        val result = findOutput(menu)
        val output = result.output
        if (output == null) {
            progress.remove(loc)
            setStatus(menu, Material.RED_STAINED_GLASS_PANE, "&c${result.message}")
            return
        }

        if (getCharge(loc) < energy) {
            setStatus(menu, Material.ORANGE_STAINED_GLASS_PANE, "&6Not enough energy", "&7Needs $energy J per tick")
            return
        }

        val next = (progress[loc] ?: 0) + 1
        if (next >= needed) {
            if (!menu.fits(output, *layout.outputs)) {
                setStatus(menu, Material.YELLOW_STAINED_GLASS_PANE, "&eOutput is full")
                return
            }
            removeCharge(loc, energy)
            menu.pushItem(output.clone(), *layout.outputs)
            progress[loc] = 0
        } else {
            removeCharge(loc, energy)
            progress[loc] = next
        }

        val percent = (progress[loc] ?: 0) * 100 / needed
        setStatus(menu, Material.LIME_STAINED_GLASS_PANE, "&aWorking", "&7Progress: &f$percent%")
    }
}
