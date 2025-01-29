package at.hannibal2.skyhanni.config.features.skillprogress

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class SkillOverflowConfig {
    @Expose
    @ConfigOption(name = "Display", desc = "Enable the overflow calculation in the progress display.")
    @ConfigEditorBoolean
    var enableInDisplay: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(
        name = "All Skill Display",
        desc = "Enable the overflow calculation in the all skill progress display."
    )
    @ConfigEditorBoolean
    var enableInAllDisplay: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "ETA Display", desc = "Enable the overflow calculation in the ETA skill display.")
    @ConfigEditorBoolean
    var enableInEtaDisplay: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Progress Bar", desc = "Enable the overflow calculation in the progress bar of the display.")
    @ConfigEditorBoolean
    var enableInProgressBar: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(
        name = "Skill Menu Stack Size",
        desc = "Enable the overflow calculation when the 'Skill Level' Item Number is enabled."
    )
    @ConfigEditorBoolean
    var enableInSkillMenuAsStackSize: Boolean = false

    @Expose
    @ConfigOption(
        name = "Skill Menu Tooltips",
        desc = "Enable the overflow calculation in the tooltip of items in skills menu."
    )
    @ConfigEditorBoolean
    var enableInSkillMenuTooltip: Boolean = false

    @Expose
    @ConfigOption(name = "Chat", desc = "Enable the overflow level up message when you gain an overflow level.")
    @ConfigEditorBoolean
    var enableInChat: Boolean = false
}
