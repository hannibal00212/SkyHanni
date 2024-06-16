package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AshfangFreezeCooldown {

    private val config get() = SkyHanniMod.feature.crimsonIsle.ashfang

    private val cryogenicBlastPattern by RepoPattern.pattern(
        "ashfang.freeze.cryogenic",
        "§cAshfang Follower's Cryogenic Blast hit you for .* damage!"
    )

    private var lastHit = SimpleTimeMark.farPast()

    private val maxDuration = 3.seconds

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (cryogenicBlastPattern.matches(event.message)) lastHit = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!isCurrentlyFrozen()) return

        val format = (maxDuration - lastHit.passedSince()).format(showMilliSeconds = true)
        config.freezeCooldownPos.renderString(
            "§cAshfang Freeze: §a$format",
            posLabel = "Ashfang Freeze Cooldown",
        )
    }

    fun isCurrentlyFrozen(): Boolean {
        val passedSince = lastHit.passedSince()
        val duration = maxDuration - passedSince
        return duration.isPositive()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "ashfang.freezeCooldown", "crimsonIsle.ashfang.freezeCooldown")
        event.move(2, "ashfang.freezeCooldownPos", "crimsonIsle.ashfang.freezeCooldownPos")
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && AshfangBlazes.isAshfangActive() && config.freezeCooldown
}
