package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import org.bukkit.plugin.java.JavaPlugin

const val GITHUB_URL = "https://github.com/mybezo/Magic-Fishing"

class MagicFishing : JavaPlugin() {

    override fun onEnable() {
        instance = this
        Minigame.load(config)
        Items.setup(this)
        server.pluginManager.registerEvents(FishingListener(), this)
        getCommand("magicfishing")?.let {
            val cmd = MagicFishingCommand(this)
            it.setExecutor(cmd)
            it.tabCompleter = cmd
        }
        logger.info("Magic Fishing enabled (experimental).")
    }

    override fun onDisable() {
        Minigame.stopAll()
    }

    companion object {
        lateinit var instance: MagicFishing
            private set
    }
}

/** Kept separate from the plugin class to avoid Kotlin conflicts with SlimefunAddon default methods. */
object Addon : SlimefunAddon {
    override fun getJavaPlugin(): JavaPlugin = MagicFishing.instance
    override fun getBugTrackerURL(): String = "$GITHUB_URL/issues"
}
