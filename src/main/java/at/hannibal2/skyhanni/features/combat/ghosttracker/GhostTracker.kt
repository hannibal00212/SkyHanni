package at.hannibal2.skyhanni.features.combat.ghosttracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.GhostDrops
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SkillExpGainEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.GraphAreaChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
    private var allowedDrops = setOf<NEUInternalName>()
    private val MAX_BESTIARY_KILLS = getBestiaryKillsUntilLevel(25)

    private var inArea: Boolean = false

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
     * REGEX-TEST:  Ghost 15§r§f: §r§b12,449/12,500
     */
    private val bestiaryTablistPattern by patternGroup.pattern(
        "tablist.bestiary",
        "\\s*Ghost (?<level>\\d+|[XVI]+)(?:§.)*: (?:§.)*(?<kills>[\\d,.]+)/(?<killsToNext>[\\d,.]+)",
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
            addSearchString(getLine(line, data))
        }
        add(tracker.addTotalProfit(profit, data.kills, "kill"))
    }

    private fun getLine(line: GhostTrackerLines, data: Data): String {
        return when (line) {
            GhostTrackerLines.KILLS -> "§7Kills: §e${data.kills.addSeparators()}"
            GhostTrackerLines.GHOSTS_SINCE_SORROW -> "§7Ghosts Since Sorrow: §e${data.ghostsSinceSorrow.addSeparators()}"
            GhostTrackerLines.MAX_KILL_COMBO -> "§7Max Kill Combo: §e${data.maxKillCombo.addSeparators()}"
            GhostTrackerLines.COMBAT_XP_GAINED -> "§7Combat XP Gained: §e${data.combatXpGained.addSeparators()}"
            GhostTrackerLines.AVERAGE_MAGIC_FIND ->
                "§7Average Magic Find: §e${
                    getAverageMagicFind(
                        data.totalMagicFind,
                        data.totalMagicFindKills,
                    )
                }"

            GhostTrackerLines.BESTIARY_KILLS ->
                "§7Bestiary Kills: §e" + if (currentBestiaryKills >= MAX_BESTIARY_KILLS) "MAX" else currentBestiaryKills.addSeparators()
        }
    }

    @SubscribeEvent
    fun onSkillExp(event: SkillExpGainEvent) {
        if (!isEnabled()) return
        if (event.gained > 10_000) return
        tracker.modify {
            it.combatXpGained += event.gained.toLong()
        }
    }

    @SubscribeEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled()) return
        if (event.reason != PurseChangeCause.GAIN_MOB_KILL) return
        if (event.coins !in 200.0..2_000.0) return
        tracker.addCoins(event.coins.toInt(), false)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        itemDropPattern.matchMatcher(event.message) {
            val internalName = NEUInternalName.fromItemNameOrNull(group("item")) ?: return
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
            val kill = group("kill").formatInt().toLong()
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

    @SubscribeEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.BESTIARY)) return
        if (isMaxBestiary || !isEnabled()) return
        event.lines.forEach { line ->
            bestiaryTablistPattern.matchMatcher(line) {
                val kills = group("kills").formatInt().toLong()
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
                return
            }
            if (maxBestiaryTablistPattern.matches(line)) {
                currentBestiaryKills = MAX_BESTIARY_KILLS.toLong()
                return
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        tracker.renderDisplay(config.position)
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        allowedDrops = event.getConstant<GhostDrops>("GhostDrops").ghostDrops
    }

    @HandleEvent
    fun onAreaChange(event: GraphAreaChangeEvent) {
        inArea = event.area == "The Mist" && IslandType.DWARVEN_MINES.isInIsland()
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.DWARVEN_MINES) {
            tracker.firstUpdate()
        }
    }

    private fun isAllowedItem(internalName: NEUInternalName): Boolean = internalName in allowedDrops

    private fun getAverageMagicFind(mf: Long, kills: Long) =
        if (mf == 0L || kills == 0L) 0.0 else mf / (kills).toDouble()


    private fun isEnabled() = inArea && config.enabled

    enum class GhostTrackerLines(private val display: String) {
        KILLS("§7Kills: §e7,813"),
        GHOSTS_SINCE_SORROW("§7Ghosts Since Sorrow: §e71"),
        MAX_KILL_COMBO("§7Max Kill Combo: §e681"),
        COMBAT_XP_GAINED("§7Combat XP Gained: §e4,687,800"),
        AVERAGE_MAGIC_FIND("§7Average Magic Find: §b278.9"),
        BESTIARY_KILLS("§7Bestiary Kills: §e 71,893"),
        ;

        override fun toString(): String {
            return display
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val storage = storage ?: return
        if (storage.migratedTotalKills) return
        tracker.modify {
            var count = 0L
            it.items.forEach { (_, item) ->
                count += item.timesGained.toInt()
            }
            it.totalMagicFindKills = count
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

    // TODO: In the future move to a utils class and use neu bestiary data
    private fun getBestiaryKillsUntilLevel(level: Int): Int {
        var killsUntilLevel = 0
        for (i in 1..level) {
            killsUntilLevel += getBestiaryKillsInLevel(i)
        }
        return killsUntilLevel
    }

    private fun getBestiaryKillsInLevel(level: Int): Int {
        return when (level) {
            1, 2, 3, 4, 5 -> 4
            6 -> 20
            7 -> 40
            8, 9 -> 60
            10 -> 100
            11 -> 300
            12 -> 600
            13 -> 800
            14, 15, 16, 17 -> 1_000
            18 -> 1_200
            19, 20 -> 1_400
            21 -> 10_000
            22, 23, 24, 25 -> 20_000
            else -> 0
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {

        fun migrateItem(oldData: JsonElement): JsonElement {
            val oldAmount = oldData.asInt
            val newData = JsonObject()
            newData.addProperty("timesGained", oldAmount)
            newData.addProperty("totalAmount", oldAmount)
            newData.addProperty("hidden", false)
            return newData
        }

        event.move(67, "#profile.ghostCounter.data.KILLS", "#profile.ghostStorage.ghostTracker.kills")
        event.move(67, "#profile.ghostCounter.data.GHOSTSINCESORROW", "#profile.ghostStorage.ghostTracker.ghostsSinceSorrow")
        event.move(67, "#profile.ghostCounter.data.MAXKILLCOMBO", "#profile.ghostStorage.ghostTracker.maxKillCombo")
        event.move(67, "#profile.ghostCounter.data.SKILLXPGAINED", "#profile.ghostStorage.ghostTracker.combatXpGained")
        event.move(67, "#profile.ghostCounter.totalMF", "#profile.ghostStorage.ghostTracker.totalMagicFind")

        event.move(67, "#profile.ghostCounter.data.SORROWCOUNT", "#profile.ghostStorage.ghostTracker.items.SORROW", ::migrateItem)
        event.move(67, "#profile.ghostCounter.data.PLASMACOUNT", "#profile.ghostStorage.ghostTracker.items.PLASMA", ::migrateItem)
        event.move(67, "#profile.ghostCounter.data.VOLTACOUNT", "#profile.ghostStorage.ghostTracker.items.VOLTA", ::migrateItem)
        event.move(67, "#profile.ghostCounter.data.GHOSTLYBOOTS", "#profile.ghostStorage.ghostTracker.items.GHOST_BOOTS", ::migrateItem)
    }
}
