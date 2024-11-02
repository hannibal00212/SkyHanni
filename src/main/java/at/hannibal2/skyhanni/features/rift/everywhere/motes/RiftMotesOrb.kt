package at.hannibal2.skyhanni.features.rift.everywhere.motes

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RiftMotesOrb {

    private val config get() = RiftAPI.config.motesOrbs

    private val motesPattern by RepoPattern.pattern(
        "rift.everywhere.motesorb",
        "§5§lORB! §r§dPicked up §r§5+.* Motes§r§d.*"
    )

    private var motesOrbs = emptyList<MotesOrb>()

    class MotesOrb(
        var location: LorenzVec,
        var counter: Int = 0,
        var startTime: Long = System.currentTimeMillis(),
        var lastTime: Long = System.currentTimeMillis(),
        var isOrb: Boolean = false,
        var pickedUp: Boolean = false,
    )

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        val location = event.location.add(-0.5, 0.0, -0.5)

        if (event.type == EnumParticleTypes.SPELL_MOB) {
            val orb =
                motesOrbs.find { it.location.distance(location) < 3 } ?: MotesOrb(location).also {
                    motesOrbs = motesOrbs.editCopy { add(it) }
                }

            orb.location = location
            orb.lastTime = System.currentTimeMillis()
            orb.counter++
            orb.pickedUp = false
            if (config.hideParticles && orb.isOrb) {
                event.cancel()
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        motesPattern.matchMatcher(event.message) {
            motesOrbs.minByOrNull { it.location.distanceToPlayer() }?.let {
                it.pickedUp = true
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        motesOrbs = motesOrbs.editCopy { removeIf { System.currentTimeMillis() > it.lastTime + 2000 } }

        for (orb in motesOrbs) {
            val ageInSeconds = (System.currentTimeMillis() - orb.startTime).toDouble() / 1000
            if (ageInSeconds < 0.5) continue

            val particlesPerSecond = (orb.counter.toDouble() / ageInSeconds).roundTo(1)
            if (particlesPerSecond < 60 || particlesPerSecond > 90) continue
            orb.isOrb = true

            if (System.currentTimeMillis() > orb.lastTime + 300) {
                orb.pickedUp = true
            }

            val location = orb.location.up(0.5)
            val sizeOffset = (5 - config.size) * -0.1
            val color = if (orb.pickedUp) LorenzColor.GRAY else LorenzColor.LIGHT_PURPLE
            val text = color.getChatColor() + "Motes Orb"
            event.drawDynamicText(location, text, 1.5 + sizeOffset, ignoreBlocks = false)
            event.drawWaypointFilled(location, color.toColor(), extraSize = sizeOffset)
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(9, "rift.area.motesOrbsConfig", "rift.area.motesOrbs")
    }
}
