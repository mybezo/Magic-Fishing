package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode
import io.github.thebusybiscuit.slimefun4.core.services.sounds.SoundEffect
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * The Magic Fishing guide category. Same layout as Slimefun's [NestedItemGroup],
 * plus a GitHub button in the top middle slot.
 */
class GuideGroup(key: NamespacedKey, item: ItemStack) : NestedItemGroup(key, item) {

    private val groups = ArrayList<SubItemGroup>()

    override fun addSubGroup(group: SubItemGroup) {
        super.addSubGroup(group)
        if (group !in groups) groups.add(group)
    }

    override fun removeSubGroup(group: SubItemGroup) {
        super.removeSubGroup(group)
        groups.remove(group)
    }

    override fun open(p: Player, profile: PlayerProfile, mode: SlimefunGuideMode) {
        openPage(p, profile, mode, 1)
    }

    private fun openPage(p: Player, profile: PlayerProfile, mode: SlimefunGuideMode, page: Int) {
        val history = profile.guideHistory
        if (mode == SlimefunGuideMode.SURVIVAL_MODE) history.add(this, page)

        val menu = ChestMenu(Slimefun.getLocalization().getMessage(p, "guide.title.main"))
        val guide = Slimefun.getRegistry().getSlimefunGuide(mode) as SurvivalSlimefunGuide

        menu.setEmptySlotsClickable(false)
        menu.addMenuOpeningHandler { pl -> SoundEffect.GUIDE_BUTTON_CLICK_SOUND.playFor(pl) }
        guide.createHeader(p, profile, menu)

        menu.addItem(
            1,
            ChestMenuUtils.getBackButton(p, "", ChatColor.GRAY.toString() + Slimefun.getLocalization().getMessage(p, "guide.back.guide"))
        ) { _, _, _, _ ->
            SlimefunGuide.openMainMenu(profile, mode, history.mainMenuPage)
            false
        }

        // GitHub button (top middle).
        menu.addItem(4, GITHUB_BUTTON) { pl, _, _, _ ->
            pl.closeInventory()
            sendGithubLink(pl)
            false
        }

        val visible = groups.filter { it.isVisibleInNested(p) }
        val pages = maxOf(1, (visible.size + GROUP_SIZE - 1) / GROUP_SIZE)
        val current = page.coerceIn(1, pages)
        visible.drop((current - 1) * GROUP_SIZE).take(GROUP_SIZE).forEachIndexed { i, group ->
            menu.addItem(9 + i, group.getItem(p)) { _, _, _, _ ->
                SlimefunGuide.openItemGroup(profile, group, mode, 1)
                false
            }
        }

        menu.addItem(46, ChestMenuUtils.getPreviousButton(p, current, pages)) { _, _, _, _ ->
            if (current > 1) openPage(p, profile, mode, current - 1)
            false
        }
        menu.addItem(52, ChestMenuUtils.getNextButton(p, current, pages)) { _, _, _, _ ->
            if (current < pages) openPage(p, profile, mode, current + 1)
            false
        }

        menu.open(p)
    }

    companion object {
        private const val GROUP_SIZE = 36

        private val GITHUB_BUTTON: ItemStack = icon(
            Material.WRITABLE_BOOK, "&bMagic Fishing on GitHub",
            "", "&7Source code, updates, wiki", "&7and bug reports.",
            "", "&eClick &7to get the link in chat"
        )

        fun sendGithubLink(player: Player) {
            val link = TextComponent(color("&b&n$GITHUB_URL"))
            link.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, GITHUB_URL)
            link.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(color("&7Click to open")))
            player.spigot().sendMessage(TextComponent(color("&bMagic Fishing &7» ")), link)
        }
    }
}
