package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.combat.SpidersDenAPI.isAtTopOfNest
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.eventsConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardEvent.VOTING
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.features.misc.ServerRestartTitle
import at.hannibal2.skyhanni.features.rift.area.stillgorechateau.RiftBloodEffigies
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.LorenzUtils.inAdvancedMiningIsland
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern as SbPattern

/**
 * This enum contains all the lines that either are events or other lines that are so rare/not often seen that they
 * don't fit in the normal [ScoreboardElement] enum.
 *
 * We for example have the [VOTING] Event, while this is clearly not an event, I don't consider them as normal lines
 * because they are visible for a maximum of like 1 minute every 5 days and ~12 hours.
 */

private fun getSbLines(): List<String> = CustomScoreboard.activeLines

enum class ScoreboardEvent(
    private val displayLine: () -> List<String>,
    private val showWhen: () -> Boolean,
    private val configLine: String,
) {
    VOTING(
        ::getVotingLines,
        ::getVotingShowWhen,
        "§7(All Voting Lines)",
    ),
    SERVER_CLOSE(
        ::getServerCloseLines,
        ::getServerCloseShowWhen,
        "§cServer closing soon!",
    ),
    DUNGEONS(
        ::getDungeonsLines,
        ::getDungeonsShowWhen,
        "§7(All Dungeons Lines)",
    ),
    KUUDRA(
        ::getKuudraLines,
        ::getKuudraShowWhen,
        "§7(All Kuudra Lines)",
    ),
    DOJO(
        ::getDojoLines,
        ::getDojoShowWhen,
        "§7(All Dojo Lines)",
    ),
    DARK_AUCTION(
        ::getDarkAuctionLines,
        ::getDarkAuctionShowWhen,
        "Time Left: §b11\n" +
            "Current Item:\n" +
            " §5Travel Scroll to Sirius",
    ),
    JACOB_CONTEST(
        ::getJacobContestLines,
        ::getJacobContestShowWhen,
        "§eJacob's Contest\n" +
            "§e○ §fCarrot §a18m17s\n" +
            " Collected §e8,264",
    ),
    JACOB_MEDALS(
        ::getJacobMedalsLines,
        ::getJacobMedalsShowWhen,
        "§6§lGOLD §fmedals: §613\n" +
            "§f§lSILVER §fmedals: §f3\n" +
            "§c§lBRONZE §fmedals: §c4",
    ),
    TRAPPER(
        ::getTrapperLines,
        ::getTrapperShowWhen,
        "Pelts: §5711\n" +
            "Tracker Mob Location:\n" +
            "§bMushroom Gorge",
    ),
    GARDEN_CLEAN_UP(
        ::getGardenCleanUpLines,
        ::getGardenCleanUpShowWhen,
        "Cleanup: §c12.6%",
    ),
    GARDEN_PASTING(
        ::getGardenPastingLines,
        ::getGardenPastingShowWhen,
        "§fBarn Pasting§7: §e12.3%",
    ),
    FLIGHT_DURATION(
        ::getFlightDurationLines,
        ::getFlightDurationShowWhen,
        "Flight Duration: §a10m 0s",
    ),
    WINTER(
        ::getWinterLines,
        ::getWinterShowWhen,
        "§7(All Winter Event Lines)",
    ),
    NEW_YEAR(
        ::getNewYearLines,
        ::getNewYearShowWhen,
        "§dNew Year Event!§f 24:25",
    ),
    SPOOKY(
        ::getSpookyLines,
        ::getSpookyShowWhen,
        "§6Spooky Festival§f 50:54\n" +
            "§7Your Candy:\n" +
            "§a1 Green§7, §50 Purple §7(§61 §7pts.)",
    ),
    BROODMOTHER(
        ::getBroodmotherLines,
        ::isAtTopOfNest,
        "§4Broodmother§7: §eDormant",
    ),
    MINING_EVENTS(
        ::getMiningEventsLines,
        { inAdvancedMiningIsland() },
        "§7(All Mining Event Lines)",
    ),
    DAMAGE(
        ::getDamageLines,
        ::getDamageShowWhen,
        "Dragon HP: §a6,180,925 §c❤\n" +
            "Your Damage: §c375,298.5",
    ),
    MAGMA_BOSS(
        ::getMagmaBossLines,
        ::getMagmaBossShowWhen,
        "§7(All Magma Boss Lines)\n" +
            "§7Boss: §c0%\n" +
            "§7Damage Soaked:\n" +
            "§e▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎§7▎▎▎▎▎",
    ),
    CARNIVAL(
        ::getCarnivalLines,
        ::getCarnivalShowWhen,
        "§7(All Carnival Lines)",
    ),
    RIFT(
        ::getRiftLines,
        { IslandType.THE_RIFT.isInIsland() },
        "§7(All Rift Lines)",
    ),
    ESSENCE(
        ::getEssenceLines,
        ::getEssenceShowWhen,
        "Dragon Essence: §d1,285",
    ),
    QUEUE(
        ::getQueueLines,
        ::getQueueShowWhen,
        "Queued: Glacite Mineshafts\n" +
            "Position: §b#45 §fSince: §a00:00",
    ),
    ACTIVE_TABLIST_EVENTS(
        ::getActiveEventLine,
        ::getActiveEventShowWhen,
        "§7(All Active Tablist Events)\n§dHoppity's Hunt\n §fEnds in: §e26h",
    ),
    STARTING_SOON_TABLIST_EVENTS(
        ::getSoonEventLine,
        ::getSoonEventShowWhen,
        "§7(All Starting Soon Tablist Events)\n§6Mining Fiesta\n §fStarts in: §e52min",
    ),
    REDSTONE(
        ::getRedstoneLines,
        ::getRedstoneShowWhen,
        "§e§l⚡ §cRedstone: §e§b7%",
    ),
    ANNIVERSARY(
        ::getAnniversaryLines,
        ::getAnniversaryShowWhen,
        "§d5th Anniversary§f 167:59:54",
    ),
    ;

    override fun toString() = configLine

    fun getLines(): List<String> = displayLine()

    companion object {
        fun getEvent() = buildList<ScoreboardEvent?> {
            if (eventsConfig.showAllActiveEvents) {
                for (event in eventsConfig.eventEntries) {
                    if (event.showWhen()) {
                        add(event)
                    }
                }
            } else {
                add(eventsConfig.eventEntries.firstOrNull { it.showWhen() && it.getLines().isNotEmpty() })
            }
        }

        // I don't know why, but this field is needed for it to work
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
            GARDEN_CLEAN_UP,
            GARDEN_PASTING,
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
        )
    }
}

private fun getVotingLines() = buildList {
    val sbLines = getSbLines()

    val yearLine = sbLines.firstOrNull { SbPattern.yearVotesPattern.matches(it) } ?: return emptyList<String>()
    add(yearLine)

    if (sbLines.nextAfter(yearLine) == "§7Waiting for") {
        add("§7Waiting for")
        add("§7your vote...")
    } else {
        if (SbPattern.votesPattern.anyMatches(sbLines)) {
            addAll(sbLines.filter { SbPattern.votesPattern.matches(it) })
        }
    }
}

private fun getVotingShowWhen(): Boolean = SbPattern.yearVotesPattern.anyMatches(getSbLines())

private fun getServerCloseLines() = buildList {
    val matchingLine = getSbLines().first { ServerRestartTitle.restartingGreedyPattern.matches(it) }
    add(matchingLine.split("§8")[0])
}

private fun getServerCloseShowWhen(): Boolean = ServerRestartTitle.restartingGreedyPattern.anyMatches(getSbLines())

private fun getDungeonsLines() = listOf(
    SbPattern.m7dragonsPattern,
    SbPattern.autoClosingPattern,
    SbPattern.startingInPattern,
    SbPattern.keysPattern,
    SbPattern.timeElapsedPattern,
    SbPattern.clearedPattern,
    SbPattern.soloPattern,
    SbPattern.teammatesPattern,
    SbPattern.floor3GuardiansPattern,
).let { patterns ->
    // BetterMap adds a random §r at the start, making the line go black
    getSbLines().filter { line -> patterns.any { it.matches(line) } }.map { it.removePrefix("§r") }
}

private fun getDungeonsShowWhen(): Boolean = DungeonAPI.inDungeon()

private fun getKuudraLines() = listOf(
    SbPattern.autoClosingPattern,
    SbPattern.startingInPattern,
    SbPattern.timeElapsedPattern,
    SbPattern.instanceShutdownPattern,
    SbPattern.wavePattern,
    SbPattern.tokensPattern,
    SbPattern.submergesPattern,
)
    .mapNotNull { pattern ->
        getSbLines().firstOrNull { pattern.matches(it) }
    }

private fun getKuudraShowWhen(): Boolean = IslandType.KUUDRA_ARENA.isInIsland()

private fun getDojoLines() = listOf(
    SbPattern.dojoChallengePattern,
    SbPattern.dojoDifficultyPattern,
    SbPattern.dojoPointsPattern,
    SbPattern.dojoTimePattern,
)
    .mapNotNull { pattern ->
        getSbLines().firstOrNull { pattern.matches(it) }
    }

private fun getDojoShowWhen(): Boolean = SbPattern.dojoChallengePattern.anyMatches(getSbLines())

private fun getDarkAuctionLines() = buildList {
    getSbLines().firstOrNull { SbPattern.startingInPattern.matches(it) }?.let { add(it) }
    getSbLines().firstOrNull { SbPattern.timeLeftPattern.matches(it) }?.let { add(it) }

    val darkAuctionCurrentItemLine = getSbLines().firstOrNull { SbPattern.darkAuctionCurrentItemPattern.matches(it) }

    if (darkAuctionCurrentItemLine != null) {
        add(darkAuctionCurrentItemLine)
        getSbLines().nextAfter(darkAuctionCurrentItemLine)?.let { add(it) }
    }
}

private fun getDarkAuctionShowWhen(): Boolean = IslandType.DARK_AUCTION.isInIsland()

private fun getJacobContestLines() = buildList {
    getSbLines().firstOrNull { SbPattern.jacobsContestPattern.matches(it) }?.let { line ->
        add(line)
        getSbLines().nextAfter(line)?.let { add(it) }
        getSbLines().nextAfter(line, 2)?.let { add(it) }
        getSbLines().nextAfter(line, 3)?.let {
            if (!SbPattern.footerPattern.matches(it)) add(it)
        }
    }
}

private fun getJacobContestShowWhen(): Boolean = SbPattern.jacobsContestPattern.anyMatches(getSbLines())

private fun getJacobMedalsLines(): List<String> = getSbLines().filter { SbPattern.medalsPattern.matches(it) }

private fun getJacobMedalsShowWhen(): Boolean = SbPattern.medalsPattern.anyMatches(getSbLines())

private fun getTrapperLines() = buildList {
    getSbLines().firstOrNull { SbPattern.peltsPattern.matches(it) }?.let { add(it) }

    val trapperMobLocationLine = getSbLines().firstOrNull { SbPattern.mobLocationPattern.matches(it) }
    if (trapperMobLocationLine != null) {
        add("Tracker Mob Location:")
        getSbLines().nextAfter(trapperMobLocationLine)?.let { add(it) }
    }
}

private fun getTrapperShowWhen(): Boolean =
    getSbLines().any { ScoreboardPattern.peltsPattern.matches(it) || ScoreboardPattern.mobLocationPattern.matches(it) }

private fun getGardenCleanUpLines(): List<String> =
    listOf(getSbLines().first { SbPattern.cleanUpPattern.matches(it) }.trim())

private fun getGardenCleanUpShowWhen(): Boolean = SbPattern.cleanUpPattern.anyMatches(getSbLines())

private fun getGardenPastingLines(): List<String> =
    listOf(getSbLines().first { SbPattern.pastingPattern.matches(it) }.trim())

private fun getGardenPastingShowWhen(): Boolean = SbPattern.pastingPattern.anyMatches(getSbLines())

private fun getFlightDurationLines(): List<String> =
    listOf(getSbLines().first { SbPattern.flightDurationPattern.matches(it) }.trim())

private fun getFlightDurationShowWhen(): Boolean = SbPattern.flightDurationPattern.anyMatches(getSbLines())

private fun getWinterLines() = buildList {
    getSbLines().firstOrNull { SbPattern.winterEventStartPattern.matches(it) }?.let { add(it) }
    getSbLines().firstOrNull { SbPattern.winterNextWavePattern.matches(it) && !it.endsWith("Soon!") }?.let { add(it) }
    getSbLines().firstOrNull { SbPattern.winterWavePattern.matches(it) }?.let { add(it) }
    getSbLines().firstOrNull { SbPattern.winterMagmaLeftPattern.matches(it) }?.let { add(it) }
    getSbLines().firstOrNull { SbPattern.winterTotalDmgPattern.matches(it) }?.let { add(it) }
    getSbLines().firstOrNull { SbPattern.winterCubeDmgPattern.matches(it) }?.let { add(it) }
}

private fun getWinterShowWhen(): Boolean = getSbLines().any {
    ScoreboardPattern.winterEventStartPattern.matches(it)
        || (ScoreboardPattern.winterNextWavePattern.matches(it) && !it.endsWith("Soon!"))
        || ScoreboardPattern.winterWavePattern.matches(it)
}

private fun getNewYearLines() = listOf(getSbLines().first { SbPattern.newYearPattern.matches(it) })

private fun getNewYearShowWhen(): Boolean = SbPattern.newYearPattern.anyMatches(getSbLines())

private fun getSpookyLines() = buildList {
    getSbLines().firstOrNull { SbPattern.spookyPattern.matches(it) }?.let { add(it) } // Time
    add("§7Your Candy: ")
    add(
        TabListData.getFooter()
            .removeResets()
            .split("\n")
            .firstOrNull { it.startsWith("§7Your Candy:") }
            ?.removePrefix("§7Your Candy:") ?: "§cCandy not found",
    ) // Candy
}

private fun getSpookyShowWhen(): Boolean = getSbLines().any { ScoreboardPattern.spookyPattern.matches(it) }

private fun getTablistEvent(): String? =
    TabListData.getTabList().firstOrNull { SbPattern.eventNamePattern.matches(it) }
        ?.let {
            SbPattern.eventNamePattern.matchMatcher(it) {
                group("name")
            }
        }

private fun getActiveEventLine(): List<String> {
    val currentActiveEvent = getTablistEvent() ?: return emptyList()

    // Some Active Events are better not shown from the tablist,
    // but from other locations like the scoreboard
    val blockedEvents = listOf("Spooky Festival", "Carnival", "5th SkyBlock Anniversary", "New Year Celebration")
    if (blockedEvents.contains(currentActiveEvent.removeColor())) return emptyList()

    val currentActiveEventTime = TabListData.getTabList().firstOrNull { SbPattern.eventTimeEndsPattern.matches(it) }
        ?.let {
            SbPattern.eventTimeEndsPattern.matchMatcher(it) {
                group("time")
            }
        }

    return listOf(currentActiveEvent, " Ends in: §e$currentActiveEventTime")
}

private fun getActiveEventShowWhen(): Boolean =
    getTablistEvent() != null && TabListData.getTabList().any { SbPattern.eventTimeEndsPattern.matches(it) }

private fun getSoonEventLine(): List<String> {
    val soonActiveEvent = getTablistEvent() ?: return emptyList()
    val soonActiveEventTime = TabListData.getTabList().firstOrNull { SbPattern.eventTimeStartsPattern.matches(it) }
        ?.let {
            SbPattern.eventTimeStartsPattern.matchMatcher(it) {
                group("time")
            }
        }

    return listOf(soonActiveEvent, " Starts in: §e$soonActiveEventTime")
}

private fun getSoonEventShowWhen(): Boolean =
    getTablistEvent() != null && TabListData.getTabList().any { SbPattern.eventTimeStartsPattern.matches(it) }

private fun getBroodmotherLines(): List<String> =
    listOf(getSbLines().first { SbPattern.broodmotherPattern.matches(it) })

private fun getMiningEventsLines() = buildList {
    // Wind
    if (getSbLines().any { SbPattern.windCompassPattern.matches(it) }
        && getSbLines().any { SbPattern.windCompassArrowPattern.matches(it) }) {
        add(getSbLines().first { SbPattern.windCompassPattern.matches(it) })
        add("| ${getSbLines().first { SbPattern.windCompassArrowPattern.matches(it) }} §f|")
    }

    // Better Together
    if (getSbLines().any { SbPattern.nearbyPlayersPattern.matches(it) }) {
        add("§dBetter Together")
        add(" ${getSbLines().first { SbPattern.nearbyPlayersPattern.matches(it) }}")
    }

    // Zone Events
    if (getSbLines().any { SbPattern.miningEventPattern.matches(it) }
        && getSbLines().any { SbPattern.miningEventZonePattern.matches(it) }) {
        add(getSbLines().first { SbPattern.miningEventPattern.matches(it) }.removePrefix("Event: "))
        add("in ${getSbLines().first { SbPattern.miningEventZonePattern.matches(it) }.removePrefix("Zone: ")}")
    }

    // Zone Events but no Zone Line
    if (getSbLines().any { SbPattern.miningEventPattern.matches(it) }
        && getSbLines().none { SbPattern.miningEventZonePattern.matches(it) }) {
        add(
            getSbLines().first { SbPattern.miningEventPattern.matches(it) }
                .removePrefix("Event: "),
        )
    }

    // Mithril Gourmand
    if (getSbLines().any { SbPattern.mithrilRemainingPattern.matches(it) }
        && getSbLines().any { SbPattern.mithrilYourMithrilPattern.matches(it) }) {
        add(getSbLines().first { SbPattern.mithrilRemainingPattern.matches(it) })
        add(getSbLines().first { SbPattern.mithrilYourMithrilPattern.matches(it) })
    }

    // Raffle
    if (getSbLines().any { SbPattern.raffleTicketsPattern.matches(it) }
        && getSbLines().any { SbPattern.rafflePoolPattern.matches(it) }) {
        add(getSbLines().first { SbPattern.raffleTicketsPattern.matches(it) })
        add(getSbLines().first { SbPattern.rafflePoolPattern.matches(it) })
    }

    // Raid
    if (getSbLines().any { SbPattern.yourGoblinKillsPattern.matches(it) }
        && getSbLines().any { SbPattern.remainingGoblinPattern.matches(it) }) {
        add(getSbLines().first { SbPattern.yourGoblinKillsPattern.matches(it) })
        add(getSbLines().first { SbPattern.remainingGoblinPattern.matches(it) })
    }

    // Fortunate Freezing
    if (getSbLines().any { SbPattern.fortunateFreezingBonusPattern.matches(it) }) {
        add(getSbLines().first { SbPattern.fortunateFreezingBonusPattern.matches(it) })
    }

    // Fossil Dust
    if (getSbLines().any { SbPattern.fossilDustPattern.matches(it) }) {
        add(getSbLines().first { SbPattern.fossilDustPattern.matches(it) })
    }
}

private fun getDamageLines(): List<String> =
    listOf(getSbLines().first { SbPattern.bossHPPattern.matches(it) }) +
        (getSbLines().first { SbPattern.bossDamagePattern.matches(it) })

private fun getDamageShowWhen(): Boolean =
    getSbLines().any { SbPattern.bossHPPattern.matches(it) }
        && getSbLines().any { SbPattern.bossDamagePattern.matches(it) }

private fun getMagmaBossLines() = getSbLines().filter { line ->
    SbPattern.magmaBossPattern.matches(line)
        || SbPattern.damageSoakedPattern.matches(line)
        || SbPattern.killMagmasPattern.matches(line)
        || SbPattern.killMagmasDamagedSoakedBarPattern.matches(line)
        || SbPattern.reformingPattern.matches(line)
        || SbPattern.bossHealthPattern.matches(line)
        || SbPattern.bossHealthBarPattern.matches(line)
}

private fun getMagmaBossShowWhen(): Boolean = SbPattern.magmaChamberPattern.matches(HypixelData.skyBlockArea)

private fun getCarnivalLines() = listOf(
    SbPattern.carnivalPattern,
    SbPattern.carnivalTokensPattern,
    SbPattern.carnivalTasksPattern,
    SbPattern.timeLeftPattern,
    SbPattern.carnivalCatchStreakPattern,
    SbPattern.carnivalFruitsPattern,
    SbPattern.carnivalAccuracyPattern,
    SbPattern.carnivalKillsPattern,
    SbPattern.carnivalScorePattern,
)
    .mapNotNull { pattern ->
        getSbLines().firstOrNull { pattern.matches(it) }
    }

private fun getCarnivalShowWhen(): Boolean = SbPattern.carnivalPattern.anyMatches(getSbLines())

private fun getRiftLines() = getSbLines().filter { line ->
    RiftBloodEffigies.heartsPattern.matches(line)
        || SbPattern.riftHotdogTitlePattern.matches(line)
        || SbPattern.timeLeftPattern.matches(line)
        || SbPattern.riftHotdogEatenPattern.matches(line)
        || SbPattern.riftAveikxPattern.matches(line)
        || SbPattern.riftHayEatenPattern.matches(line)
        || SbPattern.cluesPattern.matches(line)
        || SbPattern.barryProtestorsQuestlinePattern.matches(line)
        || SbPattern.barryProtestorsHandledPattern.matches(line)
}

private fun getEssenceLines(): List<String> = listOf(getSbLines().first { SbPattern.essencePattern.matches(it) })

private fun getEssenceShowWhen(): Boolean = SbPattern.essencePattern.anyMatches(getSbLines())

private fun getQueueLines(): List<String> =
    listOf(getSbLines().first { SbPattern.queuePattern.matches(it) }) +
        (getSbLines().first { SbPattern.queueTierPattern.matches(it) }) +
        (getSbLines().first { SbPattern.queuePositionPattern.matches(it) })

private fun getQueueShowWhen(): Boolean = SbPattern.queuePattern.anyMatches(getSbLines())

private fun getRedstoneLines(): List<String> = listOf(getSbLines().first { SbPattern.redstonePattern.matches(it) })

private fun getRedstoneShowWhen(): Boolean = SbPattern.redstonePattern.anyMatches(getSbLines())

private fun getAnniversaryLines() = listOf(getSbLines().first { SbPattern.anniversaryPattern.matches(it) })

private fun getAnniversaryShowWhen(): Boolean = SbPattern.anniversaryPattern.anyMatches(getSbLines())
