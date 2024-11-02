package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import io.github.notenoughupdates.moulconfig.internal.KeybindHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.lang3.SystemUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

@SkyHanniModule
object KeyboardManager {

    private var lastClickedMouseButton = -1

    // A mac-only key, represents Windows key on windows (but different key code)
    private fun isCommandKeyDown() = Keyboard.KEY_LMETA.isKeyHeld() || Keyboard.KEY_RMETA.isKeyHeld()

    // Windows: Alt key Mac: Option key
    private fun isMenuKeyDown() = Keyboard.KEY_LMENU.isKeyHeld() || Keyboard.KEY_RMENU.isKeyHeld()

    private fun isControlKeyDown() = Keyboard.KEY_LCONTROL.isKeyHeld() || Keyboard.KEY_RCONTROL.isKeyHeld()

    fun isDeleteWordDown() =
        Keyboard.KEY_BACK.isKeyHeld() && if (SystemUtils.IS_OS_MAC) isMenuKeyDown() else isControlKeyDown()

    fun isDeleteLineDown() =
        Keyboard.KEY_BACK.isKeyHeld() && if (SystemUtils.IS_OS_MAC) isCommandKeyDown() else isControlKeyDown() && isShiftKeyDown()

    fun isShiftKeyDown() = Keyboard.KEY_LSHIFT.isKeyHeld() || Keyboard.KEY_RSHIFT.isKeyHeld()

    fun isPastingKeysDown() = isModifierKeyDown() && Keyboard.KEY_V.isKeyHeld()

    fun isCopyingKeysDown() = isModifierKeyDown() && Keyboard.KEY_C.isKeyHeld()

    fun isModifierKeyDown() = if (SystemUtils.IS_OS_MAC) isCommandKeyDown() else isControlKeyDown()

    /**
     * TODO make use of this function unnecessary: Try to avoid using `isModifierKeyDown` as the only option,
     * allow the user to set a different option instead and just set the default key to isModifierKeyDown
     */
    fun getModifierKeyName(): String = if (SystemUtils.IS_OS_MAC) "Command" else "Control"

    @SubscribeEvent
    fun onGuiScreenKeybind(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val guiScreen = event.gui as? GuiContainer ?: return
        if (GuiKeyPressEvent(guiScreen).postAndCatch()) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        val currentScreen = Minecraft.getMinecraft().currentScreen
        val isConfigScreen = currentScreen is GuiScreenElementWrapper
        if (isConfigScreen) return
        if (currentScreen is GuiChat) return


        if (Mouse.getEventButtonState() && Mouse.getEventButton() != -1) {
            val key = Mouse.getEventButton() - 100
            LorenzKeyPressEvent(key).postAndCatch()
            lastClickedMouseButton = key
            return
        }

        if (Keyboard.getEventKeyState() && Keyboard.getEventKey() != 0) {
            val key = Keyboard.getEventKey()
            LorenzKeyPressEvent(key).postAndCatch()
            lastClickedMouseButton = -1
            return
        }

        if (Mouse.getEventButton() == -1 && lastClickedMouseButton != -1) {
            if (lastClickedMouseButton.isKeyHeld()) {
                LorenzKeyPressEvent(lastClickedMouseButton).postAndCatch()
                return
            }
            lastClickedMouseButton = -1
        }

        // This is needed because of other keyboards that don't have a key code for the key, but is read as a character
        if (Keyboard.getEventKey() == 0) {
            LorenzKeyPressEvent(Keyboard.getEventCharacter().code + 256).postAndCatch()
        }
    }

    fun KeyBinding.isActive(): Boolean {
        if (!Keyboard.isCreated()) return false
        try {
            if (keyCode.isKeyHeld()) return true
        } catch (e: IndexOutOfBoundsException) {
            ErrorManager.logErrorWithData(
                e, "Error while checking if a key is pressed.",
                "keyCode" to keyCode,
            )
            return false
        }
        return this.isKeyDown || this.isPressed
    }

    fun Int.isKeyHeld(): Boolean = when {
        this == 0 -> false
        this < 0 -> Mouse.isButtonDown(this + 100)
        this >= Keyboard.KEYBOARD_SIZE -> {
            val pressedKey = if (Keyboard.getEventKey() == 0) Keyboard.getEventCharacter().code + 256 else Keyboard.getEventKey()
            Keyboard.getEventKeyState() && this == pressedKey
        }

        else -> Keyboard.isKeyDown(this)
    }

    private val pressedKeys = mutableMapOf<Int, Boolean>()

    /** Can only be used once per click. Since the function locks itself until the key is no longer held*/
    fun Int.isKeyClicked(): Boolean = if (this.isKeyHeld()) {
        if (pressedKeys[this] != true) {
            pressedKeys[this] = true
            true
        } else {
            false
        }
    } else {
        pressedKeys[this] = false
        false
    }

    fun getKeyName(keyCode: Int): String = KeybindHelper.getKeyName(keyCode)

    object WasdInputMatrix : Iterable<KeyBinding> {
        operator fun contains(keyBinding: KeyBinding) = when (keyBinding) {
            w, a, s, d, up, down -> true
            else -> false
        }

        val w get() = Minecraft.getMinecraft().gameSettings.keyBindForward!!
        val a get() = Minecraft.getMinecraft().gameSettings.keyBindLeft!!
        val s get() = Minecraft.getMinecraft().gameSettings.keyBindBack!!
        val d get() = Minecraft.getMinecraft().gameSettings.keyBindRight!!

        val up get() = Minecraft.getMinecraft().gameSettings.keyBindJump!!
        val down get() = Minecraft.getMinecraft().gameSettings.keyBindSneak!!

        override fun iterator(): Iterator<KeyBinding> =
            object : Iterator<KeyBinding> {

                var current = w
                var finished = false

                override fun hasNext(): Boolean =
                    !finished

                override fun next(): KeyBinding {
                    if (!hasNext()) throw NoSuchElementException()

                    return current.also {
                        current = when (it) {
                            w -> a
                            a -> s
                            s -> d
                            d -> up
                            up -> down
                            else -> {
                                finished = true
                                throw NoSuchElementException()
                            }
                        }
                    }
                }

            }

    }
}
