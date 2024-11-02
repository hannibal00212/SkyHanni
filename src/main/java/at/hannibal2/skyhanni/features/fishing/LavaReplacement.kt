package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object LavaReplacement {

    private val config get() = SkyHanniMod.feature.fishing.lavaReplacement

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.enabled, config.onlyInCrimsonIsle) {
            Minecraft.getMinecraft().renderGlobal.loadRenderers()
        }
    }

    @JvmStatic
    fun replaceLava(): Boolean {
        if (!LorenzUtils.inSkyBlock || !config.enabled.get()) return false
        if (config.onlyInCrimsonIsle.get() && !IslandType.CRIMSON_ISLE.isInIsland()) return false
        return true
    }
}
