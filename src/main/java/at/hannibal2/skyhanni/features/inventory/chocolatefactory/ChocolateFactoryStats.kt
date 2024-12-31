package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEventSummary
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.partyModeReplace
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getAvailableEggs
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getHitmanTimeToAll
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getOpenSlots
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getPurchasedSlots
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getTimeToFull
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.toRoman
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration

@SkyHanniModule
object ChocolateFactoryStats {

    private val config get() = ChocolateFactoryAPI.config
    private val profileStorage get() = ChocolateFactoryAPI.profileStorage

    private var display: Renderable? = null

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!ChocolateFactoryAPI.chocolateFactoryPaused) return
        updateDisplay()
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory && !ChocolateFactoryAPI.chocolateFactoryPaused) return
        if (!config.statsDisplay) return

        display?.let {
            config.position.renderRenderable(it, posLabel = "Chocolate Factory Stats")
        }
    }

    fun updateDisplay() {
        val profileStorage = profileStorage ?: return

        val map = buildMap {
            put(ChocolateFactoryStat.HEADER, "§6§lChocolate Factory ${ChocolateFactoryAPI.currentPrestige.toRoman()}")

            val maxSuffix = if (ChocolateFactoryAPI.isMax()) " §cMax!" else ""
            put(ChocolateFactoryStat.CURRENT, "§eCurrent Chocolate: §6${ChocolateAmount.CURRENT.formatted}$maxSuffix")
            put(ChocolateFactoryStat.THIS_PRESTIGE, "§eThis Prestige: §6${ChocolateAmount.PRESTIGE.formatted}")
            put(ChocolateFactoryStat.ALL_TIME, "§eAll-time: §6${ChocolateAmount.ALL_TIME.formatted}")

            addProduction()

            put(ChocolateFactoryStat.MULTIPLIER, "§eChocolate Multiplier: §6${profileStorage.chocolateMultiplier}")
            put(ChocolateFactoryStat.BARN, "§eBarn: §6${ChocolateFactoryBarnManager.barnStatus()}")

            addLeaderboard()

            put(ChocolateFactoryStat.EMPTY, "")
            put(ChocolateFactoryStat.EMPTY_2, "")
            put(ChocolateFactoryStat.EMPTY_3, "")
            put(ChocolateFactoryStat.EMPTY_4, "")
            put(ChocolateFactoryStat.EMPTY_5, "")

            addTimeTower()

            put(
                ChocolateFactoryStat.RAW_PER_SECOND,
                "§eRaw Per Second: §6${profileStorage.rawChocPerSecond.addSeparators()}",
            )

            addPrestige()

            val upgradeAvailableAt = ChocolateAmount.CURRENT.formattedTimeUntilGoal(profileStorage.bestUpgradeCost)
            put(ChocolateFactoryStat.TIME_TO_BEST_UPGRADE, "§eBest Upgrade: $upgradeAvailableAt")

            addHitman()
        }
        val text = config.statsDisplayList.filter {
            it.shouldDisplay()
        }.flatMap {
            map[it]?.partyModeReplace()?.split("\n").orEmpty()
        }
        display = createDisplay(text)
    }

    private fun MutableMap<ChocolateFactoryStat, String>.addLeaderboard() {
        val position = ChocolateFactoryAPI.leaderboardPosition
        val positionText = position?.addSeparators() ?: "???"
        val percentile = ChocolateFactoryAPI.leaderboardPercentile
        val percentileText = percentile?.let { "§7Top §a$it%" }.orEmpty()
        val leaderboard = "#$positionText $percentileText"
        ChocolatePositionChange.update(position, leaderboard)
        HoppityEventSummary.updateCfPosition(position, percentile)
        put(ChocolateFactoryStat.LEADERBOARD_POS, "§ePosition: §b$leaderboard")
    }

    private fun SimpleTimeMark?.formatIfFuture(): String? = this?.takeIfFuture()?.timeUntil()?.format()

    private fun MutableMap<ChocolateFactoryStat, String>.addHitman() {
        val profileStorage = ChocolateFactoryStats.profileStorage ?: return

        val hitmanStats = profileStorage.hitmanStats
        val availableHitmanEggs = hitmanStats.getAvailableEggs().takeIf { it > 0 }?.toString() ?: "§7None"
        val hitmanSingleSlotCd = hitmanStats.singleSlotCooldownMark.formatIfFuture() ?: "§aAll Ready"
        val hitmanAllSlotsCd = hitmanStats.allSlotsCooldownMark.formatIfFuture() ?: "§aAll Ready"
        val openSlotsNow = hitmanStats.getOpenSlots()
        val purchasedSlots = hitmanStats.getPurchasedSlots()

        val (hitmanAllSlotsTime, allSlotsEventInhibited) = hitmanStats.getHitmanTimeToAll()
        val hitmanAllClaimString = hitmanAllSlotsTime.takeIf { it > Duration.ZERO }?.format() ?: "§aAll Ready"
        val hitmanAllClaimReady = "${if (allSlotsEventInhibited) "§c" else "§b"}$hitmanAllClaimString"

        val (hitmanFullTime, hitmanFullEventInhibited) = hitmanStats.getTimeToFull()
        val hitmanFullString = if (openSlotsNow == 0) "§7Cooldown..."
        else hitmanFullTime.takeIf { it > Duration.ZERO }?.format() ?: "§cFull Now"
        val hitmanSlotsFull = "${if (hitmanFullEventInhibited) "§c" else "§b"}$hitmanFullString"
        put(ChocolateFactoryStat.HITMAN_HEADER, "§c§lRabbit Hitman")
        put(ChocolateFactoryStat.AVAILABLE_HITMAN_EGGS, "§eAvailable Hitman Eggs: §6$availableHitmanEggs")
        put(ChocolateFactoryStat.OPEN_HITMAN_SLOTS, "§eOpen Hitman Slots: §6$openSlotsNow")
        put(ChocolateFactoryStat.HITMAN_SLOT_COOLDOWN, "§eHitman Slot Cooldown: §b$hitmanSingleSlotCd")
        put(ChocolateFactoryStat.HITMAN_ALL_SLOTS, "§eAll Hitman Slots Cooldown: §b$hitmanAllSlotsCd")

        if (HoppityAPI.isHoppityEvent()) {
            put(ChocolateFactoryStat.HITMAN_FULL_SLOTS, "§eFull Hitman Slots: §b$hitmanSlotsFull")
            put(ChocolateFactoryStat.HITMAN_28_SLOTS, "§e$purchasedSlots Hitman Claims: $hitmanAllClaimReady")
        }
    }

    private fun MutableMap<ChocolateFactoryStat, String>.addPrestige() {
        val allTime = ChocolateAmount.ALL_TIME.chocolate()
        val nextChocolateMilestone = ChocolateFactoryAPI.getNextMilestoneChocolate(allTime)
        val amountUntilNextMilestone = nextChocolateMilestone - allTime
        val amountFormat = amountUntilNextMilestone.addSeparators()
        val maxMilestoneEstimate = ChocolateAmount.ALL_TIME.formattedTimeUntilGoal(nextChocolateMilestone)
        val prestigeEstimate = ChocolateAmount.PRESTIGE.formattedTimeUntilGoal(ChocolateFactoryAPI.chocolateForPrestige)
        val chocolateUntilPrestigeCalculation = ChocolateFactoryAPI.chocolateForPrestige - ChocolateAmount.PRESTIGE.chocolate()

        var chocolateUntilPrestige = "§6${chocolateUntilPrestigeCalculation.addSeparators()}"

        if (chocolateUntilPrestigeCalculation <= 0) {
            chocolateUntilPrestige = "§aPrestige Available"
        }
        val prestigeData = when {
            !ChocolateFactoryAPI.isMaxPrestige() -> mapOf(
                ChocolateFactoryStat.TIME_TO_PRESTIGE to "§eTime To Prestige: $prestigeEstimate",
                ChocolateFactoryStat.CHOCOLATE_UNTIL_PRESTIGE to "§eChocolate To Prestige: §6$chocolateUntilPrestige",
            )

            amountUntilNextMilestone >= 0 -> mapOf(
                ChocolateFactoryStat.TIME_TO_PRESTIGE to "§eTime To Next Milestone: $maxMilestoneEstimate",
                ChocolateFactoryStat.CHOCOLATE_UNTIL_PRESTIGE to "§eChocolate To Next Milestone: §6$amountFormat",
            )

            else -> emptyMap()
        }
        putAll(prestigeData)
    }

    private fun MutableMap<ChocolateFactoryStat, String>.addTimeTower() {
        val timeTowerInfo = if (ChocolateFactoryTimeTowerManager.timeTowerActive()) {
            "§d§lActive"
        } else {
            "§6${ChocolateFactoryTimeTowerManager.timeTowerCharges()}"
        }
        put(ChocolateFactoryStat.TIME_TOWER, "§eTime Tower: §6$timeTowerInfo")

        val timeTowerFull = ChocolateFactoryTimeTowerManager.timeTowerFullTimeMark()
        put(
            ChocolateFactoryStat.TIME_TOWER_FULL,
            if (ChocolateFactoryTimeTowerManager.timeTowerFull()) {
                "§eFull Tower Charges: §a§lNow\n" + "§eHappens at: §a§lNow"
            } else {
                "§eFull Tower Charges: §b${
                    timeTowerFull.timeUntil().format()
                }\n" + "§eHappens at: §b${timeTowerFull.formattedDate("EEEE, MMM d h:mm a")}"
            },
        )
    }

    private fun createDisplay(text: List<String>) = Renderable.clickAndHover(
        Renderable.verticalContainer(text.map(Renderable::string)),
        tips = listOf("§bCopy to Clipboard!"),
        onClick = {
            val list = text.toMutableList()
            list.add(0, "${LorenzUtils.getPlayerName()}'s Chocolate Factory Stats")

            ClipboardUtils.copyToClipboard(list.joinToString("\n") { it.removeColor() })
        },
    )

    private fun MutableMap<ChocolateFactoryStat, String>.addProduction() {
        val perSecond = ChocolateFactoryAPI.chocolatePerSecond
        val perMinute = perSecond * 60
        val perHour = perMinute * 60
        val perDay = perHour * 24
        put(ChocolateFactoryStat.PER_SECOND, "§ePer Second: §6${perSecond.addSeparators()}")
        put(ChocolateFactoryStat.PER_MINUTE, "§ePer Minute: §6${perMinute.addSeparators()}")
        put(ChocolateFactoryStat.PER_HOUR, "§ePer Hour: §6${perHour.addSeparators()}")
        put(ChocolateFactoryStat.PER_DAY, "§ePer Day: §6${perDay.addSeparators()}")
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(42, "event.chocolateFactory.statsDisplayList") { element ->
            addToDisplayList(element, "TIME_TOWER", "TIME_TO_PRESTIGE")
        }
        event.transform(45, "inventory.chocolateFactory.statsDisplayList") { element ->
            addToDisplayList(element, "TIME_TO_BEST_UPGRADE")
        }
    }

    private fun addToDisplayList(element: JsonElement, vararg toAdd: String): JsonElement {
        val jsonArray = element.asJsonArray
        toAdd.forEach { jsonArray.add(JsonPrimitive(it)) }
        return jsonArray
    }

    enum class ChocolateFactoryStat(private val display: String, val shouldDisplay: () -> Boolean = { true }) {
        HEADER("§6§lChocolate Factory Stats"),
        CURRENT("§eCurrent Chocolate: §65,272,230"),
        THIS_PRESTIGE("§eThis Prestige: §6483,023,853", { ChocolateFactoryAPI.currentPrestige != 1 }),
        ALL_TIME("§eAll-time: §6641,119,115"),
        PER_SECOND("§ePer Second: §63,780.72"),
        PER_MINUTE("§ePer Minute: §6226,843.2"),
        PER_HOUR("§ePer Hour: §613,610,592"),
        PER_DAY("§ePer Day: §6326,654,208"),
        MULTIPLIER("§eChocolate Multiplier: §61.77"),
        BARN("§eBarn: §6171/190 Rabbits"),
        LEADERBOARD_POS("§ePosition: §b#103 §7Top §a0.87%"),
        EMPTY(""),
        EMPTY_2(""),
        EMPTY_3(""),
        EMPTY_4(""),
        EMPTY_5(""),
        TIME_TOWER("§eTime Tower: §62/3 Charges", { ChocolateFactoryTimeTowerManager.currentCharges() != -1 }),
        TIME_TOWER_FULL(
            "§eTime Tower Full Charges: §b5h 13m 59s\n§bHappens at: Monday, May 13 5:32 AM",
            { ChocolateFactoryTimeTowerManager.currentCharges() != -1 || ChocolateFactoryTimeTowerManager.timeTowerFull() },
        ),
        TIME_TO_PRESTIGE("§eTime To Prestige: §b1d 13h 59m 4s"),
        RAW_PER_SECOND("§eRaw Per Second: §62,136"),
        CHOCOLATE_UNTIL_PRESTIGE("§eChocolate To Prestige: §65,851"),
        TIME_TO_BEST_UPGRADE(
            "§eBest Upgrade: §b 59m 4s",
            { ChocolateFactoryAPI.profileStorage?.bestUpgradeCost != 0L },
        ),
        HITMAN_HEADER("§c§lRabbit Hitman"),
        AVAILABLE_HITMAN_EGGS("§eAvailable Hitman Eggs: §63"),
        OPEN_HITMAN_SLOTS("§eOpen Hitman Slots: §63"),
        HITMAN_SLOT_COOLDOWN("§eHitman Slot Cooldown: §b8m 6s"),
        HITMAN_ALL_SLOTS("§eAll Hitman Slots Cooldown: §b8h 8m 6s"),
        HITMAN_FULL_SLOTS("§eFull Hitman Slots: §b2h 10m"),
        HITMAN_28_SLOTS("§e28 Hitman Claims: §b3h 20m"),
        ;

        override fun toString(): String {
            return display
        }
    }
}
