package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.EffectAPI.NonGodPotEffect
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.EffectDurationChangeEvent
import at.hannibal2.skyhanni.events.EffectDurationChangeType
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
    private val profileStorage get() = ProfileStorageData.profileSpecific

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: /cf
     * REGEX-TEST: /cf test
     * REGEX-TEST: /chocolatefactory
     * REGEX-TEST: /chocolatefactory123456789
     * REGEX-TEST: /factory
     */
    private val commandPattern by RepoPattern.pattern(
        "inventory.chocolatefactory.opencommand",
        "\\/(?:cf|(?:chocolate)?factory)(?: .*)?",
    )

    /**
     * REGEX-TEST: §6Chocolate Factory
     * REGEX-TEST: §6Open Chocolate Factory
     */
    private val openCfItemPattern by RepoPattern.pattern(
        "inventory.chocolatefactory.openitem",
        "§6(?:Open )?Chocolate Factory",
    )
    // </editor-fold>

    private var commandSentTimer = SimpleTimeMark.farPast()

    @HandleEvent
    fun onEffectUpdate(event: EffectDurationChangeEvent) {
        if (event.effect != NonGodPotEffect.HOT_CHOCOLATE || event.duration == null) return
        val chocolateFactory = profileStorage?.chocolateFactory ?: return

        chocolateFactory.hotChocolateMixinExpiry = when (event.durationChangeType) {
            EffectDurationChangeType.ADD -> chocolateFactory.hotChocolateMixinExpiry + event.duration
            EffectDurationChangeType.REMOVE -> SimpleTimeMark.farPast()
            EffectDurationChangeType.SET -> SimpleTimeMark.now() + event.duration
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val slotDisplayName = event.slot?.stack?.displayName ?: return
        if (!openCfItemPattern.matches(slotDisplayName)) return

        if (checkIsBlocked()) event.cancel()
    }

    @HandleEvent
    fun onCommandSend(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!commandPattern.matches(event.message)) return
        if (commandSentTimer.passedSince() < 5.seconds) return
        if (LorenzUtils.isBingoProfile) return

        if (checkIsBlocked()) {
            commandSentTimer = SimpleTimeMark.now()
            event.cancel()
        }
    }

    private fun checkIsBlocked() = tryBlock() != TryBlockResult.SUCCESS

    private enum class TryBlockResult {
        SUCCESS,
        FAIL_NO_RABBIT,
        FAIL_NO_BOOSTER_COOKIE,
        FAIL_NO_MIXIN,
    }

    private fun tryBlock(): TryBlockResult {
        return if (config.mythicRabbitRequirement && !MythicRabbitPetWarning.correctPet()) {
            ChatUtils.clickToActionOrDisable(
                "§cBlocked opening the Chocolate Factory without a §dMythic Rabbit Pet §cequipped!",
                config::mythicRabbitRequirement,
                actionName = "open pets menu",
                action = { HypixelCommands.pet() },
            )
            TryBlockResult.FAIL_NO_RABBIT
        } else if (config.boosterCookieRequirement) {
            profileStorage?.bits?.boosterCookieExpiryTime?.let {
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
                TryBlockResult.FAIL_NO_BOOSTER_COOKIE
            } ?: TryBlockResult.SUCCESS
        } else if (config.hotChocolateMixinRequirement) {
            val mixinExpiryTime = profileStorage?.chocolateFactory?.hotChocolateMixinExpiry ?: SimpleTimeMark.farPast()
            val godPotExpiryTime = profileStorage?.godPotExpiryTime ?: SimpleTimeMark.farPast()
            if (mixinExpiryTime.isInPast()) {
                ChatUtils.clickToActionOrDisable(
                    "§cBlocked opening the Chocolate Factory without a §dHot Chocolate Mix §cactive! " +
                        "§7You may need to open §c/effects §7to refresh mixin status.",
                    config::hotChocolateMixinRequirement,
                    actionName = "search AH for mixin",
                    action = { HypixelCommands.auctionSearch("hot chocolate mixin") },
                )
                TryBlockResult.FAIL_NO_MIXIN
            } else if (godPotExpiryTime.isInPast()) {
                ChatUtils.clickToActionOrDisable(
                    "§cBlocked opening the Chocolate Factory without a §dGod Potion §cactive! " +
                        "§7You may need to open §c/effects §7and cycle the §aFilter §7to §bGod Potion Effects §7to refresh potion status.",
                    config::hotChocolateMixinRequirement,
                    actionName = "search AH for god potion",
                    action = { HypixelCommands.auctionSearch("god potion") },
                )
                TryBlockResult.FAIL_NO_MIXIN
            } else TryBlockResult.SUCCESS
        } else TryBlockResult.SUCCESS
    }
}
