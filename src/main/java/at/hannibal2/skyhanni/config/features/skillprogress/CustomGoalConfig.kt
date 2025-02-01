package at.hannibal2.skyhanni.config.features.skillprogress

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CustomGoalConfig {
    @Expose
    @ConfigOption(name = "Display", desc = "Enable the custom goal in the progress display.")
    @ConfigEditorBoolean
    var enableInDisplay: Boolean = true

    @Expose
    @ConfigOption(name = "All Skill Display", desc = "Enable the custom goal in the all skill display.")
    @ConfigEditorBoolean
    var enableInAllDisplay: Boolean = false

    @Expose
    @ConfigOption(name = "ETA Display", desc = "Enable the custom goal in the ETA skill display.")
    @ConfigEditorBoolean
    var enableInETADisplay: Boolean = false

    @Expose
    @ConfigOption(name = "Progress Bar", desc = "Enable the custom goal in the progress bar.")
    @ConfigEditorBoolean
    var enableInProgressBar: Boolean = true

    @Expose
    @ConfigOption(name = "Skill Menu Tooltips", desc = "Enable the custom goal in the tooltip of items in skills menu.")
    @ConfigEditorBoolean
    var enableInSkillMenuTooltip: Boolean = false

    @Expose
    @ConfigOption(name = "Chat", desc = "Send a message when you reach your goal.")
    @ConfigEditorBoolean
    var enableInChat: Boolean = false
}
