package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.enoughupdates.ItemResolutionQuery
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

@SkyHanniModule
object ItemNameResolver {
    private val itemNameCache = mutableMapOf<String, NEUInternalName>() // item name -> internal name

    internal fun getInternalNameOrNull(itemName: String): NEUInternalName? {
        val lowercase = itemName.lowercase()
        itemNameCache[lowercase]?.let {
            return it
        }

        getInternalNameOrNullIgnoreCase(lowercase)?.let {
            return itemNameCache.getOrPut(lowercase) { it }
        }

        if (itemName == "§cmissing repo item") {
            return itemNameCache.getOrPut(lowercase) { NEUInternalName.MISSING_ITEM }
        }

        ItemResolutionQuery.resolveEnchantmentByName(itemName)?.let {
            return itemNameCache.getOrPut(lowercase) { fixEnchantmentName(it) }
        }

        resolveEnchantmentByCleanName(itemName)?.let {
            return itemNameCache.getOrPut(lowercase) { it }
        }

        if (itemName.endsWith("gemstone", ignoreCase = true)) {
            val split = lowercase.split(" ")
            if (split.size == 3) {
                val gemstoneQuery = "${
                    when (split[1]) {
                        "jade", "peridot", "citrine" -> '☘'
                        "amethyst" -> '❈'
                        "ruby" -> '❤'
                        "amber" -> '⸕'
                        "opal" -> '❂'
                        "topaz" -> '✧'
                        "onyx" -> '☠'
                        "sapphire" -> '✎'
                        "aquamarine" -> 'α'
                        "jasper" -> '❁'
                        else -> ' '
                    }
                } ${split.joinToString("_").allLettersFirstUppercase()}"
                ItemResolutionQuery.findInternalNameByDisplayName(gemstoneQuery, true)?.let {
                    return itemNameCache.getOrPut(lowercase) { it.toInternalName() }
                }
            }
        }

        val internalName = when (itemName) {
            "SUPERBOOM TNT" -> "SUPERBOOM_TNT".toInternalName()
            else -> {
                ItemResolutionQuery.findInternalNameByDisplayName(itemName, true)?.let {

                    // This fixes a NEU bug with §9Hay Bale (cosmetic item)
                    // TODO remove workaround when this is fixed in neu
                    val rawInternalName = if (it == "HAY_BALE") "HAY_BLOCK" else it
                    rawInternalName.toInternalName()
                } ?: return null
            }
        }

        itemNameCache[lowercase] = internalName
        return internalName
    }

    private fun resolveEnchantmentByCleanName(itemName: String): NEUInternalName? {
        UtilsPatterns.cleanEnchantedNamePattern.matchMatcher(itemName) {
            val name = group("name").replace("'", "")
            val level = group("level").romanToDecimalIfNecessary()
            val rawInternalName = "$name;$level".uppercase()

            var internalName = fixEnchantmentName(rawInternalName)
            internalName.getItemStackOrNull()?.let {
                return internalName
            }

            internalName = fixEnchantmentName("ULTIMATE_$rawInternalName")
            internalName.getItemStackOrNull()?.let {
                return internalName
            }

            return null
        }
        return null
    }

    // Workaround for duplex
    private val duplexPattern = "ULTIMATE_DUPLEX;(?<tier>.*)".toPattern()

    fun fixEnchantmentName(originalName: String): NEUInternalName {
        duplexPattern.matchMatcher(originalName) {
            val tier = group("tier")
            return "ULTIMATE_REITERATE;$tier".toInternalName()
        }
        // TODO USE SH-REPO
        return originalName.toInternalName()
    }

    private fun getInternalNameOrNullIgnoreCase(itemName: String): NEUInternalName? {
        itemNameCache[itemName]?.let {
            return it
        }

        if (NEUItems.allItemsCache.isEmpty()) {
            NEUItems.allItemsCache = NEUItems.readAllNeuItems()
        }

        // supports colored names, rarities
        NEUItems.allItemsCache[itemName]?.let {
            itemNameCache[itemName] = it
            return it
        }

        // if nothing got found with colors, try without colors
        val removeColor = itemName.removeColor()
        return NEUItems.allItemsCache.filter { it.key.removeColor() == removeColor }.values.firstOrNull()
    }

    @HandleEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        itemNameCache.clear()
    }
}
