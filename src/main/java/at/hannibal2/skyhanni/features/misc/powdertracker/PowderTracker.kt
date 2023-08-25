package at.hannibal2.skyhanni.features.misc.powdertracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.Storage
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.*
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.LorenzUtils.addSelector
import at.hannibal2.skyhanni.utils.LorenzUtils.afterChange
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatNumber
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt

class PowderTracker {

    private val config get() = SkyHanniMod.feature.misc.powderTrackerConfig
    private var display = emptyList<List<Any>>()
    private val picked = "§6You have successfully picked the lock on this chest!".toPattern()
    private val powderEvent = ".*§r§b§l2X POWDER STARTED!.*".toPattern()
    private val powderEnded = ".*§r§b§l2X POWDER ENDED!.*".toPattern()
    private var lastChestPicked = 0L
    private var isGrinding = false
    private val gemstoneInfo = ResourceInfo(0L, 0L, 0, 0.0, mutableListOf())
    private val mithrilInfo = ResourceInfo(0L, 0L, 0, 0.0, mutableListOf())
    private val chestInfo = ResourceInfo(0L, 0L, 0, 0.0, mutableListOf())
    private var doublePowder = false
    private var currentDisplayMode = DisplayMode.TOTAL
    private var inventoryOpen = false
    private var currentSessionData = mutableMapOf<Int, Storage.ProfileSpecific.PowderTracker>()
    private val gemstones = listOf(
        "Ruby" to "§c",
        "Sapphire" to "§b",
        "Amber" to "§6",
        "Amethyst" to "§5",
        "Jade" to "§a",
        "Topaz" to "§e"
    )

    init {
        fixedRateTimer(name = "skyhanni-powder-tracker", period = 1000) {
            if (!isEnabled()) return@fixedRateTimer
            calculateResourceHour(gemstoneInfo)
            calculateResourceHour(mithrilInfo)
            calculateResourceHour(chestInfo)
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        val currentlyOpen = Minecraft.getMinecraft().currentScreen is GuiInventory
        if (inventoryOpen != currentlyOpen) {
            inventoryOpen = currentlyOpen
            saveAndUpdate()
        }

        if (config.onlyWhenPowderGrinding && !isGrinding) return

        config.position.renderStringsAndItems(
            display,
            posLabel = "Powder Chest Tracker")
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val msg = event.message
        val both = currentLog() ?: return

        picked.matchMatcher(msg) {
            both.modify {
                it.totalChestPicked += 1
            }
            isGrinding = true
            lastChestPicked = System.currentTimeMillis()
        }
        powderEvent.matchMatcher(msg) { doublePowder = true }
        powderEnded.matchMatcher(msg) { doublePowder = false }

        for (reward in PowderChestReward.entries) {
            reward.pattern.matchMatcher(msg) {
                both.modify {
                    val count = it.rewards[reward] ?: 0
                    it.rewards[reward] = count + group("amount").formatNumber()
                }
            }
        }
        saveAndUpdate()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!config.onlyWhenPowderGrinding) return
        if (System.currentTimeMillis() - lastChestPicked > 60_000) {
            isGrinding = false
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.textFormat.afterChange { saveAndUpdate() }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (!isEnabled()) return
        gemstoneInfo.perHour = 0.0
        gemstoneInfo.stoppedChecks = 0
        gemstoneInfo.perMin.clear()
        mithrilInfo.perHour = 0.0
        mithrilInfo.stoppedChecks = 0
        mithrilInfo.perMin.clear()
        chestInfo.perHour = 0.0
        chestInfo.stoppedChecks = 0
        chestInfo.perMin.clear()
        saveAndUpdate()
    }

    private fun saveAndUpdate() {
        calculateGemstone()
        calculateMithril()
        calculateChest()
        display = formatDisplay(drawDisplay())
    }

    private fun formatDisplay(map: List<List<Any>>) = buildList {
        if (map.isEmpty()) return@buildList
        for (index in config.textFormat.get()) {
            add(map[index])
        }
    }

    private fun drawDisplay() = buildList<List<Any>> {
        addAsSingletonList("§b§lPowder Tracker")
        if (inventoryOpen) {
            addSelector<DisplayMode>(
                "§7Display Mode: ",
                getName = { type -> type.displayName },
                isCurrent = { it == currentDisplayMode },
                onChange = {
                    currentDisplayMode = it
                    saveAndUpdate()
                }
            )
        }
        val both = currentLog() ?: return@buildList
        val display = both.get(currentDisplayMode)
        val rewards = display.rewards

        addAsSingletonList("§d${display.totalChestPicked.addSeparators()} Total Chests Picked §7(${chestInfo.perHour.roundToInt().addSeparators()}/h)")
        addAsSingletonList("§bDouble Powder: ${if (doublePowder) "§aActive!" else "§cInactive!"}")

        val mithril = PowderChestReward.entries[0]
        val mithrilCount = rewards.getOrDefault(mithril, 0).addSeparators()
        addAsSingletonList("§b$mithrilCount ${mithril.displayName} §7(${mithrilInfo.perHour.roundToInt().addSeparators()}/h)")

        val gemstone = PowderChestReward.entries[1]
        val gemstoneCount = rewards.getOrDefault(gemstone, 0).addSeparators()
        addAsSingletonList("§b$gemstoneCount ${gemstone.displayName} §7(${gemstoneInfo.perHour.roundToInt().addSeparators()}/h)")

        addAsSingletonList("")

        for ((gem, color) in gemstones) {
            var totalGemstone = 0L

            for (quality in arrayOf("ROUGH", "FLAWED", "FINE", "FLAWLESS")) {
                val gemstoneType = PowderChestReward.valueOf("${quality}_${gem.uppercase()}_GEMSTONE")
                val count = rewards.getOrDefault(gemstoneType, 0)
                val multiplier = when (quality) {
                    "FLAWED" -> 80
                    "FINE" -> 6400
                    "FLAWLESS" -> 512000
                    else -> 1
                }
                totalGemstone += count * multiplier
            }

            val (flawless, fine, flawed, rough) = convert(totalGemstone)
            addAsSingletonList("§5${flawless}§7-§9${fine}§7-§a${flawed}§f-${rough} $color$gem Gemstone")
        }

        for (reward in PowderChestReward.entries.subList(26, 32)) { // robots part
            val count = rewards.getOrDefault(reward, 0).addSeparators()
            addAsSingletonList("§b$count ${reward.displayName}")
        }

        val goblinEgg = rewards.getOrDefault(PowderChestReward.GOBLIN_EGG, 0)
        val greenEgg = rewards.getOrDefault(PowderChestReward.GREEN_GOBLIN_EGG, 0)
        val redEgg = rewards.getOrDefault(PowderChestReward.RED_GOBLIN_EGG, 0)
        val yellowEgg = rewards.getOrDefault(PowderChestReward.YELLOW_GOBLIN_EGG, 0)
        val blueEgg = rewards.getOrDefault(PowderChestReward.BLUE_GOBLIN_EGG, 0)
        addAsSingletonList("§9$goblinEgg§7-§a$greenEgg§7-§c$redEgg§f-§e$yellowEgg§f-§3$blueEgg §fGoblin Egg")

        for (reward in PowderChestReward.entries.subList(37, 46)) {
            val count = rewards.getOrDefault(reward, 0).addSeparators()
            addAsSingletonList("§b$count ${reward.displayName}")
        }
    }

    private fun calculateResourceHour(resourceInfo: ResourceInfo) {
        val difference = resourceInfo.estimated - resourceInfo.lastEstimated
        resourceInfo.lastEstimated = resourceInfo.estimated

        if (difference == resourceInfo.estimated) {
            return
        }

        resourceInfo.perHour = resourceInfo.perMin.average() * 3600
        resourceInfo.perMin.add(difference)

        if (difference == 0L) {
            resourceInfo.stoppedChecks += 1

            if (resourceInfo.stoppedChecks == 60) {
                resourceInfo.stoppedChecks = 0
                resourceInfo.perMin.clear()
                resourceInfo.perHour = 0.0
            }
            return
        }
        resourceInfo.stoppedChecks = 0
    }

    private fun calculateGemstone() {
        val both = currentLog() ?: return
        val display = both.get(currentDisplayMode)
        val rewards = display.rewards
        gemstoneInfo.estimated = 0
        gemstoneInfo.estimated += rewards.getOrDefault(PowderChestReward.GEMSTONE_POWDER, 0)
    }

    private fun calculateMithril() {
        val both = currentLog() ?: return
        val display = both.get(currentDisplayMode)
        val rewards = display.rewards
        mithrilInfo.estimated = 0
        mithrilInfo.estimated += rewards.getOrDefault(PowderChestReward.MITHRIL_POWDER, 0)
    }

    private fun calculateChest() {
        val both = currentLog() ?: return
        val display = both.get(currentDisplayMode)
        chestInfo.estimated = 0
        chestInfo.estimated += display.totalChestPicked
    }

    private fun convert(roughCount: Long): Gem {
        val flawlessRatio = 512000
        val fineRatio = 6400
        val flawedRatio = 80

        val flawlessCount = roughCount / flawlessRatio
        val remainingAfterFlawless = roughCount % flawlessRatio

        val fineCount = remainingAfterFlawless / fineRatio
        val remainingAfterFine = remainingAfterFlawless % fineRatio

        val flawedCount = remainingAfterFine / flawedRatio
        val remainingRoughCount = remainingAfterFine % flawedRatio

        return Gem(flawlessCount, fineCount, flawedCount, remainingRoughCount)
    }

    data class Gem(val flawless: Long, val fine: Long, val flawed: Long, val rough: Long)
    private data class ResourceInfo(
        var estimated: Long,
        var lastEstimated: Long,
        var stoppedChecks: Int,
        var perHour: Double,
        val perMin: MutableList<Long>
    )

    enum class DisplayMode(val displayName: String) {
        TOTAL("Total"),
        CURRENT("This Session"),
        ;
    }


    private fun currentLog(): AbstractPowderTracker? {
        val profileSpecific = ProfileStorageData.profileSpecific ?: return null

        return AbstractPowderTracker(
            profileSpecific.powderTracker.getOrPut(0) { Storage.ProfileSpecific.PowderTracker() },
            currentSessionData.getOrPut(0) { Storage.ProfileSpecific.PowderTracker() }
        )
    }

    class AbstractPowderTracker(
        private val total: Storage.ProfileSpecific.PowderTracker,
        private val currentSession: Storage.ProfileSpecific.PowderTracker,
    ) {

        fun modify(modifyFunction: (Storage.ProfileSpecific.PowderTracker) -> Unit) {
            modifyFunction(total)
            modifyFunction(currentSession)
        }

        fun get(displayMode: DisplayMode) = when (displayMode) {
            DisplayMode.TOTAL -> total
            DisplayMode.CURRENT -> currentSession
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && LorenzUtils.skyBlockIsland == IslandType.CRYSTAL_HOLLOWS && config.enabled
}