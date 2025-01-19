package at.hannibal2.skyhanni.features.misc.items

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.ReforgeAPI
import at.hannibal2.skyhanni.features.misc.discordrpc.DiscordRPCManager
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI.getKuudraTier
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI.isKuudraArmor
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI.removeKuudraTier
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.EssenceUtils
import at.hannibal2.skyhanni.utils.EssenceUtils.getEssencePrices
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getRawCraftCostOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getAttributeFromShard
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getReadableNBTDump
import at.hannibal2.skyhanni.utils.ItemUtils.isRune
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.itemNameWithoutColor
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.SKYBLOCK_COIN
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NEUItems.removePrefix
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.PrimitiveIngredient
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getAbilityScrolls
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getAppliedPocketSackInASack
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getArmorDye
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getAttributes
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getBookwormBookCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getDrillUpgrades
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getDungeonStarCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnrichment
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getExtraAttributes
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getFarmingForDummiesCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getGemstones
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHelmetSkin
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHotPotatoCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getManaDisintegrators
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getMithrilInfusion
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPolarvoidBookCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPowerScroll
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getRune
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getSilexCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getStarCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getTransmissionTunerCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasArtOfPeace
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasArtOfWar
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasBookOfStats
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasDivanPowderCoating
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasEtherwarp
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasJalapenoBook
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasWoodSingularity
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.isRecombobulated
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.item.ItemStack
import java.util.Locale

// TODO split into smaller sub classes
@Suppress("LargeClass")
object EstimatedItemValueCalculator {

    private val config get() = SkyHanniMod.feature.inventory.estimatedItemValues

    var starChange = 0
        get() = if (LorenzUtils.debug) field else 0

    private val additionalCostFunctions = listOf(
        ::addAttributeCost,
        ::addReforgeStone,

        // once
        ::addRecombobulator,
        ::addArtOfWar,
        ::addArtOfPeace,
        ::addEtherwarp,
        ::addPowerScrolls,
        ::addWoodSingularity,
        ::addJalapenoBook,
        ::addStatsBook,
        ::addEnrichment,
        ::addDivanPowderCoating,
        ::addMithrilInfusion,

        // counted
        ::addStars, // crimson, dungeon
        ::addMasterStars,
        ::addHotPotatoBooks,
        ::addFarmingForDummies,
        ::addSilex,
        ::addTransmissionTuners,
        ::addManaDisintegrators,
        ::addPolarvoidBook,
        ::addBookwormBook,
        ::addPocketSackInASack,

        // cosmetic
        ::addHelmetSkin,
        ::addArmorDye,
        ::addRune,

        // dynamic
        ::addAbilityScrolls,
        ::addDrillUpgrades,
        ::addGemstoneSlotUnlockCost,
        ::addGemstones,
        ::addEnchantments,
    )

    private val FARMING_FOR_DUMMIES = "FARMING_FOR_DUMMIES".toInternalName()
    private val ETHERWARP_CONDUIT = "ETHERWARP_CONDUIT".toInternalName()
    private val ETHERWARP_MERGER = "ETHERWARP_MERGER".toInternalName()
    private val FUMING_POTATO_BOOK = "FUMING_POTATO_BOOK".toInternalName()
    private val HOT_POTATO_BOOK = "HOT_POTATO_BOOK".toInternalName()
    private val SILEX = "SIL_EX".toInternalName()
    private val TRANSMISSION_TUNER = "TRANSMISSION_TUNER".toInternalName()
    private val MANA_DISINTEGRATOR = "MANA_DISINTEGRATOR".toInternalName()
    private val RECOMBOBULATOR_3000 = "RECOMBOBULATOR_3000".toInternalName()
    private val JALAPENO_BOOK = "JALAPENO_BOOK".toInternalName()
    private val WOOD_SINGULARITY = "WOOD_SINGULARITY".toInternalName()
    private val DIVAN_POWDER_COATING = "DIVAN_POWDER_COATING".toInternalName()
    private val ART_OF_WAR = "THE_ART_OF_WAR".toInternalName()
    private val BOOK_OF_STATS = "BOOK_OF_STATS".toInternalName()
    private val ART_OF_PEACE = "THE_ART_OF_PEACE".toInternalName()
    private val POLARVOID_BOOK = "POLARVOID_BOOK".toInternalName()
    private val POCKET_SACK_IN_A_SACK = "POCKET_SACK_IN_A_SACK".toInternalName()
    private val BOOKWORM_BOOK = "BOOKWORM_BOOK".toInternalName()
    private val STONK_PICKAXE = "STONK_PICKAXE".toInternalName()
    private val MITHRIL_INFUSION = "MITHRIL_INFUSION".toInternalName()

    fun getTotalPrice(stack: ItemStack): Double = calculate(stack, mutableListOf()).first

    fun calculate(stack: ItemStack, list: MutableList<String>): Pair<Double, Double> {
        val basePrice = addBaseItem(stack, list)
        val totalPrice = additionalCostFunctions.fold(basePrice) { total, function -> total + function(stack, list) }
        return totalPrice to basePrice
    }

    private fun addAttributeCost(stack: ItemStack, list: MutableList<String>): Double {
        val attributes = stack.getAttributes() ?: return 0.0
        val internalName = stack.getInternalName()
        val internalNameString = internalName.removeKuudraTier().removePrefix("VANQUISHED_").asString()
        var genericName = internalNameString
        if (internalName.isKuudraArmor()) {
            genericName = KuudraAPI.kuudraSets.fold(internalNameString) { acc, part -> acc.replace(part, "GENERIC_KUUDRA") }
        }
        stack.getAttributeFromShard()?.let {
            return 0.0
        }
        if (attributes.size != 2) return 0.0
        val basePrice = internalName.getPrice()
        var subTotal = 0.0
        val combo = ("$internalNameString+ATTRIBUTE_${attributes[0].first}+ATTRIBUTE_${attributes[1].first}")
        val comboPrice = combo.toInternalName().getPriceOrNull()

        if (comboPrice != null) {
            val useless = isUselessAttribute(combo)
            val color = if (comboPrice > basePrice && !useless) "§6" else "§7"
            list.add("§7Attribute Combo: ($color${comboPrice.shortFormat()}§7)")
            if (!useless) {
                subTotal += addAttributePrice(comboPrice, basePrice)
            }
        } else {
            list.add("§7Attributes:")
        }
        for (attr in attributes) {
            val attributeName = "$genericName+ATTRIBUTE_${attr.first}"
            val price = getPriceOrCompositePriceForAttribute(attributeName, attr.second)
            var priceColor = "§7"
            val useless = isUselessAttribute(attributeName)
            val nameColor = if (!useless) "§9" else "§7"
            if (price != null) {
                if (price > basePrice && !useless) {
                    subTotal += addAttributePrice(price, basePrice)
                    priceColor = "§6"
                }

            }
            val displayName = attr.first.fixMending()
            list.add(
                "  $nameColor${
                    displayName.allLettersFirstUppercase()
                } ${attr.second}§7: $priceColor${price?.shortFormat() ?: "Unknown"}",
            )
        }
        // Adding 0.1 so that we always show the estimated item value overlay
        return subTotal + 0.1
    }

    private fun addAttributePrice(attributePrice: Double, basePrice: Double): Double = if (attributePrice > basePrice) {
        attributePrice - basePrice
    } else {
        0.0
    }

    private fun isUselessAttribute(internalName: String): Boolean {
        if (internalName.contains("RESISTANCE")) return true
        if (internalName.contains("FISHING_SPEED")) return false
        if (internalName.contains("SPEED")) return true
        if (internalName.contains("EXPERIENCE")) return true
        if (internalName.contains("FORTITUDE")) return true
        if (internalName.contains("ENDER")) return true

        return false
    }

    private fun String.fixMending() = if (this == "MENDING") "VITALITY" else this

    private fun getPriceOrCompositePriceForAttribute(attributeName: String, level: Int): Double? {
        val intRange = if (config.useAttributeComposite.get()) 1..10 else level..level
        return intRange.mapNotNull { lowerLevel ->
            "$attributeName;$lowerLevel".toInternalName().getPriceOrNull()?.let {
                it / (1 shl lowerLevel) * (1 shl level).toDouble()
            }
        }.minOrNull()
    }

    private fun addReforgeStone(stack: ItemStack, list: MutableList<String>): Double {
        val rawReforgeName = stack.getReforgeName() ?: return 0.0

        val reforge = ReforgeAPI.onlyPowerStoneReforge.firstOrNull {
            rawReforgeName == it.lowercaseName || rawReforgeName == it.reforgeStone?.asString()?.lowercase()
        } ?: return 0.0
        val internalName = reforge.reforgeStone ?: return 0.0
        val reforgeStonePrice = internalName.getPrice()
        val reforgeStoneName = internalName.itemName
        val applyCost = reforge.costs?.let { getReforgeStoneApplyCost(stack, it, internalName) } ?: return 0.0

        list.add("§7Reforge: §9${reforge.name}")
        list.add(" §7Stone: $reforgeStoneName §7(§6" + reforgeStonePrice.shortFormat() + "§7)")
        list.add(" §7Apply cost: (§6" + applyCost.shortFormat() + "§7)")
        return reforgeStonePrice + applyCost
    }

    private fun getReforgeStoneApplyCost(
        stack: ItemStack,
        reforgeCosts: Map<LorenzRarity, Long>,
        reforgeStone: NEUInternalName,
    ): Int? {
        var itemRarity = stack.getItemRarityOrNull() ?: return null

        // Catch cases of special or very special
        if (itemRarity > LorenzRarity.MYTHIC) {
            itemRarity = LorenzRarity.LEGENDARY
        } else {
            if (stack.isRecombobulated()) {
                val oneBelow = itemRarity.oneBelow(logError = false)
                if (oneBelow == null) {
                    ErrorManager.logErrorStateWithData(
                        "Wrong item rarity detected in estimated item value for item ${stack.name}",
                        "Recombobulated item is common",
                        "internal name" to stack.getInternalName(),
                        "itemRarity" to itemRarity,
                        "item name" to stack.name,
                        "item nbt" to stack.readNbtDump(),
                    )
                    return null
                }
                itemRarity = oneBelow
            }
        }

        return reforgeCosts[itemRarity]?.toInt() ?: run {
            ErrorManager.logErrorStateWithData(
                "Could not calculate reforge cost for item ${stack.name}",
                "Item not in NEU repo reforge cost",
                "reforgeCosts" to reforgeCosts,
                "itemRarity" to itemRarity,
                "internal name" to stack.getInternalName(),
                "item name" to stack.name,
                "reforgeStone" to reforgeStone,
                "item nbt" to stack.readNbtDump(),
            )
            null
        }
    }

    private fun addRecombobulator(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.isRecombobulated()) return 0.0

        val price = RECOMBOBULATOR_3000.getPrice()
        list.add("§7Recombobulated: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addJalapenoBook(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasJalapenoBook()) return 0.0

        val price = JALAPENO_BOOK.getPrice()
        list.add("§7Jalapeno Book: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addEtherwarp(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasEtherwarp()) return 0.0

        val price = ETHERWARP_CONDUIT.getPrice() + ETHERWARP_MERGER.getPrice()
        list.add("§7Etherwarp: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addWoodSingularity(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasWoodSingularity()) return 0.0

        val price = WOOD_SINGULARITY.getPrice()
        list.add("§7Wood Singularity: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addDivanPowderCoating(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasDivanPowderCoating()) return 0.0

        val price = DIVAN_POWDER_COATING.getPrice()
        list.add("§7Divan Powder Coating: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addMithrilInfusion(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.getMithrilInfusion()) return 0.0
        val price = MITHRIL_INFUSION.getPrice()
        list.add("§7Mithril Infusion: §a§l✔ §7(§6${price.shortFormat()}§7)")
        return price
    }

    private fun addArtOfWar(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasArtOfWar()) return 0.0

        val price = ART_OF_WAR.getPrice()
        list.add("§7The Art of War: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addStatsBook(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasBookOfStats()) return 0.0

        val price = BOOK_OF_STATS.getPrice()
        list.add("§7Book of Stats: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    // TODO untested
    private fun addArtOfPeace(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasArtOfPeace()) return 0.0

        val price = ART_OF_PEACE.getPrice()
        list.add("§7The Art Of Peace: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addHotPotatoBooks(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getHotPotatoCount() ?: return 0.0

        val hpb: Int
        val fuming: Int
        if (count <= 10) {
            hpb = count
            fuming = 0
        } else {
            hpb = 10
            fuming = count - 10
        }

        var totalPrice = 0.0

        val hpbPrice = HOT_POTATO_BOOK.getPrice() * hpb
        list.add("§7HPB's: §e$hpb§7/§e10 §7(§6" + hpbPrice.shortFormat() + "§7)")
        totalPrice += hpbPrice

        if (fuming > 0) {
            val fumingPrice = FUMING_POTATO_BOOK.getPrice() * fuming
            list.add("§7Fuming: §e$fuming§7/§e5 §7(§6" + fumingPrice.shortFormat() + "§7)")
            totalPrice += fumingPrice
        }

        return totalPrice
    }

    private fun addFarmingForDummies(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getFarmingForDummiesCount() ?: return 0.0

        val price = FARMING_FOR_DUMMIES.getPrice() * count
        list.add("§7Farming for Dummies: §e$count§7/§e5 §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addPolarvoidBook(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getPolarvoidBookCount() ?: return 0.0

        val price = POLARVOID_BOOK.getPrice() * count
        list.add("§7Polarvoid: §e$count§7/§e5 §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addPocketSackInASack(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getAppliedPocketSackInASack() ?: return 0.0

        val price = POCKET_SACK_IN_A_SACK.getPrice() * count
        list.add("§7Pocket Sack-in-a-Sack: §e$count§7/§e3 §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addBookwormBook(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getBookwormBookCount() ?: return 0.0

        val price = BOOKWORM_BOOK.getPrice() * count
        list.add("§7Bookworm's Favorite Book: §e$count§7/§e5 §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addSilex(stack: ItemStack, list: MutableList<String>): Double {
        val tier = stack.getSilexCount() ?: return 0.0

        val internalName = stack.getInternalName()
        val maxTier = if (internalName == STONK_PICKAXE) 4 else 5

        val price = SILEX.getPrice() * tier
        list.add("§7Silex: §e$tier§7/§e$maxTier §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addTransmissionTuners(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getTransmissionTunerCount() ?: return 0.0

        val price = TRANSMISSION_TUNER.getPrice() * count
        list.add("§7Transmission Tuners: §e$count§7/§e4 §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addManaDisintegrators(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getManaDisintegrators() ?: return 0.0

        val price = MANA_DISINTEGRATOR.getPrice() * count
        list.add("§7Mana Disintegrators: §e$count§7/§e10 §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addStars(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getInternalNameOrNull() ?: return 0.0
        var totalStars = stack.getDungeonStarCount() ?: stack.getStarCount() ?: 0

        starChange.takeIf { it != 0 }?.let {
            list.add("[Debug] added stars: $it")
            totalStars += it
        }

        val (price, stars) = calculateStarPrice(internalName, totalStars) ?: return 0.0
        val (havingStars, maxStars) = stars

        val items = mutableMapOf<NEUInternalName, Long>()
        price.essencePrice.let {
            val essenceName = "ESSENCE_${it.essenceType}".toInternalName()
            val amount = it.essenceAmount
            items[essenceName] = amount.toLong()
        }

        price.coinPrice.takeIf { it != 0L }?.let {
            items[SKYBLOCK_COIN] = it
        }

        for ((materialInternalName, amount) in price.itemPrice) {
            items[materialInternalName] = amount.toLong()
        }
        val (totalPrice, names) = getTotalAndNames(items)

        list.add("§7Stars: §e$havingStars§7/§e$maxStars §7(§6" + totalPrice.shortFormat() + "§7)")
        val starMaterialCap: Int = config.starMaterialCap.get()
        list.addAll(names.take(starMaterialCap))
        return totalPrice
    }

    private fun calculateStarPrice(
        internalName: NEUInternalName,
        inputStars: Int,
    ): Pair<EssenceUtils.EssenceUpgradePrice, Pair<Int, Int>>? {
        var totalStars = inputStars
        val (price, maxStars) = if (internalName.isKuudraArmor()) {
            val tier = (internalName.getKuudraTier() ?: 0) - 1
            totalStars += tier * 10

            var remainingStars = totalStars

            val removed = internalName.removeKuudraTier().asString()
            var maxStars = 0
            var finalPrice: EssenceUtils.EssenceUpgradePrice? = null

            val tiers = mutableMapOf<NEUInternalName, Int>()

            for ((id, _) in EssenceUtils.itemPrices) {
                if (!id.contains(removed)) continue
                tiers[id] = (id.getKuudraTier() ?: 0) - 1

            }
            for ((id, _) in tiers.sorted()) {
                val prices = EssenceUtils.itemPrices[id].orEmpty()
                maxStars += prices.size
                if (remainingStars <= 0) continue

                val price = getPriceFor(prices, remainingStars) ?: return null
                finalPrice = finalPrice?.let { it + price } ?: price
                remainingStars -= prices.size
            }
            if (finalPrice == null) return null

            finalPrice to maxStars
        } else {
            val prices = internalName.getEssencePrices()
            if (totalStars == 0 || prices == null) return null

            (getPriceFor(prices, totalStars) ?: return null) to prices.size
        }
        val havingStars = totalStars.coerceAtMost(maxStars)

        return price to (havingStars to maxStars)
    }

    private fun getPriceFor(
        prices: Map<Int, EssenceUtils.EssenceUpgradePrice>,
        totalStars: Int,
    ): EssenceUtils.EssenceUpgradePrice? {
        var totalEssencePrice: EssenceUtils.EssencePrice? = null
        var totalCoinPrice = 0L
        val totalItemPrice = mutableMapOf<NEUInternalName, Int>()

        for ((tier, price) in prices) {
            if (tier > totalStars) break
            val essencePrice = price.essencePrice
            totalEssencePrice = totalEssencePrice?.let { it + essencePrice } ?: essencePrice

            price.coinPrice?.let {
                totalCoinPrice += it
            }
            for (entry in price.itemPrice) {
                totalItemPrice.addOrPut(entry.key, entry.value)
            }
        }
        totalEssencePrice ?: return null
        return EssenceUtils.EssenceUpgradePrice(totalEssencePrice, totalCoinPrice, totalItemPrice)
    }

    private fun addMasterStars(stack: ItemStack, list: MutableList<String>): Double {
        var totalStars = stack.getDungeonStarCount() ?: return 0.0
        starChange.takeIf { it != 0 }?.let {
            totalStars += it
        }

        val masterStars = (totalStars - 5).coerceAtMost(5)
        if (masterStars < 1) return 0.0

        var price = 0.0

        val stars = mapOf(
            "FIRST" to 1,
            "SECOND" to 2,
            "THIRD" to 3,
            "FOURTH" to 4,
            "FIFTH" to 5,
        )

        for ((prefix, number) in stars) {
            if (masterStars >= number) {
                price += "${prefix}_MASTER_STAR".toInternalName().getPrice()
            }
        }

        list.add("§7Master Stars: §e$masterStars§7/§e5 §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun getTotalAndNames(
        singleItems: List<NEUInternalName>,
    ): Pair<Double, List<String>> {
        return getTotalAndNames(singleItems.associateWith { 1 })
    }

    private fun getTotalAndNames(
        items: Map<NEUInternalName, Number>,
    ): Pair<Double, List<String>> {
        var totalPrice = 0.0
        val map = mutableMapOf<String, Double>()
        for ((internalName, amount) in items) {
            val price = internalName.getPriceOrNull() ?: continue

            totalPrice += price * amount.toDouble()
            map[internalName.getPriceName(amount)] = price
        }
        return totalPrice to map.sortedDesc().keys.toList()
    }

    private fun addDrillUpgrades(stack: ItemStack, list: MutableList<String>): Double {
        val drillUpgrades = stack.getDrillUpgrades() ?: return 0.0

        val (totalPrice, items) = getTotalAndNames(drillUpgrades)
        if (items.isNotEmpty()) {
            list.add("§7Drill upgrades: §6" + totalPrice.shortFormat())
            list += items
        }
        return totalPrice
    }

    private fun addPowerScrolls(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getPowerScroll() ?: return 0.0

        val price = internalName.getPrice()
        val name = internalName.itemNameWithoutColor
        list.add("§7$name: §a§l✔ §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addHelmetSkin(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getHelmetSkin() ?: return 0.0
        return addCosmetic(internalName, list, "Skin", config.ignoreHelmetSkins)
    }

    private fun addArmorDye(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getArmorDye() ?: return 0.0
        return addCosmetic(internalName, list, "Dye", config.ignoreArmorDyes)
    }

    private fun addCosmetic(
        internalName: NEUInternalName,
        list: MutableList<String>,
        label: String,
        shouldIgnorePrice: Property<Boolean>,
    ): Double {
        val price = internalName.getPrice()
        val name = internalName.getNameOrRepoError()
        val displayName = name ?: "§c${internalName.asString()}"
        val color = if (shouldIgnorePrice.get()) "§7" else "§6"
        list.add("§7$label: $displayName §7($color" + price.shortFormat() + "§7)")
        if (name == null) {
            list.add("   §8(Not yet in NEU Repo)")
        }

        return if (shouldIgnorePrice.get()) 0.0 else price
    }

    private fun addEnrichment(stack: ItemStack, list: MutableList<String>): Double {

        val enrichmentName = stack.getEnrichment() ?: return 0.0
        val internalName = "TALISMAN_ENRICHMENT_$enrichmentName".toInternalName()

        val price = internalName.getPrice()
        val name = internalName.itemName
        list.add("§7Enrichment: $name §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addRune(stack: ItemStack, list: MutableList<String>): Double {
        if (stack.getInternalName().isRune()) return 0.0
        val internalName = stack.getRune() ?: return 0.0

        return addCosmetic(internalName, list, "Rune", config.ignoreRunes)
    }

    private fun NEUInternalName.getNameOrRepoError(): String? = getItemStackOrNull()?.itemName

    private fun addAbilityScrolls(stack: ItemStack, list: MutableList<String>): Double {
        val abilityScrolls = stack.getAbilityScrolls() ?: return 0.0

        val (totalPrice, items) = getTotalAndNames(abilityScrolls)
        if (items.isNotEmpty()) {
            list.add("§7Ability Scrolls: §6" + totalPrice.shortFormat())
            list += items
        }
        return totalPrice
    }

    private fun addBaseItem(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getInternalName().removeKuudraTier()

        stack.getAttributeFromShard()?.let {
            val price = it.getAttributePrice()
            if (price != null) {
                val name = it.getAttributeName()
                list.add("§7Base item: $name §7(§6" + price.shortFormat() + "§7)")
                return price
            }
        }

        var price = internalName.getPrice()
        if (price == -1.0) {
            price = 0.0
        }

        // If craft cost price is greater than npc price, and there is no ah/bz price, use craft cost instead
        internalName.getNpcPriceOrNull()?.let { npcPrice ->
            if (price == npcPrice) {
                internalName.getRawCraftCostOrNull()?.let { rawCraftPrice ->
                    if (rawCraftPrice > npcPrice) {
                        price = rawCraftPrice
                    }
                }
            }
        }

        val name = internalName.itemName
        if (internalName.startsWith("ENCHANTED_BOOK_BUNDLE_")) {
            list.add("§7Base item: $name")
            return 0.0
        }

        list.add("§7Base item: $name §7(§6" + price.shortFormat() + "§7)")
        return price
    }

    private fun addEnchantments(stack: ItemStack, list: MutableList<String>): Double {
        val enchantments = stack.getEnchantments() ?: return 0.0

        val internalName = stack.getInternalName()
        val items = mutableMapOf<NEUInternalName, Int>()
        for ((rawName, rawLevel) in enchantments) {
            // efficiency 1-5 is cheap, 6-10 is handled by silex
            if (rawName == "efficiency") continue

            val isAlwaysActive = EstimatedItemValue.itemValueCalculationData?.alwaysActiveEnchants.orEmpty().entries.any {
                it.key == rawName && it.value.level == rawLevel && it.value.internalNames.contains(internalName)
            }
            if (isAlwaysActive) continue

            var level = rawLevel
            var multiplier = 1
            if (rawName in EstimatedItemValue.itemValueCalculationData?.onlyTierOnePrices.orEmpty()) {

                when (rawLevel) {
                    2 -> multiplier = 2
                    3 -> multiplier = 4
                    4 -> multiplier = 8
                    5 -> multiplier = 16
                }
                level = 1
            }
            if (rawName in EstimatedItemValue.itemValueCalculationData?.onlyTierFivePrices.orEmpty()) {
                when (rawLevel) {
                    6 -> multiplier = 2
                    7 -> multiplier = 4
                    8 -> multiplier = 8
                    9 -> multiplier = 16
                    10 -> multiplier = 32
                }
                if (multiplier > 1) {
                    level = 5
                }
            }
            if (internalName.startsWith("ENCHANTED_BOOK_BUNDLE_")) {
                multiplier = EstimatedItemValue.bookBundleAmount.getOrDefault(rawName, 5)
            }
            if (rawName in DiscordRPCManager.stackingEnchants.keys) level = 1

            val enchantmentName = "$rawName;$level".toInternalName()

            items[enchantmentName] = multiplier
        }
        val (totalPrice, names) = getTotalAndNames(items)
        val enchantmentsCap: Int = config.enchantmentsCap.get()
        if (names.isEmpty()) return 0.0
        list.add("§7Enchantments: §6" + totalPrice.shortFormat())
        var i = 0
        for (name in names) {
            if (i == enchantmentsCap) {
                val missing = names.size - enchantmentsCap
                list.add(" §7§o$missing more enchantments..")
                break
            }
            list.add(name)
            i++
        }
        return totalPrice
    }

    private fun addGemstones(stack: ItemStack, list: MutableList<String>): Double {
        val gemstones = stack.getGemstones() ?: return 0.0

        val items = mutableMapOf<NEUInternalName, Int>()
        for (gemstone in gemstones) {
            val internalName = gemstone.getInternalName()
            val old = items[internalName] ?: 0
            items[internalName] = old + 1
        }

        val (totalPrice, names) = getTotalAndNames(items)
        if (names.isNotEmpty()) {
            list.add("§7Gemstones Applied: §6" + totalPrice.shortFormat())
            list += names
        }
        return totalPrice
    }

    private fun ItemStack.readNbtDump() = tagCompound?.getReadableNBTDump(includeLore = true)?.joinToString("\n")
        ?: "no tag compound"

    private fun ItemStack.readUnlockedSlots(): String? {
        // item have to contains gems.unlocked_slots NBT array for unlocked slot detection
        val unlockedSlots = getExtraAttributes()?.getCompoundTag("gems")?.getTag("unlocked_slots")?.toString() ?: return null

        // TODO detection for old items which doesn't have gems.unlocked_slots NBT array
//        if (unlockedSlots == "null") return 0.0

        if (EstimatedItemValue.gemstoneUnlockCosts.isEmpty()) return null

        val internalName = getInternalName()
        if (internalName !in EstimatedItemValue.gemstoneUnlockCosts) {
            ErrorManager.logErrorStateWithData(
                "Could not find gemstone slot price for $name",
                "EstimatedItemValue has no gemstoneUnlockCosts for $internalName",
                "internal name" to internalName,
                "gemstoneUnlockCosts" to EstimatedItemValue.gemstoneUnlockCosts,
                "item name" to name,
                "item nbt" to readNbtDump(),
            )
            return null
        }

        return unlockedSlots
    }

    private fun addGemstoneSlotUnlockCost(stack: ItemStack, list: MutableList<String>): Double {
        val unlockedSlots = stack.readUnlockedSlots() ?: return 0.0

        val items = mutableMapOf<NEUInternalName, Int>()
        val slots = EstimatedItemValue.gemstoneUnlockCosts[stack.getInternalName()] ?: return 0.0
        val slotNames = mutableListOf<String>()
        for ((key, value) in slots) {
            if (!unlockedSlots.contains(key)) continue

            for (ingredients in value) {
                val ingredient = PrimitiveIngredient(ingredients)
                val amount = ingredient.count.toInt()
                items.addOrPut(ingredient.internalName, amount)
            }

            val splitSlot = key.split("_") // eg. SAPPHIRE_1
            val colorCode = SkyBlockItemModifierUtils.GemstoneSlotType.getColorCode(splitSlot[0])

            // eg. SAPPHIRE_1 -> Sapphire Slot 2
            val displayName = splitSlot[0].lowercase(Locale.ENGLISH).replaceFirstChar(Char::uppercase) + " Slot" +
                // If the slot index is 0, we don't need to specify
                if (splitSlot[1] != "0") " " + (splitSlot[1].toInt() + 1) else ""

            slotNames.add("§$colorCode$displayName")
        }

        if (slotNames.isEmpty()) return 0.0

        val (totalPrice, names) = getTotalAndNames(items)
        list.add("§7Gemstone Slot Unlock Cost: §6" + totalPrice.shortFormat())

        list += names

        // TODO add toggle that is default enabled "show unlocked gemstone slot name
        list.add(" §7Unlocked slots: " + slotNames.joinToString("§7, "))

        return totalPrice
    }

    private fun NEUInternalName.getPriceName(amount: Number): String {
        val price = getPrice() * amount.toDouble()
        if (this == SKYBLOCK_COIN) return " §6${price.shortFormat()} coins"

        val prefix = if (amount == 1.0) "" else "§8${amount.addSeparators()}x "
        return " $prefix§r$itemName §7(§6${price.shortFormat()}§7)"
    }

    private fun NEUInternalName.getPrice(): Double = getPriceOrNull() ?: 0.0
    private fun NEUInternalName.getPriceOrNull(): Double? = getPriceOrNull(config.priceSource.get())

    fun Pair<String, Int>.getAttributeName(): String {
        val name = first.fixMending().allLettersFirstUppercase()
        return "§b$name $second Shard"
    }

    private fun Pair<String, Int>.getAttributePrice(): Double? = getPriceOrCompositePriceForAttribute(
        "ATTRIBUTE_SHARD+ATTRIBUTE_$first",
        second,
    )
}
