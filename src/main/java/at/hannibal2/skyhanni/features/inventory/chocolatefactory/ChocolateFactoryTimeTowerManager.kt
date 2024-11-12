package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryTimeTowerManager {

    private val config get() = ChocolateFactoryAPI.config
    private val profileStorage get() = ChocolateFactoryAPI.profileStorage

    private var lastTimeTowerWarning = SimpleTimeMark.farPast()
    private var warnAboutNewCharge = false
    private var wasTimeTowerRecentlyActive = false

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        wasTimeTowerRecentlyActive = false
    }

    private const val HOVER_TEXT = "§eClick to run /cf!"

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val profileStorage = profileStorage ?: return

        if (profileStorage.currentTimeTowerEnds.isInPast()) {
            profileStorage.currentTimeTowerEnds = SimpleTimeMark.farPast()
        }

        checkTimeTowerExpired()

        if (ChocolateFactoryAPI.inChocolateFactory) return

        if (timeTowerFullTimeMark().isInPast()) {
            profileStorage.currentTimeTowerUses = maxCharges()
        } else {
            var nextCharge = profileStorage.nextTimeTower
            while (nextCharge.isInPast() && !nextCharge.isFarPast()) {
                profileStorage.currentTimeTowerUses++
                nextCharge += ChocolateFactoryAPI.timeTowerChargeDuration()
                profileStorage.nextTimeTower = nextCharge
                warnAboutNewCharge = true
            }
        }

        if (currentCharges() > 0 && currentCharges() < maxCharges()) {
            if (!config.timeTowerWarning || timeTowerActive()) return
            if (!warnAboutNewCharge) return
            ChatUtils.clickableChat(
                "Your Time Tower has an available charge §7(${timeTowerCharges()})§e. " +
                    "Click here to use one.",
                onClick = { HypixelCommands.chocolateFactory() },
                HOVER_TEXT,
            )
            SoundUtils.playBeepSound()
            lastTimeTowerWarning = SimpleTimeMark.now()
            warnAboutNewCharge = false
        }
        checkTimeTowerWarning(false)
    }

    private fun checkTimeTowerExpired() {
        val isTimeTowerActive = timeTowerActive()
        if (!isTimeTowerActive && wasTimeTowerRecentlyActive && config.timeTowerReminder && currentCharges() > 0) {
            val charges = StringUtils.pluralize(currentCharges(), "charge", "charges", withNumber = true)
            ChatUtils.clickableChat(
                "§cYour Time Tower just expired and has $charges remaining. Click here to use one.",
                onClick = {
                    HypixelCommands.chocolateFactory()
                },
                hover = "§eClick to run /cf!",
            )
            SoundUtils.playBeepSound()
        }
        wasTimeTowerRecentlyActive = isTimeTowerActive
    }

    fun checkTimeTowerWarning(inInventory: Boolean) {
        if (!ChocolateFactoryAPI.isEnabled()) return
        if (!config.timeTowerWarning) return
        if (!timeTowerFull()) return
        if (ReminderUtils.isBusy()) return

        val warningSeparation = if (inInventory) 30.seconds else 5.minutes
        if (lastTimeTowerWarning.passedSince() < warningSeparation) return

        ChatUtils.clickToActionOrDisable(
            "§cYour Time Tower is full §7(${timeTowerCharges()})§c, Use one to avoid wasting time tower usages!",
            config::timeTowerWarning,
            actionName = "open Chocolate Factory",
            action = { HypixelCommands.chocolateFactory() },
        )
        SoundUtils.playBeepSound()
        lastTimeTowerWarning = SimpleTimeMark.now()
    }

    fun timeTowerCharges(): String {
        return "${currentCharges()}/${maxCharges()} Charges"
    }

    fun currentCharges(): Int {
        return profileStorage?.currentTimeTowerUses ?: -1
    }

    private fun maxCharges(): Int {
        return profileStorage?.maxTimeTowerUses ?: 3
    }

    fun timeTowerFull() = currentCharges() >= maxCharges()

    fun timeTowerActive(): Boolean {
        val currentTime = profileStorage?.lastDataSave ?: SimpleTimeMark.farPast()
        val endTime = timeTowerEnds()

        return endTime > currentTime
    }

    private fun timeTowerEnds(): SimpleTimeMark = profileStorage?.currentTimeTowerEnds ?: SimpleTimeMark.farPast()

    fun timeTowerFullTimeMark(): SimpleTimeMark {
        val profileStorage = profileStorage ?: return SimpleTimeMark.farPast()
        if (timeTowerFull()) return SimpleTimeMark.farPast()
        val nextChargeDuration = profileStorage.nextTimeTower
        val remainingChargesAfter = profileStorage.maxTimeTowerUses - (profileStorage.currentTimeTowerUses + 1)
        val endTime = nextChargeDuration + ChocolateFactoryAPI.timeTowerChargeDuration() * remainingChargesAfter

        return endTime
    }

    fun timeTowerActiveDuration(): Duration {
        if (!timeTowerActive()) return Duration.ZERO
        val currentTime = profileStorage?.lastDataSave ?: SimpleTimeMark.farPast()
        val endTime = profileStorage?.currentTimeTowerEnds ?: SimpleTimeMark.farPast()

        return endTime - currentTime
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        lastTimeTowerWarning = SimpleTimeMark.farPast()
    }
}
