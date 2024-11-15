package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.ItemInHandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.garden.pests.PestKillEvent
import at.hannibal2.skyhanni.events.garden.pests.PestSpawnEvent
import at.hannibal2.skyhanni.events.garden.pests.PestUpdateEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isBarn
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isPestCountInaccurate
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.locked
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.name
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.pests
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.uncleared
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PestAPI {

    val config get() = GardenAPI.config.pests
    val storage get() = GardenAPI.storage

    var scoreboardPests: Int
        get() = storage?.scoreboardPests ?: 0
        set(value) {
            storage?.scoreboardPests = value
        }

    var lastPestKillTime = SimpleTimeMark.farPast()
    var lastTimeVacuumHold = SimpleTimeMark.farPast()

    // TODO move into repo
    val vacuumVariants = listOf(
        "SKYMART_VACUUM".toInternalName(),
        "SKYMART_TURBO_VACUUM".toInternalName(),
        "SKYMART_HYPER_VACUUM".toInternalName(),
        "INFINI_VACUUM".toInternalName(),
        "INFINI_VACUUM_HOOVERIUS".toInternalName(),
    )

    fun hasVacuumInHand() = InventoryUtils.itemInHandId in vacuumVariants

    fun SprayType.getPests() = PestType.entries.filter { it.spray == this }

    private val patternGroup = RepoPattern.group("garden.pestsapi")
    private val pestsInScoreboardPattern by patternGroup.pattern(
        "scoreboard.pests",
        " §7⏣ §[ac]The Garden §4§lൠ§7 x(?<pests>.*)"
    )

    /**
     * REGEX-TEST:  §7⏣ §aPlot §7- §b22a
     * REGEX-TEST:  §7⏣ §aThe Garden
     */
    private val noPestsInScoreboardPattern by patternGroup.pattern(
        "scoreboard.nopests",
        " §7⏣ §a(?:The Garden|Plot §7- §b.+)$"
    )

    /**
     * REGEX-TEST:    §aPlot §7- §b4 §4§lൠ§7 x1
     */
    private val pestsInPlotScoreboardPattern by patternGroup.pattern(
        "scoreboard.plot.pests",
        "\\s*(?:§.)*Plot (?:§.)*- (?:§.)*(?<plot>.+) (?:§.)*ൠ(?:§.)* x(?<pests>\\d+)"
    )

    /**
     * REGEX-TEST:  §aPlot §7- §b3
     */
    private val noPestsInPlotScoreboardPattern by patternGroup.pattern(
        "scoreboard.plot.nopests",
        "\\s*(?:§.)*Plot (?:§.)*- (?:§.)*(?<plot>.{1,3})$"
    )
    private val pestInventoryPattern by patternGroup.pattern(
        "inventory",
        "§4§lൠ §cThis plot has §6(?<amount>\\d) Pests?§c!"
    )

    /**
     * REGEX-TEST:  Plots: §r§b4§r§f, §r§b12§r§f, §r§b13§r§f, §r§b18§r§f, §r§b20
     */
    private val infectedPlotsTablistPattern by patternGroup.pattern(
        "tablist.infectedplots",
        "\\sPlots: (?<plots>.*)"
    )

    /**
     * REGEX-TEST: §eYou received §a7x Enchanted Potato §efor killing a §6Locust§e!
     * REGEX-TEST: §eYou received §a6x Enchanted Cocoa Beans §efor killing a §6Moth§e!
     */
    val pestDeathChatPattern by patternGroup.pattern(
        "chat.pestdeath",
        "§eYou received §a(?<amount>[0-9]*)x (?<item>.*) §efor killing an? §6(?<pest>.*)§e!",
    )
    val noPestsChatPattern by patternGroup.pattern(
        "chat.nopests",
        "§cThere are not any Pests on your Garden right now! Keep farming!"
    )

    var gardenJoinTime = SimpleTimeMark.farPast()
    var firstScoreboardCheck = false

    private fun fixPests(loop: Int = 2) {
        DelayedRun.runDelayed(2.seconds) {
            val accurateAmount = getPlotsWithAccuratePests().sumOf { it.pests }
            val inaccurate = getPlotsWithInaccuratePests()
            val inaccurateAmount = inaccurate.size
            when {
                // if we can assume all inaccurate plots have 1 pest each
                scoreboardPests == accurateAmount + inaccurateAmount -> {
                    for (plot in inaccurate) {
                        plot.pests = 1
                        plot.isPestCountInaccurate = false
                    }
                }
                // if we can assume all the inaccurate pests are in the only inaccurate plot
                inaccurateAmount == 1 -> {
                    val plot = inaccurate.first()
                    plot.pests = scoreboardPests - accurateAmount
                    plot.isPestCountInaccurate = false
                }
                // when logic fails and we reach impossible pest counts
                accurateAmount + inaccurateAmount > scoreboardPests -> {
                    getInfestedPlots().forEach {
                        it.pests = 0
                        it.isPestCountInaccurate = true
                    }
                    if (loop > 0) fixPests(loop - 1)
                    else sendPestError()
                }
            }
        }
    }

    private fun updatePests() {
        if (!firstScoreboardCheck) return
        fixPests()
        PestUpdateEvent.post()
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onPestSpawn(event: PestSpawnEvent) {
        PestSpawnTimer.lastSpawnTime = SimpleTimeMark.now()
        val plotNames = event.plotNames
        for (plotName in plotNames) {
            val plot = GardenPlotAPI.getPlotByName(plotName)
            if (plot == null) {
                ChatUtils.userError("Open Plot Management Menu to load plot names and pest locations!")
                return
            }
            if (event.unknownAmount) {
                plot.isPestCountInaccurate = true
            } else {
                plot.pests += event.amountPests
                plot.isPestCountInaccurate = false
            }
        }
        if (!event.unknownAmount) scoreboardPests += event.amountPests
        updatePests()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!GardenAPI.inGarden()) return
        if (event.inventoryName != "Configure Plots") return

        for (plot in GardenPlotAPI.plots) {
            if (plot.isBarn() || plot.locked || plot.uncleared) continue
            plot.pests = 0
            plot.isPestCountInaccurate = false
            val item = event.inventoryItems[plot.inventorySlot] ?: continue
            item.getLore().matchFirst(pestInventoryPattern) {
                plot.pests = group("amount").toInt()
            }
        }
        updatePests()
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!GardenAPI.inGarden()) return
        for (line in event.tabList) {
            infectedPlotsTablistPattern.matchMatcher(line) {
                val plotList = group("plots").removeColor().split(", ").map { it.toInt() }
                if (plotList.sorted() == getInfestedPlots().map { it.id }.sorted()) return

                for (plot in GardenPlotAPI.plots) {
                    if (plotList.contains(plot.id)) {
                        if (!plot.isPestCountInaccurate && plot.pests == 0) {
                            plot.isPestCountInaccurate = true
                        }
                    } else {
                        plot.pests = 0
                        plot.isPestCountInaccurate = false
                    }
                }
                updatePests()
            }
        }
    }

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        if (!GardenAPI.inGarden()) return
        if (!firstScoreboardCheck) return
        checkScoreboardLines(event.added)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!GardenAPI.inGarden()) return
        pestDeathChatPattern.matchMatcher(event.message) {
            val pest = PestType.getByNameOrNull(group("pest").lowercase()) ?: return
            val item = NEUInternalName.fromItemNameOrNull(group("item")) ?: return
            val amount = group("amount").toInt()

            val killEvent = PestKillEvent(pest, item, amount)
            killEvent.postAndCatch()
            killEvent.blockedReason?.let { event.blockedReason = it }
        }
        if (noPestsChatPattern.matches(event.message)) {
            resetAllPests()
        }
    }

    @SubscribeEvent
    fun onPestKill(event: PestKillEvent) {
        lastPestKillTime = SimpleTimeMark.now()
        removeNearestPest()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!GardenAPI.inGarden()) return
        if (!firstScoreboardCheck && gardenJoinTime.passedSince() > 5.seconds) {
            checkScoreboardLines(ScoreboardData.sidebarLinesFormatted)
            firstScoreboardCheck = true
            updatePests()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastPestKillTime = SimpleTimeMark.farPast()
        lastTimeVacuumHold = SimpleTimeMark.farPast()
        gardenJoinTime = SimpleTimeMark.now()
        firstScoreboardCheck = false
    }

    @SubscribeEvent
    fun onItemInHandChange(event: ItemInHandChangeEvent) {
        if (!GardenAPI.inGarden()) return
        if (event.oldItem !in vacuumVariants) return
        lastTimeVacuumHold = SimpleTimeMark.now()
    }

    private fun getPlotsWithAccuratePests() = GardenPlotAPI.plots.filter { it.pests > 0 && !it.isPestCountInaccurate }

    private fun getPlotsWithInaccuratePests() = GardenPlotAPI.plots.filter { it.isPestCountInaccurate }

    fun getInfestedPlots() = GardenPlotAPI.plots.filter { it.pests > 0 || it.isPestCountInaccurate }

    fun getPlotsWithoutPests() = GardenPlotAPI.plots.filter { it.pests == 0 || !it.isPestCountInaccurate }

    fun getNearestInfestedPlot() = getInfestedPlots().minByOrNull { it.middle.distanceSqToPlayer() }

    private fun removePests(removedPests: Int) {
        if (removedPests < 1) return
        repeat(removedPests) {
            removeNearestPest()
        }
    }

    private fun removeNearestPest() {
        val plot = getNearestInfestedPlot()
            ?: ErrorManager.skyHanniError("Can not remove nearest pest: No infested plots detected.")

        if (!plot.isPestCountInaccurate) plot.pests--
        scoreboardPests--
        updatePests()
    }

    private fun resetAllPests() {
        scoreboardPests = 0
        GardenPlotAPI.plots.forEach {
            it.pests = 0
            it.isPestCountInaccurate = false
        }
        updatePests()
    }

    private fun sendPestError() {
        ErrorManager.logErrorStateWithData(
            "Error getting pest count",
            "Impossible pest count",
            "scoreboardPests" to scoreboardPests,
            "plots" to getInfestedPlots().map { "id: ${it.id} pests: ${it.pests} isInaccurate: ${it.isPestCountInaccurate}" },
            noStackTrace = true,
            betaOnly = true
        )
    }

    private fun checkScoreboardLines(list: List<String>) {
        for (line in list) {
            // gets if there are no pests remaining in the garden
            noPestsInScoreboardPattern.matchMatcher(line) {
                if (scoreboardPests != 0 || getInfestedPlots().isNotEmpty()) {
                    resetAllPests()
                }
                return
            }

            // gets the total amount of pests in the garden
            pestsInScoreboardPattern.matchMatcher(line) {
                val newPests = group("pests").formatInt()
                if (newPests != scoreboardPests) {
                    scoreboardPests = newPests
                    updatePests()
                }
            }

            // gets the amount of pests in the current plot
            pestsInPlotScoreboardPattern.matchMatcher(line) {
                val plotName = group("plot")
                val pestsInPlot = group("pests").toInt()
                val plot = GardenPlotAPI.getPlotByName(plotName) ?: return
                if (pestsInPlot != plot.pests || plot.isPestCountInaccurate) {
                    plot.pests = pestsInPlot
                    plot.isPestCountInaccurate = false
                    updatePests()
                }
            }

            // gets if there are no pests remaining in the current plot
            noPestsInPlotScoreboardPattern.matchMatcher(line) {
                val plotName = group("plot")
                val plot = GardenPlotAPI.getPlotByName(plotName) ?: return
                if (plot.pests != 0 || plot.isPestCountInaccurate) {
                    plot.pests = 0
                    plot.isPestCountInaccurate = false
                    updatePests()
                }
            }
        }
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Garden Pests")

        if (!GardenAPI.inGarden()) {
            event.addIrrelevant("not in garden")
            return
        }
        val disabled = with(config.pestFinder) {
            !showDisplay && !showPlotInWorld && teleportHotkey == Keyboard.KEY_NONE
        }
        if (disabled) {
            event.addIrrelevant("disabled in config")
            return
        }

        event.addIrrelevant {
            add("scoreboardPests is $scoreboardPests")
            add("")
            getInfestedPlots().forEach {
                add("id: ${it.id}")
                add(" name: ${it.name}")
                add(" isPestCountInaccurate: ${it.isPestCountInaccurate}")
                add(" pests: ${it.pests}")
                add(" ")
            }
        }
    }
}
