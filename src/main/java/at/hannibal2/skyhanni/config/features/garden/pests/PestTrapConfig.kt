package at.hannibal2.skyhanni.config.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.ConfigUtils.jumpToEditor
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class PestTrapConfig {

    private val tabWidgetConfig get() = SkyHanniMod.feature.gui.tabWidget

    @Expose
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
        @ConfigOption(name = "Warning Sound", desc = "The sound that plays for a warning.\nClear to disable sound.")
        @ConfigEditorText
        var warningSound: Property<String> = Property.of("note.pling")

        enum class WarningReason(val displayName: String) {
            PEST_CAUGHT("§2Pest Caught"),
            TRAP_FULL("§cTrap Full"),
            NO_BAIT("§eNo Bait"),
            ;

            override fun toString() = displayName
        }

        @Expose
        @ConfigOption(name = "Enabled Warnings", desc = "Which warning types to enable.")
        @ConfigEditorDraggableList
        var enabledWarnings: MutableList<WarningReason> = mutableListOf(WarningReason.TRAP_FULL)

    }
}
