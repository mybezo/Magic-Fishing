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
    const val ROD_MINECRAFT = "MF_ROD_MINECRAFT"
    const val ROD_SLIMEFUN = "MF_ROD_SLIMEFUN"

    fun setup(plugin: MagicFishing) {
        val cfg = plugin.config
        fun cInt(path: String, def: Int): Int { cfg.addDefault(path, def); return cfg.getInt(path, def) }
        fun key(k: String) = NamespacedKey(plugin, k)

        Fishes.all.forEach { it.weight = cInt("fishing.weights.${it.id}", it.defaultWeight) }

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
                "&8\u25B8 &7Magic Aquarium: &fnot consumed",
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
        fun rodLore(kind: String): Array<String> = arrayOf(
            "", "&7Catches $kind &7resource fish.",
            "",
            "&8\u25B8 &7Bait: &6Fish Pellet",
            "&8\u25B8 &7Uses &f1 pellet &7per fish caught",
            "&8\u25B8 &7Keep pellets in your inventory"
        )

        MagicRod(
            gRod,
            SlimefunItemStack(ROD_MINECRAFT, Material.FISHING_ROD, "&bMagic Fishing Rod &7(Minecraft Fish)", *rodLore("&bMinecraft")),
            RecipeType.MAGIC_WORKBENCH,
            arrayOf(
                sf("MAGIC_LUMP_3"), aqua, sf("MAGIC_LUMP_3"),
                sf("HARDENED_METAL_INGOT"), mc(Material.FISHING_ROD), sf("HARDENED_METAL_INGOT"),
                sf("MAGIC_LUMP_3"), aqua, sf("MAGIC_LUMP_3")
            )
        ).register(Addon)

        MagicRod(
            gRod,
            SlimefunItemStack(ROD_SLIMEFUN, Material.FISHING_ROD, "&dMagic Fishing Rod &7(Slimefun Fish)", *rodLore("&dSlimefun")),
            RecipeType.MAGIC_WORKBENCH,
            arrayOf(
                sf("CARBONADO"), aqua, sf("CARBONADO"),
                sf("BLISTERING_INGOT_3"), sf(ROD_MINECRAFT), sf("BLISTERING_INGOT_3"),
                sf("REINFORCED_ALLOY_INGOT"), aqua, sf("REINFORCED_ALLOY_INGOT")
            )
        ).register(Addon)
        val rodMc = sf(ROD_MINECRAFT)
        val rodSf = sf(ROD_SLIMEFUN)

        // ===== Fish =====
        for (fish in Fishes.all) {
            val rodName = if (fish.slimefun) "&dMagic Fishing Rod (Slimefun Fish)" else "&bMagic Fishing Rod (Minecraft Fish)"
            MagicFish(
                if (fish.slimefun) gFishSf else gFishMc,
                SlimefunItemStack(
                    fish.id, fish.icon, fish.name,
                    "", "&7Produces &f${fish.resourceName}",
                    "",
                    "&8\u25B8 &7Process in: &bMagic Fish Refinery",
                    "&8\u25B8 &7Breed in: &bMagic Aquarium",
                    "&8\u25B8 &7Caught with: $rodName",
                    "",
                    "&8Not edible"
                ),
                catchType,
                arrayOf(null, null, null, null, if (fish.slimefun) rodSf else rodMc, null, null, pellet, null),
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

        val refineryDefaults = mapOf(1 to (100 to 10), 2 to (250 to 6), 3 to (1000 to 1))
        val refineryBlocks = mapOf(1 to Material.PRISMARINE, 2 to Material.PRISMARINE_BRICKS, 3 to Material.DARK_PRISMARINE)
        var prevRefinery: ItemStack? = null
        for (tier in 1..3) {
            val id = "MF_FISH_REFINERY_$tier"
            val (defEnergy, defSeconds) = refineryDefaults.getValue(tier)
            val energy = cInt("machines.$id.energy-per-tick", defEnergy)
            val seconds = cInt("machines.$id.seconds-per-output", defSeconds)
            val recipe: Array<ItemStack?> = when (tier) {
                1 -> arrayOf(
                    sf("HARDENED_METAL_INGOT"), aqua, sf("HARDENED_METAL_INGOT"),
                    sf("ELECTRIC_MOTOR"), sf("ELECTRIC_FURNACE"), sf("ELECTRIC_MOTOR"),
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
                    id, refineryBlocks.getValue(tier), "&bMagic Fish Refinery &7(Tier ${roman(tier)})",
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

        val aqEnergy = cInt("machines.MF_MAGIC_AQUARIUM.energy-per-tick", 150)
        val aqSeconds = cInt("machines.MF_MAGIC_AQUARIUM.seconds-per-output", 180)
        MagicAquarium(
            gMachine,
            SlimefunItemStack(
                "MF_MAGIC_AQUARIUM", Material.LIGHT_BLUE_STAINED_GLASS, "&bMagic Aquarium",
                *machineLore(
                    aqEnergy, aqSeconds, "fish",
                    "&7Breeds 2 fish of the same type",
                    "&7into 1 new fish using &6Fish Pellet&7.",
                    "&7Fish and pellet are &fnever consumed&7."
                )
            ),
            arrayOf(
                sf("HARDENED_GLASS"), sf("HARDENED_GLASS"), sf("HARDENED_GLASS"),
                aqua, sf("ANCIENT_RUNE_WATER"), aqua,
                sf("REINFORCED_ALLOY_INGOT"), sf("ADVANCED_CIRCUIT_BOARD"), sf("REINFORCED_ALLOY_INGOT")
            ),
            aqEnergy, aqSeconds
        ).register(Addon)

        cfg.options().copyDefaults(true)
        plugin.saveConfig()
    }

    /** null = not a magic rod, false = Minecraft fish rod, true = Slimefun fish rod. */
    fun rodIsSlimefun(stack: ItemStack?): Boolean? {
        if (stack == null || stack.type != Material.FISHING_ROD) return null
        return when (SlimefunItem.getByItem(stack)?.id) {
            ROD_MINECRAFT -> false
            ROD_SLIMEFUN -> true
            else -> null
        }
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
