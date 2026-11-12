package id.irwan.magicfishing

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Bait stored on the Magic Fishing Rod itself. A player loads Fish Pellets into the rod by clicking
 * it with pellets on the cursor in their inventory (up to [max], 64 by default). Each fish caught
 * uses 1 pellet; casting needs at least 1.
 */
object Bait {

    /** Bait count (Int). */
    private val countKey: NamespacedKey by lazy { NamespacedKey(MagicFishing.instance, "bait-count") }

    /** Older builds stored a single bait as a Byte flag; it counts as 1 pellet. */
    private val legacyKey: NamespacedKey by lazy { NamespacedKey(MagicFishing.instance, "bait") }

    private val PREFIX = color("&7Bait: ")

    /** Maximum pellets a rod can hold (configurable: fishing.max-bait). */
    var max: Int = 64

    /** Lore line shown on a freshly crafted rod. */
    const val EMPTY_LORE = "&7Bait: &cNone"

    fun count(rod: ItemStack?): Int {
        val pdc = rod?.itemMeta?.persistentDataContainer ?: return 0
        pdc.get(countKey, PersistentDataType.INTEGER)?.let { return it.coerceAtLeast(0) }
        return if (pdc.get(legacyKey, PersistentDataType.BYTE) == 1.toByte()) 1 else 0
    }

    fun isBaited(rod: ItemStack?): Boolean = count(rod) > 0

    /** Sets the bait count and updates the rod's lore line. Changes [rod] in place. */
    fun setCount(rod: ItemStack, amount: Int) {
        val meta = rod.itemMeta ?: return
        val value = amount.coerceAtLeast(0)
        val pdc = meta.persistentDataContainer
        pdc.remove(legacyKey)
        if (value > 0) pdc.set(countKey, PersistentDataType.INTEGER, value) else pdc.remove(countKey)

        val line = if (value > 0) color("&7Bait: &6$value&7/&6$max Fish Pellet") else color(EMPTY_LORE)
        val lore = (meta.lore ?: mutableListOf()).toMutableList()
        val index = lore.indexOfFirst { it.startsWith(PREFIX) }
        if (index >= 0) lore[index] = line else lore.add(line)
        meta.lore = lore
        rod.itemMeta = meta
    }
}
