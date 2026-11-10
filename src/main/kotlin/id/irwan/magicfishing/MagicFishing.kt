package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import org.bukkit.plugin.java.JavaPlugin

class MagicFishing : JavaPlugin() {

    override fun onEnable() {
        instance = this
        Items.setup(this)
        server.pluginManager.registerEvents(FishingListener(), this)
        logger.info("Magic Fishing enabled (experimental).")
    }

    companion object {
        lateinit var instance: MagicFishing
            private set
    }
}

/** Kept separate from the plugin class to avoid Kotlin conflicts with SlimefunAddon default methods. */
object Addon : SlimefunAddon {
    override fun getJavaPlugin(): JavaPlugin = MagicFishing.instance
    override fun getBugTrackerURL(): String? = null
}
