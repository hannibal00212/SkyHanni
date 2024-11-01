package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXYAligned
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GuardianReminder {

    private val config get() = SkyHanniMod.feature.inventory.experimentationTable
    private var lastInventoryOpen = SimpleTimeMark.farPast()
    private var lastErrorSound = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onInventory(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (event.inventoryName != "Experimentation Table") return
        lastInventoryOpen = SimpleTimeMark.now()

        DelayedRun.runDelayed(200.milliseconds, ::warn)
    }

    private fun warn() {
        if (ExperimentationTableAPI.hasGuardianPet()) return

        ChatUtils.clickToActionOrDisable(
            "Use a §9§lGuardian Pet §efor more Exp in the Experimentation Table.",
            config::guardianReminder,
            actionName = "open pets menu",
            action = { HypixelCommands.pet() },
        )
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (InventoryUtils.openInventoryName() != "Experimentation Table") return
        if (lastInventoryOpen.passedSince() > 2.seconds) return
        if (ExperimentationTableAPI.hasGuardianPet()) return
        val gui = Minecraft.getMinecraft().currentScreen as? GuiContainer ?: return

        sendTitle(gui.width, gui.height)
        if (lastErrorSound.passedSince() > 200.milliseconds) {
            lastErrorSound = SimpleTimeMark.now()
            SoundUtils.playPlingSound()
        }
    }

    // TODO rename to "send title in inventory", move to utils
    private fun sendTitle(width: Int, height: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, -150f, 500f)
        Renderable.drawInsideRoundedRect(
            Renderable.string("§cWrong Pet equipped!", 1.5),
            Color(Color.DARK_GRAY.withAlpha(0), true),
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
            verticalAlign = RenderUtils.VerticalAlignment.CENTER,
        ).renderXYAligned(0, 125, width, height)

        GlStateManager.translate(0f, 150f, -500f)
        GlStateManager.popMatrix()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(59, "inventory.helper.enchanting.guardianReminder", "inventory.experimentationTable.guardianReminder")
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.guardianReminder
}
