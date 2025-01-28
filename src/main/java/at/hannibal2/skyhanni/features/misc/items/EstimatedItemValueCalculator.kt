package at.hannibal2.skyhanni.features.misc.items

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.ReforgeApi
import at.hannibal2.skyhanni.data.jsonobjects.repo.ItemValueCalculationDataJson
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.isBazaarItem
import at.hannibal2.skyhanni.features.misc.discordrpc.DiscordRPCManager
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraApi
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraApi.getKuudraTier
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraApi.isKuudraArmor
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraApi.removeKuudraTier
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.EssenceUtils
import at.hannibal2.skyhanni.utils.EssenceUtils.getEssencePrices
import at.hannibal2.skyhanni.utils.InventoryUtils
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
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.SKYBLOCK_COIN
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NeuItems.removePrefix
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.intPow
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
            genericName = KuudraApi.kuudraSets.fold(internalNameString) { acc, part -> acc.replace(part, "GENERIC_KUUDRA") }
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
            val gray = comboPrice <= basePrice || useless
            list.add("§7Attribute Combo: ${comboPrice.formatWithBrackets(gray)}")
            if (!useless) {
                subTotal += addAttributePrice(comboPrice, basePrice)
            }
        } else {
            list.add("§7Attributes:")
        }
        for (attr in attributes) {
            val attributeName = "$genericName+ATTRIBUTE_${attr.first}"
            val price = getPriceOrCompositePriceForAttribute(attributeName, attr.second)
            var gray = true
            val useless = isUselessAttribute(attributeName)
            val nameColor = if (!useless) "§9" else "§7"
            if (price != null) {
                if (price > basePrice && !useless) {
                    subTotal += addAttributePrice(price, basePrice)
                    gray = false
                }

            }
            val displayName = attr.first.fixMending()
            list.add(
                "  $nameColor${
                    displayName.allLettersFirstUppercase()
                } ${attr.second}§7: ${price?.format(gray) ?: "Unknown"}",
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

        val reforge = ReforgeApi.onlyPowerStoneReforge.firstOrNull {
            rawReforgeName == it.lowercaseName || rawReforgeName == it.reforgeStone?.asString()?.lowercase()
        } ?: return 0.0
        val internalName = reforge.reforgeStone ?: return 0.0
        val reforgeStonePrice = internalName.getPrice()
        val reforgeStoneName = internalName.itemName
        val applyCost = reforge.costs?.let { getReforgeStoneApplyCost(stack, it, internalName) } ?: return 0.0

        list.add("§7Reforge: §9${reforge.name}")
        list.add(" §7Stone: $reforgeStoneName ${reforgeStonePrice.formatWithBrackets()}")
        list.add(" §7Apply cost: ${applyCost.formatWithBrackets()}")
        return reforgeStonePrice + applyCost
    }

    private fun getReforgeStoneApplyCost(
        stack: ItemStack,
        reforgeCosts: Map<LorenzRarity, Long>,
        reforgeStone: NeuInternalName,
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

        return list.formatHaving("Recombobulated", RECOMBOBULATOR_3000)
    }

    private fun addJalapenoBook(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasJalapenoBook()) return 0.0

        return list.formatHaving("Jalapeno Book", JALAPENO_BOOK)
    }

    private fun addWoodSingularity(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasWoodSingularity()) return 0.0

        return list.formatHaving("Wood Singularity", WOOD_SINGULARITY)
    }

    private fun addDivanPowderCoating(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasDivanPowderCoating()) return 0.0

        return list.formatHaving("Divan Powder Coating", DIVAN_POWDER_COATING)
    }

    private fun addMithrilInfusion(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.getMithrilInfusion()) return 0.0
        return list.formatHaving("Mithril Infusion", MITHRIL_INFUSION)
    }

    private fun addArtOfWar(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasArtOfWar()) return 0.0

        return list.formatHaving("The Art of War", ART_OF_WAR)
    }

    private fun addStatsBook(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasBookOfStats()) return 0.0

        return list.formatHaving("Book of Stats", BOOK_OF_STATS)
    }

    // TODO untested
    private fun addArtOfPeace(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasArtOfPeace()) return 0.0

        return list.formatHaving("The Art Of Peace", ART_OF_PEACE)
    }

    private fun addPowerScrolls(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getPowerScroll() ?: return 0.0

        val name = internalName.itemNameWithoutColor
        return list.formatHaving(name, internalName)
    }

    private fun MutableList<String>.formatHaving(label: String, internalName: NeuInternalName): Double {
        val price = internalName.getPrice()
        add("§7$label: §a§l✔ ${price.formatWithBrackets()}")
        return price
    }

    private fun addEtherwarp(stack: ItemStack, list: MutableList<String>): Double {
        if (!stack.hasEtherwarp()) return 0.0
        val price = ETHERWARP_CONDUIT.getPrice() + ETHERWARP_MERGER.getPrice()
        list.add("§7Etherwarp: §a§l✔ ${price.formatWithBrackets()}")
        return price
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

        list.add(formatProgress("Master Stars", masterStars, max = 5, price))
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
        list.add(formatProgress("HPB's", hpb, max = 10, hpbPrice))
        totalPrice += hpbPrice

        if (fuming > 0) {
            val fumingPrice = FUMING_POTATO_BOOK.getPrice() * fuming
            list.add(formatProgress("Fuming", fuming, max = 5, fumingPrice))
            totalPrice += fumingPrice
        }

        return totalPrice
    }

    private fun addFarmingForDummies(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getFarmingForDummiesCount() ?: return 0.0

        val price = FARMING_FOR_DUMMIES.getPrice() * count
        list.add(formatProgress("Farming for Dummies", count, max = 5, price))
        return price
    }

    private fun addPolarvoidBook(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getPolarvoidBookCount() ?: return 0.0

        val price = POLARVOID_BOOK.getPrice() * count
        list.add(formatProgress("Polarvoid", count, max = 5, price))
        return price
    }

    private fun addPocketSackInASack(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getAppliedPocketSackInASack() ?: return 0.0

        val price = POCKET_SACK_IN_A_SACK.getPrice() * count
        list.add(formatProgress("Pocket Sack-in-a-Sack", count, max = 3, price))
        return price
    }

    private fun addBookwormBook(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getBookwormBookCount() ?: return 0.0

        val price = BOOKWORM_BOOK.getPrice() * count
        list.add(formatProgress("Bookworm's Favorite Book", count, max = 5, price))
        return price
    }

    private fun addSilex(stack: ItemStack, list: MutableList<String>): Double {
        val tier = stack.getSilexCount() ?: return 0.0

        val internalName = stack.getInternalName()
        val maxTier = if (internalName == STONK_PICKAXE) 4 else 5

        val price = SILEX.getPrice() * tier
        list.add(formatProgress("Silex", tier, maxTier, price))
        return price
    }

    private fun addTransmissionTuners(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getTransmissionTunerCount() ?: return 0.0

        val price = TRANSMISSION_TUNER.getPrice() * count
        list.add(formatProgress("Transmission Tuners", count, max = 4, price))
        return price
    }

    private fun addManaDisintegrators(stack: ItemStack, list: MutableList<String>): Double {
        val count = stack.getManaDisintegrators() ?: return 0.0

        val price = MANA_DISINTEGRATOR.getPrice() * count
        list.add(formatProgress("Mana Disintegrators", count, max = 10, price))
        return price
    }

    private fun formatProgress(label: String, have: Int, max: Int, price: Number): String {
        return "§7$label: §e$have§7/§e$max ${price.formatWithBrackets()}"
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

        val items = mutableMapOf<NeuInternalName, Number>()
        price.essencePrice.let {
            val essenceName = "ESSENCE_${it.essenceType}".toInternalName()
            val amount = it.essenceAmount
            items[essenceName] = amount
        }

        price.coinPrice.takeIf { it != 0L }?.let {
            items[SKYBLOCK_COIN] = it
        }

        for ((materialInternalName, amount) in price.itemPrice) {
            items[materialInternalName] = amount
        }
        val (totalPrice, names) = getTotalAndNames(items)

        list.add(formatProgress("Stars", havingStars, maxStars, totalPrice))
        val starMaterialCap: Int = config.starMaterialCap.get()
        list.addAll(names.take(starMaterialCap))
        return totalPrice
    }

    private fun calculateStarPrice(
        internalName: NeuInternalName,
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

            val tiers = mutableMapOf<NeuInternalName, Int>()

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
        val totalItemPrice = mutableMapOf<NeuInternalName, Int>()

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

    private fun addDrillUpgrades(stack: ItemStack, list: MutableList<String>): Double {
        val drillUpgrades = stack.getDrillUpgrades() ?: return 0.0

        val (totalPrice, names) = getTotalAndNames(drillUpgrades)
        if (names.isNotEmpty()) {
            list.add("§7Drill upgrades: " + totalPrice.format())
            list += names
        }
        return totalPrice
    }

    private fun addAbilityScrolls(stack: ItemStack, list: MutableList<String>): Double {
        val abilityScrolls = stack.getAbilityScrolls() ?: return 0.0

        val (totalPrice, names) = getTotalAndNames(abilityScrolls)
        if (names.isNotEmpty()) {
            list.add("§7Ability Scrolls: " + totalPrice.format())
            list += names
        }
        return totalPrice
    }

    private fun addEnchantments(stack: ItemStack, list: MutableList<String>): Double {
        val (totalPrice, names) = stack.getEnchantmentItems() ?: return 0.0

        val enchantmentsCap: Int = config.enchantmentsCap.get()
        if (names.isEmpty()) return 0.0
        list.add("§7Enchantments: " + totalPrice.format())
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

    private fun ItemStack.getEnchantmentItems(): Pair<Double, List<String>>? {
        val enchantments = getEnchantments() ?: return null
        val data = EstimatedItemValue.itemValueCalculationData ?: return null
        val items = fetchEnchantmentItems(enchantments, getInternalName(), data)
        return getTotalAndNames(items)
    }

    private fun fetchEnchantmentItems(
        enchantments: Map<String, Int>,
        internalName: NeuInternalName,
        data: ItemValueCalculationDataJson,
    ): Map<NeuInternalName, Int> {

        val items = mutableMapOf<NeuInternalName, Int>()
        for ((rawName, rawLevel) in enchantments) {
            // efficiency 1-5 is cheap, 6-10 is handled by silex
            if (rawName == "efficiency") continue

            val isAlwaysActive = try {
                data.alwaysActiveEnchants.entries.any { (key, value) ->
                    val level = value.level
                    val internalNames = value.internalNames
                    key == rawName && level == rawLevel && internalNames.contains(internalName)
                }
            } catch (e: NullPointerException) {
                ErrorManager.logErrorWithData(
                    e, "Estimated Item value failed to properly show ${internalName.itemName}",
                    "openInventoryName" to InventoryUtils.openInventoryName(),
                    "internalName" to internalName,
                    "rawName" to rawName,
                    "rawLevel" to rawLevel,
                    "alwaysActiveEnchants" to data.alwaysActiveEnchants.entries,
                )
                false
            }
            if (isAlwaysActive) continue
            var level = rawLevel
            var multiplier = 1

            when {
                rawName in data.onlyTierOnePrices && rawLevel in 2..5 -> {
                    multiplier = 2.intPow(rawLevel - 1)
                    level = 1
                }

                rawName in data.onlyTierFivePrices && rawLevel in 6..10 -> {
                    multiplier = 2.intPow(rawLevel - 5)
                    if (multiplier > 1) level = 5
                }
            }
            if (internalName.startsWith("ENCHANTED_BOOK_BUNDLE_")) {
                multiplier = EstimatedItemValue.bookBundleAmount.getOrDefault(rawName, 5)
            }
            if (rawName in DiscordRPCManager.stackingEnchants.keys) level = 1

            val enchantmentName = "$rawName;$level".toInternalName()

            if (enchantmentName.isBazaarItem()) {
                items[enchantmentName] = multiplier
            }
        }
        return items
    }

    private fun addGemstones(stack: ItemStack, list: MutableList<String>): Double {
        val gemstones = stack.getGemstones() ?: return 0.0

        val items = mutableMapOf<NeuInternalName, Int>()
        for (gemstone in gemstones) {
            val internalName = gemstone.getInternalName()
            val old = items[internalName] ?: 0
            items[internalName] = old + 1
        }

        val (totalPrice, names) = getTotalAndNames(items)
        if (names.isNotEmpty()) {
            list.add("§7Gemstones Applied: " + totalPrice.format())
            list += names
        }
        return totalPrice
    }

    private fun addGemstoneSlotUnlockCost(stack: ItemStack, list: MutableList<String>): Double {
        val unlockedSlots = stack.readUnlockedSlots() ?: return 0.0

        val items = mutableMapOf<NeuInternalName, Int>()
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
        list.add("§7Gemstone Slot Unlock Cost: " + totalPrice.format())

        list += names

        // TODO add toggle that is default enabled "show unlocked gemstone slot name
        list.add(" §7Unlocked slots: " + slotNames.joinToString("§7, "))

        return totalPrice
    }

    private fun getTotalAndNames(
        singleItems: List<NeuInternalName>,
    ): Pair<Double, List<String>> {
        return getTotalAndNames(singleItems.associateWith { 1 })
    }

    private fun getTotalAndNames(
        items: Map<NeuInternalName, Number>,
    ): Pair<Double, List<String>> {
        var totalPrice = 0.0
        val map = mutableMapOf<String, Double>()
        for ((internalName, amount) in items) {
            val price = internalName.getPriceOrNull()
            if (price != null) {
                totalPrice += price * amount.toDouble()
                map[internalName.getPriceName(amount)] = price
            } else {
                val name = internalName.getNumberedName(amount)
                map[" $name §cUnknown price!"] = Double.MAX_VALUE
            }
        }
        return totalPrice to map.sortedDesc().keys.toList()
    }

    private fun NeuInternalName.getPriceName(amount: Number): String {
        val price = getPrice() * amount.toDouble()
        if (this == SKYBLOCK_COIN) return " ${price.format()} coins"

        return " ${getNumberedName(amount)} ${price.formatWithBrackets()}"
    }

    private fun NeuInternalName.getNumberedName(amount: Number): String {
        val prefix = if (amount == 1.0) "" else "§8${amount.addSeparators()}x "
        return "$prefix§r$itemName"
    }

    private fun Number.formatWithBrackets(gray: Boolean = false): String {
        return "§7(" + format(gray) + "§7)"
    }

    private fun Number.format(gray: Boolean = false): String {
        val color = if (gray) "§7" else "§6"
        return color + shortFormat()
    }

    private fun addHelmetSkin(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getHelmetSkin() ?: return 0.0
        return addCosmetic(internalName, list, "Skin", config.ignoreHelmetSkins)
    }

    private fun addArmorDye(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getArmorDye() ?: return 0.0
        return addCosmetic(internalName, list, "Dye", config.ignoreArmorDyes)
    }

    private fun addRune(stack: ItemStack, list: MutableList<String>): Double {
        if (stack.getInternalName().isRune()) return 0.0
        val internalName = stack.getRune() ?: return 0.0

        return addCosmetic(internalName, list, "Rune", config.ignoreRunes)
    }

    private fun addCosmetic(
        internalName: NeuInternalName,
        list: MutableList<String>,
        label: String,
        shouldIgnorePrice: Property<Boolean>,
    ): Double {
        val price = internalName.getPrice()
        val name = internalName.getItemStackOrNull()?.itemName
        val displayName = name ?: "§c${internalName.asString()}"
        val gray = shouldIgnorePrice.get()

        list.add("§7$label: $displayName " + price.formatWithBrackets(gray))
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
        list.add("§7Enrichment: $name ${price.formatWithBrackets()}")
        return price
    }

    private fun addBaseItem(stack: ItemStack, list: MutableList<String>): Double {
        val internalName = stack.getInternalName().removeKuudraTier()

        stack.getAttributeFromShard()?.let {
            val price = it.getAttributePrice()
            if (price != null) {
                val name = it.getAttributeName()
                list.add("§7Base item: $name ${price.formatWithBrackets()}")
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

        list.add("§7Base item: $name ${price.formatWithBrackets()}")
        return price
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

    private fun NeuInternalName.getPrice(): Double = getPriceOrNull() ?: 0.0
    private fun NeuInternalName.getPriceOrNull(): Double? = getPriceOrNull(config.priceSource.get())

    // TODO create attribute class and use this instead of pair, sync with getAttributeFromShard()
    fun Pair<String, Int>.getAttributeName(): String {
        val name = first.fixMending().allLettersFirstUppercase()
        return "§b$name $second Shard"
    }

    private fun Pair<String, Int>.getAttributePrice(): Double? = getPriceOrCompositePriceForAttribute(
        "ATTRIBUTE_SHARD+ATTRIBUTE_$first",
        second,
    )
}
