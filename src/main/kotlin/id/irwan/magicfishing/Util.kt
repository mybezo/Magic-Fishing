package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun color(text: String): String = ChatColor.translateAlternateColorCodes('&', text)

fun icon(material: Material, name: String, vararg lore: String): ItemStack {
    val stack = ItemStack(material)
    val meta = stack.itemMeta ?: return stack
    meta.setDisplayName(color(name))
    meta.lore = lore.map { color(it) }
    stack.itemMeta = meta
    return stack
}

/** Gets a Slimefun item by ID (works on old and new Slimefun versions). */
fun sf(id: String): ItemStack {
    val item = SlimefunItem.getById(id)?.item?.clone()
    if (item == null) {
        MagicFishing.instance.logger.warning("Slimefun item '$id' not found, using BARRIER instead.")
        return ItemStack(Material.BARRIER)
    }
    return item
}

fun mc(material: Material): ItemStack = ItemStack(material)

fun roman(n: Int): String = when (n) {
    1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"; else -> n.toString()
}

fun actionBar(player: org.bukkit.entity.Player, text: String) {
    player.spigot().sendMessage(
        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
        *net.md_5.bungee.api.chat.TextComponent.fromLegacyText(color(text))
    )
}
