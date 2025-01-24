package at.hannibal2.skyhanni.config.features.skillprogress;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CustomGoalConfig {

    @Expose
    @ConfigOption(name = "Display", desc = "Enable the custom goal in the progress display.")
    @ConfigEditorBoolean
    public boolean enableInDisplay = true;

    @Expose
    @ConfigOption(name = "All Skill Display", desc = "Enable the custom goal in the all skill display.")
    @ConfigEditorBoolean
    public boolean enableInAllDisplay = false;

    @Expose
    @ConfigOption(name = "ETA Display", desc = "Enable the custom goal in the ETA skill display.")
    @ConfigEditorBoolean
    public boolean enableInETADisplay = false;

    @Expose
    @ConfigOption(name = "Progress Bar", desc = "Enable the custom goal in the progress bar.")
    @ConfigEditorBoolean
    public boolean enableInProgressBar = true;

    @Expose
    @ConfigOption(name = "Skill Menu Tooltips", desc = "Enable the custom goal in the tooltip of items in skills menu.")
    @ConfigEditorBoolean
    public boolean enableInSkillMenuTooltip = false;

    @Expose
    @ConfigOption(name = "Chat", desc = "Send a message when you reach your goal.")
    @ConfigEditorBoolean
    public boolean enableInChat = false;

}
