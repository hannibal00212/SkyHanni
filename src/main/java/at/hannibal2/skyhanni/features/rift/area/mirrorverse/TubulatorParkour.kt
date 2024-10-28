package at.hannibal2.skyhanni.features.rift.area.mirrorverse

import at.hannibal2.skyhanni.data.jsonobjects.repo.ParkourJson
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.ParkourHelper
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TubulatorParkour {

    private val config get() = RiftAPI.config.area.mirrorverse.tubulatorConfig
    private var parkourHelper: ParkourHelper? = null
    private val puzzleRoom = AxisAlignedBB(-298.0, 0.0, -112.0, -309.0, 63.0, -101.0)

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ParkourJson>("RiftTubulator")
        parkourHelper = ParkourHelper(
            data.locations,
            data.shortCuts,
            platformSize = 1.0,
            detectionRange = 2.0,
        )
        updateConfig()
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!isEnabled()) return
        if (!config.hidePlayers) return

        parkourHelper?.let {
            if (it.inParkour()) {
                event.cancel()
            }
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.rainbowColor, config.monochromeColor, config.lookAhead) {
            updateConfig()
        }
    }

    private fun updateConfig() {
        parkourHelper?.run {
            rainbowColor = config.rainbowColor.get()
            monochromeColor = config.monochromeColor.get().toChromaColor()
            lookAhead = config.lookAhead.get() + 1
            outline = config.outline
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        parkourHelper?.render(event)
    }

    fun isEnabled() =
        RiftAPI.inRift() && RiftAPI.inMirrorVerse && config.enabled && puzzleRoom.isPlayerInside()
}
