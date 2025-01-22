package at.hannibal2.skyhanni.config.features.crimsonisle

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.HasLegacyId
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import org.lwjgl.input.Keyboard

class ReputationHelperConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable features around Reputation features in the Crimson Isle.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Hide Completed", desc = "Hide tasks after they've been completed.")
    @ConfigEditorBoolean
    var hideComplete: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Use Hotkey", desc = "Only show the Reputation Helper while pressing the hotkey.")
    @ConfigEditorBoolean
    var useHotkey: Boolean = false

    @Expose
    @ConfigOption(name = "Hotkey", desc = "Press this hotkey to show the Reputation Helper.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var hotkey: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigLink(owner = ReputationHelperConfig::class, field = "enabled")
    var position: Position = Position(10, 10, false, true)

    @Expose
    @ConfigOption(name = "Show Locations", desc = "Crimson Isles waypoints for locations to get reputation.")
    @ConfigEditorDropdown
    var showLocation: ShowLocationEntry = ShowLocationEntry.ONLY_HOTKEY

    enum class ShowLocationEntry(private val displayName: String, private val legacyId: Int = -1) : HasLegacyId {
        ALWAYS("Always", 0),
        ONLY_HOTKEY("Only With Hotkey", 1),
        NEVER("Never", 2);

        override fun getLegacyId() = legacyId
        override fun toString() = displayName
    }
}
