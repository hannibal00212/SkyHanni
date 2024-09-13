package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery

object ItemNameResolver {
    private val itemNameCache = mutableMapOf<String, NEUInternalName>() // item name -> internal name

    internal fun getInternalNameOrNull(itemName: String): NEUInternalName? {
        val lowercase = itemName.lowercase()
        itemNameCache[lowercase]?.let {
            return it
        }

        getInternalNameOrNullIgnoreCase(itemName)?.let {
            return itemNameCache.getOrPut(lowercase) { it }
        }

        if (itemName == "§cmissing repo item") {
            return itemNameCache.getOrPut(lowercase) { NEUInternalName.MISSING_ITEM }
        }

        resolveEnchantmentByName(itemName)?.let {
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
                    return itemNameCache.getOrPut(lowercase) { it.asInternalName() }
                }
            }
        }

        val internalName = when (itemName) {
            "SUPERBOOM TNT" -> "SUPERBOOM_TNT".asInternalName()
            else -> {
                ItemResolutionQuery.findInternalNameByDisplayName(itemName, true)?.let {

                    // This fixes a NEU bug with §9Hay Bale (cosmetic item)
                    // TODO remove workaround when this is fixed in neu
                    val rawInternalName = if (it == "HAY_BALE") "HAY_BLOCK" else it
                    rawInternalName.asInternalName()
                } ?: return null
            }
        }

        itemNameCache[lowercase] = internalName
        return internalName
    }

    private fun resolveEnchantmentByCleanName(itemName: String): NEUInternalName? {
        UtilsPatterns.cleanEnchantedNamePattern.matchMatcher(itemName) {
            val name = group("name")
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

    // does not work without color codes, or with roman numbers
    // Taken and edited from NEU
    private fun resolveEnchantmentByName(enchantmentName: String) =
        UtilsPatterns.enchantmentNamePattern.matchMatcher(enchantmentName) {
            val name = group("name").trim { it <= ' ' }
            val ultimate = group("format").lowercase().contains("§l")
                ((if (ultimate && name != "Ultimate Wise" && name != "Ultimate Jerry") "ULTIMATE_" else "")
                + turboCheck(name).replace(" ", "_").replace("-", "_").uppercase()
                + ";" + group("level").romanToDecimal())
        }

    private fun turboCheck(text: String): String {
        if (text == "Turbo-Cocoa") return "Turbo-Coco"
        if (text == "Turbo-Cacti") return "Turbo-Cactus"
        return text
    }

    // Workaround for duplex
    private val duplexPattern = "ULTIMATE_DUPLEX;(?<tier>.*)".toPattern()

    fun fixEnchantmentName(originalName: String): NEUInternalName {
        duplexPattern.matchMatcher(originalName) {
            val tier = group("tier")
            return "ULTIMATE_REITERATE;$tier".asInternalName()
        }
        // TODO USE SH-REPO
        return originalName.asInternalName()
    }

    private fun getInternalNameOrNullIgnoreCase(itemName: String): NEUInternalName? {
        val lowercase = itemName.lowercase()
        itemNameCache[lowercase]?.let {
            return it
        }

        if (NEUItems.allItemsCache.isEmpty()) {
            NEUItems.allItemsCache = NEUItems.readAllNeuItems()
        }

        // supports colored names, rarities
        NEUItems.allItemsCache[lowercase]?.let {
            itemNameCache[lowercase] = it
            return it
        }

        // if nothing got found with colors, try without colors
        val removeColor = lowercase.removeColor()
        return NEUItems.allItemsCache.filter { it.key.removeColor() == removeColor }.values.firstOrNull()
    }
}
