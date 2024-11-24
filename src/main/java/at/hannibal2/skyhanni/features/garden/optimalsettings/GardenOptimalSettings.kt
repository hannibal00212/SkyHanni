package at.hannibal2.skyhanni.features.garden.optimalsettings

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isMousematSign
import at.hannibal2.skyhanni.utils.LorenzUtils.isRancherSign
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.renderables.Renderable
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenOptimalSettings {

    private val config get() = GardenAPI.config.optimalSettings

    private val configCustomSettings get() = config.customSettings
    private var sneakingSince = SimpleTimeMark.farFuture()
    private var sneakingTime = 0.seconds
    private val sneaking get() = Minecraft.getMinecraft().thePlayer.isSneaking
    private val sneakingPersistent get() = sneakingSince.passedSince() > 5.seconds
    private val rancherBoots = "RANCHERS_BOOTS".toInternalName()
    private val mousemat = "SQUEAKY_MOUSEMAT".toInternalName()

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
    private var currentYaw = 0f
    private var currentPitch = 0f

    private var optimalSpeed: Int? = null
    private var optimalYaw: Float? = null
    private var optimalPitch: Float? = null

    private var lastWarnSpeed = SimpleTimeMark.farPast()
    private var lastWarnYaw = SimpleTimeMark.farPast()
    private var lastWarnPitch = SimpleTimeMark.farPast()

    private var cropInHand: CropType? = null
    private var lastCrop: CropType? = null
    private var display = listOf<Renderable>()
    private var lastToolSwitch = SimpleTimeMark.farPast()

    private data class CropSettings(val speed: Int, val yaw: Float, val pitch: Float)
    private data class CropPropertys(val speed: Property<Float>, val yaw: Property<Float>, val pitch: Property<Float>)

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!GardenAPI.inGarden()) return
        val player = Minecraft.getMinecraft().thePlayer

        currentSpeed = (player.capabilities.walkSpeed * 1000).toInt()
        currentYaw = player.rotationYaw.roundTo(2)
        currentPitch = player.rotationPitch.roundTo(2)

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
        if (!isShortcutGUIEnabled()) return
        val gui = event.gui as? GuiEditSign ?: return
        if (!gui.isRancherSign() && !gui.isMousematSign()) return

        display = createDisplay(gui)
    }

    private fun createDisplay(gui: GuiEditSign): List<Renderable> {
        val crops = CropType.entries.map { it to it.getOptimalSettings() }
        val isRancher = gui.isRancherSign()

        return if (config.compactShortcutGUI) {
            crops.groupBy(
                { if (isRancher) it.second.speed to 0 else it.second.yaw to it.second.pitch },
                { it.first },
            ).map { (settings, crops) ->
                val color = if (lastCrop in crops) LorenzColor.GOLD else LorenzColor.WHITE
                val label = if (isRancher) "${settings.first}" else "${settings.first}°/${settings.second}°"

                Renderable.link(
                    Renderable.horizontalContainer(
                        listOf(
                            Renderable.horizontalContainer(crops.map { Renderable.itemStack(it.icon) }),
                            Renderable.string("${color.getChatColor()} - $label"),
                        ),
                        spacing = 2,
                    ),
                    underlineColor = color.toColor(),
                    onClick = {
                        LorenzUtils.setTextIntoSign("${settings.first}")
                        if (!isRancher) LorenzUtils.setTextIntoSign("${settings.second}", line = 3)
                    },
                )
            }
        } else {
            crops.map { (crop, settings) ->
                val color = if (lastCrop == crop) LorenzColor.GOLD else LorenzColor.WHITE
                val label = if (isRancher) "${settings.speed}" else "${settings.yaw}°/${settings.pitch}°"

                Renderable.link(
                    Renderable.horizontalContainer(
                        listOf(
                            Renderable.itemStack(crop.icon),
                            Renderable.string("${color.getChatColor()}${crop.cropName} - $label"),
                        ),
                        spacing = 2,
                    ),
                    underlineColor = color.toColor(),
                    onClick = {
                        LorenzUtils.setTextIntoSign(
                            if (isRancher) "${settings.speed}" else "${settings.yaw}",
                        )
                        if (!isRancher) LorenzUtils.setTextIntoSign("${settings.pitch}", line = 3)
                    },
                )
            }
        }
    }

    @SubscribeEvent
    fun onGuiRender(event: DrawScreenEvent.Post) {
        if (!isShortcutGUIEnabled()) return
        val gui = event.gui as? GuiEditSign ?: return
        if (!gui.isRancherSign() && !gui.isMousematSign()) return

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

        val optimalSettings = cropInHand?.getOptimalSettings()

        optimalSpeed = optimalSettings?.speed
        optimalYaw = optimalSettings?.yaw
        optimalPitch = optimalSettings?.pitch
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        for (value in CropType.entries) {
            val propertys = value.getConfig()

            listOf(
                propertys.speed to { optimalSpeed = propertys.speed.get().toInt() },
                propertys.yaw to { optimalYaw = propertys.yaw.get() },
                propertys.pitch to { optimalPitch = propertys.pitch.get() },
            ).forEach { (property, action) ->
                ConditionalUtils.onToggle(property) {
                    if (value == cropInHand) action()
                }
            }
        }
    }

    private fun CropType.getOptimalSettings(): CropSettings = with(getConfig()) {
        return CropSettings(
            speed = this.speed.get().toInt(),
            yaw = this.yaw.get(),
            pitch = this.pitch.get(),
        )
    }

    private fun CropType.getConfig(): CropPropertys = with(configCustomSettings) {
        when (this@getConfig) {
            CropType.WHEAT -> CropPropertys(wheat.speed, wheat.yaw, wheat.pitch)
            CropType.CARROT -> CropPropertys(carrot.speed, carrot.yaw, carrot.pitch)
            CropType.POTATO -> CropPropertys(potato.speed, potato.yaw, potato.pitch)
            CropType.NETHER_WART -> CropPropertys(netherWart.speed, netherWart.yaw, netherWart.pitch)
            CropType.PUMPKIN -> CropPropertys(pumpkin.speed, pumpkin.yaw, pumpkin.pitch)
            CropType.MELON -> CropPropertys(melon.speed, melon.yaw, melon.pitch)
            CropType.COCOA_BEANS -> CropPropertys(cocoaBeans.speed, cocoaBeans.yaw, cocoaBeans.pitch)
            CropType.SUGAR_CANE -> CropPropertys(sugarCane.speed, sugarCane.yaw, sugarCane.pitch)
            CropType.CACTUS -> CropPropertys(cactus.speed, cactus.yaw, cactus.pitch)
            CropType.MUSHROOM -> CropPropertys(mushroom.speed, mushroom.yaw, mushroom.pitch)
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!GardenAPI.inGarden() || GardenAPI.hideExtraGuis()) return

        val speed = optimalSpeed ?: return
        val yaw = optimalYaw ?: return
        val pitch = optimalPitch ?: return

        val recentlySwitchedTool = lastToolSwitch.passedSince() < 1.5.seconds
        val recentlyStartedSneaking = sneaking && !sneakingPersistent

        val (speedColor, yawColor, pitchColor) = listOf(
            if (recentlySwitchedTool || recentlyStartedSneaking) "7" else if (speed != currentSpeed) "c" else "a",
            if (recentlySwitchedTool) "7" else if (yaw != currentYaw) "c" else "a",
            if (recentlySwitchedTool) "7" else if (pitch != currentPitch) "c" else "a"
        )

        val list = Renderable.verticalContainer(
            listOf(
                buildStatusString("Speed", speedColor, speed, currentSpeed, recentlySwitchedTool, recentlyStartedSneaking),
                buildStatusString("Yaw", yawColor, yaw, currentYaw, recentlySwitchedTool),
                buildStatusString("Pitch", pitchColor, pitch, currentPitch, recentlySwitchedTool)
            )
        )

        if (config.showOnHUD) config.pos.renderRenderable(list, posLabel = "Garden Optimal Settings")

        if (!recentlySwitchedTool) {
            if (speed != currentSpeed && !recentlyStartedSneaking) warnSpeed(speed)
            if (yaw != currentYaw) warnYaw(yaw)
            if (pitch != currentPitch) warnPitch(pitch)
        }
    }

    private fun buildStatusString(type: String, color: String, value: Any, currentValue: Any, recentlySwitchedTool: Boolean, recentlyStartedSneaking: Boolean = false): Renderable {
        var statusString = "§${color}Optimal $type: §f$value"
        if (value != currentValue) {
            statusString += " (§eCurrent: §f$currentValue"
            if (sneaking) statusString += " §7[Sneaking]"
            statusString += "§f)"
        }
        return Renderable.string(statusString)
    }

    private fun warnText(type: String, current: String, optimal: String): String {
        LorenzUtils.sendTitle("§cWrong Settings! Fix them in chat.", 3.seconds)
        val cropInHand = cropInHand ?: return ""
        return "§cWrong $type while farming ${cropInHand.cropName} detected!" +
            "§eCurrent ${type.firstLetterUppercase()}: §f$current§e, Optimal ${type.firstLetterUppercase()}: §f$optimal"
    }

    private fun warnSpeed(optimalSpeed: Int) {
        if (!isEligibleForWarn() || lastWarnSpeed.passedSince() < 20.seconds) return
        if (InventoryUtils.getBoots()?.getInternalNameOrNull() != rancherBoots) return
        lastWarnSpeed = SimpleTimeMark.now()

        ChatUtils.clickToActionOrDisable(
            warnText("speed", "✦ $currentSpeed", "✦ $optimalSpeed"),
            config::warning,
            actionName = "change the speed",
            action = { HypixelCommands.setMaxSpeed(optimalSpeed) }
        )
    }

    private fun warnYaw(optimalYaw: Float) {
        if (!isEligibleForWarn() || lastWarnYaw.passedSince() < 20.seconds) return
        lastWarnYaw = SimpleTimeMark.now()

        ChatUtils.clickToActionOrDisable(
            warnText("yaw", "$currentYaw°", "$optimalYaw°"),
            config::warning,
            actionName = "change the yaw",
            action = { HypixelCommands.setYaw(optimalYaw) }
        )
    }

    private fun warnPitch(optimalPitch: Float) {
        if (!isEligibleForWarn() || lastWarnPitch.passedSince() < 20.seconds) return
        lastWarnPitch = SimpleTimeMark.now()

        ChatUtils.clickToActionOrDisable(
            warnText("pitch", "$currentPitch°", "$optimalPitch°"),
            config::warning,
            actionName = "change the pitch",
            action = { HypixelCommands.setPitch(optimalPitch) }
        )
    }

    private fun isEligibleForWarn() =
        Minecraft.getMinecraft().thePlayer.onGround && !GardenAPI.onBarnPlot && config.warning && GardenAPI.isCurrentlyFarming()

    private fun isShortcutGUIEnabled() = GardenAPI.inGarden() && config.shortcutGUI

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
