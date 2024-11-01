package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.mining.eventtracker.MiningEventType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SkullTextureHolder {

    private var skullTextures = mutableMapOf<String, String>()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        skullTextures = event.getConstant<Map<String, String>>("Skulls").toMutableMap()
        fixLateInits()
    }

    fun getTexture(name: String): String {
        return skullTextures[name].orEmpty()
    }

    // Any classes that rely on textures that cannot make use of by lazy or other late initializers
    private fun fixLateInits() {
        MiningEventType.fixGoblinItemStack()
    }
}
