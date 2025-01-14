package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase

// TODO: replace id with ordinal
enum class LorenzRarity(val color: LorenzColor, val id: Int) {

    COMMON(LorenzColor.WHITE, 0),
    UNCOMMON(LorenzColor.GREEN, 1),
    RARE(LorenzColor.BLUE, 2),
    EPIC(LorenzColor.DARK_PURPLE, 3),
    LEGENDARY(LorenzColor.GOLD, 4),
    MYTHIC(LorenzColor.LIGHT_PURPLE, 5),
    DIVINE(LorenzColor.AQUA, 6),
    SUPREME(LorenzColor.DARK_RED, 7),
    SPECIAL(LorenzColor.RED, 8),
    VERY_SPECIAL(LorenzColor.RED, 9),
    ULTIMATE(LorenzColor.DARK_RED, 10),
    ;

    val chatColorCode get() = color.getChatColor()
    val rawName = name.replace("_", " ")

    fun oneBelow(logError: Boolean = true): LorenzRarity? {
        val rarityBelow = getById(ordinal - 1)
        if (rarityBelow == null && logError) {
            ErrorManager.logErrorStateWithData(
                "Problem with item rarity detected.",
                "Trying to get an item rarity below common",
                "ordinal" to ordinal,
            )
        }
        return rarityBelow
    }

    fun oneAbove(logError: Boolean = true): LorenzRarity? {
        val rarityBelow = getById(ordinal + 1)
        if (rarityBelow == null && logError) {
            ErrorManager.logErrorStateWithData(
                "Problem with item rarity detected.",
                "Trying to get an item rarity above special",
                "ordinal" to ordinal,
            )
        }
        return rarityBelow
    }

    fun getCleanName(): String {
        val wordArray = rawName.split("_").toTypedArray()
        val returnList = mutableListOf<String>()
        for (word in wordArray) {
            returnList.add(word.firstLetterUppercase())
        }
        return returnList.joinToString(" ")
    }

    fun isAtLeast(other: LorenzRarity): Boolean = this.ordinal >= other.ordinal

    companion object {

        fun getById(id: Int) = if (entries.size > id) entries[id] else null

        fun getByName(name: String): LorenzRarity? = entries.find { it.name.equals(name, ignoreCase = true) }

        fun getByColorCode(colorCode: Char): LorenzRarity? = entries.find { it.color.chatColorCode == colorCode }
    }
}
