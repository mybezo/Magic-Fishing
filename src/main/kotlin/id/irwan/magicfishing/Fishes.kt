package id.irwan.magicfishing

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class FishType(
    val id: String,
    val name: String,
    val resourceName: String,
    val icon: Material,
    val defaultWeight: Int,
    val slimefun: Boolean,
    private val outputMaterial: Material? = null,
    private val outputId: String? = null
) {
    var weight: Int = defaultWeight

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

    private fun mcFish(key: String, display: String?, icon: Material, weight: Int, vararg outputNames: String): FishType? {
        val out = mat(*outputNames) ?: return null
        val name = display ?: pretty(out)
        return FishType("MF_FISH_$key", "&b$name Fish", name, icon, weight, false, outputMaterial = out)
    }

    private fun sfFish(sfId: String, name: String, weight: Int) =
        FishType("MF_FISH_SF_$sfId", "&d$name Fish", name, Material.TROPICAL_FISH_BUCKET, weight, true, outputId = sfId)

    private val ores = listOf(
        mcFish("COAL", "Coal", Material.COD_BUCKET, 30, "COAL"),
        mcFish("COPPER", "Copper", Material.COD_BUCKET, 30, "COPPER_INGOT"),
        mcFish("IRON", "Iron", Material.COD_BUCKET, 25, "IRON_INGOT"),
        mcFish("GOLD", "Gold", Material.SALMON_BUCKET, 20, "GOLD_INGOT"),
        mcFish("LAPIS", "Lapis", Material.SALMON_BUCKET, 20, "LAPIS_LAZULI"),
        mcFish("REDSTONE", "Redstone", Material.SALMON_BUCKET, 20, "REDSTONE"),
        mcFish("QUARTZ", "Quartz", Material.SALMON_BUCKET, 20, "QUARTZ"),
        mcFish("DIAMOND", "Diamond", Material.TROPICAL_FISH_BUCKET, 8, "DIAMOND"),
        mcFish("EMERALD", "Emerald", Material.TROPICAL_FISH_BUCKET, 8, "EMERALD"),
        mcFish("NETHERITE", "Netherite", Material.PUFFERFISH_BUCKET, 3, "NETHERITE_SCRAP")
    )

    private val logs =
        listOf("OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK", "MANGROVE", "CHERRY", "PALE_OAK")
            .map { mcFish("${it}_LOG", null, Material.COD_BUCKET, 25, "${it}_LOG") } +
            listOf(
                mcFish("CRIMSON_STEM", null, Material.COD_BUCKET, 20, "CRIMSON_STEM"),
                mcFish("WARPED_STEM", null, Material.COD_BUCKET, 20, "WARPED_STEM")
            )

    private val leaves =
        listOf("OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK", "MANGROVE", "CHERRY", "PALE_OAK", "AZALEA", "FLOWERING_AZALEA")
            .map { mcFish("${it}_LEAVES", null, Material.SALMON_BUCKET, 20, "${it}_LEAVES") }

    /** All mob drops except Nether Star. */
    private val mobDrops = listOf(
        "ROTTEN_FLESH" to 25, "BONE" to 25, "ARROW" to 20, "STRING" to 25, "SPIDER_EYE" to 20,
        "GUNPOWDER" to 20, "ENDER_PEARL" to 8, "BLAZE_ROD" to 8, "GHAST_TEAR" to 5, "SLIME_BALL" to 15,
        "MAGMA_CREAM" to 12, "LEATHER" to 20, "FEATHER" to 20, "EGG" to 15, "WHITE_WOOL" to 20,
        "BEEF" to 20, "PORKCHOP" to 20, "CHICKEN" to 20, "MUTTON" to 20, "RABBIT" to 15,
        "RABBIT_HIDE" to 15, "RABBIT_FOOT" to 5, "INK_SAC" to 18, "GLOW_INK_SAC" to 10,
        "PRISMARINE_SHARD" to 15, "PRISMARINE_CRYSTALS" to 12, "PHANTOM_MEMBRANE" to 8,
        "SHULKER_SHELL" to 4, "GOLD_NUGGET" to 20, "NAUTILUS_SHELL" to 4, "TRIDENT" to 2,
        "WITHER_SKELETON_SKULL" to 2, "TOTEM_OF_UNDYING" to 2, "ARMADILLO_SCUTE" to 8, "BREEZE_ROD" to 5
    ).map { (m, w) -> mcFish(m, null, Material.PUFFERFISH_BUCKET, w, m) } +
        listOf(mcFish("TURTLE_SCUTE", "Turtle Scute", Material.PUFFERFISH_BUCKET, 5, "TURTLE_SCUTE", "SCUTE"))

    private val slimefunDusts = listOf(
        sfFish("IRON_DUST", "Iron Dust", 12),
        sfFish("GOLD_DUST", "Gold Dust", 12),
        sfFish("COPPER_DUST", "Copper Dust", 12),
        sfFish("TIN_DUST", "Tin Dust", 12),
        sfFish("SILVER_DUST", "Silver Dust", 12),
        sfFish("ALUMINUM_DUST", "Aluminum Dust", 12),
        sfFish("LEAD_DUST", "Lead Dust", 12),
        sfFish("ZINC_DUST", "Zinc Dust", 12),
        sfFish("MAGNESIUM_DUST", "Magnesium Dust", 12),
        sfFish("SIFTED_ORE", "Sifted Ore", 12)
    )

    /** Slimefun ingots, excluding alloys, Hardened, Blistering, Damascus and Steel. */
    private val slimefunIngots = listOf(
        sfFish("ALUMINUM_INGOT", "Aluminum Ingot", 10),
        sfFish("COPPER_INGOT", "Copper Ingot", 10),
        sfFish("TIN_INGOT", "Tin Ingot", 10),
        sfFish("SILVER_INGOT", "Silver Ingot", 10),
        sfFish("LEAD_INGOT", "Lead Ingot", 10),
        sfFish("ZINC_INGOT", "Zinc Ingot", 10),
        sfFish("MAGNESIUM_INGOT", "Magnesium Ingot", 10),
        sfFish("NICKEL_INGOT", "Nickel Ingot", 7),
        sfFish("COBALT_INGOT", "Cobalt Ingot", 7)
    ) + listOf(4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24).map { k ->
        sfFish("GOLD_${k}K", "$k-Carat Gold Ingot", maxOf(2, 10 - k / 3))
    }

    val all: List<FishType> = (ores + logs + leaves + mobDrops).filterNotNull() + slimefunDusts + slimefunIngots

    /** Weighted random fish for the given rod type. */
    fun roll(slimefunRod: Boolean): FishType? {
        val pool = all.filter { it.slimefun == slimefunRod && it.weight > 0 }
        val total = pool.sumOf { it.weight }
        if (total <= 0) return null
        var r = Random.nextInt(total)
        for (fish in pool) {
            r -= fish.weight
            if (r < 0) return fish
        }
        return null
    }
}
