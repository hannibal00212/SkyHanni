package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.StringUtils.splitLines

object TrophyFishAPI {

    fun hoverInfo(internalName: String): String? {
        val trophyFishes = TrophyFishManager.fish ?: return null
        val info = TrophyFishManager.getInfo(internalName) ?: return null
        val counts = trophyFishes[internalName].orEmpty()
        val bestFishObtained = counts.keys.maxOrNull() ?: TrophyRarity.BRONZE
        val rateString = if (info.rate != null) "§8[§7${info.rate}%§8]" else ""
        return """
                |${info.displayName} $rateString
                |${info.description.splitLines(150)}
                |
                |${TrophyRarity.DIAMOND.formattedString}: ${formatCount(counts, TrophyRarity.DIAMOND)}
                |${TrophyRarity.GOLD.formattedString}: ${formatCount(counts, TrophyRarity.GOLD)}
                |${TrophyRarity.SILVER.formattedString}: ${formatCount(counts, TrophyRarity.SILVER)}
                |${TrophyRarity.BRONZE.formattedString}: ${formatCount(counts, TrophyRarity.BRONZE)}
                |
                |§7Total: ${bestFishObtained.formatCode}${counts.values.sum().addSeparators()}
        """.trimMargin()
    }

    private fun formatCount(counts: Map<TrophyRarity, Int>, rarity: TrophyRarity): String {
        val count = counts.getOrDefault(rarity, 0)
        return if (count > 0) "§6${count.addSeparators()}" else "§c✖"
    }
}
