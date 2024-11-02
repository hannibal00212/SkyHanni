package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object LavaReplacement {

    private val config get() = SkyHanniMod.feature.fishing.lavaReplacement

    @JvmStatic
    var isActive: Boolean = false
        private set

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        update()
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.enabled, config.everywhere, config.islands) {
            update()
        }
    }

    private fun update() {
        val newActive = shouldReplace()
        if (newActive == isActive) return
        isActive = newActive
        if (isActive) Minecraft.getMinecraft().renderGlobal.loadRenderers()
    }

    private fun shouldReplace(): Boolean {
        if (!LorenzUtils.inSkyBlock || !config.enabled.get()) return false
        if (config.everywhere.get()) return true
        return config.islands.get().any(IslandsToReplace::inIsland)
    }

    enum class IslandsToReplace(private val displayName: String, val island: IslandType) {
        KUUDRA("§4Kuudra", IslandType.KUUDRA_ARENA),
        CATACOMBS("§2Dungeons", IslandType.CATACOMBS),
        CRIMSON_ISLE("§cCrimson Isle", IslandType.CRIMSON_ISLE),
        ;

        override fun toString() = displayName

        fun inIsland() = island.isInIsland()
    }
    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(65, "fishing.lavaReplacement.onlyInCrimsonIsle", "fishing.lavaReplacement.everywhere") { element ->
            JsonPrimitive(!element.asBoolean)
        }
        event.move(65, "fishing.lavaReplacement.onlyInCrimsonIsle", "fishing.lavaReplacement.islands") { element ->
            JsonArray().apply { if (element.asBoolean) add(JsonPrimitive(IslandsToReplace.CRIMSON_ISLE.name)) }
        }
    }
}
