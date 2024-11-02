package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.concurrent.fixedRateTimer

@SkyHanniModule
object MovementSpeedDisplay {

    private val config get() = SkyHanniMod.feature.misc

    private var display = ""
    private val soulsandSpeeds = mutableListOf<Double>()

    /**
     * This speed value represents the movement speed in blocks per second.
     * This has nothing to do with the speed stat.
     */
    var speed = 0.0
    var usingSoulsandSpeed = false

    init {
        // TODO use LorenzTickEvent
        fixedRateTimer(name = "skyhanni-movement-speed-display", period = 250, initialDelay = 1_000) {
            checkSpeed()
        }
    }

    private fun checkSpeed() {
        if (!LorenzUtils.onHypixel) return

        speed = with(Minecraft.getMinecraft().thePlayer) {
            val oldPos = LorenzVec(prevPosX, prevPosY, prevPosZ)
            val newPos = LorenzVec(posX, posY, posZ)

            // Distance from previous tick, multiplied by TPS
            oldPos.distance(newPos) * 20
        }
        val movingOnSoulsand = LocationUtils.playerLocation().getBlockAt() == Blocks.soul_sand && speed > 0.0
        if (movingOnSoulsand) {
            soulsandSpeeds.add(speed)
            if (soulsandSpeeds.size > 6) {
                speed = soulsandSpeeds.average()
                soulsandSpeeds.removeAt(0)
            }
        } else {
            soulsandSpeeds.clear()
        }
        usingSoulsandSpeed = movingOnSoulsand && soulsandSpeeds.size == 6
        if (isEnabled()) {
            display = "Movement Speed: ${speed.roundTo(2)}"
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.playerMovementSpeedPos.renderString(display, posLabel = "Movement Speed")
    }

    fun isEnabled() = LorenzUtils.onHypixel &&
        (LorenzUtils.inSkyBlock || OutsideSbFeature.MOVEMENT_SPEED.isSelected()) &&
        config.playerMovementSpeed
}
