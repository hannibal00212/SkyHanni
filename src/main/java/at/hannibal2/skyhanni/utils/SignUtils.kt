package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiEditSign
import at.hannibal2.skyhanni.utils.StringUtils.capAtMinecraftLength
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.util.ChatComponentText

object SignUtils {
    private var pasteLastClicked = false
    private var copyLastClicked = false
    private var deleteLastClicked = false

    fun setTextIntoSign(text: String, line: Int = 0) {
        val gui = Minecraft.getMinecraft().currentScreen
        if (gui !is AccessorGuiEditSign) return
        gui.tileSign.signText[line] = ChatComponentText(text)
    }

    private fun addTextIntoSign(addedText: String) {
        val gui = Minecraft.getMinecraft().currentScreen
        if (gui !is AccessorGuiEditSign) return
        val lines = gui.tileSign.signText
        val index = gui.editLine
        val text = lines[index].unformattedText + addedText
        lines[index] = ChatComponentText(text.capAtMinecraftLength(91))
    }

    fun checkDeleting(gui: GuiScreen?) {
        val deleteClicked = KeyboardManager.isDeleteWordDown() || KeyboardManager.isDeleteLineDown()
        if (!deleteLastClicked && deleteClicked && gui is AccessorGuiEditSign) {
            SkyHanniMod.coroutineScope.launch {
                val newLine = if (KeyboardManager.isDeleteLineDown()) ""
                else if (KeyboardManager.isDeleteWordDown()) {
                    val currentLine = gui.tileSign.signText[gui.editLine].unformattedText

                    val lastSpaceIndex = currentLine.trimEnd().lastIndexOf(' ')
                    if (lastSpaceIndex >= 0) currentLine.substring(0, lastSpaceIndex + 2) else ""
                } else return@launch
                setTextIntoSign(newLine, gui.editLine)
            }
        }
        deleteLastClicked = deleteClicked
    }

    fun checkCopying(gui: GuiScreen?) {
        val copyClicked = KeyboardManager.isCopyingKeysDown()
        if (!copyLastClicked && copyClicked && gui is AccessorGuiEditSign) {
            SkyHanniMod.coroutineScope.launch {
                ClipboardUtils.copyToClipboard(gui.tileSign.signText[gui.editLine].unformattedText)
            }
        }
        copyLastClicked = copyClicked
    }

    fun checkPaste() {
        val pasteClicked = KeyboardManager.isPastingKeysDown()
        if (!pasteLastClicked && pasteClicked) {
            SkyHanniMod.coroutineScope.launch {
                OSUtils.readFromClipboard()?.let {
                    addTextIntoSign(it)
                }
            }
        }
        pasteLastClicked = pasteClicked
    }

    fun GuiEditSign.isRancherSign(): Boolean {
        if (this !is AccessorGuiEditSign) return false
        val signText = (this as AccessorGuiEditSign).tileSign.signText.map { it.unformattedText.removeColor() }
        return signText[1] == "^^^^^^" && signText[2] == "Set your" && signText[3] == "speed cap!"
    }

    fun GuiEditSign.isMousematSign(): Boolean {
        if (this !is AccessorGuiEditSign) return false

        val tileSign = (this as AccessorGuiEditSign).tileSign
        return (
            tileSign.signText[1].unformattedText.removeColor() == "Set Yaw Above!" &&
                tileSign.signText[2].unformattedText.removeColor() == "Set Pitch Below!"
            )
    }

    fun GuiEditSign.isGardenSign(): Boolean {
        return isRancherSign() || isMousematSign()
    }
}
