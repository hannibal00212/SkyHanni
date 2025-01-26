package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.diana.BurrowDugEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GriffinPetWarning {
    private val config get() = SkyHanniMod.feature.event.diana
    private var wasCorrectPetAlready = false
    private var lastWarnTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!event.isMod(10)) return
        if (!config.petWarning) return
        if (!DianaApi.isDoingDiana()) return
        if (!DianaApi.hasSpadeInHand()) return

        val hasGriffinPet = DianaApi.hasGriffinPet()
        if (hasGriffinPet) {
            wasCorrectPetAlready = true
            return
        }

        if (wasCorrectPetAlready) return

        warn()
    }

    @HandleEvent
    fun onBurrowDug(event: BurrowDugEvent) {
        DelayedRun.runDelayed(2.seconds) {
            wasCorrectPetAlready = false
        }
    }

    @HandleEvent
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
