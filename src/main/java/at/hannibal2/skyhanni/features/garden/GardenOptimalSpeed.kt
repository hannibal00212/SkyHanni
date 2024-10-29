package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ConfigUtils.jumpToEditor
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isRancherSign
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenOptimalSpeed {

    private val config get() = GardenAPI.config.optimalSpeeds

    private val configCustomSpeed get() = config.customSpeed
    private var sneakingSince = SimpleTimeMark.farFuture()
    private var sneakingTime = 0.seconds
    private val sneaking get() = Minecraft.getMinecraft().thePlayer.isSneaking
    private val sneakingPersistent get() = sneakingSince.passedSince() > 5.seconds
    private val rancherBoots = "RANCHERS_BOOTS".asInternalName()

    /**
     * This speed value represents the walking speed, not the speed stat.
     * Blocks per second = 4.317 * speed / 100
     *
     * It has an absolute speed cap of 500, and items that normally increase the cap do not apply here:
     * (Black Cat pet, Cactus knife, Racing Helmet or Young Dragon Armor)
     *
     * If this information ever gets abstracted away and made available outside this class,
     * and some features need the actual value of the Speed stat instead,
     * we can always just have two separate variables, like walkSpeed and speedStat.
     * But since this change is confined to Garden-specific code, it's fine the way it is for now.
     */
    private var currentSpeed = 100

    private var optimalSpeed: Int? = null
    private var lastWarnTime = SimpleTimeMark.farPast()
    private var cropInHand: CropType? = null
    private var lastCrop: CropType? = null
    private var display = listOf<Renderable>()
    private var lastToolSwitch = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!GardenAPI.inGarden()) return
        currentSpeed = (Minecraft.getMinecraft().thePlayer.capabilities.walkSpeed * 1000).toInt()

        if (sneaking && !sneakingSince.isInPast()) {
            sneakingSince = SimpleTimeMark.now()
            currentSpeed = (currentSpeed * 0.3).toInt()
        } else if (!sneaking && sneakingSince.isInPast()) {
            sneakingTime = 0.seconds
            sneakingSince = SimpleTimeMark.farFuture()
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!isRancherOverlayEnabled()) return
        val gui = event.gui as? GuiEditSign ?: return
        if (!gui.isRancherSign()) return

        val crops = CropType.entries.map { it to it.getOptimalSpeed() }

        display = if (config.compactRancherGui) {
            crops.groupBy({ it.second }, { it.first }).map { (speed, crops) ->
                val color = if (lastCrop in crops) LorenzColor.GOLD else LorenzColor.WHITE
                val renderable = Renderable.horizontalContainer(
                    listOf(
                        Renderable.horizontalContainer(crops.map { Renderable.itemStack(it.icon) }),
                        Renderable.string("${color.getChatColor()} - $speed"),
                    ),
                    spacing = 2,
                )
                Renderable.link(renderable, underlineColor = color.toColor(), onClick = { LorenzUtils.setTextIntoSign("$speed") })
            }
        } else {
            crops.map { (crop, speed) ->
                val color = if (lastCrop == crop) LorenzColor.GOLD else LorenzColor.WHITE
                val renderable = Renderable.horizontalContainer(
                    listOf(
                        Renderable.itemStack(crop.icon),
                        Renderable.string("${color.getChatColor()}${crop.cropName} - $speed"),
                    ),
                    spacing = 2,
                )
                Renderable.link(renderable, underlineColor = color.toColor(), onClick = { LorenzUtils.setTextIntoSign("$speed") })
            }
        }
    }

    @SubscribeEvent
    fun onGuiRender(event: DrawScreenEvent.Post) {
        if (!isRancherOverlayEnabled()) return
        val gui = event.gui as? GuiEditSign ?: return
        if (!gui.isRancherSign()) return
        config.signPosition.renderRenderables(
            display,
            posLabel = "Optimal Speed Rancher Overlay",
        )
    }

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        lastToolSwitch = SimpleTimeMark.now()
        cropInHand = event.crop
        event.crop?.let { lastCrop = it }
        optimalSpeed = cropInHand?.getOptimalSpeed()
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        for (value in CropType.entries) {
            ConditionalUtils.onToggle(value.getConfig()) {
                if (value == cropInHand) {
                    optimalSpeed = value.getOptimalSpeed()
                }
            }
        }
    }

    private fun CropType.getOptimalSpeed() = getConfig().get().toInt()

    private fun CropType.getConfig(): Property<Float> = with(configCustomSpeed) {
        when (this@getConfig) {
            CropType.WHEAT -> wheat
            CropType.CARROT -> carrot
            CropType.POTATO -> potato
            CropType.NETHER_WART -> netherWart
            CropType.PUMPKIN -> pumpkin
            CropType.MELON -> melon
            CropType.COCOA_BEANS -> cocoaBeans
            CropType.SUGAR_CANE -> sugarCane
            CropType.CACTUS -> cactus
            CropType.MUSHROOM -> mushroom
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!GardenAPI.inGarden()) return

        val speed = optimalSpeed ?: return

        if (GardenAPI.hideExtraGuis()) return

        var text = "Optimal Speed: §f$speed"
        if (speed != currentSpeed) {
            text += " (§eCurrent: §f$currentSpeed"
            if (sneaking) text += " §7[Sneaking]"
            text += "§f)"
        }

        val recentlySwitchedTool = lastToolSwitch.passedSince() < 1.5.seconds
        val recentlyStartedSneaking = sneaking && !sneakingPersistent

        val colorCode = if (recentlySwitchedTool || recentlyStartedSneaking) "7" else if (speed != currentSpeed) "c" else "a"

        if (config.showOnHUD) config.pos.renderString("§$colorCode$text", posLabel = "Garden Optimal Speed")
        if (speed != currentSpeed && !recentlySwitchedTool && !recentlyStartedSneaking) warn(speed)
    }

    private fun warn(optimalSpeed: Int) {
        if (!Minecraft.getMinecraft().thePlayer.onGround) return
        if (GardenAPI.onBarnPlot) return
        if (!config.warning) return
        if (!GardenAPI.isCurrentlyFarming()) return
        if (lastWarnTime.passedSince() < 20.seconds) return

        lastWarnTime = SimpleTimeMark.now()
        LorenzUtils.sendTitle("§cWrong speed!", 3.seconds)
        val cropInHand = cropInHand ?: return

        var text = "§cWrong speed while farming ${cropInHand.cropName} detected!" +
            "\n§eCurrent Speed: §f$currentSpeed§e, Optimal Speed: §f$optimalSpeed"
        
        if (InventoryUtils.getBoots()?.getInternalNameOrNull() == rancherBoots) {
            ChatUtils.clickToActionOrDisable(
                text,
                config::warning,
                actionName = "change the speed",
                action = { HypixelCommands.setMaxSpeed() },
            )
        } else {
            ChatUtils.clickableChat(
                text,
                onClick = { config::warning.jumpToEditor() },
                hover = "Click to disable this feature!",
            )
        }
    }

    private fun isRancherOverlayEnabled() = GardenAPI.inGarden() && config.signEnabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.optimalSpeedEnabled", "garden.optimalSpeeds.enabled")
        event.move(3, "garden.optimalSpeedWarning", "garden.optimalSpeeds.warning")
        event.move(3, "garden.optimalSpeedSignEnabled", "garden.optimalSpeeds.signEnabled")
        event.move(3, "garden.optimalSpeedSignPosition", "garden.optimalSpeeds.signPosition")
        event.move(3, "garden.optimalSpeedPos", "garden.optimalSpeeds.pos")
        event.move(3, "garden.optimalSpeedCustom.wheat", "garden.optimalSpeeds.customSpeed.wheat")
        event.move(3, "garden.optimalSpeedCustom.carrot", "garden.optimalSpeeds.customSpeed.carrot")
        event.move(3, "garden.optimalSpeedCustom.potato", "garden.optimalSpeeds.customSpeed.potato")
        event.move(3, "garden.optimalSpeedCustom.netherWart", "garden.optimalSpeeds.customSpeed.netherWart")
        event.move(3, "garden.optimalSpeedCustom.pumpkin", "garden.optimalSpeeds.customSpeed.pumpkin")
        event.move(3, "garden.optimalSpeedCustom.melon", "garden.optimalSpeeds.customSpeed.melon")
        event.move(3, "garden.optimalSpeedCustom.cocoaBeans", "garden.optimalSpeeds.customSpeed.cocoaBeans")
        event.move(3, "garden.optimalSpeedCustom.sugarCane", "garden.optimalSpeeds.customSpeed.sugarCane")
        event.move(3, "garden.optimalSpeedCustom.cactus", "garden.optimalSpeeds.customSpeed.cactus")
        event.move(3, "garden.optimalSpeedCustom.mushroom", "garden.optimalSpeeds.customSpeed.mushroom")

        event.move(14, "garden.optimalSpeeds.enabled", "garden.optimalSpeeds.showOnHUD")
    }
}
