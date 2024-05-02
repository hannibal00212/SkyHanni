package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes

object ChocolateFactoryUpgradeWarning {

    private val config get() = ChocolateFactoryAPI.config.chocolateUpgradeWarnings
    private val profileStorage get() = ChocolateFactoryAPI.profileStorage

    private var lastUpgradeWarning = SimpleTimeMark.farPast()
    private var lastUpgradeSlot = -1
    private var lastUpgradeLevel = 0

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val profileStorage = profileStorage ?: return

        val upgradeAvailableAt = SimpleTimeMark(profileStorage.bestUpgradeAvailableAt)
        if (upgradeAvailableAt.isInPast() && !upgradeAvailableAt.isFarPast()) {
            checkUpgradeWarning()
        }
    }

    private fun checkUpgradeWarning() {
        if (!ChocolateFactoryAPI.isEnabled()) return
        if (!config.upgradeWarning) return
        if (ReminderUtils.isBusy()) return
        if (lastUpgradeWarning.passedSince() < config.timeBetweenWarnings.toDouble().minutes) return

        ChatUtils.clickableChat(
            "You have a Chocolate factory upgrade available to purchase!",
            onClick = {
                HypixelCommands.chocolateFactory()
            }
        )
        if (config.upgradeWarningSound) {
            SoundUtils.playBeepSound()
        }

        lastUpgradeWarning = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        lastUpgradeWarning = SimpleTimeMark.farPast()
    }

    fun checkUpgradeChange(slot: Int, level: Int) {
        if (slot != lastUpgradeSlot || level != lastUpgradeLevel) {
            lastUpgradeWarning = SimpleTimeMark.farPast()
            lastUpgradeSlot = slot
            lastUpgradeLevel = level
        }
    }
}
