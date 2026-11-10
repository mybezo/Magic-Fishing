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

    val display: String get() = "$color&l${label.uppercase()}"
}

class FishType(
    val id: String,
    val resourceName: String,
    val rarity: Rarity,
    val slimefun: Boolean,
    private val outputMaterial: Material? = null,
    private val outputId: String? = null
) {
    val name: String get() = "${rarity.color}$resourceName Fish"
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

object Fishes {

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
            mc(Rarity.EPIC, "SHULKER_SHELL", "TRIDENT", "WITHER_SKELETON_SKULL", "TOTEM_OF_UNDYING")

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

    /** Percentage chance of a rarity tier for the given rod (only tiers that have fish count). */
    fun tierChance(rarity: Rarity, slimefunRod: Boolean): Double {
        val tiers = all.filter { it.slimefun == slimefunRod }.map { it.rarity }.toSet()
        if (rarity !in tiers) return 0.0
        val total = tiers.sumOf { it.chance.coerceAtLeast(0.0) }
        return if (total <= 0) 0.0 else rarity.chance.coerceAtLeast(0.0) / total * 100.0
    }

    /** Rolls a rarity tier first, then a random fish of that tier for the given rod type. */
    fun roll(slimefunRod: Boolean): FishType? {
        val byTier = all.filter { it.slimefun == slimefunRod }.groupBy { it.rarity }
        val tiers = byTier.keys.filter { it.chance > 0 }
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
