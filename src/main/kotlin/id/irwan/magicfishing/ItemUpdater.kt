package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

/**
 * Brings Magic Fishing items made by older versions (different lore, bucket/head fish, old glint)
 * up to date, so old and new items are the same item again: they stack and match in Networks
 * (auto crafters, Quantum storage) and other plugins that compare lore.
 *
 * Runs when a player joins (inventory, armor, off hand, ender chest) and when any inventory is
 * opened (chests, machines, ...). The amount is kept; a rod keeps its bait and durability.
 * Items stored virtually by other plugins (e.g. Networks Quantum storage, blueprints) are not
 * touched; they are updated once taken out into a normal inventory.
 */
class ItemUpdater : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        update(e.player.inventory)
        update(e.player.enderChest)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onOpen(e: InventoryOpenEvent) {
        update(e.inventory)
        (e.player as? Player)?.let { update(it.inventory) }
    }

    private fun update(inventory: Inventory) {
        for (slot in 0 until inventory.size) {
            val stack = inventory.getItem(slot) ?: continue
            val updated = updated(stack) ?: continue
            inventory.setItem(slot, updated)
        }
    }

    companion object {

        /** Materials Magic Fishing items use (current and older versions). Anything else is skipped fast. */
        private val MATERIALS: Set<Material> = java.util.EnumSet.of(
            // fish (current, old buckets, old heads)
            Material.COD, Material.SALMON, Material.TROPICAL_FISH,
            Material.COD_BUCKET, Material.SALMON_BUCKET, Material.TROPICAL_FISH_BUCKET, Material.PUFFERFISH_BUCKET,
            Material.PLAYER_HEAD,
            // pellet, aqua core, rod
            Material.BROWN_DYE, Material.HEART_OF_THE_SEA, Material.FISHING_ROD,
            // machines (normal and compressed)
            Material.PRISMARINE, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE, Material.LIGHT_BLUE_STAINED_GLASS
        )

        /** The up-to-date version of [stack], or null if it is not a Magic Fishing item or already current. */
        fun updated(stack: ItemStack): ItemStack? {
            if (stack.type.isAir || stack.type !in MATERIALS) return null
            val sfItem = SlimefunItem.getByItem(stack) ?: return null
            if (sfItem.addon !== Addon) return null

            val target = sfItem.item.clone()
            target.amount = stack.amount
            if (Items.isRod(target)) {
                // keep the bait and the durability of the rod
                val bait = Bait.count(stack)
                if (bait > 0) Bait.setCount(target, bait)
                val damage = (stack.itemMeta as? Damageable)?.damage ?: 0
                if (damage > 0) {
                    val meta = target.itemMeta
                    if (meta is Damageable) {
                        meta.damage = damage
                        target.itemMeta = meta
                    }
                }
            }
            return if (target.isSimilar(stack)) null else target
        }
    }
}
