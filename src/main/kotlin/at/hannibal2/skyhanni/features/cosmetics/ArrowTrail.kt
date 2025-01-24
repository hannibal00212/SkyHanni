package at.hannibal2.skyhanni.features.cosmetics

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.minecraft.RenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.getPrevLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.entity.projectile.EntityArrow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.LinkedList
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SkyHanniModule
object ArrowTrail {

    private val config get() = SkyHanniMod.feature.gui.cosmetic.arrowTrail

    private data class Line(val start: LorenzVec, val end: LorenzVec, val deathTime: SimpleTimeMark)

    private val listAllArrow: MutableList<Line> = LinkedList<Line>()
    private val listYourArrow: MutableList<Line> = LinkedList<Line>()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        val secondsAlive = config.secondsAlive.toDouble().toDuration(DurationUnit.SECONDS)
        val time = SimpleTimeMark.now()
        val deathTime = time.plus(secondsAlive)

        listAllArrow.removeIf { it.deathTime.isInPast() }
        listYourArrow.removeIf { it.deathTime.isInPast() }

        for (arrow in EntityUtils.getEntities<EntityArrow>()) {
            val line = Line(arrow.getPrevLorenzVec(), arrow.getLorenzVec(), deathTime)
            if (arrow.shootingEntity == Minecraft.getMinecraft().thePlayer) {
                listYourArrow.add(line)
            } else {
                listAllArrow.add(line)
            }
        }
    }

    @HandleEvent
    fun onRenderWorld(event: RenderWorldEvent) {
        if (!isEnabled()) return
        val color = if (config.handlePlayerArrowsDifferently) config.playerArrowColor else config.arrowColor
        val playerArrowColor = color.toSpecialColor()
        listYourArrow.forEach {
            event.draw3DLine(it.start, it.end, playerArrowColor, config.lineWidth, true)
        }
        if (!config.hideOtherArrows) {
            val arrowColor = config.arrowColor.toSpecialColor()
            listAllArrow.forEach {
                event.draw3DLine(it.start, it.end, arrowColor, config.lineWidth, true)
            }
        }
    }

    private fun isEnabled() = config.enabled && (LorenzUtils.inSkyBlock || OutsideSbFeature.ARROW_TRAIL.isSelected())

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        listAllArrow.clear()
        listYourArrow.clear()
    }
}
