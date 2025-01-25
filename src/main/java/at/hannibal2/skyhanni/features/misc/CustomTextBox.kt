package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSBFeature
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings

@SkyHanniModule
object CustomTextBox {

    private val config get() = SkyHanniMod.feature.gui.customTextBox
    private var display = listOf<String>()

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        display = config.text.get().format()

        config.text.afterChange {
            display = format()
        }
    }

    private fun String.format() = replace("&", "§").split("\\n").toList()

    @HandleEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!config.onlyInGUI) return
        if (!isEnabled()) return

        config.position.renderStrings(display, posLabel = "Custom Text Box")
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (config.onlyInGUI) return
        if (!isEnabled()) return

        config.position.renderStrings(display, posLabel = "Custom Text Box")
    }

    private fun isEnabled() =
        (LorenzUtils.inSkyBlock || OutsideSBFeature.CUSTOM_TEXT_BOX.isSelected()) && config.enabled

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.textBox", "gui.customTextBox")
    }
}
