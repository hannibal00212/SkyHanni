package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TransferCooldown {

    private val config get() = SkyHanniMod.feature.misc.commands
    private var lastRunCompleted: SimpleTimeMark = SimpleTimeMark.farPast()
    private var action: (() -> Unit)? = null

    @SubscribeEvent
    fun onWorldLoad(event: LorenzWorldChangeEvent) {
        if (!config.transferCooldown || lastRunCompleted.isInFuture()) return
        lastRunCompleted = DelayedRun.runDelayed(3.seconds) {
            if (config.transferCooldownMessage && LorenzUtils.inSkyBlock) {
                ChatUtils.chat("§aPlayer Transfer Cooldown has ended.")
            }
            action?.invoke()
            action = null
        }
    }

    @SubscribeEvent
    fun onCommand(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock || !config.transferCooldown || lastRunCompleted.isInPast()) return
        when (event.splitMessage[0]) {
            "/is" -> {
                event.cancel()
                action = { HypixelCommands.island() }
            }

            "/warp" -> {
                event.cancel()
                action = {
                    HypixelCommands.warp(event.splitMessage.subList(1, event.splitMessage.size).joinToString(" "))
                }
            }

            "/warpforge" -> {
                event.cancel()
                action = { HypixelCommands.warp("forge") }
            }

            "/hub" -> {
                event.cancel()
                action = { HypixelCommands.warp("hub") }
            }
        }
    }
}
