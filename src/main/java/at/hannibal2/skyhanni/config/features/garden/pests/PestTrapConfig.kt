package at.hannibal2.skyhanni.config.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.gui.TabWidgetConfig
import at.hannibal2.skyhanni.utils.ConfigUtils.jumpToEditor
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class PestTrapConfig {

    private val tabWidgetConfig: TabWidgetConfig get() = SkyHanniMod.feature.gui.tabWidget

    @ConfigOption(
        name = "Display",
        desc = "Display the status of pest traps in a GUI element.\nWill take you to Tab Widget Display to enable."
    )
    @ConfigEditorButton(buttonText = "Go")
    var displayRunnable = Runnable { tabWidgetConfig::display.jumpToEditor() }

    @Expose
    @ConfigOption(name = "Warnings", desc = "")
    @Accordion
    var warningConfig = WarningConfig()

    class WarningConfig {

        @Expose
        @ConfigOption(name = "Enabled Warnings", desc = "Which warning types to enable.")
        @ConfigEditorDraggableList
        var enabledWarnings: Property<MutableList<WarningReason>> = Property.of(mutableListOf())

        @Expose
        @ConfigOption(name = "Warning Sound", desc = "The sound that plays for a warning.\nClear to disable sound.")
        @ConfigEditorText
        var warningSound: Property<String> = Property.of("note.pling")

        enum class WarningDisplayType(val displayName: String) {
            CHAT("Chat"),
            TITLE("Title"),
            BOTH("Both"),
            ;

            override fun toString() = displayName
        }

        @Expose
        @ConfigOption(name = "Warning Message", desc = "How the warning message should display")
        @ConfigEditorDropdown
        var warningDisplayType: WarningDisplayType = WarningDisplayType.TITLE

        enum class WarningReason(val displayName: String) {
            PEST_CAUGHT("§2Pest Caught"),
            TRAP_FULL("§cTrap Full"),
            NO_BAIT("§eNo Bait"),
            ;

            override fun toString() = displayName
        }

        @Expose
        @ConfigOption(name = "Warning Interval", desc = "Reminder interval for messages in seconds.")
        @ConfigEditorSlider(minValue = 1f, minStep = 1f, maxValue = 300f)
        var warningIntervalSeconds: Property<Int> = Property.of(0)
    }
}
