package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.OtherModsSettings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object PatcherFixes {
    private val config get() = SkyHanniMod.feature.misc

    private var lastCheck = SimpleTimeMark.farPast()
    private var lastChatMessage = SimpleTimeMark.farPast()

    fun onPlayerEyeLine() {
        if (!isEnabled()) return
        if (lastCheck.passedSince() < 5.seconds) return
        lastCheck = SimpleTimeMark.now()

        val patcher = OtherModsSettings.patcher()
        if (!patcher.getBoolean("parallaxFix")) return

        if (lastChatMessage.passedSince() < 3.minutes) return
        lastChatMessage = SimpleTimeMark.now()

        ChatUtils.clickToActionOrDisable(
            "§cPatcher's Parallax Fix breaks SkyHanni's line rendering!",
            config::fixPatcherLines,
            actionName = "disable this option in Patcher",
            action = { tryFix() },
        )
    }

    private fun tryFix() {
        val patcher = OtherModsSettings.patcher()
        if (patcher.getBoolean("parallaxFix")) {
            patcher.setBoolean("parallaxFix", false)
            ChatUtils.chat("§aDisabled Patcher's Parallax Fix! SkyHanni's lines should now work correctly.")
        } else {
            ChatUtils.userError("Patcher's Parallax is already disabled!")
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.fixPatcherLines
}
