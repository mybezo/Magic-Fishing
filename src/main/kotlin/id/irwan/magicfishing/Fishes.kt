package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/** Fish rarity, based on how hard the resource is to obtain or craft. */
/**
 * [icon] is a plain vanilla fish (stacks to 64 everywhere, Java and Bedrock).
 * [glow] adds an enchant glint so Epic / Legendary stand out.
 */
enum class Rarity(val label: String, val color: String, val icon: Material, val glow: Boolean, val defaultChance: Double) {
    COMMON("Common", "&f", Material.COD, false, 50.0),
    UNCOMMON("Uncommon", "&a", Material.SALMON, false, 28.0),
    RARE("Rare", "&9", Material.TROPICAL_FISH, false, 15.0),
    EPIC("Epic", "&5", Material.SALMON, true, 6.0),
    LEGENDARY("Legendary", "&6", Material.TROPICAL_FISH, true, 1.0);

    /** Relative chance of rolling this tier (configurable, see Items.setup). */
    var chance: Double = defaultChance

    val display: String get() = "$color$label"
}

class FishType(
    val id: String,
    val resourceName: String,
    val rarity: Rarity,
    val slimefun: Boolean,
    private val outputMaterial: Material? = null,
    private val outputId: String? = null
) {
    /**
     * Fish in the same group share one slot in the roll (e.g. all wool colors):
     * the group is picked first, then a random fish (color) inside it.
     */
    var group: String? = null
        internal set

    /** Name color follows the output item: Slimefun item name color, or the vanilla item's look. */
    val nameColor: String by lazy {
        if (outputMaterial != null) Fishes.colorOf(outputMaterial)
        else leadingCodes(SlimefunItem.getById(outputId ?: "")?.itemName) ?: rarity.color
    }

    /** "Coal Fish", but "Magic Tropical Fish" / "Magic Pufferfish" instead of "... Fish Fish". */
    val name: String
        get() = if (resourceName.endsWith("fish", ignoreCase = true)) "${nameColor}Magic $resourceName"
        else "$nameColor$resourceName Fish"

    /** One output item, or null if the Slimefun item does not exist. */
    fun output(): ItemStack? {
        if (outputMaterial != null) return ItemStack(outputMaterial)
        val item = SlimefunItem.getById(outputId ?: return null)?.item?.clone() ?: return null
        item.amount = 1
        return item
    }

    fun outputDisplay(): ItemStack = output() ?: ItemStack(Material.BARRIER)
}

/** Leading color/format codes of a display name (e.g. "§b§lHardened Metal" -> "§b§l"), or null. */
private fun leadingCodes(text: String?): String? {
    if (text == null) return null
    var i = 0
    while (i + 1 < text.length && (text[i] == '\u00A7' || text[i] == '&') && text[i + 1].lowercaseChar() in "0123456789abcdefklmnor") i += 2
    return if (i == 0) null else text.substring(0, i)
}

object Fishes {

    /** Color that resembles a vanilla item (vanilla names are mostly plain white). */
    private val vanillaColors = mapOf(
        "COAL" to "&8", "COPPER_INGOT" to "&6", "IRON_INGOT" to "&f", "GOLD_INGOT" to "&e",
        "LAPIS_LAZULI" to "&9", "REDSTONE" to "&c", "QUARTZ" to "&f", "DIAMOND" to "&b",
        "EMERALD" to "&a", "NETHERITE_SCRAP" to "&4",
        "ROTTEN_FLESH" to "&6", "BONE" to "&f", "ARROW" to "&f", "STRING" to "&f", "SPIDER_EYE" to "&c",
        "GUNPOWDER" to "&7", "LEATHER" to "&6", "FEATHER" to "&f", "EGG" to "&f", "WHITE_WOOL" to "&f",
        "BEEF" to "&c", "PORKCHOP" to "&d", "CHICKEN" to "&f", "MUTTON" to "&c", "INK_SAC" to "&8",
        "GOLD_NUGGET" to "&e", "SLIME_BALL" to "&a", "MAGMA_CREAM" to "&6", "RABBIT" to "&6",
        "RABBIT_HIDE" to "&6", "GLOW_INK_SAC" to "&b", "PRISMARINE_SHARD" to "&3", "PRISMARINE_CRYSTALS" to "&b",
        "ENDER_PEARL" to "&3", "BLAZE_ROD" to "&6", "GHAST_TEAR" to "&f", "PHANTOM_MEMBRANE" to "&7",
        "RABBIT_FOOT" to "&6", "ARMADILLO_SCUTE" to "&d", "BREEZE_ROD" to "&b", "NAUTILUS_SHELL" to "&f",
        "TURTLE_SCUTE" to "&a", "SCUTE" to "&a", "SHULKER_SHELL" to "&5", "WITHER_SKELETON_SKULL" to "&8",
        "TOTEM_OF_UNDYING" to "&e",
        "BIRCH_LOG" to "&f", "MANGROVE_LOG" to "&c", "CHERRY_LOG" to "&d", "PALE_OAK_LOG" to "&7",
        "CRIMSON_STEM" to "&4", "WARPED_STEM" to "&3",
        "GLASS" to "&f", "GLASS_PANE" to "&f", "TINTED_GLASS" to "&5", "NETHER_STAR" to "&e",
        "STONE" to "&7", "GRAVEL" to "&7", "COBBLESTONE" to "&7", "SAND" to "&e", "DIORITE" to "&f",
        "GRANITE" to "&6", "ANDESITE" to "&7", "END_STONE" to "&e", "CLAY" to "&7", "SNOW_BLOCK" to "&f",
        "CLAY_BALL" to "&7", "SNOWBALL" to "&f",
        "FLINT" to "&8", "BONE_BLOCK" to "&f", "SOUL_SAND" to "&6", "NETHERRACK" to "&c", "OBSIDIAN" to "&5",
        "MAGMA_BLOCK" to "&6", "WATER_BUCKET" to "&9", "LAVA_BUCKET" to "&6", "QUARTZ_BLOCK" to "&f",
        "LILY_PAD" to "&2", "ICE" to "&b", "PACKED_ICE" to "&b", "BLUE_ICE" to "&9",
        "COD" to "&6", "SALMON" to "&c", "TROPICAL_FISH" to "&6", "PUFFERFISH" to "&e",
        "DRIED_KELP" to "&2", "BONE_MEAL" to "&f",
        "WHEAT" to "&e", "WHEAT_SEEDS" to "&2", "CARROT" to "&6", "POTATO" to "&e", "BEETROOT" to "&4",
        "BEETROOT_SEEDS" to "&f", "MELON_SLICE" to "&c", "PUMPKIN" to "&6", "SUGAR_CANE" to "&a",
        "CACTUS" to "&2", "BAMBOO" to "&a", "KELP" to "&2", "SWEET_BERRIES" to "&c", "COCOA_BEANS" to "&6",
        "BROWN_MUSHROOM" to "&6", "RED_MUSHROOM" to "&c", "NETHER_WART" to "&4", "GLOW_BERRIES" to "&e",
        "HONEYCOMB" to "&6", "HONEY_BOTTLE" to "&e", "HONEY_BLOCK" to "&6", "HONEYCOMB_BLOCK" to "&6",
        "CHORUS_FRUIT" to "&5", "TORCHFLOWER_SEEDS" to "&6", "PITCHER_POD" to "&b",
        "CHERRY_LEAVES" to "&d", "PALE_OAK_LEAVES" to "&7", "AZALEA_LEAVES" to "&a", "FLOWERING_AZALEA_LEAVES" to "&d"
    )

    private val DYES = listOf(
        "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY",
        "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"
    )

    private val dyeColors = mapOf(
        "WHITE" to "&f", "ORANGE" to "&6", "MAGENTA" to "&d", "LIGHT_BLUE" to "&b", "YELLOW" to "&e",
        "LIME" to "&a", "PINK" to "&d", "GRAY" to "&8", "LIGHT_GRAY" to "&7", "CYAN" to "&3",
        "PURPLE" to "&5", "BLUE" to "&9", "BROWN" to "&6", "GREEN" to "&2", "RED" to "&c", "BLACK" to "&8"
    )

    /** Dye color of a colored block like LIGHT_BLUE_STAINED_GLASS (longest prefix wins). */
    private fun dyeColorOf(name: String): String? =
        DYES.sortedByDescending { it.length }.firstOrNull { name.startsWith("${it}_") }?.let { dyeColors[it] }

    fun colorOf(material: Material): String = vanillaColors[material.name] ?: dyeColorOf(material.name) ?: when {
        material.name.endsWith("_LOG") -> "&6"
        material.name.endsWith("_LEAVES") -> "&2"
        else -> "&f"
    }

    /** First material that exists on this server version (keeps older/newer versions safe). */
    private fun mat(vararg names: String): Material? = names.firstNotNullOfOrNull { Material.matchMaterial(it) }

    private fun pretty(material: Material): String =
        material.name.lowercase().split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    private fun mc(key: String, display: String?, rarity: Rarity, vararg outputNames: String): FishType? {
        val out = mat(*outputNames) ?: return null
        return FishType("MF_FISH_$key", display ?: pretty(out), rarity, false, outputMaterial = out)
    }

    private fun mc(rarity: Rarity, vararg materials: String): List<FishType?> = materials.map { mc(it, null, rarity, it) }

    private fun sf(sfId: String, name: String, rarity: Rarity) =
        FishType("MF_FISH_SF_$sfId", name, rarity, true, outputId = sfId)

    // ===================== Minecraft resources =====================

    private val ores = listOf(
        mc("COAL", "Coal", Rarity.COMMON, "COAL"),
        mc("COPPER", "Copper", Rarity.COMMON, "COPPER_INGOT"),
        mc("IRON", "Iron", Rarity.UNCOMMON, "IRON_INGOT"),
        mc("GOLD", "Gold", Rarity.UNCOMMON, "GOLD_INGOT"),
        mc("LAPIS", "Lapis", Rarity.UNCOMMON, "LAPIS_LAZULI"),
        mc("REDSTONE", "Redstone", Rarity.UNCOMMON, "REDSTONE"),
        mc("QUARTZ", "Quartz", Rarity.UNCOMMON, "QUARTZ"),
        mc("DIAMOND", "Diamond", Rarity.RARE, "DIAMOND"),
        mc("EMERALD", "Emerald", Rarity.RARE, "EMERALD"),
        mc("NETHERITE", "Netherite", Rarity.LEGENDARY, "NETHERITE_SCRAP")
    )

    private val logs =
        listOf("OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK", "MANGROVE")
            .map { mc("${it}_LOG", null, Rarity.COMMON, "${it}_LOG") } +
            listOf("CHERRY", "PALE_OAK").map { mc("${it}_LOG", null, Rarity.UNCOMMON, "${it}_LOG") } +
            listOf(
                mc("CRIMSON_STEM", null, Rarity.UNCOMMON, "CRIMSON_STEM"),
                mc("WARPED_STEM", null, Rarity.UNCOMMON, "WARPED_STEM")
            )

    private val leaves =
        listOf("OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK", "MANGROVE", "AZALEA")
            .map { mc("${it}_LEAVES", null, Rarity.COMMON, "${it}_LEAVES") } +
            listOf("CHERRY", "PALE_OAK", "FLOWERING_AZALEA").map { mc("${it}_LEAVES", null, Rarity.UNCOMMON, "${it}_LEAVES") }

    /** All mob drops except Nether Star. */
    private val mobDrops =
        mc(
            Rarity.COMMON,
            "ROTTEN_FLESH", "BONE", "ARROW", "STRING", "SPIDER_EYE", "GUNPOWDER", "LEATHER", "FEATHER",
            "EGG", "BEEF", "PORKCHOP", "CHICKEN", "MUTTON", "INK_SAC", "GOLD_NUGGET"
        ) +
            mc(
                Rarity.UNCOMMON,
                "SLIME_BALL", "MAGMA_CREAM", "RABBIT", "RABBIT_HIDE", "GLOW_INK_SAC",
                "PRISMARINE_SHARD", "PRISMARINE_CRYSTALS"
            ) +
            mc(
                Rarity.RARE,
                "ENDER_PEARL", "BLAZE_ROD", "GHAST_TEAR", "PHANTOM_MEMBRANE", "RABBIT_FOOT",
                "ARMADILLO_SCUTE", "BREEZE_ROD", "NAUTILUS_SHELL"
            ) +
            listOf(mc("TURTLE_SCUTE", "Turtle Scute", Rarity.RARE, "TURTLE_SCUTE", "SCUTE")) +
            mc(Rarity.EPIC, "SHULKER_SHELL", "WITHER_SKELETON_SKULL", "TOTEM_OF_UNDYING") +
            mc(Rarity.LEGENDARY, "NETHER_STAR")

    private fun List<FishType?>.inGroup(group: String): List<FishType?> = onEach { it?.group = group }

    /** All wool, glass and glass pane colors. Each family counts as one fish in the roll. */
    private val coloredBlocks =
        mc(Rarity.COMMON, *DYES.map { "${it}_WOOL" }.toTypedArray()).inGroup("WOOL") +
            mc(Rarity.COMMON, "GLASS", *DYES.map { "${it}_STAINED_GLASS" }.toTypedArray()).inGroup("GLASS") +
            mc(Rarity.UNCOMMON, "TINTED_GLASS") +
            mc(Rarity.COMMON, "GLASS_PANE", *DYES.map { "${it}_STAINED_GLASS_PANE" }.toTypedArray()).inGroup("GLASS_PANE")

    private val earth =
        mc(
            Rarity.COMMON,
            "STONE", "COBBLESTONE", "GRAVEL", "SAND", "DIORITE", "GRANITE", "ANDESITE",
            "CLAY", "CLAY_BALL", "SNOW_BLOCK", "SNOWBALL", "FLINT", "BONE_BLOCK", "NETHERRACK",
            "LILY_PAD", "ICE"
        ) +
            mc(
                Rarity.UNCOMMON,
                "END_STONE", "SOUL_SAND", "OBSIDIAN", "MAGMA_BLOCK", "QUARTZ_BLOCK",
                "WATER_BUCKET", "LAVA_BUCKET", "PACKED_ICE"
            ) +
            mc(Rarity.RARE, "BLUE_ICE")

    /** Farm crops. Common crops count as one fish in the roll (like wool), then a random crop. */
    private val farm =
        mc(
            Rarity.COMMON,
            "WHEAT_SEEDS", "CARROT", "POTATO", "BEETROOT", "BEETROOT_SEEDS", "MELON_SLICE",
            "PUMPKIN", "SUGAR_CANE", "CACTUS", "BAMBOO", "KELP", "SWEET_BERRIES", "COCOA_BEANS",
            "BROWN_MUSHROOM", "RED_MUSHROOM"
        ).inGroup("FARM") +
            mc(Rarity.UNCOMMON, "NETHER_WART", "GLOW_BERRIES") +
            mc(Rarity.RARE, "CHORUS_FRUIT", "TORCHFLOWER_SEEDS", "PITCHER_POD")

    /** Fish Pellet ingredients (wheat, dried kelp, bone meal, raw fish). Each one is its own fish. */
    private val pelletIngredients =
        mc(Rarity.COMMON, "WHEAT", "DRIED_KELP", "BONE_MEAL", "COD", "SALMON") +
            mc(Rarity.UNCOMMON, "TROPICAL_FISH", "PUFFERFISH")

    /** Bee products. */
    private val honey =
        mc(Rarity.UNCOMMON, "HONEYCOMB", "HONEY_BOTTLE") +
            mc(Rarity.RARE, "HONEY_BLOCK", "HONEYCOMB_BLOCK")

    // ===================== Slimefun resources =====================

    private val slimefunDusts = listOf(
        sf("IRON_DUST", "Iron Dust", Rarity.COMMON),
        sf("GOLD_DUST", "Gold Dust", Rarity.COMMON),
        sf("COPPER_DUST", "Copper Dust", Rarity.COMMON),
        sf("TIN_DUST", "Tin Dust", Rarity.COMMON),
        sf("SILVER_DUST", "Silver Dust", Rarity.COMMON),
        sf("ALUMINUM_DUST", "Aluminum Dust", Rarity.COMMON),
        sf("LEAD_DUST", "Lead Dust", Rarity.COMMON),
        sf("ZINC_DUST", "Zinc Dust", Rarity.COMMON),
        sf("MAGNESIUM_DUST", "Magnesium Dust", Rarity.COMMON),
        sf("SIFTED_ORE", "Sifted Ore", Rarity.COMMON)
    )

    private val slimefunIngots = listOf(
        sf("ALUMINUM_INGOT", "Aluminum Ingot", Rarity.UNCOMMON),
        sf("COPPER_INGOT", "Copper Ingot", Rarity.UNCOMMON),
        sf("TIN_INGOT", "Tin Ingot", Rarity.UNCOMMON),
        sf("SILVER_INGOT", "Silver Ingot", Rarity.UNCOMMON),
        sf("LEAD_INGOT", "Lead Ingot", Rarity.UNCOMMON),
        sf("ZINC_INGOT", "Zinc Ingot", Rarity.UNCOMMON),
        sf("MAGNESIUM_INGOT", "Magnesium Ingot", Rarity.UNCOMMON),
        sf("NICKEL_INGOT", "Nickel Ingot", Rarity.RARE),
        sf("COBALT_INGOT", "Cobalt Ingot", Rarity.RARE)
    ) + listOf(4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24).map { k ->
        val rarity = when {
            k <= 10 -> Rarity.UNCOMMON
            k <= 18 -> Rarity.RARE
            else -> Rarity.EPIC
        }
        sf("GOLD_${k}K", "$k-Carat Gold Ingot", rarity)
    }

    /** Smeltery alloys + Hardened / Reinforced. */
    private val slimefunAlloys = listOf(
        sf("BRONZE_INGOT", "Bronze Ingot", Rarity.RARE),
        sf("BRASS_INGOT", "Brass Ingot", Rarity.RARE),
        sf("BILLON_INGOT", "Billon Ingot", Rarity.RARE),
        sf("SOLDER_INGOT", "Solder Ingot", Rarity.RARE),
        sf("STEEL_INGOT", "Steel Ingot", Rarity.RARE),
        sf("ALUMINUM_BRASS_INGOT", "Aluminum Brass Ingot", Rarity.RARE),
        sf("ALUMINUM_BRONZE_INGOT", "Aluminum Bronze Ingot", Rarity.RARE),
        sf("DURALUMIN_INGOT", "Duralumin Ingot", Rarity.EPIC),
        sf("CORINTHIAN_BRONZE_INGOT", "Corinthian Bronze Ingot", Rarity.EPIC),
        sf("DAMASCUS_STEEL_INGOT", "Damascus Steel Ingot", Rarity.EPIC),
        sf("REDSTONE_ALLOY", "Redstone Alloy Ingot", Rarity.EPIC),
        sf("GILDED_IRON", "Gilded Iron", Rarity.EPIC),
        sf("HARDENED_METAL_INGOT", "Hardened Metal Ingot", Rarity.EPIC),
        sf("REINFORCED_ALLOY_INGOT", "Reinforced Alloy Ingot", Rarity.LEGENDARY)
    )

    /** Slimefun magical lumps, components and radioactive materials. */
    private val slimefunComponents = listOf(
        sf("MAGIC_LUMP_1", "Magical Lump I", Rarity.UNCOMMON),
        sf("MAGIC_LUMP_2", "Magical Lump II", Rarity.RARE),
        sf("MAGIC_LUMP_3", "Magical Lump III", Rarity.EPIC),
        sf("ENDER_LUMP_1", "Ender Lump I", Rarity.UNCOMMON),
        sf("ENDER_LUMP_2", "Ender Lump II", Rarity.RARE),
        sf("ENDER_LUMP_3", "Ender Lump III", Rarity.EPIC),
        sf("HEATING_COIL", "Heating Coil", Rarity.RARE),
        sf("ELECTRO_MAGNET", "Electromagnet", Rarity.RARE),
        sf("ELECTRIC_MOTOR", "Electric Motor", Rarity.RARE),
        sf("PLASTIC_SHEET", "Plastic Sheet", Rarity.EPIC),
        sf("URANIUM", "Uranium", Rarity.EPIC),
        sf("BLISTERING_INGOT", "Blistering Ingot (33%)", Rarity.EPIC),
        sf("BLISTERING_INGOT_2", "Blistering Ingot (66%)", Rarity.EPIC),
        sf("BLISTERING_INGOT_3", "Blistering Ingot", Rarity.LEGENDARY)
    )

    val all: List<FishType> =
        (ores + logs + leaves + mobDrops + coloredBlocks + earth + farm + pelletIngredients + honey).filterNotNull() + slimefunDusts + slimefunIngots + slimefunAlloys + slimefunComponents

    /** Chance (%) that a catch is a Slimefun fish instead of a Minecraft fish (configurable). */
    var slimefunChance: Double = 50.0

    private fun categoryChance(slimefun: Boolean): Double {
        val sf = slimefunChance.coerceIn(0.0, 100.0)
        return if (slimefun) sf else 100.0 - sf
    }

    /** Percentage chance that one catch is a fish of this rarity in this category (Minecraft / Slimefun). */
    fun dropChance(rarity: Rarity, slimefun: Boolean): Double {
        val tiers = all.filter { it.slimefun == slimefun }.map { it.rarity }.toSet()
        if (rarity !in tiers) return 0.0
        val total = tiers.sumOf { it.chance.coerceAtLeast(0.0) }
        if (total <= 0) return 0.0
        return categoryChance(slimefun) * rarity.chance.coerceAtLeast(0.0) / total
    }

    /** Picks one slot (a single fish or a whole group), then a random fish inside it. */
    private fun pick(fishes: List<FishType>): FishType =
        fishes.groupBy { it.group ?: it.id }.values.random().random()

    /** Rolls the category (Minecraft / Slimefun), then a rarity tier, then a random fish of that tier. */
    fun roll(): FishType? {
        val slimefun = Random.nextDouble(100.0) < slimefunChance.coerceIn(0.0, 100.0)
        val byTier = all.filter { it.slimefun == slimefun }.groupBy { it.rarity }
        val tiers = byTier.keys.filter { it.chance > 0 }.sorted()
        val total = tiers.sumOf { it.chance }
        if (total <= 0) return null
        var r = Random.nextDouble(total)
        for (tier in tiers) {
            r -= tier.chance
            if (r < 0) return pick(byTier.getValue(tier))
        }
        return pick(byTier.getValue(tiers.last()))
    }
}
