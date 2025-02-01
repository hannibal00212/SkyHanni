package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.features.dungeon.DungeonSecretChime
import at.hannibal2.skyhanni.utils.OSUtils
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SecretChimeConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Play a sound effect when a secret is found.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Mute Sounds", desc = "Disables chest and lever sounds.")
    @Accordion
    var muteSecretSound: MuteSecretSoundConfig = MuteSecretSoundConfig()

    class MuteSecretSoundConfig {

        @Expose
        @ConfigOption(name = "Mute Chest Sound", desc = "Disables chest opening sound.")
        @ConfigEditorBoolean
        @FeatureToggle
        var muteChestSound: Boolean = false

        @Expose
        @ConfigOption(name = "Mute Lever Sound", desc = "Disables lever activation sound.")
        @ConfigEditorBoolean
        @FeatureToggle
        var muteLeverSound: Boolean = false
    }

    @Expose
    @ConfigOption(name = "Secret Chime Sound", desc = "The sound played for the secret chime.")
    @ConfigEditorText
    var soundName: String = "random.orb"

    @Expose
    @ConfigOption(name = "Pitch", desc = "The pitch of the secret chime sound.")
    @ConfigEditorSlider(minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
    var soundPitch: Float = 1.0f

    @ConfigOption(
        name = "Sounds",
        desc = "Click to open the list of available sounds.\n" +
            "§l§cWarning: Clicking this will open a webpage in your browser.",
    )
    @ConfigEditorButton(buttonText = "OPEN")
    var soundsListURL: Runnable = Runnable(OSUtils::openSoundsListInBrowser)

    @ConfigOption(name = "Play Sound", desc = "Plays current secret chime sound.")
    @ConfigEditorButton(buttonText = "Play")
    var checkSound: Runnable = Runnable(DungeonSecretChime::playSound)
}
