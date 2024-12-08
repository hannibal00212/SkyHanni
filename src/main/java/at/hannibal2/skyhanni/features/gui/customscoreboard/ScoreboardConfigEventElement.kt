package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEvent
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventActiveTablist
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventAnniversary
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventBroodmother
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventCarnival
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventDamage
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventDarkAuction
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventDojo
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventDungeons
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventEssence
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventFlightDuration
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventGarden
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventJacobContest
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventJacobMedals
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventKuudra
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventMagmaBoss
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventMining
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventNewYear
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventQueue
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventRedstone
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventRift
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventServerClose
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventSpooky
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventStartingSoonTablist
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventTrapper
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventVoting
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEventWinter

/**
 * This enum contains all the lines that either are events or other lines that are so rare/not often seen that they
 * don't fit in the normal [ScoreboardConfigElement] enum.
 *
 * We for example have the [ScoreboardEventVoting] Event, while this is clearly not an event, I don't consider them as normal lines
 * because they are visible for a maximum of like 1 minute every 5 days and ~12 hours.
 */

enum class ScoreboardConfigEventElement(val event: ScoreboardEvent) {
    VOTING(ScoreboardEventVoting),
    SERVER_CLOSE(ScoreboardEventServerClose),
    DUNGEONS(ScoreboardEventDungeons),
    KUUDRA(ScoreboardEventKuudra),
    DOJO(ScoreboardEventDojo),
    DARK_AUCTION(ScoreboardEventDarkAuction),
    JACOB_CONTEST(ScoreboardEventJacobContest),
    JACOB_MEDALS(ScoreboardEventJacobMedals),
    TRAPPER(ScoreboardEventTrapper),
    GARDEN(ScoreboardEventGarden),
    FLIGHT_DURATION(ScoreboardEventFlightDuration),
    WINTER(ScoreboardEventWinter),
    NEW_YEAR(ScoreboardEventNewYear),
    SPOOKY(ScoreboardEventSpooky),
    BROODMOTHER(ScoreboardEventBroodmother),
    MINING_EVENTS(ScoreboardEventMining),
    DAMAGE(ScoreboardEventDamage),
    MAGMA_BOSS(ScoreboardEventMagmaBoss),
    CARNIVAL(ScoreboardEventCarnival),
    RIFT(ScoreboardEventRift),
    ESSENCE(ScoreboardEventEssence),
    QUEUE(ScoreboardEventQueue),
    ANNIVERSARY(ScoreboardEventAnniversary),
    ACTIVE_TABLIST_EVENTS(ScoreboardEventActiveTablist),
    STARTING_SOON_TABLIST_EVENTS(ScoreboardEventStartingSoonTablist),
    REDSTONE(ScoreboardEventRedstone),
    ;

    override fun toString() = event.configLine

    companion object {
        @JvmField
        val defaultOption = listOf(
            VOTING,
            SERVER_CLOSE,
            DUNGEONS,
            KUUDRA,
            DOJO,
            DARK_AUCTION,
            JACOB_CONTEST,
            JACOB_MEDALS,
            TRAPPER,
            GARDEN,
            FLIGHT_DURATION,
            NEW_YEAR,
            WINTER,
            SPOOKY,
            BROODMOTHER,
            MINING_EVENTS,
            DAMAGE,
            MAGMA_BOSS,
            CARNIVAL,
            RIFT,
            ESSENCE,
            ACTIVE_TABLIST_EVENTS,
            REDSTONE,
        )
    }
}
