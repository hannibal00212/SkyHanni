package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AshfangNextResetCooldown {

    private val config get() = AshfangManager.config
    private val ashfangResetTime = 46.1.seconds

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (AshfangManager.lastSpawnTime.isFarPast()) return
        val nextSpawn = AshfangManager.lastSpawnTime + ashfangResetTime

        val format = if (nextSpawn.isInPast()) "§aNow!"
        else "§b${nextSpawn.timeUntil().format(TimeUnit.SECOND, showMilliSeconds = true)}"

        config.nextResetCooldownPos.renderString(
            "§cAshfang next reset in: $format",
            posLabel = "Ashfang Reset Cooldown",
        )
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "ashfang.nextResetCooldown", "crimsonIsle.ashfang.nextResetCooldown")
        event.move(2, "ashfang.nextResetCooldownPos", "crimsonIsle.ashfang.nextResetCooldownPos")
    }

    private fun isEnabled() = AshfangManager.active && config.nextResetCooldown
}
