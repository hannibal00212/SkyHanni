package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object PrecisionMiningHighlight {

    private val config get() = SkyHanniMod.feature.mining.highlightPrecisionMiningParticles

    private var lastParticle: AxisAlignedBB? = null
    private var lookingAtParticle: Boolean = false
    private var deleteTime: SimpleTimeMark? = null

    @SubscribeEvent
    fun onParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (!(event.type == EnumParticleTypes.CRIT || event.type == EnumParticleTypes.VILLAGER_HAPPY) ||
            !Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown
        ) return

        val mouseOverObject = Minecraft.getMinecraft().objectMouseOver
        if (mouseOverObject.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return

        val particleBoundingBox = event.location.add(-0.12, -0.12, -0.12)
            .axisAlignedTo(event.location.clone().add(0.12, 0.12, 0.12))

        val blockBoundingBox = mouseOverObject.blockPos.toLorenzVec()
            .axisAlignedTo(mouseOverObject.blockPos.add(1, 1, 1).toLorenzVec())
        if (!blockBoundingBox.intersectsWith(particleBoundingBox)) return

        lookingAtParticle = event.type == EnumParticleTypes.VILLAGER_HAPPY
        lastParticle = particleBoundingBox
        deleteTime = 5.ticks.fromNow()
    }

    @SubscribeEvent
    fun onRender(event: LorenzRenderWorldEvent) {
        val particleBoundingBox = lastParticle ?: return

        event.drawFilledBoundingBoxNea(particleBoundingBox, if (lookingAtParticle) Color.GREEN else Color.CYAN)
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        lastParticle ?: return
        val deletionTime = deleteTime ?: return
        if (deletionTime.isInPast()) {
            deleteTime = null
            lastParticle = null
        }
    }

    fun isEnabled() = MiningAPI.inCustomMiningIsland() && config
}
