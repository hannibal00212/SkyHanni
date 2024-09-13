package at.hannibal2.skyhanni.config.features.combat.broodmother;

import at.hannibal2.skyhanni.features.combat.BroodmotherFeatures;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BroodmotherSpawnAlertConfig {

    @Expose
    @ConfigOption(name = "Alert Sound", desc = "The sound that plays for the alert.")
    @ConfigEditorText
    public String alertSound = "note.pling";

    @Expose
    @ConfigOption(name = "Pitch", desc = "The pitch of the alert sound.")
    @ConfigEditorSlider(minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
    public float pitch = 1.0f;

    @ConfigOption(name = "Test Sound", desc = "Test current sound settings.")
    @ConfigEditorButton(buttonText = "Test")
    public Runnable testSound = BroodmotherFeatures::playTestSound;

    @Expose
    @ConfigOption(name = "Repeat Sound", desc = "How many times the sound should be repeated.")
    @ConfigEditorSlider(minValue = 1, maxValue = 20, minStep = 1)
    public int repeatSound = 20;

    @ConfigOption(name = "Sounds", desc = "Click to open the list of available sounds.")
    @ConfigEditorButton(buttonText = "OPEN")
    public Runnable sounds = () -> OSUtils.openBrowser("https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments");

    @Expose
    @ConfigOption(name = "Text", desc = "The text with color to be displayed as the title notification.")
    @ConfigEditorText
    public String text = "&4Broodmother has spawned!";

}
