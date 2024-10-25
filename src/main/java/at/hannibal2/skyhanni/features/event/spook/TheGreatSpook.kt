package at.hannibal2.skyhanni.features.event.spook

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUCalculator
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TheGreatSpook {

    // §r§cPrimal Fears§r§7: §r§6§lREADY!!
    private val config get() = SkyHanniMod.feature.event.spook
    private var displayTimer = ""
    private var displayTimeLeft = ""
    private var notificationSeconds = 0

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (config.primalFearTimer || config.primalFearNotification) displayTimer = checkTabList(" §r§cPrimal Fears§r§7: ")
        if (config.greatSpookTimeLeft) displayTimeLeft = checkTabList(" §r§dEnds In§r§7: ")
        if (config.primalFearNotification) {
            if (displayTimer.endsWith("READY!!")) {
                if (notificationSeconds > 0) {
                    SoundUtils.playBeepSound()
                    notificationSeconds--
                }
            } else if (displayTimer.isNotEmpty()) {
                notificationSeconds = 5
            }
        }
    }

    private fun checkTabList(matchString: String): String {
        return (TabListData.getTabList().find { it.contains(matchString) }.orEmpty()).trim()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (config.primalFearTimer) config.positionTimer.renderString(displayTimer, posLabel = "Primal Fear Timer")
        if (config.fearStatDisplay) {
            SkyblockStat.FEAR.displayValue?.let {
                config.positionFear.renderString(it, posLabel = "Fear Stat Display")
            }
        }
        if (config.greatSpookTimeLeft) config.positionTimeLeft.renderString(displayTimeLeft, posLabel = "Time Left Display")
    }

    /**
     * REGEX-TEST: §d§lQUICK MATHS! §r§7Solve: §r§e(10*2)+12*5
     */
    private val mathFearMessagePattern by RepoPattern.pattern(
        "chat.math",
        "§d§lQUICK MATHS! §r§7Solve: §r§e(?<math>.*)",
    )

    /**
     * REGEX-TEST: §4[FEAR] Public Speaking Demon§r§f: Speak PlasticEating!
     */
    private val speakingFearMessagePattern by RepoPattern.pattern(
        "chat.speaking",
        "§4\\[FEAR] Public Speaking Demon§r§f: (Speak|Say something interesting) (?<name>.*)!",
    )

    private fun mathSolver(query: String?) {
        val answer = query?.let { NEUCalculator.calculateOrNull(it)?.toInt() } ?: run {
            ChatUtils.userError("Failed to solve $query!")
            return
        }
        ChatUtils.clickToActionOrDisable(
            "The answer is §b$answer§e.",
            config.primalFearSolver::math,
            actionName = "Send the answer",
            action = {
                HypixelCommands.allChat(answer.toString())
            },
        )
    }

    private fun publicSpeakingSolver() {
        ChatUtils.clickToActionOrDisable(
            "Solving Public Speak puzzle for you.",
            config.primalFearSolver::publicSpeaking,
            actionName = "send a random string.",
            action = {
                HypixelCommands.allChat("I looove SkyHanni! ${StringUtils.generateRandomString(4)}")
            },
        )
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (config.primalFearSolver.math) {
            mathFearMessagePattern.matchMatcher(event.message) {
                DelayedRun.runNextTick {
                    mathSolver(group("math"))
                }
            }
        }

        if (config.primalFearSolver.publicSpeaking) {
            speakingFearMessagePattern.matchMatcher(event.message) {
                DelayedRun.runNextTick {
                    publicSpeakingSolver()
                }
            }
        }
    }
}
