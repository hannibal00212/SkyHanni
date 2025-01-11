package at.hannibal2.skyhanni.config.features.garden.laneswitch;

import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLaneFeatures;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LaneSwitchSoundSettings {
    @Expose
    @ConfigOption(name = "Notification Sound", desc = "The sound played for the notification.")
    @ConfigEditorText
    public String name = "random.orb";

    @Expose
    @ConfigOption(name = "Pitch", desc = "The pitch of the notification sound.")
    @ConfigEditorSlider(minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
    public float pitch = 1.0f;

    @ConfigOption(name = "Test Sound", desc = "Test current sound settings.")
    @ConfigEditorButton(buttonText = "Test")
    public Runnable testSound = FarmingLaneFeatures::playUserSound;

    @Expose
    @ConfigOption(
        name = "Repeat Duration",
        desc = "Change how often the sound should be repeated in ticks. Change to 20 for only once per second."
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 20, minStep = 1)
    public int repeatDuration = 20;

    @ConfigOption(name = "List of Sounds", desc = "A list of available sounds.")
    @ConfigEditorButton(buttonText = "Open")
    public Runnable listOfSounds = OSUtils::openSoundsListInBrowser;
}
