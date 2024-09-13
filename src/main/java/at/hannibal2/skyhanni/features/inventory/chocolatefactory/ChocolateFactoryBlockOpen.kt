package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.features.event.hoppity.MythicRabbitPetWarning
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryBlockOpen {
    private val config get() = SkyHanniMod.feature.inventory.chocolateFactory

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

    private var commandSentTimer = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onCommandSend(event: MessageSendToServerEvent) {
        if (!isEnabled()) return
        if (!commandPattern.matches(event.message)) return
        if (commandSentTimer.passedSince() < 5.seconds) return
        if (MythicRabbitPetWarning.correctPet()) return

        commandSentTimer = SimpleTimeMark.now()
        event.cancel()
        ChatUtils.clickToActionOrDisable(
            "§cBlocked opening the Chocolate Factory without a §dMythic Rabbit Pet §cequipped!",
            config::mythicRabbitRequirement,
            actionName = "open pets menu",
            action = { HypixelCommands.pet() },
        )
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.mythicRabbitRequirement
}
