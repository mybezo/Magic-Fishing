package id.irwan.magicfishing

import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Event
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
import org.bukkit.event.inventory.InventoryType
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
import org.bukkit.inventory.ItemStack

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
                val refilled = BaitBag.refillRod(e.player, rod)
                if (refilled > 0) {
                    if (mainHand) inv.setItemInMainHand(rod) else inv.setItemInOffHand(rod)
                    notifyRefill(e.player, rod, refilled)
                } else {
                    e.isCancelled = true
                    actionBar(e.player, "&cBait your rod first: &7click it with &6Fish Pellet &7in your inventory")
                }
            }
            return
        }
        if (e.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val caught = e.caught as? Item ?: return
        // Casting needs bait, so this only happens in edge cases; then it is a normal vanilla catch.
        if (!Bait.isBaited(rod)) return

        // Each fish caught uses 1 pellet. Reeling in without a fish keeps the bait.
        val left = Bait.count(rod) - 1
        Bait.setCount(rod, left)
        if (left == 0) {
            val refilled = BaitBag.refillRod(e.player, rod)
            if (refilled > 0) notifyRefill(e.player, rod, refilled)
            else actionBar(e.player, "&eYour rod is out of bait")
        }
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

    /**
     * Baiting: click the Magic Fishing Rod with Fish Pellets on the cursor.
     * Left click loads as many as fit (up to Bait.max), right click loads 1.
     */
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
        val current = Bait.count(rod)
        val space = Bait.max - current
        if (space <= 0) {
            actionBar(player, "&7Your rod is full of bait &8(&6$current&8/&6${Bait.max}&8)")
            return
        }
        val pellets = cursor!!.amount
        val add = if (e.click == ClickType.RIGHT) 1 else minOf(space, pellets)

        val newRod = rod.clone()
        Bait.setCount(newRod, current + add)
        e.currentItem = newRod

        val rest = cursor.clone()
        rest.amount = pellets - add
        player.setItemOnCursor(if (rest.amount > 0) rest else null)
        actionBar(player, "&aBait loaded: &6${current + add}&7/&6${Bait.max} Fish Pellet")
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f)
    }

    /**
     * Bait Bag inventory interactions:
     * - Click Bait Bag with Fish Pellet on cursor: left click loads all (up to capacity), right click loads 1.
     * - Click Magic Fishing Rod with Bait Bag on cursor: left click fills rod, right click loads 1.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBaitBagClick(e: InventoryClickEvent) {
        if (e.click != ClickType.LEFT && e.click != ClickType.RIGHT) return
        if (e.slotType == InventoryType.SlotType.RESULT) return
        val player = e.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.CREATIVE) return
        val cursor = e.cursor ?: return
        val current = e.currentItem ?: return

        if (Items.isPellet(cursor) && Items.isBaitBag(current)) {
            e.isCancelled = true
            if (current.amount != 1) {
                actionBar(player, "&cUnstack your Bait Bags before loading pellets")
                return
            }
            val stored = BaitBag.count(current)
            val space = (BaitBag.capacity - stored).coerceAtLeast(0)
            if (space <= 0) {
                actionBar(player, "&7Your Bait Bag is full &8(&6$stored&8/&6${BaitBag.capacity}&8)")
                return
            }
            val pellets = cursor.amount
            val add = if (e.click == ClickType.RIGHT) 1 else minOf(space, pellets)

            val newBag = current.clone()
            BaitBag.setCount(newBag, stored + add)
            e.currentItem = newBag

            val rest = cursor.clone()
            rest.amount = pellets - add
            player.setItemOnCursor(if (rest.amount > 0) rest else null)
            actionBar(player, "&aBait Bag: &6${stored + add}&7/&6${BaitBag.capacity} Fish Pellet &8(+$add)")
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f)
            return
        }

        if (Items.isBaitBag(cursor) && Items.isRod(current)) {
            e.isCancelled = true
            if (cursor.amount != 1) {
                actionBar(player, "&cUnstack your Bait Bags first")
                return
            }
            val rodBait = Bait.count(current)
            val space = (Bait.max - rodBait).coerceAtLeast(0)
            if (space <= 0) {
                actionBar(player, "&7Your rod is full of bait &8(&6$rodBait&8/&6${Bait.max}&8)")
                return
            }
            val stored = BaitBag.count(cursor)
            if (stored <= 0) {
                actionBar(player, "&cYour Bait Bag is empty")
                return
            }
            val move = if (e.click == ClickType.RIGHT) 1 else minOf(space, stored)

            val newRod = current.clone()
            Bait.setCount(newRod, rodBait + move)
            e.currentItem = newRod

            val newBag = cursor.clone()
            BaitBag.setCount(newBag, stored - move)
            player.setItemOnCursor(newBag)
            actionBar(player, "&aBait loaded: &6${rodBait + move}&7/&6${Bait.max} &8(bag: &6${stored - move}&8)")
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f)
        }
    }

    /**
     * Bait Bag in hand:
     * - Right click: deposit all Fish Pellets from inventory into the bag (up to capacity).
     * - Shift + Right click: withdraw up to 64 Fish Pellets into inventory.
     *
     * Does not use ignoreCancelled = true because RIGHT_CLICK_AIR defaults to isCancelled = true in Bukkit/Paper.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onBaitBagInteract(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) return
        if (e.useItemInHand() == Event.Result.DENY) return
        val hand = e.hand ?: return
        val bag = e.item ?: return
        if (!Items.isBaitBag(bag)) return

        val player = e.player
        val inv = player.inventory
        // Carrying the bag in off-hand while fishing with a rod in main hand should not trigger deposit/withdraw.
        if (hand == EquipmentSlot.OFF_HAND && Items.isRod(inv.itemInMainHand)) return
        // Allow opening chests, machines, doors, etc. when not sneaking.
        if (e.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking && e.clickedBlock?.type?.isInteractable == true) return

        e.isCancelled = true
        if (bag.amount != 1) {
            actionBar(player, "&cUnstack your Bait Bags first")
            return
        }

        val stored = BaitBag.count(bag)
        if (player.isSneaking) {
            if (stored <= 0) {
                actionBar(player, "&cYour Bait Bag is empty")
                return
            }
            val pull = minOf(64, stored)
            val updated = bag.clone()
            BaitBag.setCount(updated, stored - pull)
            if (hand == EquipmentSlot.OFF_HAND) inv.setItemInOffHand(updated) else inv.setItemInMainHand(updated)

            val leftover = inv.addItem(Items.pelletItem(pull))
            leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
            actionBar(player, "&eWithdrew &6$pull &7Fish Pellet &8(&6${stored - pull}&8/&6${BaitBag.capacity} &7left&8)")
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.1f)
            return
        }

        var space = (BaitBag.capacity - stored).coerceAtLeast(0)
        if (space <= 0) {
            actionBar(player, "&7Your Bait Bag is full &8(&6$stored&8/&6${BaitBag.capacity}&8)")
            return
        }

        var added = 0
        for (slot in 0 until inv.storageContents.size) {
            if (space <= 0) break
            val stack = inv.getItem(slot) ?: continue
            if (!Items.isPellet(stack)) continue
            val take = minOf(space, stack.amount)
            if (take >= stack.amount) {
                inv.setItem(slot, null)
            } else {
                val rest = stack.clone()
                rest.amount = stack.amount - take
                inv.setItem(slot, rest)
            }
            added += take
            space -= take
        }

        if (space > 0 && hand == EquipmentSlot.HAND) {
            val off = inv.itemInOffHand
            if (Items.isPellet(off)) {
                val take = minOf(space, off.amount)
                if (take >= off.amount) {
                    inv.setItemInOffHand(null)
                } else {
                    val rest = off.clone()
                    rest.amount = off.amount - take
                    inv.setItemInOffHand(rest)
                }
                added += take
            }
        }

        if (added == 0) {
            actionBar(player, "&7No &6Fish Pellet &7in your inventory &8(&6$stored&8/&6${BaitBag.capacity}&8)")
            return
        }

        val updated = bag.clone()
        BaitBag.setCount(updated, stored + added)
        if (hand == EquipmentSlot.OFF_HAND) inv.setItemInOffHand(updated) else inv.setItemInMainHand(updated)
        actionBar(player, "&aStored &6+$added &7Fish Pellet &8(&6${stored + added}&8/&6${BaitBag.capacity}&8)")
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f)
    }

    private fun notifyRefill(player: Player, rod: ItemStack, refilled: Int) {
        val bagLeft = BaitBag.totalStored(player)
        actionBar(player, "&aRefilled rod: &6+$refilled &7(&6${Bait.count(rod)}&7/&6${Bait.max}&7, bag: &6$bagLeft&7)")
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

    /** Old magic fish (player head version) and Bait Bags must not be placed as blocks. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlace(e: BlockPlaceEvent) {
        if (Items.fishOf(e.itemInHand) != null || Items.isBaitBag(e.itemInHand)) e.isCancelled = true
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
