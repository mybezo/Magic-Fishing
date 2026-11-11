package id.irwan.magicfishing.machines

import id.irwan.magicfishing.Items
import id.irwan.magicfishing.icon
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu
import org.bukkit.inventory.ItemStack
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player

class MachineMenu(id: String, title: String) : BlockMenuPreset(id, title) {

    private fun layout(): MachineLayout? = MachineLayout.registry[getID()]

    override fun init() {
        val l = layout() ?: return
        val none = ChestMenuUtils.getEmptyClickHandler()
        l.decorations.forEach { (slot, item) -> addItem(slot, item, none) }
        addItem(l.statusSlot, icon(Material.GRAY_STAINED_GLASS_PANE, "&7Idle"), none)
    }

    override fun canOpen(b: Block, p: Player): Boolean =
        p.hasPermission("slimefun.inventory.bypass") ||
            Slimefun.getProtectionManager().hasPermission(p, b.location, Interaction.INTERACT_BLOCK)

    /**
     * Cargo may pull outputs. Fish inputs are placed by hand (they are never consumed); the only
     * cargo input is Fish Pellet into the Magic Aquarium pellet slot.
     */
    override fun getSlotsAccessedByItemTransport(flow: ItemTransportFlow): IntArray {
        val l = layout() ?: return IntArray(0)
        return if (flow == ItemTransportFlow.WITHDRAW) l.outputs else IntArray(0)
    }

    override fun getSlotsAccessedByItemTransport(menu: DirtyChestMenu, flow: ItemTransportFlow, item: ItemStack): IntArray {
        val l = layout() ?: return IntArray(0)
        if (flow == ItemTransportFlow.INSERT) {
            return if (l.pelletSlot >= 0 && Items.isPellet(item)) intArrayOf(l.pelletSlot) else IntArray(0)
        }
        return l.outputs
    }
}
