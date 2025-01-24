package at.hannibal2.skyhanni.config.features.garden.laneswitch;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LaneSwitchNotificationConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Send a notification when approaching the end of a lane and you should switch lanes.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Seconds Before", desc = "How many seconds before reaching the end of the lane should the warning happen?")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 10,
        minStep = 1
    )
    public int secondsBefore = 5;

    @Expose
    @ConfigOption(name = "Text", desc = "The text with color to be displayed as the notification.")
    @ConfigEditorText
    public String text = "&eLane Switch incoming.";

    @Expose
    @ConfigOption(name = "Sound Settings", desc = "")
    @Accordion
    public LaneSwitchSoundSettings sound = new LaneSwitchSoundSettings();
}
