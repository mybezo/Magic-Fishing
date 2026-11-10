package id.irwan.magicfishing

import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

class FishingListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onFish(e: PlayerFishEvent) {
        if (e.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val inv = e.player.inventory
        val slimefunRod = Items.rodIsSlimefun(inv.itemInMainHand) ?: Items.rodIsSlimefun(inv.itemInOffHand) ?: return
        val caught = e.caught as? Item ?: return

        if (!takePellet(e.player)) {
            e.player.sendMessage(color("&cYou need &6Fish Pellet &cin your inventory to catch magic fish."))
            return
        }
        val fish = Fishes.roll(slimefunRod) ?: return
        caught.itemStack = Items.fishItem(fish)
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
}
