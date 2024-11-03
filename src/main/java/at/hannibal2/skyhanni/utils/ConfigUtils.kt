package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.EventHandler
import at.hannibal2.skyhanni.config.ConfigGuiManager
import at.hannibal2.skyhanni.config.HasLegacyId
import at.hannibal2.skyhanni.events.LorenzEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.javaField

object ConfigUtils {

    /**
     * Migrates an Int ArrayList to an Enum ArrayList.
     * The new enum class should implement HasLegacyId and have a getter for LegacyId
     *
     * @param element The JsonElement to migrate
     * @param enumClass The enum class to migrate to
     * @return The migrated JsonElement
     */
    fun <T> migrateIntArrayListToEnumArrayList(element: JsonElement, enumClass: Class<T>): JsonElement
        where T : Enum<T>, T : HasLegacyId {
        require(element is JsonArray) { "Expected a JsonArray but got ${element.javaClass.simpleName}" }

        // An array of enum constants that are to be migrated
        val migratedArray = element.mapNotNull { jsonElement ->
            val index = jsonElement.asInt
            getEnumConstantFromLegacyId(index, enumClass)?.name
        }.map { JsonPrimitive(it) }

        // Return a JsonArray of the migrated enum constants
        return JsonArray().apply {
            migratedArray.forEach { add(it) }
        }
    }

    /**
     * Gets an enum constant from a legacy id
     * @param legacyId The legacy id to get the enum constant from
     * @param enumClass The enum class to get the enum constant from
     * @return The enum constant, or null if not found
     */
    private fun <T> getEnumConstantFromLegacyId(
        legacyId: Int,
        enumClass: Class<T>,
    ): T? where T : Enum<T>, T : HasLegacyId = enumClass.getEnumConstants().firstOrNull { it.legacyId == legacyId }

    /**
     * Migrates an Int to an Enum Constant.
     * The new enum class should implement HasLegacyId and have a getter for LegacyId
     *
     * @param element The JsonElement to migrate
     * @param enumClass The enum class to migrate to
     * @return The migrated JsonElement
     */
    fun <T> migrateIntToEnum(element: JsonElement, enumClass: Class<T>): JsonElement
        where T : Enum<T>, T : HasLegacyId {
        require(element is JsonPrimitive) { "Expected a JsonPrimitive but got ${element.javaClass.simpleName}" }
        return JsonPrimitive(getEnumConstantFromLegacyId(element.asInt, enumClass)?.name)
    }

    /**
     * Migrates a Boolean to an Enum Constant.
     *
     * @param element The JsonElement to migrate
     * @param trueValue The enum value it should map to if the value is true
     * @param falseValue The enum value it should map to if the value is false
     * @return The migrated JsonElement
     */
    fun <T : Enum<T>> migrateBooleanToEnum(element: JsonElement, trueValue: T, falseValue: T): JsonElement {
        require(element is JsonPrimitive) { "Expected a JsonPrimitive but got ${element.javaClass.simpleName}" }
        return JsonPrimitive(if (element.asBoolean) trueValue.name else falseValue.name)
    }

    fun KMutableProperty0<*>.tryFindEditor(editor: MoulConfigEditor<*>): ProcessedOption? {
        return editor.processedConfig.getOptionFromField(this.javaField ?: return null)
    }

    fun KMutableProperty0<*>.jumpToEditor() {
        if (tryJumpToEditor(ConfigGuiManager.getEditorInstance())) return

        // TODO create utils function "crashIfInDevEnv"
        if (LorenzEvent.isInGuardedEventHandler || EventHandler.isInEventHandler) {
            throw Error("can not jump to editor $name")
        }
        ErrorManager.logErrorStateWithData(
            "Can not open the config",
            "error while trying to jump to an editor element",
            "this.name" to this.name,
            "this.toString()" to this.toString(),
            "this" to this,
        )
    }

    private fun KMutableProperty0<*>.tryJumpToEditor(editor: MoulConfigEditor<*>): Boolean {
        val option = tryFindEditor(editor) ?: return false
        editor.search("")
        if (!editor.goToOption(option)) return false
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
        return true
    }
}
