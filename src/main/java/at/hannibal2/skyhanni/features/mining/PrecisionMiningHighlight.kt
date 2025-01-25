package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.MiningApi
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
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
import java.awt.Color

@SkyHanniModule
object PrecisionMiningHighlight {

    private val config get() = SkyHanniMod.feature.mining.highlightPrecisionMiningParticles

    private var lastParticle: AxisAlignedBB? = null
    private var lookingAtParticle: Boolean = false
    private var deleteTime: SimpleTimeMark? = null

    @HandleEvent(onlyOnSkyblock = true)
    fun onParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (!(event.type == EnumParticleTypes.CRIT || event.type == EnumParticleTypes.VILLAGER_HAPPY) ||
            !Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown
        ) return

        val mouseOverObject = Minecraft.getMinecraft().objectMouseOver ?: return
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

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        val particleBoundingBox = lastParticle ?: return

        event.drawFilledBoundingBoxNea(particleBoundingBox, if (lookingAtParticle) Color.GREEN else Color.CYAN)
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        lastParticle ?: return
        val deletionTime = deleteTime ?: return
        if (deletionTime.isInPast()) {
            deleteTime = null
            lastParticle = null
        }
    }

    fun isEnabled() = MiningApi.inCustomMiningIsland() && config
}
