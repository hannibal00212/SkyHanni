package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.input.Keyboard;

public class HoppityCallWarningConfig {

    @Expose
    @ConfigOption(name = "Hoppity Call Warning", desc = "Warn when hoppity is calling you.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Accept Call Hotkey",
        desc = "Accept the call from hoppity by pressing this keybind."
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int acceptHotkey = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Warning Sound", desc = "The sound that plays when hoppity calls.\n" +
        "§eYou can use custom sounds, put it in the §bskyhanni/sounds §efolder in your resource pack.\n" +
        "§eThen write §bskyhanni:yourfilename\n" +
        "§cMust be a .ogg file")
    @ConfigEditorText
    public Property<String> hoppityCallSound = Property.of("note.pling");

    @Expose
    @ConfigOption(name = "Flash Color", desc = "Color of the screen when flashing")
    @ConfigEditorColour
    public String flashColor = "0:127:0:238:255";

    @ConfigOption(name = "Sounds", desc = "Click to open the list of available sounds.")
    @ConfigEditorButton(buttonText = "OPEN")
    public Runnable sounds = () -> OSUtils.openBrowser("https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments");
}
