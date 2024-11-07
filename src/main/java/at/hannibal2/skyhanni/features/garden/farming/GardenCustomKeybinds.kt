package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.mixins.transformers.AccessorKeyBinding
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.IdentityHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenCustomKeybinds {

    private val config get() = GardenAPI.config.keyBind
    private val mcSettings get() = Minecraft.getMinecraft().gameSettings

    private val map: MutableMap<KeyBinding, () -> Int> = IdentityHashMap()
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastDuplicateKeybindsWarnTime = SimpleTimeMark.farPast()

    init {
        map[mcSettings.keyBindAttack] = config::attack
        map[mcSettings.keyBindUseItem] = config::useItem
        map[mcSettings.keyBindLeft] = config::left
        map[mcSettings.keyBindRight] = config::right
        map[mcSettings.keyBindForward] = config::forward
        map[mcSettings.keyBindBack] = config::back
        map[mcSettings.keyBindJump] = config::jump
        map[mcSettings.keyBindSneak] = config::sneak
    }

    private fun isEnabled() = GardenAPI.inGarden() && config.enabled && !(GardenAPI.onBarnPlot && config.excludeBarn)

    private fun isActive(): Boolean {
        if (!isEnabled()) return false
        if (GardenAPI.toolInHand == null) return false

        if (Minecraft.getMinecraft().currentScreen != null) {
            if (Minecraft.getMinecraft().currentScreen is GuiEditSign) {
                lastWindowOpenTime = SimpleTimeMark.now()
            }
            return false
        }

        // TODO remove workaround
        if (lastWindowOpenTime.passedSince() < 300.milliseconds) return false

        val areDuplicates = map.values
            .map { it() }
            .filter { it != Keyboard.KEY_NONE }
            .let { values -> values.size != values.toSet().size }
        if (areDuplicates) {
            if (lastDuplicateKeybindsWarnTime.passedSince() > 30.seconds) {
                ChatUtils.chatAndOpenConfig(
                    "Duplicate Custom Keybinds aren't allowed!",
                    GardenAPI.config::keyBind
                )
                lastDuplicateKeybindsWarnTime = SimpleTimeMark.now()
            }
            return false
        }

        return true
    }

    @JvmStatic
    fun isKeyDown(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = map[keyBinding] ?: return
        val keyCode = override()
        cir.returnValue = keyCode.isKeyHeld()
    }

    @JvmStatic
    fun onTick(keyCode: Int, ci: CallbackInfo) {
        if (!isActive()) return
        if (keyCode == 0) return
        val keyBinding = map.entries.firstOrNull { it.value() == keyCode }?.key ?: return
        ci.cancel()
        if (keyBinding.isKeyDown) {
            (keyBinding as AccessorKeyBinding).pressTime_skyhanni++
            KeyBinding.setKeyBindState(keyCode, true)
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.keyBindEnabled", "garden.keyBind.enabled")
        event.move(3, "garden.keyBindAttack", "garden.keyBind.attack")
        event.move(3, "garden.keyBindUseItem", "garden.keyBind.useItem")
        event.move(3, "garden.keyBindLeft", "garden.keyBind.left")
        event.move(3, "garden.keyBindRight", "garden.keyBind.right")
        event.move(3, "garden.keyBindForward", "garden.keyBind.forward")
        event.move(3, "garden.keyBindBack", "garden.keyBind.back")
        event.move(3, "garden.keyBindJump", "garden.keyBind.jump")
        event.move(3, "garden.keyBindSneak", "garden.keyBind.sneak")
    }
}
