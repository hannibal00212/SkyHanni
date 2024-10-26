package at.hannibal2.skyhanni.features.misc.visualwords

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.chat
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.convertToFormatted
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

open class VisualWordGui : GuiScreen() {

    private var guiLeft = 0
    private var guiTop = 0
    private var screenHeight = 0
    private val sizeX = 360
    private val sizeY = 180
    private val maxTextLength = 75

    private var mouseX = 0
    private var mouseY = 0
    private var lastMouseScroll = 0
    private var noMouseScrollFrames = 0

    private var pageScroll = 0
    private var scrollVelocity = 0.0
    private val maxNoInputFrames = 100

    private var lastClickedHeight = 0
    private var lastClickedWidth = 0
    private var changedIndex = -1
    private var changedAction = ActionType.NONE

    private var currentlyEditing = false
    private var currentIndex = -1

    private var currentTextBox = SelectedTextBox.NONE
    private var currentText = ""

    private var modifiedWords = mutableListOf<VisualWord>()

    private val shouldDrawImport get() = drawImport && !SkyHanniMod.feature.storage.visualWordsImported

    companion object {

        @JvmStatic
        fun onCommand() {
            if (!LorenzUtils.onHypixel) {
                ChatUtils.userError("You need to join Hypixel to use this feature!")
            } else {
                if (sbeConfigPath.exists()) drawImport = true
                SkyHanniMod.screenToOpen = VisualWordGui()
            }
        }

        fun isInGui() = Minecraft.getMinecraft().currentScreen is VisualWordGui
        var sbeConfigPath = File("." + File.separator + "config" + File.separator + "SkyblockExtras.cfg")
        var drawImport = false

        val itemUp by lazy {
            ItemUtils.createSkull(
                displayName = "§§Up",
                uuid = "7f68dd73-1ff6-4193-b246-820975d6fab1",
                value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1" +
                    "cmUvNzczMzRjZGRmYWI0NWQ3NWFkMjhlMWE0N2JmOGNmNTAxN2QyZjA5ODJmNjczN2RhMjJkNDk3Mjk1MjUxMDY2MSJ9fX0=",
            )
        }

        val itemDown by lazy {
            ItemUtils.createSkull(
                displayName = "§§Down",
                uuid = "e4ace6de-0629-4719-aea3-3e113314dd3f",
                value =
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc3NDIwMz" +
                    "RmNTlkYjg5MGM4MDA0MTU2YjcyN2M3N2NhNjk1YzQzOTlkOGUwZGE1Y2U5MjI3Y2Y4MzZiYjhlMiJ9fX0=",
            )
        }
    }

    override fun drawScreen(unusedX: Int, unusedY: Int, partialTicks: Float) {
        super.drawScreen(unusedX, unusedY, partialTicks)
        drawDefaultBackground()
        screenHeight = height
        guiLeft = (width - sizeX) / 2
        guiTop = (height - sizeY) / 2

        mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth
        mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1

        GlStateManager.pushMatrix()
        drawRect(guiLeft, guiTop, guiLeft + sizeX, guiTop + sizeY, 0x50000000)
        val scale = 0.75f
        val inverseScale = 1 / scale

        val colorA = 0x50828282
        val colorB = 0x50303030
        if (!currentlyEditing) {
            val adjustedY = guiTop + 30 + pageScroll
            var toRemove: VisualWord? = null

            val x = guiLeft + 180
            val y = guiTop + 170

            drawUnmodifiedStringCentered("§aAdd New", x, y)
            val color = if (isPointInMousePos(x - 30, y - 10, 60, 20)) colorA else colorB
            drawRect(x - 30, y - 10, x + 30, y + 10, color)

            if (shouldDrawImport) {
                val importX = guiLeft + sizeX - 45
                val importY = guiTop + sizeY - 10
                GuiRenderUtils.drawStringCentered("§aImport from SBE", importX, importY)
                val importColor = if (isPointInMousePos(importX - 45, importY - 10, 90, 20)) colorA else colorB
                drawRect(importX - 45, importY - 10, importX + 45, importY + 10, importColor)
            }

            GlStateManager.scale(scale, scale, 1f)

            drawUnmodifiedStringCentered(
                "§7Modify Words. Replaces the top with the bottom",
                (guiLeft + 180) * inverseScale,
                (guiTop + 9) * inverseScale,
            )
            drawUnmodifiedString("§bPhrase", (guiLeft + 30) * inverseScale, (guiTop + 5) * inverseScale)
            drawUnmodifiedString("§bStatus", (guiLeft + 310) * inverseScale, (guiTop + 5) * inverseScale)

            for ((index, phrase) in modifiedWords.withIndex()) {
                if (adjustedY + 30 * index < guiTop + 20) continue
                if (adjustedY + 30 * index > guiTop + 125) continue

                if (phrase.phrase == "" && phrase.replacement == "") {
                    toRemove = phrase
                }

                var inBox = false
                if (isPointInMousePos(guiLeft, adjustedY + 30 * index, sizeX, 30)) {
                    inBox = true
                }

                drawUnmodifiedString(
                    "${index + 1}.",
                    (guiLeft + 5) * inverseScale,
                    (adjustedY + 10 + 30 * index) * inverseScale,
                )

                val top = adjustedY + 30 * index + 7
                if (isPointInLastClicked(guiLeft + 335, top, 16, 16)) {
                    lastClickedWidth = 0
                    lastClickedHeight = 0
                    phrase.enabled = !phrase.enabled
                    saveChanges()
                    SoundUtils.playClickSound()
                } else if (isPointInLastClicked(guiLeft + 295, top, 16, 16) && index != 0) {
                    lastClickedWidth = 0
                    lastClickedHeight = 0
                    SoundUtils.playClickSound()
                    changedIndex = index
                    changedAction = ActionType.UP
                } else if (isPointInLastClicked(guiLeft + 315, top, 16, 16) && index != modifiedWords.size - 1) {
                    lastClickedWidth = 0
                    lastClickedHeight = 0
                    SoundUtils.playClickSound()
                    changedIndex = index
                    changedAction = ActionType.DOWN
                } else if (isPointInLastClicked(guiLeft, adjustedY + 30 * index, sizeX, 30)) {
                    lastClickedWidth = 0
                    lastClickedHeight = 0
                    SoundUtils.playClickSound()
                    currentlyEditing = true
                    currentIndex = index
                }

                if (inBox) {
                    GuiRenderUtils.drawScaledRec(
                        guiLeft,
                        adjustedY + 30 * index,
                        guiLeft + sizeX,
                        adjustedY + 30 * index + 30,
                        colorB,
                        inverseScale,
                    )
                }

                val statusBlock = if (phrase.enabled) {
                    ItemStack(Blocks.stained_hardened_clay, 1, 13)
                } else {
                    ItemStack(Blocks.stained_hardened_clay, 1, 14)
                }

                GlStateManager.scale(inverseScale, inverseScale, 1f)

                if (index != 0) {
                    GuiRenderUtils.renderItemAndBackground(itemUp, guiLeft + 295, top, colorA)
                }
                if (index != modifiedWords.size - 1) {
                    GuiRenderUtils.renderItemAndBackground(itemDown, guiLeft + 315, top, colorA)
                }

                GuiRenderUtils.renderItemAndBackground(statusBlock, guiLeft + 335, top, colorA)

                GlStateManager.scale(scale, scale, 1f)

                if (inBox) {
                    drawUnmodifiedString(
                        phrase.phrase,
                        (guiLeft + 15) * inverseScale,
                        (adjustedY + 5 + 30 * index) * inverseScale,
                    )
                    drawUnmodifiedString(
                        phrase.replacement,
                        (guiLeft + 15) * inverseScale,
                        (adjustedY + 15 + 30 * index) * inverseScale,
                    )
                } else {
                    drawUnmodifiedString(
                        phrase.phrase.convertToFormatted(),
                        (guiLeft + 15) * inverseScale,
                        (adjustedY + 5 + 30 * index) * inverseScale,
                    )
                    drawUnmodifiedString(
                        phrase.replacement.convertToFormatted(),
                        (guiLeft + 15) * inverseScale,
                        (adjustedY + 15 + 30 * index) * inverseScale,
                    )
                }
            }

            if (modifiedWords.size < 1) {
                modifiedWords = ModifyVisualWords.modifiedWords
            }

            if (toRemove != null) {
                modifiedWords.remove(toRemove)
                saveChanges()
            }

            GlStateManager.scale(inverseScale, inverseScale, 1f)

            scrollScreen()
        } else {
            var x = guiLeft + 180
            var y = guiTop + 140
            drawUnmodifiedStringCentered("§cDelete", x, y)
            var color = if (isPointInMousePos(x - 30, y - 10, 60, 20)) colorA else colorB
            drawRect(x - 30, y - 10, x + 30, y + 10, color)
            y += 30
            drawUnmodifiedStringCentered("§eBack", x, y)
            color = if (isPointInMousePos(x - 30, y - 10, 60, 20)) colorA else colorB
            drawRect(x - 30, y - 10, x + 30, y + 10, color)

            if (currentIndex < modifiedWords.size && currentIndex != -1) {
                val currentPhrase = modifiedWords[currentIndex]

                x -= 100
                drawUnmodifiedStringCentered("§bReplacement Enabled", x, y - 20)
                var status = if (currentPhrase.enabled) "§2Enabled" else "§4Disabled"
                drawUnmodifiedStringCentered(status, x, y)
                color = if (isPointInMousePos(x - 30, y - 10, 60, 20)) colorA else colorB
                drawRect(x - 30, y - 10, x + 30, y + 10, color)

                x += 200
                drawUnmodifiedStringCentered("§bCase Sensitive", x, y - 20)
                status = if (!currentPhrase.isCaseSensitive()) "§2True" else "§4False"
                drawUnmodifiedStringCentered(status, x, y)
                color = if (isPointInMousePos(x - 30, y - 10, 60, 20)) colorA else colorB
                drawRect(x - 30, y - 10, x + 30, y + 10, color)

                drawUnmodifiedString("§bIs replaced by:", guiLeft + 30, guiTop + 75)

                if (isPointInMousePos(guiLeft, guiTop + 35, sizeX, 30)) {
                    drawRect(guiLeft, guiTop + 35, guiLeft + sizeX, guiTop + 35 + 30, colorB)
                }
                if (currentTextBox == SelectedTextBox.PHRASE) {
                    drawRect(guiLeft, guiTop + 35, guiLeft + sizeX, guiTop + 35 + 30, colorA)
                }

                if (isPointInMousePos(guiLeft, guiTop + 90, sizeX, 30)) {
                    drawRect(guiLeft, guiTop + 90, guiLeft + sizeX, guiTop + 90 + 30, colorB)
                }
                if (currentTextBox == SelectedTextBox.REPLACEMENT) {
                    drawRect(guiLeft, guiTop + 90, guiLeft + sizeX, guiTop + 90 + 30, colorA)
                }

                GlStateManager.scale(0.75f, 0.75f, 1f)

                // TODO remove more code duplication
                drawUnmodifiedString(
                    "§bThe top line of each section",
                    (guiLeft + 10) * inverseScale,
                    (guiTop + 12) * inverseScale,
                )
                drawUnmodifiedString(
                    "§bis the preview of the bottom text",
                    (guiLeft + 10) * inverseScale,
                    (guiTop + 22) * inverseScale,
                )

                drawUnmodifiedString(
                    "§bTo get the Minecraft",
                    (guiLeft + 220) * inverseScale,
                    (guiTop + 12) * inverseScale,
                )
                drawUnmodifiedString(
                    "§b formatting character use \"&&\"",
                    (guiLeft + 220) * inverseScale,
                    (guiTop + 22) * inverseScale,
                )

                drawUnmodifiedString(
                    currentPhrase.phrase.convertToFormatted(),
                    (guiLeft + 30) * inverseScale,
                    (guiTop + 40) * inverseScale,
                )
                drawUnmodifiedString(currentPhrase.phrase, (guiLeft + 30) * inverseScale, (guiTop + 55) * inverseScale)

                drawUnmodifiedString(
                    currentPhrase.replacement.convertToFormatted(),
                    (guiLeft + 30) * inverseScale,
                    (guiTop + 95) * inverseScale,
                )
                drawUnmodifiedString(
                    currentPhrase.replacement,
                    (guiLeft + 30) * inverseScale,
                    (guiTop + 110) * inverseScale,
                )

                GlStateManager.scale(inverseScale, inverseScale, 1f)
            }
        }

        if (changedIndex != -1) {
            if (changedAction == ActionType.UP) {
                if (changedIndex > 0) {
                    val temp = modifiedWords[changedIndex]
                    modifiedWords[changedIndex] = modifiedWords[changedIndex - 1]
                    modifiedWords[changedIndex - 1] = temp
                }
            } else if (changedAction == ActionType.DOWN) {
                if (changedIndex < modifiedWords.size - 1) {
                    val temp = modifiedWords[changedIndex]
                    modifiedWords[changedIndex] = modifiedWords[changedIndex + 1]
                    modifiedWords[changedIndex + 1] = temp
                }
            }

            changedIndex = -1
            changedAction = ActionType.NONE
            saveChanges()
        }

        GlStateManager.popMatrix()
    }

    private fun isPointInMousePos(left: Int, top: Int, width: Int, height: Int) =
        GuiRenderUtils.isPointInRect(mouseX, mouseY, left, top, width, height)

    private fun isPointInLastClicked(left: Int, top: Int, width: Int, height: Int) =
        GuiRenderUtils.isPointInRect(lastClickedWidth, lastClickedHeight, left, top, width, height)

    override fun handleMouseInput() {
        super.handleMouseInput()

        if (Mouse.getEventButtonState()) {
            mouseClickEvent()
        }
        if (!Mouse.getEventButtonState()) {
            if (Mouse.getEventDWheel() != 0) {
                lastMouseScroll = Mouse.getEventDWheel()
                noMouseScrollFrames = 0
            }
        }
    }

    @Throws(IOException::class)
    fun mouseClickEvent() {
        if (!currentlyEditing) {
            if (isPointInMousePos(guiLeft, guiTop, sizeX, sizeY - 25)) {
                lastClickedWidth = mouseX
                lastClickedHeight = mouseY
            }
        }
        var x = guiLeft + 180
        var y = guiTop + 140
        if (currentlyEditing) {
            if (isPointInMousePos(x - 30, y - 10, 60, 20)) {
                SoundUtils.playClickSound()
                currentlyEditing = false
                modifiedWords.removeAt(currentIndex)
                currentIndex = -1
                saveChanges()
                currentTextBox = SelectedTextBox.NONE
            }
            if (currentIndex < modifiedWords.size && currentIndex != -1) {
                x -= 100
                y += 30
                if (isPointInMousePos(x - 30, y - 10, 60, 20)) {
                    SoundUtils.playClickSound()
                    modifiedWords[currentIndex].enabled = !modifiedWords[currentIndex].enabled
                    saveChanges()
                }
                x += 200
                if (isPointInMousePos(x - 30, y - 10, 60, 20)) {
                    SoundUtils.playClickSound()
                    modifiedWords[currentIndex].setCaseSensitive(!modifiedWords[currentIndex].isCaseSensitive())
                    saveChanges()
                } else if (isPointInMousePos(guiLeft, guiTop + 35, sizeX, 30)) {
                    SoundUtils.playClickSound()
                    currentTextBox = SelectedTextBox.PHRASE
                    currentText = modifiedWords[currentIndex].phrase
                } else if (isPointInMousePos(guiLeft, guiTop + 90, sizeX, 30)) {
                    SoundUtils.playClickSound()
                    currentTextBox = SelectedTextBox.REPLACEMENT
                    currentText = modifiedWords[currentIndex].replacement
                } else {
                    if (currentTextBox != SelectedTextBox.NONE) {
                        SoundUtils.playClickSound()
                        currentTextBox = SelectedTextBox.NONE
                    }
                }
            }
        }
        y = guiTop + 170
        x = guiLeft + 180
        if (isPointInMousePos(x - 30, y - 10, 60, 20)) {
            SoundUtils.playClickSound()
            if (currentlyEditing) {
                val currentVisualWord = modifiedWords.elementAt(currentIndex)

                if (currentVisualWord.phrase == "" && currentVisualWord.replacement == "") {
                    modifiedWords.remove(currentVisualWord)
                    saveChanges()
                }

                currentIndex = -1
                currentTextBox = SelectedTextBox.NONE
            } else {
                modifiedWords.add(VisualWord("", "", true, caseSensitive = false))
                currentTextBox = SelectedTextBox.PHRASE
                currentText = ""
                currentIndex = modifiedWords.size - 1
                saveChanges()
                pageScroll = -(modifiedWords.size * 30 - 100)
                scrollScreen()
            }
            currentlyEditing = !currentlyEditing
        }
        if (shouldDrawImport) {
            val importX = guiLeft + sizeX - 45
            val importY = guiTop + sizeY - 10
            if (isPointInMousePos(importX - 45, importY - 10, 90, 20)) {
                SoundUtils.playClickSound()
                tryImportFromSBE()
            }
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        if (!currentlyEditing) {
            if (keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_S) {
                if (KeyboardManager.isModifierKeyDown()) {
                    pageScroll = -(modifiedWords.size * 30 - 100)
                } else {
                    pageScroll -= 30
                }
                scrollScreen()
            }
            if (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_W) {
                if (KeyboardManager.isModifierKeyDown()) {
                    pageScroll = 0
                } else {
                    pageScroll += 30
                }
                scrollScreen()
            }
            return
        }
        if (currentTextBox == SelectedTextBox.NONE) return
        if (currentIndex >= modifiedWords.size || currentIndex == -1) return

        if (keyCode == Keyboard.KEY_BACK) {
            if (currentText.isNotEmpty()) {
                currentText = if (KeyboardManager.isDeleteLineDown()) ""
                else if (KeyboardManager.isDeleteWordDown()) {
                    val lastSpaceIndex = currentText.trimEnd().removeSuffix(" ").lastIndexOf(' ')
                    if (lastSpaceIndex >= 0) currentText.substring(0, lastSpaceIndex + 1) else ""
                } else {
                    currentText.substring(0, currentText.length - 1)
                }
                saveTextChanges()
            }

            return
        }

        if (currentText.length < maxTextLength && !Character.isISOControl(typedChar)) {
            currentText += typedChar
            saveTextChanges()
            return
        }

        if (KeyboardManager.isPastingKeysDown()) {
            SkyHanniMod.coroutineScope.launch {
                val clipboard = OSUtils.readFromClipboard().orEmpty()
                for (char in clipboard) {
                    if (currentText.length < maxTextLength && !Character.isISOControl(char)) {
                        currentText += char
                    }
                }
                saveTextChanges()
            }
            return
        }

        if (KeyboardManager.isCopyingKeysDown()) {
            OSUtils.copyToClipboard(currentText)
            return
        }
    }

    private fun saveTextChanges() {
        if (currentTextBox == SelectedTextBox.PHRASE) {
            modifiedWords[currentIndex].phrase = currentText
        } else if (currentTextBox == SelectedTextBox.REPLACEMENT) {
            modifiedWords[currentIndex].replacement = currentText
        }
        saveChanges()
    }

    private fun scrollScreen() {
        scrollVelocity += lastMouseScroll / 48.0
        scrollVelocity *= 0.95
        pageScroll += scrollVelocity.toInt() + lastMouseScroll / 24

        noMouseScrollFrames++

        if (noMouseScrollFrames >= maxNoInputFrames) {
            scrollVelocity *= 0.75
        }

        if (pageScroll > 0) {
            pageScroll = 0
        }

        pageScroll = MathHelper.clamp_int(pageScroll, -(modifiedWords.size * 30 - 100), 0)
        lastMouseScroll = 0
    }

    private fun saveChanges() {
        ModifyVisualWords.modifiedWords = modifiedWords
        ModifyVisualWords.textCache.clear()
        SkyHanniMod.visualWordsData.modifiedWords = modifiedWords
        SkyHanniMod.configManager.saveConfig(ConfigFileType.VISUAL_WORDS, "Updated visual words")
    }

    private fun tryImportFromSBE() {
        if (!drawImport) return
        try {
            val reader = InputStreamReader(FileInputStream(sbeConfigPath), StandardCharsets.UTF_8)
            val json = ConfigManager.gson.fromJson(reader, JsonObject::class.java)
            var importedWords = 0
            var skippedWords = 0
            val lists = json["custom"].asJsonObject["visualWords"].asJsonArray
            val pattern = "(?<from>.*)@-(?<to>.*)@:-(?<state>false|true)".toPattern()
            loop@ for (line in lists) {
                pattern.matchMatcher(line.asString) {
                    val from = group("from").replace("&", "&&")
                    val to = group("to").replace("&", "&&")
                    val state = group("state").toBoolean()

                    if (modifiedWords.any { it.phrase == from }) {
                        skippedWords++
                        continue@loop
                    }

                    modifiedWords.add(VisualWord(from, to, state, false))
                    importedWords++
                }
            }
            if (importedWords > 0 || skippedWords > 0) {
                chat(
                    "§aSuccessfully imported §e$importedWords §aand skipped §e$skippedWords §aVisualWords from SkyBlockExtras !"
                )
                SkyHanniMod.feature.storage.visualWordsImported = true
                drawImport = false
            }
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Failed to load visual words from SBE")
        }
    }

    private fun drawUnmodifiedString(str: String, x: Float, y: Float) {
        GuiRenderUtils.drawString("§§$str", x, y)
    }

    private fun drawUnmodifiedString(str: String, x: Int, y: Int) {
        drawUnmodifiedString(str, x.toFloat(), y.toFloat())
    }

    private fun drawUnmodifiedStringCentered(str: String?, x: Int, y: Int) {
        GuiRenderUtils.drawStringCentered("§§$str", x, y)
    }

    private fun drawUnmodifiedStringCentered(str: String?, x: Float, y: Float) {
        drawUnmodifiedStringCentered(str, x.toInt(), y.toInt())
    }
}

private enum class ActionType {
    UP,
    DOWN,
    NONE
}

private enum class SelectedTextBox {
    PHRASE,
    REPLACEMENT,
    NONE
}
