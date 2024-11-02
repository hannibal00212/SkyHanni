package at.hannibal2.skyhanni.data.mob

import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object LineToMobHandler {

    data class LineSettings(
        val color: Color,
        val width: Int,
        val depth: Boolean,
    )

    private val lines = mutableMapOf<Mob, LineSettings>()

    fun register(mob: Mob, color: Color, width: Int, depth: Boolean) = register(mob, LineSettings(color, width, depth))

    fun register(mob: Mob, settings: LineSettings) {
        lines[mob] = settings
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn) {
        lines.remove(event.mob)
    }

    @SubscribeEvent
    fun onLorenzRenderWorld(event: LorenzRenderWorldEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (lines.isEmpty()) return
        RenderUtils.LineDrawer.draw3D(event.partialTicks) {
            for ((mob, settings) in lines) {
                if (!mob.canBeSeen()) continue
                draw3DLineFromPlayer(mob.centerCords, settings.color, settings.width, settings.depth)
            }
        }
    }
}
