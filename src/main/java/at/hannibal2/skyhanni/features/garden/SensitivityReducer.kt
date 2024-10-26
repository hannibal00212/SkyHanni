package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.SensitivityReducerConfig
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.HypixelJoinEvent
import at.hannibal2.skyhanni.events.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.misc.LockMouseLook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SensitivityReducer {
    private val config get() = SkyHanniMod.feature.garden.sensitivityReducerConfig
    private val storage get() = SkyHanniMod.feature.storage
    private var isToggled = false
    private var isManualToggle = false
    private var lastCheckCooldown = SimpleTimeMark.farPast()
    private const val LOCKED = -1F / 3F

    private val mc get() = Minecraft.getMinecraft()
    private val gameSettings = mc.gameSettings

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!GardenAPI.inGarden()) {
            if (isToggled && lastCheckCooldown.passedSince() > 1.seconds) {
                lastCheckCooldown = SimpleTimeMark.now()
                isToggled = false
                restoreSensitivity()
            }
            return
        }
        if (isManualToggle) return
        if (isToggled && config.onGround.get() && !mc.thePlayer.onGround) {
            restoreSensitivity()
            isToggled = false
            return
        }
        when (config.mode) {
            SensitivityReducerConfig.Mode.OFF -> {
                if (isToggled) toggle(false)
                return
            }

            SensitivityReducerConfig.Mode.TOOL -> {
                if (isHoldingTool() && !isToggled) toggle(true)
                else if (isToggled && !isHoldingTool()) toggle(false)
            }

            SensitivityReducerConfig.Mode.KEYBIND -> {
                if (isHoldingKey() && !isToggled) toggle(true)
                else if (isToggled && !isHoldingKey()) toggle(false)
            }

            else -> return
        }
        if (isToggled && lastCheckCooldown.passedSince() > 1.seconds) {
            if (GardenAPI.onBarnPlot && config.onlyPlot.get()) {
                isToggled = false
                restoreSensitivity()
            }
            if (!mc.thePlayer.onGround && config.onGround.get()) {
                isToggled = false
                restoreSensitivity()
            }
            lastCheckCooldown = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.reducingFactor.afterChange {
            reloadSensitivity()
        }
        config.onlyPlot.afterChange {
            if (isToggled && config.onlyPlot.get() && GardenAPI.onBarnPlot) {
                restoreSensitivity()
                isToggled = false
            }
        }
        config.onGround.afterChange {
            if (isToggled && config.onGround.get() && mc.thePlayer.onGround) {
                restoreSensitivity()
                isToggled = false
            }
        }
    }

    private fun reloadSensitivity() {
        if (isToggled || isManualToggle) {
            restoreSensitivity()
            lowerSensitivity()
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!(isToggled || isManualToggle)) return
        if (!config.showGUI) return
        if (LockMouseLook.lockedMouse) return
        config.position.renderString("§eSensitivity Lowered", posLabel = "Sensitivity Lowered")
    }

    private fun isHoldingTool(): Boolean {
        return GardenAPI.toolInHand != null
    }

    private fun isHoldingKey(): Boolean {
        return config.keybind.isKeyHeld() && mc.currentScreen == null
    }

    fun isEnabled(): Boolean {
        return isToggled || isManualToggle
    }

    fun manualToggle() {
        if (isToggled) {
            ChatUtils.userError("This command is disabled while the Sensitivity is lowered!")
            return
        }
        isManualToggle = !isManualToggle
        if (isManualToggle) {
            lowerSensitivity(true)
        } else restoreSensitivity(true)
    }

    private fun lowerSensitivity(showMessage: Boolean = false) {
        val divisor = config.reducingFactor.get()
        ChatUtils.debug("dividing by $divisor")

        if (!LockMouseLook.lockedMouse) {
            storage.savedMouseloweredSensitivity = gameSettings.mouseSensitivity
            val newSens = doTheMath(storage.savedMouseloweredSensitivity)
            gameSettings.mouseSensitivity = newSens
        } else {
            storage.savedMouseloweredSensitivity = storage.savedMouselockedSensitivity
        }
        if (showMessage) ChatUtils.chat("§bMouse sensitivity is now lowered. Type /shsensreduce to restore your sensitivity.")
    }

    private fun restoreSensitivity(showMessage: Boolean = false) {
        if (!LockMouseLook.lockedMouse) gameSettings.mouseSensitivity = storage.savedMouseloweredSensitivity
        if (showMessage) ChatUtils.chat("§bMouse sensitivity is now restored.")
    }

    private fun toggle(state: Boolean) {
        if (config.onlyPlot.get() && GardenAPI.onBarnPlot) return
        if (config.onGround.get() && !mc.thePlayer.onGround) return
        if (!isToggled) {
            lowerSensitivity()
        } else restoreSensitivity()
        isToggled = state
    }

    fun doTheMath(input: Float, reverse: Boolean = false): Float {
        val divisor = config.reducingFactor.get()
        return if (!reverse) ((input - LOCKED) / divisor) + LOCKED
        else (divisor * (input - LOCKED)) + LOCKED
    }

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        val divisor = config.reducingFactor.get()
        val expectedLoweredSensitivity = doTheMath(gameSettings.mouseSensitivity, true)
        if (abs(storage.savedMouseloweredSensitivity - expectedLoweredSensitivity) <= 0.0001) {
            ChatUtils.debug("Fixing incorrectly lowered sensitivity")
            isToggled = false
            isManualToggle = false
            restoreSensitivity()
        }
    }

    @HandleEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Garden Sensitivity Reducer")

        if (!GardenAPI.inGarden()) {
            event.addIrrelevant("not in garden")
            return
        }

        if (config.mode == SensitivityReducerConfig.Mode.OFF) {
            event.addIrrelevant("disabled in config")
            return
        }

        event.addData {
            add("Current Sensitivity: ${gameSettings.mouseSensitivity}")
            add("Stored Sensitivity: ${storage.savedMouseloweredSensitivity}")
            add("onGround: ${mc.thePlayer.onGround}")
            add("onBarn: ${GardenAPI.onBarnPlot}")
            add("enabled: ${isToggled || isManualToggle}")
            add("--- config ---")
            add("mode: ${config.mode.name}")
            add("Current Divisor: ${config.reducingFactor.get()}")
            add("Keybind: ${config.keybind}")
            add("onlyGround: ${config.onGround.get()}")
            add("onlyPlot: ${config.onlyPlot.get()}")
        }
    }
}
