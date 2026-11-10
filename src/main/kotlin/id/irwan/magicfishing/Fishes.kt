package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/** Fish rarity, based on how hard the resource is to obtain or craft. */
enum class Rarity(val label: String, val color: String, val icon: Material, val defaultChance: Double) {
    COMMON("Common", "&f", Material.COD_BUCKET, 50.0),
    UNCOMMON("Uncommon", "&a", Material.SALMON_BUCKET, 28.0),
    RARE("Rare", "&9", Material.TROPICAL_FISH_BUCKET, 15.0),
    EPIC("Epic", "&5", Material.PUFFERFISH_BUCKET, 6.0),
    LEGENDARY("Legendary", "&6", Material.PUFFERFISH_BUCKET, 1.0);

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
    /** Name color follows the output item: Slimefun item name color, or the vanilla item's look. */
    val nameColor: String by lazy {
        if (outputMaterial != null) Fishes.colorOf(outputMaterial)
        else leadingCodes(SlimefunItem.getById(outputId ?: "")?.itemName) ?: rarity.color
    }

    val name: String get() = "$nameColor$resourceName Fish"
    val icon: Material get() = rarity.icon

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
        "CHERRY_LEAVES" to "&d", "PALE_OAK_LEAVES" to "&7", "AZALEA_LEAVES" to "&a", "FLOWERING_AZALEA_LEAVES" to "&d"
    )

    fun colorOf(material: Material): String = vanillaColors[material.name] ?: when {
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
            "EGG", "WHITE_WOOL", "BEEF", "PORKCHOP", "CHICKEN", "MUTTON", "INK_SAC", "GOLD_NUGGET"
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
            mc(Rarity.EPIC, "SHULKER_SHELL", "WITHER_SKELETON_SKULL", "TOTEM_OF_UNDYING")

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

    /** Smeltery alloys + Hardened / Reinforced (Blistering excluded). */
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

    val all: List<FishType> =
        (ores + logs + leaves + mobDrops).filterNotNull() + slimefunDusts + slimefunIngots + slimefunAlloys

    /** Chance (%) that a catch is a Slimefun fish instead of a Minecraft fish (configurable). */
    var slimefunChance: Double = 15.0

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
            if (r < 0) return byTier.getValue(tier).random()
        }
        return byTier.getValue(tiers.last()).random()
    }
}
