package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.BurrowDugEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GriffinPetWarning {
    private val config get() = SkyHanniMod.feature.event.diana
    private var wasCorrectPetAlready = false
    private var lastWarnTime = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!event.isMod(10)) return
        if (!config.petWarning) return
        if (!DianaAPI.isDoingDiana()) return
        if (!DianaAPI.hasSpadeInHand()) return

        val hasGriffinPet = DianaAPI.hasGriffinPet()
        if (hasGriffinPet) {
            wasCorrectPetAlready = true
            return
        }

        if (wasCorrectPetAlready) return

        warn()
    }

    @SubscribeEvent
    fun onBurrowDug(event: BurrowDugEvent) {
        DelayedRun.runDelayed(2.seconds) {
            wasCorrectPetAlready = false
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        DelayedRun.runDelayed(5.seconds) {
            wasCorrectPetAlready = false
        }
    }

    private fun warn() {
        ChatUtils.clickToActionOrDisable(
            "Reminder to use a Griffin pet for Mythological Ritual!",
            config::petWarning,
            actionName = "open pets menu",
            action = { HypixelCommands.pet() },
        )
        if (lastWarnTime.passedSince() > 30.seconds) {
            lastWarnTime = SimpleTimeMark.now()
            LorenzUtils.sendTitle("§cGriffin Pet!", 3.seconds)
        }
    }
}
