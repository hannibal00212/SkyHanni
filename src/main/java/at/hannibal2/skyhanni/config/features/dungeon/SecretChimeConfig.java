package at.hannibal2.skyhanni.config.features.dungeon;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.dungeon.DungeonSecretChime;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SecretChimeConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Play a sound effect when a secret is found.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Mute Sounds", desc = "Disables chest and lever sounds.")
    @Accordion
    public MuteSecretSoundConfig muteSecretSound = new MuteSecretSoundConfig();

    public static class MuteSecretSoundConfig {

        @Expose
        @ConfigOption(name = "Mute Chest Sound", desc = "Disables chest opening sound.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean muteChestSound = false;

        @Expose
        @ConfigOption(name = "Mute Lever Sound", desc = "Disables lever activation sound.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean muteLeverSound = false;
    }

    @Expose
    @ConfigOption(name = "Secret Chime Sound", desc = "The sound played for the secret chime.")
    @ConfigEditorText
    public String soundName = "random.orb";

    @Expose
    @ConfigOption(name = "Pitch", desc = "The pitch of the secret chime sound.")
    @ConfigEditorSlider(minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
    public float soundPitch = 1.0f;

    @ConfigOption(name = "Sounds",
        desc = "Click to open the list of available sounds.\n" +
        "§l§cWarning: Clicking this will open a webpage in your browser."
    )
    @ConfigEditorButton(buttonText = "OPEN")
    public Runnable soundsListURL = () -> OSUtils.openBrowser("https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments");

    @ConfigOption(name = "Play Sound", desc = "Plays current secret chime sound.")
    @ConfigEditorButton(buttonText = "Play")
    public Runnable checkSound = DungeonSecretChime::playSound;
}
