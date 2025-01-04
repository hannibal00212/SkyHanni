package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.GuiEditManager
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor

object ConfigGuiManager {

    var editor: MoulConfigEditor<Features>? = null

    @Suppress("AvoidBritishSpelling")
    private val replacedSearchTerms = mapOf(
        "color" to "colour",
        "colour" to "color",
        "armor" to "armour",
        "armour" to "armor",
        "endermen" to "enderman",
        "enderman" to "endermen",
        "hotkey" to "keybind",
        "keybind" to "hotkey",
        "gray" to "grey",
        "grey" to "gray",
    )

    private fun getPossibleAltWords(word: String): List<String> {
        return buildList {
            replacedSearchTerms.forEach { (first, second) ->
                if (first.startsWith(word, ignoreCase = true)) {
                    add(second)
                }
            }
        }
    }

    fun getEditorInstance() = editor ?: MoulConfigEditor(SkyHanniMod.configManager.processor).also { editor ->
        editor.setSearchFunction { optionEditor, word ->
            if (optionEditor.fulfillsSearch(word)) return@setSearchFunction true
            getPossibleAltWords(word).forEach {
                if (optionEditor.fulfillsSearch(it)) return@setSearchFunction true
            }
            return@setSearchFunction false
        }
        this.editor = editor
    }

    fun openConfigGui(search: String? = null) {
        val editor = getEditorInstance()

        if (search != null) {
            editor.search(search)
        }
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }

    fun onCommand(args: Array<String>) {
        if (args.isNotEmpty()) {
            if (args[0].lowercase() == "gui") {
                GuiEditManager.openGuiPositionEditor(hotkeyReminder = true)
            } else {
                openConfigGui(args.joinToString(" "))
            }
        } else {
            openConfigGui()
        }
    }
}
