package id.irwan.magicfishing

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Bait stored on the Magic Fishing Rod itself. A player baits the rod by clicking it with a
 * Fish Pellet on the cursor in their inventory; one bait lasts for one catch.
 */
object Bait {

    private val key: NamespacedKey by lazy { NamespacedKey(MagicFishing.instance, "bait") }

    private val BAITED = color("&7Bait: &6Fish Pellet")
    private val EMPTY = color("&7Bait: &cNone")
    private val PREFIX = color("&7Bait: ")

    /** Lore line shown on a freshly crafted rod. */
    const val EMPTY_LORE = "&7Bait: &cNone"

    fun isBaited(rod: ItemStack?): Boolean {
        val meta = rod?.itemMeta ?: return false
        return meta.persistentDataContainer.get(key, PersistentDataType.BYTE) == 1.toByte()
    }

    /** Sets or clears the bait and updates the rod's lore line. Changes [rod] in place. */
    fun setBaited(rod: ItemStack, baited: Boolean) {
        val meta = rod.itemMeta ?: return
        if (baited) meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        else meta.persistentDataContainer.remove(key)

        val lore = (meta.lore ?: mutableListOf()).toMutableList()
        val line = if (baited) BAITED else EMPTY
        val index = lore.indexOfFirst { it.startsWith(PREFIX) }
        if (index >= 0) lore[index] = line else lore.add(line)
        meta.lore = lore
        rod.itemMeta = meta
    }
}
