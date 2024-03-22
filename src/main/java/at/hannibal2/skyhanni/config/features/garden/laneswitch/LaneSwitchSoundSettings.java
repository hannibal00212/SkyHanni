package at.hannibal2.skyhanni.config.features.garden.laneswitch;

import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLaneFeatures;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigEditorText;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

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
    @ConfigOption(name = "Repeat Duration", desc = "Change in how many ticks the sound will repeat. Change to 20 for only once per second.")
    @ConfigEditorSlider(minValue = 1, maxValue = 20, minStep = 1)
    public int repeatDuration = 20;

    @ConfigOption(name = "List of Sounds", desc = "A list of available sounds.")
    @ConfigEditorButton(buttonText = "Open")
    public Runnable listOfSounds = () -> OSUtils.openBrowser("https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments");
}
