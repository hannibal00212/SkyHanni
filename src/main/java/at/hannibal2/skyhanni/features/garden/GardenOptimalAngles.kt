package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SignUtils
import at.hannibal2.skyhanni.utils.SignUtils.isMousematSign
import at.hannibal2.skyhanni.utils.renderables.Renderable
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GardenOptimalAngles {

    private val config get() = GardenAPI.config.optimalAngles

    private val configCustomAngles get() = config.customAngles

    private var cropInHand: CropType? = null
    private var lastCrop: CropType? = null
    private var display = listOf<Renderable>()

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!isSqueakyMousematEnabled()) return
        val gui = event.gui as? GuiEditSign ?: return
        if (!gui.isMousematSign()) return

        val crops = CropType.entries.map { it to it.getAngles() }

        display = if (config.compactMousematGui) {
            crops.groupBy({ it.second }, { it.first }).map { (angles, crops) ->
                val color = if (lastCrop in crops) LorenzColor.GOLD else LorenzColor.WHITE
                val renderable = Renderable.horizontalContainer(
                    listOf(
                        Renderable.horizontalContainer(crops.map { Renderable.itemStack(it.icon) }),
                        Renderable.string("${color.getChatColor()} - ${angles.first}/${angles.second}"),
                    ),
                    spacing = 2,
                )
                Renderable.link(renderable, underlineColor = color.toColor(), onClick = { setAngles(angles) })
            }
        } else {
            crops.map { (crop, angles) ->
                val color = if (lastCrop == crop) LorenzColor.GOLD else LorenzColor.WHITE
                val renderable = Renderable.horizontalContainer(
                    listOf(
                        Renderable.itemStack(crop.icon),
                        Renderable.string("${color.getChatColor()}${crop.cropName} - ${angles.first}/${angles.second}"),
                    ),
                    spacing = 2,
                )
                Renderable.link(renderable, underlineColor = color.toColor(), onClick = { setAngles(angles) })
            }
        }
    }

    @SubscribeEvent
    fun onGuiRender(event: DrawScreenEvent.Post) {
        if (!isSqueakyMousematEnabled()) return
        val gui = event.gui as? GuiEditSign ?: return
        if (!gui.isMousematSign()) return
        config.signPosition.renderRenderables(
            display,
            posLabel = "Optimal Angles Mousemat Overlay",
        )
    }

    @HandleEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        cropInHand = event.crop
        event.crop?.let { lastCrop = it }
    }

    private fun setAngles(angles: Pair<Float, Float>) {
        SignUtils.setTextIntoSign("${angles.first}", 0)
        SignUtils.setTextIntoSign("${angles.second}", 3)
    }

    private fun CropType.getAngles() = getConfig().let { Pair(it.first.get(), it.second.get()) }

    private fun CropType.getConfig(): Pair<Property<Float>, Property<Float>> = with(configCustomAngles) {
        when (this@getConfig) {
            CropType.CACTUS -> Pair(cactusYaw, cactusPitch)
            CropType.WHEAT -> Pair(wheatYaw, wheatPitch)
            CropType.CARROT -> Pair(carrotYaw, carrotPitch)
            CropType.POTATO -> Pair(potatoYaw, potatoPitch)
            CropType.NETHER_WART -> Pair(netherWartYaw, netherWartPitch)
            CropType.PUMPKIN -> Pair(pumpkinYaw, pumpkinPitch)
            CropType.MELON -> Pair(melonYaw, melonPitch)
            CropType.COCOA_BEANS -> Pair(cocoaBeansYaw, cocoaBeansPitch)
            CropType.SUGAR_CANE -> Pair(sugarCaneYaw, sugarCanePitch)
            CropType.MUSHROOM -> Pair(mushroomYaw, mushroomPitch)
        }

    }

    private fun isSqueakyMousematEnabled() = GardenAPI.inGarden() && config.signEnabled
}
