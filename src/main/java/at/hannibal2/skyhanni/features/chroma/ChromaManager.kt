package at.hannibal2.skyhanni.features.chroma

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.chroma.ChromaConfig
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

@SkyHanniModule
object ChromaManager {

    val config get() = SkyHanniMod.feature.gui.chroma

    @JvmStatic
    fun resetChromaSettings() {
        with(config) {
            chromaSize = 30f
            chromaSpeed = 6f
            chromaSaturation = 0.75f
            allChroma = false
            ignoreChat = false
            chromaDirection = ChromaConfig.Direction.FORWARD_RIGHT
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(31, "chroma", "gui.chroma")
    }
}
