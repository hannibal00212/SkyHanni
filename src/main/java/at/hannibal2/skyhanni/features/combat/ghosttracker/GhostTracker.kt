package at.hannibal2.skyhanni.features.combat.ghosttracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.GhostDropsJson
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.SkillExpGainEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.skyblock.GraphAreaChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object GhostTracker {

    private val config get() = SkyHanniMod.feature.combat.ghostCounter

    private val storage get() = ProfileStorageData.profileSpecific?.ghostStorage

    private var currentBestiaryKills: Long
        get() = storage?.bestiaryKills ?: 0
        set(value) {
            storage?.bestiaryKills = value
        }

    private val isMaxBestiary get() = currentBestiaryKills >= MAX_BESTIARY_KILLS
    private var allowedDrops = setOf<NeuInternalName>()

    // TODO: in the future get from neu bestiary data
    private const val MAX_BESTIARY_KILLS = 100_000

    private var lastNoWidgetWarningTime = SimpleTimeMark.farPast()
    private var lastNoGhostBestiaryWidgetWarningTime = SimpleTimeMark.farPast()

    private var inArea: Boolean = false
    private var foundGhostBestiary: Boolean = false

    private val tracker = SkyHanniItemTracker(
        "Ghost Tracker",
        { Data() },
        { it.ghostStorage.ghostTracker },
    ) { drawDisplay(it) }

    class Data : ItemTrackerData() {

        override fun resetItems() {
            kills = 0
            ghostsSinceSorrow = 0
            maxKillCombo = 0
            combatXpGained = 0
        }

        @Expose
        var kills = 0L

        @Expose
        var ghostsSinceSorrow = 0L

        @Expose
        var maxKillCombo = 0L

        // TODO rename to combatXPGained
        @Expose
        var combatXpGained = 0L

        @Expose
        var totalMagicFind = 0L

        @Expose
        var totalMagicFindKills = 0L

        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / kills
            val perKill = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))

            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop chance per kill: §c$perKill",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§6Dropped Coins"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val coinsFormat = item.totalAmount.shortFormat()
            return listOf(
                "§7Killing ghosts gives you coins (more with scavenger).",
                "§7You got §6$coinsFormat coins §7that way.",
            )
        }
    }

    private val patternGroup = RepoPattern.group("combat.ghosttracker")

    /**
     * REGEX-TEST: §6§lRARE DROP! §r§9Sorrow §r§b(+§r§b210% §r§b✯ Magic Find§r§b)
     */
    private val itemDropPattern by patternGroup.pattern(
        "itemdrop",
        "§6§lRARE DROP! §r§9(?<item>[^§]*) §r§b\\([+](?:§.)*(?<mf>\\d*)% §r§b✯ Magic Find§r§b\\)",
    )

    /**
     * REGEX-TEST: §cYour Kill Combo has expired! You reached a 32 Kill Combo!
     */
    private val killComboEndPattern by patternGroup.pattern(
        "killcombo.end",
        "§cYour Kill Combo has expired! You reached a (?<kill>\\d+) Kill Combo!",
    )
    private val bagOfCashPattern by patternGroup.pattern(
        "bagofcash",
        "§eThe ghost's death materialized §r§61,000,000 coins §r§efrom the mists!",
    )

    /**
     * REGEX-TEST:  Ghost 21§r§f: §r§b29,614/40,000
     * REGEX-TEST:  Ghost 15§r§f: §r§b12,449/12,500
     */
    private val bestiaryTablistPattern by patternGroup.pattern(
        "tablist.bestiary",
        "\\s*Ghost (?<level>\\d+|[XVI]+)(?:§.)*: (?:§.)*(?<kills>[\\d,.]+)\\/(?<killsToNext>[\\d,.]+)",
    )

    /**
     * REGEX-TEST:  Ghost 25§r§f: §r§b§lMAX
     */
    private val maxBestiaryTablistPattern by patternGroup.pattern(
        "tablist.bestiarymax",
        "\\s*Ghost (?<level>\\d+|[XVI]+)(?:§.)*: (?:§.)*MAX",
    )

    private val SORROW = "SORROW".toInternalName()

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lGhost Profit Tracker")
        val profit = tracker.drawItems(data, { true }, this)
        config.ghostTrackerText.forEach { line ->
            addSearchString(line.line(data))
        }
        add(tracker.addTotalProfit(profit, data.kills, "kill"))
    }

    @HandleEvent
    fun onSkillExp(event: SkillExpGainEvent) {
        if (!isEnabled()) return
        if (event.gained > 10_000) return
        tracker.modify {
            it.combatXpGained += event.gained.toLong()
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!TabWidget.BESTIARY.isActive && lastNoWidgetWarningTime.passedSince() > 1.minutes) {
            lastNoWidgetWarningTime = SimpleTimeMark.now()
            ChatUtils.clickableChat(
                "§cYou do not have the Bestiary Tab Widget enabled! Ghost Tracker will not work properly without it.",
                onClick = HypixelCommands::widget,
                "§eClick to run /widget!",
                replaceSameMessage = true,
            )
        }
        if (TabWidget.BESTIARY.isActive && !foundGhostBestiary && lastNoGhostBestiaryWidgetWarningTime.passedSince() > 1.minutes) {
            lastNoGhostBestiaryWidgetWarningTime = SimpleTimeMark.now()
            ChatUtils.clickableChat(
                "§cGhost bestiary not found in Bestiary Tab Widget! Ghost Tracker will not work properly without it.",
                onClick = HypixelCommands::widget,
                "§eClick to run /widget!",
                replaceSameMessage = true,
            )
        }
    }

    @HandleEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled()) return
        if (event.reason != PurseChangeCause.GAIN_MOB_KILL) return
        if (event.coins !in 200.0..2_000.0) return
        tracker.addCoins(event.coins.toInt(), false)
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return
        itemDropPattern.matchMatcher(event.message) {
            val internalName = NeuInternalName.fromItemNameOrNull(group("item")) ?: return
            val mf = group("mf").formatInt()
            if (!isAllowedItem(internalName)) return

            tracker.addItem(internalName, 1, false)
            tracker.modify {
                it.totalMagicFind += mf
                it.totalMagicFindKills++

                if (internalName == SORROW) {
                    it.ghostsSinceSorrow = 0
                }
            }
            return
        }
        killComboEndPattern.matchMatcher(event.message) {
            val kill = group("kill").formatLong()
            tracker.modify {
                it.maxKillCombo = kill.coerceAtLeast(it.maxKillCombo)
            }
            return
        }
        if (bagOfCashPattern.matches(event.message)) {
            tracker.addCoins(1_000_000, false)
            return
        }
    }

    private fun parseBestiaryWidget(lines: List<String>) {
        foundGhostBestiary = false
        for (line in lines) {
            if (maxBestiaryTablistPattern.matches(line)) {
                currentBestiaryKills = MAX_BESTIARY_KILLS.toLong()
                foundGhostBestiary = true
                return
            }

            val kills = bestiaryTablistPattern.matchGroup(line, "kills")?.formatLong() ?: continue
            foundGhostBestiary = true
            if (kills <= currentBestiaryKills) return
            val difference = kills - currentBestiaryKills

            if (difference > 50) {
                currentBestiaryKills = kills
                return
            }

            currentBestiaryKills = kills

            tracker.modify {
                it.kills += difference
                it.ghostsSinceSorrow += difference
            }
        }
    }

    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.BESTIARY)) return
        if (isMaxBestiary || !isEnabled()) return
        parseBestiaryWidget(event.lines)
    }

    init {
        tracker.initRenderer(config.position) { isEnabled() }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        allowedDrops = event.getConstant<GhostDropsJson>("GhostDrops").ghostDrops
    }

    @HandleEvent
    fun onAreaChange(event: GraphAreaChangeEvent) {
        inArea = event.area == "The Mist" && IslandType.DWARVEN_MINES.isInIsland()
        if (inArea) parseBestiaryWidget(TabWidget.BESTIARY.lines)
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.DWARVEN_MINES) {
            tracker.firstUpdate()
        }
    }

    private fun isAllowedItem(internalName: NeuInternalName): Boolean = internalName in allowedDrops

    private fun getAverageMagicFind(mf: Long, kills: Long) =
        if (mf == 0L || kills == 0L) 0.0 else mf / (kills).toDouble()


    private fun isEnabled() = inArea && config.enabled

    enum class GhostTrackerLines(private val display: String, val line: Data.() -> String) {
        KILLS(
            "§7Kills: §e7,813",
            { "§7Kills: §e${kills.addSeparators()}" }
        ),
        GHOSTS_SINCE_SORROW(
            "§7Ghosts Since Sorrow: §e71",
            { "§7Ghosts Since Sorrow: §e${ghostsSinceSorrow.addSeparators()}" },
        ),
        MAX_KILL_COMBO(
            "§7Max Kill Combo: §e681",
            { "§7Max Kill Combo: §e${maxKillCombo.addSeparators()}" },
        ),
        COMBAT_XP_GAINED(
            "§7Combat XP Gained: §e4,687,800",
            { "§7Combat XP Gained: §e${combatXpGained.addSeparators()}" },
        ),
        AVERAGE_MAGIC_FIND(
            "§7Average Magic Find: §b278.9",
            { "§7Average Magic Find: §e${getAverageMagicFind(totalMagicFind, totalMagicFindKills).roundTo(1)}" },
        ),
        BESTIARY_KILLS(
            "§7Bestiary Kills: §e 71,893",
            {
                val kills = if (currentBestiaryKills >= MAX_BESTIARY_KILLS) "MAX" else currentBestiaryKills.addSeparators()
                "§7Bestiary Kills: §e$kills"
            },
        ),
        ;

        override fun toString(): String = display
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val storage = storage ?: return
        if (storage.migratedTotalKills) return
        tracker.modify {
            it.totalMagicFindKills = it.items.values.sumOf { item -> item.timesGained }
        }
        storage.migratedTotalKills = true
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetghosttracker") {
            description = "Resets the Ghost Profit Tracker"
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {

        fun migrateItem(oldData: JsonElement): JsonElement {
            val oldAmount = oldData.asInt
            return JsonObject().apply {
                addProperty("timesGained", oldAmount)
                addProperty("totalAmount", oldAmount)
                addProperty("hidden", false)
            }
        }

        val oldPrefix = "#profile.ghostCounter"
        val newPrefix = "#profile.ghostStorage.ghostTracker"

        event.move(67, "$oldPrefix.data.KILLS", "$newPrefix.kills")
        event.move(67, "$oldPrefix.data.GHOSTSINCESORROW", "$newPrefix.ghostsSinceSorrow")
        event.move(67, "$oldPrefix.data.MAXKILLCOMBO", "$newPrefix.maxKillCombo")
        event.move(67, "$oldPrefix.data.SKILLXPGAINED", "$newPrefix.combatXpGained")
        event.move(67, "$oldPrefix.totalMF", "$newPrefix.totalMagicFind")

        event.move(67, "$oldPrefix.data.SORROWCOUNT", "$newPrefix.items.SORROW", ::migrateItem)
        event.move(67, "$oldPrefix.data.PLASMACOUNT", "$newPrefix.items.PLASMA", ::migrateItem)
        event.move(67, "$oldPrefix.data.VOLTACOUNT", "$newPrefix.items.VOLTA", ::migrateItem)
        event.move(67, "$oldPrefix.data.GHOSTLYBOOTS", "$newPrefix.items.GHOST_BOOTS", ::migrateItem)
    }
}
