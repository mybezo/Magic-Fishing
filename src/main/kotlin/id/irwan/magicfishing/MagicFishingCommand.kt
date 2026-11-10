package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * /mf help | github | list [page] | give <player> <item> [amount] | reload
 */
class MagicFishingCommand(private val plugin: MagicFishing) : TabExecutor {

    private val admin = "magicfishing.admin"

    /** All Slimefun item ids registered by this addon, sorted. */
    private fun itemIds(): List<String> =
        Slimefun.getRegistry().allSlimefunItems.filter { it.addon === Addon }.map { it.id }.sorted()

    private fun msg(sender: CommandSender, text: String) = sender.sendMessage(color("&bMagic Fishing &7» $text"))

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.firstOrNull()?.lowercase()) {
            null, "help" -> help(sender, label)
            "github" -> if (sender is Player) GuideGroup.sendGithubLink(sender) else msg(sender, GITHUB_URL)
            "list" -> if (checkAdmin(sender)) list(sender, args.getOrNull(1)?.toIntOrNull() ?: 1, label)
            "give" -> if (checkAdmin(sender)) give(sender, args, label)
            "reload" -> if (checkAdmin(sender)) reload(sender)
            else -> help(sender, label)
        }
        return true
    }

    private fun checkAdmin(sender: CommandSender): Boolean {
        if (sender.hasPermission(admin)) return true
        msg(sender, "&cYou don't have permission to do that.")
        return false
    }

    private fun help(sender: CommandSender, label: String) {
        msg(sender, "&fCommands:")
        sender.sendMessage(color("&b/$label github &7- Link to the GitHub page"))
        if (sender.hasPermission(admin)) {
            sender.sendMessage(color("&b/$label give <player> <item> [amount] &7- Give a Magic Fishing item"))
            sender.sendMessage(color("&b/$label list [page] &7- List all Magic Fishing item ids"))
            sender.sendMessage(color("&b/$label reload &7- Reload fishing chances and minigame settings"))
        }
    }

    private fun list(sender: CommandSender, page: Int, label: String) {
        val ids = itemIds()
        val pages = maxOf(1, (ids.size + PAGE_SIZE - 1) / PAGE_SIZE)
        val p = page.coerceIn(1, pages)
        msg(sender, "&fItems &7(${ids.size}) - page &f$p&7/&f$pages")
        ids.drop((p - 1) * PAGE_SIZE).take(PAGE_SIZE).forEach { id ->
            val name = SlimefunItem.getById(id)?.itemName ?: id
            sender.sendMessage(color("&8- &f$id &7($name&7)"))
        }
        if (p < pages) sender.sendMessage(color("&7Next page: &b/$label list ${p + 1}"))
    }

    private fun give(sender: CommandSender, args: Array<out String>, label: String) {
        if (args.size < 3) {
            msg(sender, "&cUsage: /$label give <player> <item> [amount]")
            return
        }
        val target = Bukkit.getPlayerExact(args[1])
        if (target == null) {
            msg(sender, "&cPlayer &f${args[1]} &cis not online.")
            return
        }
        val item = SlimefunItem.getById(args[2].uppercase())
        if (item == null || item.addon !== Addon) {
            msg(sender, "&cUnknown item &f${args[2]}&c. Use &b/$label list&c.")
            return
        }
        val amount = (args.getOrNull(3)?.toIntOrNull() ?: 1).coerceIn(1, 64 * 36)
        var left = amount
        while (left > 0) {
            val stack = item.item.clone()
            stack.amount = minOf(left, stack.maxStackSize)
            left -= stack.amount
            target.inventory.addItem(stack).values.forEach { target.world.dropItemNaturally(target.location, it) }
        }
        msg(sender, "&aGave &f$amount&ax ${item.itemName} &ato &f${target.name}&a.")
    }

    private fun reload(sender: CommandSender) {
        plugin.reloadConfig()
        val cfg = plugin.config
        Items.loadSettings(cfg)
        Minigame.load(cfg)
        msg(sender, "&aReloaded fishing chances and minigame settings.")
        sender.sendMessage(color("&7Machine energy / speed, pellet cost and item lore update after a restart."))
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        val subs = if (sender.hasPermission(admin)) listOf("help", "github", "give", "list", "reload") else listOf("help", "github")
        val current = args.lastOrNull()?.lowercase() ?: ""
        val options: List<String> = when {
            args.size <= 1 -> subs
            args[0].equals("give", true) && sender.hasPermission(admin) -> when (args.size) {
                2 -> Bukkit.getOnlinePlayers().map { it.name }
                3 -> itemIds()
                4 -> listOf("1", "16", "64")
                else -> emptyList()
            }
            else -> emptyList()
        }
        return options.filter { it.lowercase().startsWith(current) }.take(100)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
