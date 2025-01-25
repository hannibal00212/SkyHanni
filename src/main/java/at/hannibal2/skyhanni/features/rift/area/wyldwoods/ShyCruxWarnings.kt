package at.hannibal2.skyhanni.features.rift.area.wyldwoods

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object ShyCruxWarnings {

    private val config get() = RiftApi.config.area.wyldWoods
    private val shyNames = arrayOf("I'm ugly! :(", "Eek!", "Don't look at me!", "Look away!")

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!RiftApi.inRift() || !config.shyWarning) return
        checkForShy()
    }

    private fun checkForShy() {
        if (EntityUtils.getAllEntities().any { it.name in shyNames && it.distanceToPlayer() < 8 }) {
            LorenzUtils.sendTitle("§eLook away!", 150.milliseconds)
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(9, "rift.area.wyldWoodsConfig", "rift.area.wyldWoods")
    }
}
