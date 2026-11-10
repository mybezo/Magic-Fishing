package id.irwan.magicfishing

import id.irwan.magicfishing.machines.FishRefinery
import id.irwan.magicfishing.machines.MagicAquarium
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

/** Custom fish item - used by machines and listeners to recognise fish. */
class MagicFish(
    group: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<ItemStack?>,
    val type: FishType
) : SlimefunItem(group, item, recipeType, recipe)

/** Magic Fishing Rod: cannot be enchanted or disenchanted by Slimefun machines. */
class MagicRod(
    group: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<ItemStack?>
) : SlimefunItem(group, item, recipeType, recipe) {
    init {
        enchantable = false
        disenchantable = false
    }
}

object Items {

    const val PELLET = "MF_PELLET"
    const val AQUA_CORE = "MF_AQUA_CORE"
    const val ROD = "MF_MAGIC_ROD"
    const val AQUARIUM = "MF_MAGIC_AQUARIUM"
    private const val CONFIG_VERSION = 3

    fun setup(plugin: MagicFishing) {
        val cfg = plugin.config
        fun cInt(path: String, def: Int): Int { cfg.addDefault(path, def); return cfg.getInt(path, def) }
        fun key(k: String) = NamespacedKey(plugin, k)

        fun cDouble(path: String, def: Double): Double { cfg.addDefault(path, def); return cfg.getDouble(path, def) }

        // Config migration: apply the new machine timers and drop removed settings on old configs.
        val configVersion = cfg.getInt("config-version", 1)
        if (configVersion < 2) {
            cfg.set("fishing.weights", null)
            for ((tier, sec) in REFINERY_SECONDS) cfg.set("machines.MF_FISH_REFINERY_$tier.seconds-per-output", sec)
        }
        if (configVersion < 3) cfg.set("fishing.slimefun-chance", 50.0)
        cfg.set("config-version", CONFIG_VERSION)
        Fishes.slimefunChance = cDouble("fishing.slimefun-chance", 50.0)
        val aqPellets = cInt("machines.$AQUARIUM.pellets-per-fish", 16).coerceAtLeast(1)
        Rarity.values().forEach { it.chance = cDouble("fishing.rarity-chance.${it.name}", it.defaultChance) }

        // ===== Guide: Magic Fishing > Material, Machine, Rod, Fish Minecraft Resource, Fish Slimefun Resource =====
        val main = NestedItemGroup(key("magic_fishing"), icon(Material.FISHING_ROD, "&bMagic Fishing"))
        val gMaterial = SubItemGroup(key("mf_material"), main, icon(Material.HEART_OF_THE_SEA, "&bMaterial"))
        val gMachine = SubItemGroup(key("mf_machine"), main, icon(Material.PRISMARINE_BRICKS, "&bMachine"))
        val gRod = SubItemGroup(key("mf_rod"), main, icon(Material.FISHING_ROD, "&bRod"))
        val gFishMc = SubItemGroup(key("mf_fish_minecraft"), main, icon(Material.COD, "&bFish Minecraft Resource"))
        val gFishSf = SubItemGroup(key("mf_fish_slimefun"), main, icon(Material.TROPICAL_FISH, "&dFish Slimefun Resource"))
        main.register(Addon)
        gMaterial.register(Addon)
        gMachine.register(Addon)
        gRod.register(Addon)
        gFishMc.register(Addon)
        gFishSf.register(Addon)

        val catchType = RecipeType(
            key("magic_fishing_catch"),
            icon(Material.FISHING_ROD, "&bFishing", "&7Caught by fishing with", "&7a Magic Fishing Rod", "&7using Fish Pellet as bait")
        )

        // ===== Material =====
        val pelletItem = SlimefunItem(
            gMaterial,
            SlimefunItemStack(
                PELLET, Material.BROWN_DYE, "&6Fish Pellet",
                "", "&7Magic fish food and fishing bait.",
                "",
                "&8\u25B8 &7Fishing: &f1 pellet per fish",
                "&8\u25B8 &7Magic Aquarium: &f$aqPellets pellets per fish",
                "&8\u25B8 &7Crafting gives &f8 pellets"
            ),
            RecipeType.ENHANCED_CRAFTING_TABLE,
            arrayOf(
                mc(Material.WHEAT), sf("MAGIC_LUMP_1"), mc(Material.WHEAT),
                mc(Material.DRIED_KELP), mc(Material.COD), mc(Material.DRIED_KELP),
                mc(Material.BONE_MEAL), mc(Material.SALMON), mc(Material.BONE_MEAL)
            )
        )
        val pelletOutput = pelletItem.item.clone()
        pelletOutput.amount = 8
        pelletItem.setRecipeOutput(pelletOutput)
        pelletItem.register(Addon)

        SlimefunItem(
            gMaterial,
            SlimefunItemStack(
                AQUA_CORE, Material.HEART_OF_THE_SEA, "&3Aqua Core",
                "", "&7A crystallized core of ocean magic.",
                "",
                "&8\u25B8 &7Used in every Magic Fishing rod",
                "&8\u25B8 &7and machine"
            ),
            RecipeType.MAGIC_WORKBENCH,
            arrayOf(
                sf("SYNTHETIC_SAPPHIRE"), sf("MAGIC_LUMP_3"), sf("SYNTHETIC_SAPPHIRE"),
                sf("ENDER_LUMP_3"), sf("ANCIENT_RUNE_WATER"), sf("ENDER_LUMP_3"),
                mc(Material.PRISMARINE_CRYSTALS), sf("MAGIC_LUMP_3"), mc(Material.PRISMARINE_CRYSTALS)
            )
        ).register(Addon)
        val aqua = sf(AQUA_CORE)
        val pellet = sf(PELLET)

        // ===== Rod =====
        MagicRod(
            gRod,
            SlimefunItemStack(
                ROD, Material.FISHING_ROD, "&bMagic Fishing Rod",
                "", "&7Catches &bMinecraft &7and &dSlimefun &7resource fish.",
                "",
                "&8\u25B8 &7Slimefun fish chance: &e${pct(Fishes.slimefunChance.coerceIn(0.0, 100.0))}%",
                "&8\u25B8 &7Bait: &6Fish Pellet",
                "&8\u25B8 &7Uses &f1 pellet &7per fish caught",
                "&8\u25B8 &7Keep pellets in your inventory",
                "",
                "&7When a fish bites, &fsneak &7while the",
                "&7marker is in the &agreen zone &7to catch it.",
                "",
                "&8Cannot be enchanted"
            ),
            RecipeType.MAGIC_WORKBENCH,
            arrayOf(
                sf("MAGIC_LUMP_3"), aqua, sf("MAGIC_LUMP_3"),
                sf("HARDENED_METAL_INGOT"), mc(Material.FISHING_ROD), sf("HARDENED_METAL_INGOT"),
                sf("MAGIC_LUMP_3"), aqua, sf("MAGIC_LUMP_3")
            )
        ).register(Addon)
        val rod = sf(ROD)

        // ===== Fish =====
        for (fish in Fishes.all.sortedBy { it.rarity }) {
            MagicFish(
                if (fish.slimefun) gFishSf else gFishMc,
                SlimefunItemStack(
                    fish.id, fish.rarity.icon, fish.name,
                    "", "&7Produces &f${fish.resourceName}",
                    "",
                    "&8\u25B8 &7Rarity: ${fish.rarity.display}",
                    "&8\u25B8 &7Catch chance: &e${pct(Fishes.dropChance(fish.rarity, fish.slimefun))}%",
                    "&8\u25B8 &7Process in: &bMagic Fish Refinery",
                    "&8\u25B8 &7Breed in: &bMagic Aquarium",
                    "&8\u25B8 &7Caught with: &bMagic Fishing Rod",
                    "",
                    "&8Not edible"
                ).also { if (fish.rarity.glow) addGlint(it) },
                catchType,
                arrayOf(null, null, null, null, rod, null, null, pellet, null),
                fish
            ).register(Addon)
        }

        // ===== Machine =====
        fun machineLore(energy: Int, seconds: Int, unit: String, vararg info: String): Array<String> = arrayOf(
            "", *info,
            "",
            "&8\u25B8 &7Energy: &e$energy J/tick",
            "&8\u25B8 &7Buffer: &e${energy * 2} J",
            "&8\u25B8 &7Speed: &f1 $unit every $seconds s"
        )

        val refineryEnergy = mapOf(1 to 100, 2 to 250, 3 to 1000)
        val refineryBlocks = mapOf(1 to Material.PRISMARINE, 2 to Material.PRISMARINE_BRICKS, 3 to Material.DARK_PRISMARINE)
        var prevRefinery: ItemStack? = null
        for (tier in 1..3) {
            val id = "MF_FISH_REFINERY_$tier"
            val energy = cInt("machines.$id.energy-per-tick", refineryEnergy.getValue(tier))
            val seconds = cInt("machines.$id.seconds-per-output", REFINERY_SECONDS.getValue(tier))
            val recipe: Array<ItemStack?> = when (tier) {
                1 -> arrayOf(
                    sf("HARDENED_METAL_INGOT"), aqua, sf("HARDENED_METAL_INGOT"),
                    sf("ELECTRIC_MOTOR"), sf("ANCIENT_RUNE_WATER"), sf("ELECTRIC_MOTOR"),
                    sf("HARDENED_METAL_INGOT"), sf("MEDIUM_CAPACITOR"), sf("HARDENED_METAL_INGOT")
                )
                2 -> arrayOf(
                    sf("REINFORCED_ALLOY_INGOT"), aqua, sf("REINFORCED_ALLOY_INGOT"),
                    sf("ADVANCED_CIRCUIT_BOARD"), prevRefinery, sf("ADVANCED_CIRCUIT_BOARD"),
                    sf("REINFORCED_ALLOY_INGOT"), sf("LARGE_CAPACITOR"), sf("REINFORCED_ALLOY_INGOT")
                )
                else -> arrayOf(
                    sf("BLISTERING_INGOT_3"), aqua, sf("BLISTERING_INGOT_3"),
                    sf("CARBONADO"), prevRefinery, sf("CARBONADO"),
                    sf("REINFORCED_PLATE"), sf("CARBONADO_EDGED_CAPACITOR"), sf("REINFORCED_PLATE")
                )
            }
            FishRefinery(
                gMachine,
                SlimefunItemStack(
                    id, refineryBlocks.getValue(tier), "&bMagic Fish Refinery ${roman(tier)}",
                    *machineLore(
                        energy, seconds, "output",
                        "&7Turns a magic fish into its resource.",
                        "&7The fish is &fnever consumed&7."
                    )
                ),
                recipe, energy, seconds
            ).register(Addon)
            prevRefinery = sf(id)
        }

        val aqEnergy = cInt("machines.$AQUARIUM.energy-per-tick", 150)
        val aqSeconds = cInt("machines.$AQUARIUM.seconds-per-output", 180)
        MagicAquarium(
            gMachine,
            SlimefunItemStack(
                AQUARIUM, Material.LIGHT_BLUE_STAINED_GLASS, "&bMagic Aquarium",
                *machineLore(
                    aqEnergy, aqSeconds, "fish",
                    "&7Breeds 2 fish of the same type",
                    "&7into 1 new fish using &6Fish Pellet&7.",
                    "&7Parent fish are &fnever consumed&7.",
                    "&7Consumes &6$aqPellets Fish Pellet &7per fish,",
                    "&7gradually as progress goes up."
                )
            ),
            arrayOf(
                sf("HARDENED_GLASS"), sf("HARDENED_GLASS"), sf("HARDENED_GLASS"),
                aqua, sf("ANCIENT_RUNE_WATER"), aqua,
                sf("REINFORCED_ALLOY_INGOT"), sf("ADVANCED_CIRCUIT_BOARD"), sf("REINFORCED_ALLOY_INGOT")
            ),
            aqEnergy, aqSeconds, aqPellets
        ).register(Addon)

        cfg.options().copyDefaults(true)
        plugin.saveConfig()
    }

    /** Refinery seconds per output for tier I / II / III. */
    private val REFINERY_SECONDS = mapOf(1 to 15, 2 to 10, 3 to 5)

    /** Enchant glint without showing an enchantment (used for Epic / Legendary fish). */
    private fun addGlint(item: ItemStack) {
        val meta = item.itemMeta ?: return
        val ench = org.bukkit.enchantments.Enchantment.getByKey(NamespacedKey.minecraft("lure")) ?: return
        meta.addEnchant(ench, 1, true)
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
        item.itemMeta = meta
    }

    private fun pct(value: Double): String = String.format(java.util.Locale.ROOT, "%.2f", value)

    fun isRod(stack: ItemStack?): Boolean {
        if (stack == null || stack.type != Material.FISHING_ROD) return false
        return SlimefunItem.getByItem(stack)?.id == ROD
    }

    fun fishOf(stack: ItemStack?): FishType? {
        if (stack == null || stack.type.isAir) return null
        return (SlimefunItem.getByItem(stack) as? MagicFish)?.type
    }

    fun isPellet(stack: ItemStack?): Boolean {
        if (stack == null || stack.type.isAir) return false
        return SlimefunItem.getByItem(stack)?.id == PELLET
    }

    fun fishItem(type: FishType): ItemStack {
        val item = sf(type.id)
        item.amount = 1
        return item
    }
}
