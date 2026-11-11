package id.irwan.magicfishing

import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot

class FishingListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onFish(e: PlayerFishEvent) {
        val inv = e.player.inventory
        val mainHand = Items.isRod(inv.itemInMainHand)
        if (!mainHand && !Items.isRod(inv.itemInOffHand)) return
        val rod = if (mainHand) inv.itemInMainHand else inv.itemInOffHand

        if (e.state == PlayerFishEvent.State.FISHING) {
            // No casting while the minigame is running, and every cast needs bait.
            if (Minigame.isPlaying(e.player)) {
                e.isCancelled = true
            } else if (!Bait.isBaited(rod)) {
                e.isCancelled = true
                actionBar(e.player, "&cBait your rod first: &7click it with a &6Fish Pellet &7in your inventory")
            }
            return
        }
        if (e.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val caught = e.caught as? Item ?: return
        // Casting needs bait, so this only happens in edge cases; then it is a normal vanilla catch.
        if (!Bait.isBaited(rod)) return

        // One bait = one catch. Reeling in without a fish keeps the bait.
        Bait.setBaited(rod, false)
        if (mainHand) inv.setItemInMainHand(rod) else inv.setItemInOffHand(rod)

        val fish = Fishes.roll() ?: return
        if (Minigame.enabled) {
            // The fish is only given if the player wins the minigame.
            caught.remove()
            Minigame.start(e.player, fish)
            return
        }
        caught.itemStack = Items.fishItem(fish)
        if (fish.rarity >= Rarity.EPIC) {
            e.player.sendMessage(color("&7You caught ${fish.rarity.display} &7fish: &r${fish.name}&7!"))
        }
    }

    /** Baiting: click the Magic Fishing Rod with a Fish Pellet on the cursor (1 pellet per bait). */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBait(e: InventoryClickEvent) {
        if (e.click != ClickType.LEFT && e.click != ClickType.RIGHT) return
        val player = e.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.CREATIVE) return
        val cursor = e.cursor
        if (!Items.isPellet(cursor)) return
        val rod = e.currentItem ?: return
        if (!Items.isRod(rod)) return

        e.isCancelled = true
        if (Bait.isBaited(rod)) {
            player.sendMessage(color("&7Your rod already has bait."))
            return
        }
        val newRod = rod.clone()
        Bait.setBaited(newRod, true)
        e.currentItem = newRod

        val rest = cursor!!.clone()
        rest.amount = cursor.amount - 1
        player.setItemOnCursor(if (rest.amount > 0) rest else null)
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f)
    }

    @EventHandler(ignoreCancelled = true)
    fun onConsume(e: PlayerItemConsumeEvent) {
        if (Items.fishOf(e.item) != null) {
            e.isCancelled = true
            e.player.sendMessage(color("&cMagic fish cannot be eaten."))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onCook(e: BlockCookEvent) {
        // Furnace, smoker and campfire.
        if (Items.fishOf(e.source) != null) e.isCancelled = true
    }

    /** Magic fish are raw fish: block putting them on a campfire. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCampfire(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_BLOCK) return
        val type = e.clickedBlock?.type ?: return
        if (type != Material.CAMPFIRE && type != Material.SOUL_CAMPFIRE) return
        if (Items.fishOf(e.item) != null) e.isCancelled = true
    }

    /** Block feeding / taming with magic fish (cats, ocelots, dolphins, ...). */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onFeed(e: PlayerInteractEntityEvent) {
        val inv = e.player.inventory
        val item = if (e.hand == EquipmentSlot.OFF_HAND) inv.itemInOffHand else inv.itemInMainHand
        if (Items.fishOf(item) != null) e.isCancelled = true
    }

    /** Old magic fish (player head version) must not be placed as blocks. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlace(e: BlockPlaceEvent) {
        if (Items.fishOf(e.itemInHand) != null) e.isCancelled = true
    }

    /** Old magic fish used fish-bucket materials; prevent them from being emptied into the world. */
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

    // ===== Minigame =====

    /** Any sneak toggle counts as input (Java hold-shift and Bedrock touch toggle). */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onSneak(e: PlayerToggleSneakEvent) {
        Minigame.input(e.player)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        Minigame.stop(e.player, success = false, silent = true)
    }
}
