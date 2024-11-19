package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementBank
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementBits
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementChunkedStats
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementCold
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementCookie
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementCopper
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementDate
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementEmptyLine
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementEvents
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementFooter
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementGems
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementHeat
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementIsland
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementLobbyCode
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementLocation
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementMayor
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementMotes
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementNorthStars
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementObjective
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementParty
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementPlayerAmount
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementPowder
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementPower
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementProfile
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementPurse
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementQuiver
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementSlayer
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementSoulflow
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementTime
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementTitle
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementTuning
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementUnknown
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementVisiting

// TODO once the config can support dynamic lists, remove this enum
enum class ScoreboardConfigElement(val element: ScoreboardElement) {
    TITLE(ScoreboardElementTitle),
    PROFILE(ScoreboardElementProfile),
    PURSE(ScoreboardElementPurse),
    MOTES(ScoreboardElementMotes),
    BANK(ScoreboardElementBank),
    BITS(ScoreboardElementBits),
    COPPER(ScoreboardElementCopper),
    GEMS(ScoreboardElementGems),
    HEAT(ScoreboardElementHeat),
    COLD(ScoreboardElementCold),
    NORTH_STARS(ScoreboardElementNorthStars),
    CHUNKED_STATS(ScoreboardElementChunkedStats),
    SOULFLOW(ScoreboardElementSoulflow),
    ISLAND(ScoreboardElementIsland),
    LOCATION(ScoreboardElementLocation),
    PLAYER_AMOUNT(ScoreboardElementPlayerAmount),
    VISITING(ScoreboardElementVisiting),
    DATE(ScoreboardElementDate),
    TIME(ScoreboardElementTime),
    LOBBY_CODE(ScoreboardElementLobbyCode),
    POWER(ScoreboardElementPower),
    TUNING(ScoreboardElementTuning),
    COOKIE(ScoreboardElementCookie),
    OBJECTIVE(ScoreboardElementObjective),
    SLAYER(ScoreboardElementSlayer),
    QUIVER(ScoreboardElementQuiver),
    POWDER(ScoreboardElementPowder),
    EVENTS(ScoreboardElementEvents),
    MAYOR(ScoreboardElementMayor),
    PARTY(ScoreboardElementParty),
    FOOTER(ScoreboardElementFooter),
    EXTRA(ScoreboardElementUnknown),
    EMPTY_LINE(ScoreboardElementEmptyLine),
    EMPTY_LINE2(ScoreboardElementEmptyLine),
    EMPTY_LINE3(ScoreboardElementEmptyLine),
    EMPTY_LINE4(ScoreboardElementEmptyLine),
    EMPTY_LINE5(ScoreboardElementEmptyLine),
    EMPTY_LINE6(ScoreboardElementEmptyLine),
    EMPTY_LINE7(ScoreboardElementEmptyLine),
    EMPTY_LINE8(ScoreboardElementEmptyLine),
    EMPTY_LINE9(ScoreboardElementEmptyLine),
    EMPTY_LINE10(ScoreboardElementEmptyLine),
    ;

    override fun toString() = element.configLine

    companion object {
        fun getElements() = entries.map { it.element }

        @JvmField
        val defaultOptions = listOf(
            TITLE,
            PROFILE,
            PURSE,
            BANK,
            MOTES,
            BITS,
            COPPER,
            GEMS,
            NORTH_STARS,
            HEAT,
            COLD,
            EMPTY_LINE,
            ISLAND,
            LOCATION,
            LOBBY_CODE,
            PLAYER_AMOUNT,
            VISITING,
            EMPTY_LINE2,
            DATE,
            TIME,
            EVENTS,
            COOKIE,
            EMPTY_LINE3,
            QUIVER,
            POWER,
            TUNING,
            EMPTY_LINE4,
            OBJECTIVE,
            SLAYER,
            POWDER,
            MAYOR,
            PARTY,
            FOOTER,
            EXTRA,
        )
    }
}
