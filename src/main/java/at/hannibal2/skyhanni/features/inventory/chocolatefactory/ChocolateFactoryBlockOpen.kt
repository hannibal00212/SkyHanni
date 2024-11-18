package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.features.event.hoppity.MythicRabbitPetWarning
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryBlockOpen {
    private val config get() = SkyHanniMod.feature.inventory.chocolateFactory
    private val profileStorage get() = ProfileStorageData.profileSpecific?.bits

    /**
     * REGEX-TEST: /cf
     * REGEX-TEST: /cf test
     * REGEX-TEST: /chocolatefactory
     * REGEX-TEST: /chocolatefactory123456789
     * REGEX-TEST: /factory
     */
    private val commandPattern by RepoPattern.pattern(
        "inventory.chocolatefactory.opencommand",
        "/(?:cf|(?:chocolate)?factory)(?: .*)?",
    )

    /**
     * REGEX-TEST: §6Chocolate Factory
     * REGEX-TEST: §6Open Chocolate Factory
     */
    private val openCfItemPattern by RepoPattern.pattern(
        "inventory.chocolatefactory.openitem",
        "§6(?:Open )?Chocolate Factory",
    )

    private var commandSentTimer = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val slotDisplayName = event.slot?.stack?.displayName ?: return
        if (!openCfItemPattern.matches(slotDisplayName)) return

        if (tryBlock() != TryBlockResult.SUCCESS) event.cancel()
    }

    @SubscribeEvent
    fun onCommandSend(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!commandPattern.matches(event.message)) return
        if (commandSentTimer.passedSince() < 5.seconds) return

        if (tryBlock() != TryBlockResult.SUCCESS) {
            commandSentTimer = SimpleTimeMark.now()
            event.cancel()
        }
    }

    private enum class TryBlockResult {
        SUCCESS,
        FAIL_NO_RABBIT,
        FAIL_NO_BOOSTER_COOKIE,
    }

    private fun tryBlock(): TryBlockResult {
        if (config.mythicRabbitRequirement && !MythicRabbitPetWarning.correctPet()) {
            ChatUtils.clickToActionOrDisable(
                "§cBlocked opening the Chocolate Factory without a §dMythic Rabbit Pet §cequipped!",
                config::mythicRabbitRequirement,
                actionName = "open pets menu",
                action = { HypixelCommands.pet() },
            )
            return TryBlockResult.FAIL_NO_RABBIT
        } else if (config.boosterCookieRequirement) {
            profileStorage?.boosterCookieExpiryTime?.let {
                if (it.timeUntil() > 0.seconds) return TryBlockResult.SUCCESS
                ChatUtils.clickToActionOrDisable(
                    "§cBlocked opening the Chocolate Factory without a §dBooster Cookie §cactive!",
                    config::boosterCookieRequirement,
                    actionName = "warp to hub",
                    action = {
                        HypixelCommands.warp("hub")
                        EntityMovementData.onNextTeleport(IslandType.HUB) {
                            IslandGraphs.pathFind(LorenzVec(-32.5, 71.0, -76.5), "§aBazaar", condition = { true })
                        }
                    },
                )
                return TryBlockResult.FAIL_NO_BOOSTER_COOKIE
            }
        }
        return TryBlockResult.SUCCESS
    }
}
