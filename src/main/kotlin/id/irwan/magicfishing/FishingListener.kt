package id.irwan.magicfishing

import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot

class FishingListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onFish(e: PlayerFishEvent) {
        if (e.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val inv = e.player.inventory
        if (!Items.isRod(inv.itemInMainHand) && !Items.isRod(inv.itemInOffHand)) return
        val caught = e.caught as? Item ?: return

        if (!takePellet(e.player)) {
            e.player.sendMessage(color("&cYou need &6Fish Pellet &cin your inventory to catch magic fish."))
            return
        }
        val fish = Fishes.roll() ?: return
        caught.itemStack = Items.fishItem(fish)
        if (fish.rarity >= Rarity.EPIC) {
            e.player.sendMessage(color("&7You caught ${fish.rarity.display} &7fish: &r${fish.name}&7!"))
        }
    }

    /** Removes exactly 1 Fish Pellet from the player's inventory. */
    private fun takePellet(player: Player): Boolean {
        val inv = player.inventory
        for (slot in 0 until inv.size) {
            val stack = inv.getItem(slot) ?: continue
            if (!Items.isPellet(stack)) continue
            if (stack.amount <= 1) {
                inv.setItem(slot, null)
            } else {
                val copy = stack.clone()
                copy.amount = stack.amount - 1
                inv.setItem(slot, copy)
            }
            return true
        }
        return false
    }

    @EventHandler(ignoreCancelled = true)
    fun onConsume(e: PlayerItemConsumeEvent) {
        if (Items.fishOf(e.item) != null) {
            e.isCancelled = true
            e.player.sendMessage(color("&cMagic fish cannot be eaten."))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onSmelt(e: FurnaceSmeltEvent) {
        if (Items.fishOf(e.source) != null) e.isCancelled = true
    }

    /** Magic fish use fish-bucket materials; prevent them from being emptied into the world. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBucketEmpty(e: PlayerBucketEmptyEvent) {
        val inv = e.player.inventory
        val stack = if (e.hand == EquipmentSlot.OFF_HAND) inv.itemInOffHand else inv.itemInMainHand
        if (Items.fishOf(stack) != null) e.isCancelled = true
    }

    // ===== Magic rods cannot be enchanted =====

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPrepareEnchant(e: PrepareItemEnchantEvent) {
        if (Items.isRod(e.item)) e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEnchant(e: EnchantItemEvent) {
        if (Items.isRod(e.item)) e.isCancelled = true
    }

    /** Anvil: renaming is allowed, but combining a magic rod with anything (books, rods) is blocked. */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepareAnvil(e: PrepareAnvilEvent) {
        val first = e.inventory.getItem(0)
        val second = e.inventory.getItem(1)
        if (!Items.isRod(first) && !Items.isRod(second)) return
        if (second != null && !second.type.isAir) e.result = null
    }
}
