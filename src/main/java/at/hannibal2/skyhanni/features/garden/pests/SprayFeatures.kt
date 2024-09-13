package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.renderPlot
import at.hannibal2.skyhanni.features.garden.pests.PestAPI.getPests
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SprayFeatures {

    private val config get() = PestAPI.config.spray

    private var display: String? = null
    private var lastChangeTime = SimpleTimeMark.farPast()

    private val changeMaterialPattern by RepoPattern.pattern(
        "garden.spray.material",
        "§a§lSPRAYONATOR! §r§7Your selected material is now §r§a(?<spray>.*)§r§7!",
    )

    private fun SprayType?.getSprayEffect(): String =
        this?.getPests()?.takeIf { it.isNotEmpty() }?.let { pests ->
            pests.joinToString("§7, §6") { it.displayName }
        } ?: when (this) {
            SprayType.FINE_FLOUR -> "§6+20☘ Farming Fortune"
            else -> "§cUnknown Effect"
        }


    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        display = changeMaterialPattern.matchMatcher(event.message) {
            val sprayName = group("spray")
            val type = SprayType.getByName(sprayName)
            val sprayEffect = type.getSprayEffect()
            "§a${type?.displayName ?: sprayName} §7(§6$sprayEffect§7)"
        } ?: return

        lastChangeTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        val display = display ?: return

        if (lastChangeTime.passedSince() > 5.seconds) {
            this.display = null
            return
        }

        config.position.renderString(display, posLabel = "Pest Spray Selector")
    }

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.drawPlotsBorderWhenInHands) return
        if (!InventoryUtils.itemInHandId.equals("SPRAYONATOR")) return
        val plot = GardenPlotAPI.getCurrentPlot() ?: return
        event.renderPlot(plot, LorenzColor.YELLOW.toColor(), LorenzColor.DARK_BLUE.toColor())
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.pestWhenSelector
}
