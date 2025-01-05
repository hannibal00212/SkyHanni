package at.hannibal2.skyhanni.config.features.event.diana;

import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLaneFeatures;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class InquisitorSoundConfig {
    @Expose
    @ConfigOption(name = "Notification Sound", desc = "The sound played when an Inquisitor is found.")
    @ConfigEditorText
    public String name = "random.orb";

    @Expose
    @ConfigOption(name = "Pitch", desc = "The pitch of the sound.")
    @ConfigEditorSlider(minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
    public float pitch = 1.0f;

    @ConfigOption(name = "Test Sound", desc = "Test current sound settings.")
    @ConfigEditorButton(buttonText = "Test")
    public Runnable testSound = FarmingLaneFeatures::playUserSound;

    @ConfigOption(name = "List of Sounds", desc = "A list of available sounds.")
    @ConfigEditorButton(buttonText = "Open")
    public Runnable listOfSounds = () -> OSUtils.openBrowser("https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments");
}
