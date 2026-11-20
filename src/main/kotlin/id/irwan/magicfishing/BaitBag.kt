package id.irwan.magicfishing

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

/**
 * Storage for [Items.BAIT_BAG]: stores Fish Pellet count in the bag's PDC and can automatically
 * refill an empty Magic Fishing Rod from any Bait Bag carried in the player's inventory.
 */
object BaitBag {

    private val countKey: NamespacedKey by lazy { NamespacedKey(MagicFishing.instance, "bait-bag-count") }

    /** Per-bag id assigned once pellets are stored, preventing filled bags from stacking together. */
    private val uidKey: NamespacedKey by lazy { NamespacedKey(MagicFishing.instance, "bait-bag-uid") }

    private val PREFIX = color("&7Stored: ")

    /** Maximum Fish Pellets a Bait Bag can hold (configurable: bait-bag.capacity). */
    var capacity: Int = 1024

    /** Automatically refill an empty Magic Fishing Rod from a Bait Bag in inventory (configurable: bait-bag.auto-refill). */
    var autoRefill: Boolean = true

    /** Lore line shown on a freshly crafted Bait Bag. */
    const val EMPTY_LORE = "&7Stored: &cEmpty"

    fun count(bag: ItemStack?): Int {
        val pdc = bag?.itemMeta?.persistentDataContainer ?: return 0
        return (pdc.get(countKey, PersistentDataType.INTEGER) ?: 0).coerceAtLeast(0)
    }

    /** Sets the stored pellet count and updates the bag's lore line. Changes [bag] in place. */
    fun setCount(bag: ItemStack, amount: Int) {
        val meta = bag.itemMeta ?: return
        val value = amount.coerceAtLeast(0)
        val pdc = meta.persistentDataContainer
        if (value > 0) {
            pdc.set(countKey, PersistentDataType.INTEGER, value)
            if (!pdc.has(uidKey, PersistentDataType.STRING)) {
                pdc.set(uidKey, PersistentDataType.STRING, UUID.randomUUID().toString())
            }
        } else {
            pdc.remove(countKey)
        }

        val line = if (value > 0) color("&7Stored: &6$value&7/&6$capacity Fish Pellet") else color(EMPTY_LORE)
        val lore = (meta.lore ?: mutableListOf()).toMutableList()
        val index = lore.indexOfFirst { it.startsWith(PREFIX) }
        if (index >= 0) lore[index] = line else lore.add(line)
        meta.lore = lore
        bag.itemMeta = meta
    }

    /** Total Fish Pellets stored across all unstacked Bait Bags in [player]'s inventory (including off-hand). */
    fun totalStored(player: Player): Int {
        val inv = player.inventory
        var total = 0
        for (slot in 0 until inv.storageContents.size) {
            val stack = inv.getItem(slot) ?: continue
            if (stack.amount == 1 && Items.isBaitBag(stack)) total += count(stack)
        }
        val off = inv.itemInOffHand
        if (off.amount == 1 && Items.isBaitBag(off)) total += count(off)
        return total
    }

    /**
     * Refills [rod] up to [Bait.max] using Fish Pellets from Bait Bags in [player]'s inventory
     * (main inventory, hotbar and off-hand). Updates [rod] in place; the caller writes [rod] back
     * to the player's hand if needed. Returns the number of pellets moved into [rod].
     */
    fun refillRod(player: Player, rod: ItemStack): Int {
        if (!autoRefill) return 0
        val current = Bait.count(rod)
        var needed = Bait.max - current
        if (needed <= 0) return 0

        val inv = player.inventory
        var taken = 0

        for (slot in 0 until inv.storageContents.size) {
            if (needed <= 0) break
            val stack = inv.getItem(slot) ?: continue
            if (stack.amount != 1 || !Items.isBaitBag(stack)) continue
            val stored = count(stack)
            if (stored <= 0) continue

            val pull = minOf(needed, stored)
            val updated = stack.clone()
            setCount(updated, stored - pull)
            inv.setItem(slot, updated)
            taken += pull
            needed -= pull
        }

        if (needed > 0) {
            val off = inv.itemInOffHand
            if (off.amount == 1 && Items.isBaitBag(off)) {
                val stored = count(off)
                if (stored > 0) {
                    val pull = minOf(needed, stored)
                    val updated = off.clone()
                    setCount(updated, stored - pull)
                    inv.setItemInOffHand(updated)
                    taken += pull
                }
            }
        }

        if (taken > 0) Bait.setCount(rod, current + taken)
        return taken
    }
}
