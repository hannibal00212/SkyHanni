package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.BurrowGuessEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.event.diana.DianaAPI.isDianaSpade
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object DianaFixChat {

    private val config get() = SkyHanniMod.feature.event.diana

    private var hasSetParticleQuality = false
    private var hasSetToggleMusic = false
    private var lastParticleQualityPrompt = SimpleTimeMark.farPast()
    private var lastToggleMusicPrompt = SimpleTimeMark.farPast()
    private var errorCounter = 0
    private var successfulCounter = 0

    private var lastSpadeUse = SimpleTimeMark.farPast()
    private var lastErrorTime = SimpleTimeMark.farPast()
    private var lastGuessPoint = SimpleTimeMark.farPast()
    private var foundGuess = false

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (lastSpadeUse.passedSince() > 1.minutes) return

        if (foundGuess) {
            lastErrorTime = SimpleTimeMark.farPast()
            return
        }
        // particles don't work if a valid target point is close
        if (GriffinBurrowHelper.targetLocation != null) return
        val spadeUse = lastSpadeUse.passedSince()
        if (spadeUse <= 3.seconds) return

        if (lastErrorTime == lastSpadeUse) return
        lastErrorTime = lastSpadeUse

        noGuessFound()
    }

    private fun noGuessFound() {
        errorCounter++
        if (errorCounter == 1) {
            if (successfulCounter < 5) {
                ChatUtils.chat(
                    "Could not find Diana Guess using sound and particles, " +
                        "please try again. (Was this a funny sound easter egg?)"
                )
            }
            return
        }

        println("error")
        if (!hasSetParticleQuality) {
            if (lastParticleQualityPrompt.passedSince() > 30.seconds) {
                lastParticleQualityPrompt = SimpleTimeMark.now()
                ChatUtils.clickableChat(
                    "§cError detecting Diana Guess! §eClick here to set the particle quality to high!",
                    onClick = {
                        hasSetParticleQuality = true
                        HypixelCommands.particleQuality("high")
                        errorCounter = 0
                        ChatUtils.chat("Now try again!")
                    }
                )
            }
        } else {
            if (!hasSetToggleMusic) {
                if (lastToggleMusicPrompt.passedSince() > 30.seconds) {
                    lastToggleMusicPrompt = SimpleTimeMark.now()
                    ChatUtils.clickableChat(
                        "§cError detecting Diana Guess! Changing the Particle Quality has not worked :( " +
                            "§eClick here to disable hypixel music!",
                        onClick = {
                            hasSetToggleMusic = true
                            HypixelCommands.toggleMusic()
                            errorCounter = 0
                            ChatUtils.chat("Now try again, please!")
                        }
                    )
                }
            } else {
                ErrorManager.logErrorStateWithData(
                    "Could not find diana guess point",
                    "diana guess point failed to load after /pq and /togglemusic",
                    "errorCounter" to errorCounter,
                    "successfulCounter" to successfulCounter,
                )
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.HUB)
    fun onItemClick(event: ItemClickEvent) {
        if (!isEnabled()) return
        if (event.clickType != ClickType.RIGHT_CLICK) return
        val item = event.itemInHand ?: return
        if (!item.isDianaSpade) return

        if (lastSpadeUse.passedSince() > 5.seconds) {
            lastSpadeUse = SimpleTimeMark.now()
            foundGuess = false
        }
    }

    @SubscribeEvent
    fun onBurrowGuess(event: BurrowGuessEvent) {
        foundGuess = true

        if (hasSetToggleMusic) {
            ChatUtils.chat("Toggling the hypixel music has worked, good job!")
        } else if (hasSetParticleQuality) {
            ChatUtils.chat("Changing the particle quality has worked, good job!")
        }

        hasSetParticleQuality = false
        hasSetToggleMusic = false
        errorCounter = 0

        // This ensures we only count successes after new spade clicks, not the repeated moved guess locations
        if (lastGuessPoint != lastSpadeUse) {
            lastGuessPoint = lastSpadeUse
            lastGuessPoint = SimpleTimeMark.now()
            successfulCounter++
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        successfulCounter = 0
    }

    private fun isEnabled() = DianaAPI.isDoingDiana() && config.burrowsSoopyGuess
}
