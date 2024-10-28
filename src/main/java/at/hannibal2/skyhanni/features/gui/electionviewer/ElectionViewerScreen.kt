package at.hannibal2.skyhanni.features.gui.electionviewer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.MayorAPI
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

abstract class ElectionViewerScreen : GuiScreen() {

    private val scaledResolution get() = ScaledResolution(Minecraft.getMinecraft())
    private val windowWidth get() = scaledResolution.scaledWidth
    private val windowHeight get() = scaledResolution.scaledHeight

    private val guiWidth get() = (windowWidth * 0.65f).toInt()
    private val guiHeight get() = (windowHeight * 0.65f).toInt()

    private val padding = 10

    open val posLabel = ""

    /**
     * The main content of the display.
     *
     * Should **not** include the background Renderable.
     */
    var display: Renderable? = null

    @SubscribeEvent
    open fun onOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isInGui()) return

        display?.let {
            renderContent(it)
            renderButtons()
        }
    }

    @SubscribeEvent
    open fun onSecondPassed(event: SecondPassedEvent) {
        if (!isInGui()) return
        updateDisplay()
    }

    override fun initGui() {
        super.initGui()
        updateDisplay()
    }

    /**
     * Generates the content of the display.
     * The content should **not** include the background Renderable.
     *
     * This method is called every second and when the GUI is opened.
     */
    abstract fun updateDisplay()

    abstract fun isInGui(): Boolean

    private fun createButtons(): Renderable {
        val currentScreen = Minecraft.getMinecraft().currentScreen

        val mayorButton = createButton(
            "Mayor",
            currentScreen is CurrentMayorScreen,
        ) { SkyHanniMod.screenToOpen = CurrentMayorScreen }

        val electionButton = if (ElectionAPI.rawMayorData?.current != null) {
            createButton(
                "Election",
                currentScreen is CurrentElectionScreen,
            ) { SkyHanniMod.screenToOpen = CurrentElectionScreen }
        } else null

        val specialButton = createButton(
            "Special",
            currentScreen is SpecialMayorScreen,
        ) { SkyHanniMod.screenToOpen = SpecialMayorScreen }

        return Renderable.verticalContainer(
            listOfNotNull(mayorButton, electionButton, specialButton),
            spacing = 10,
            verticalAlign = RenderUtils.VerticalAlignment.CENTER,
            horizontalAlign = RenderUtils.HorizontalAlignment.RIGHT,
        )
    }

    private fun createButton(label: String, isActive: Boolean, onClick: () -> Unit) =
        Renderable.clickable(
            Renderable.drawInsideRoundedRect(
                Renderable.string(label, horizontalAlign = RenderUtils.HorizontalAlignment.CENTER),
                if (isActive) Color.GRAY.addAlpha(200) else Color.DARK_GRAY.addAlpha(200),
                padding = 7,
                horizontalAlign = RenderUtils.HorizontalAlignment.RIGHT,
            ),
            onClick = onClick,
            bypassChecks = true,
        )

    private fun renderContent(renderable: Renderable) {
        Position(
            windowWidth / 2 - guiWidth / 2 - padding,
            windowHeight / 2 - guiHeight / 2 - padding,
        ).renderRenderable(
            Renderable.drawInsideRoundedRect(
                Renderable.doubleLayered(
                    Renderable.placeholder(guiWidth, guiHeight),
                    renderable,
                ),
                Color.BLACK.addAlpha(180),
                padding = padding,
            ),
            posLabel = posLabel,
            addToGuiManager = false,
        )
    }

    private fun renderButtons() {
        val buttons = createButtons()
        Position(
            windowWidth / 2 - guiWidth / 2 - padding * 2 - buttons.width,
            windowHeight / 2,
        ).renderRenderable(
            buttons,
            posLabel = "Election Viewer - Buttons",
            addToGuiManager = false,
        )
    }
}
