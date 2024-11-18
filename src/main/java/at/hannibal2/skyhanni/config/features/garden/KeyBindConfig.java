package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.garden.farming.GardenCustomKeybinds;
import at.hannibal2.skyhanni.utils.KeyboardManager;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.input.Keyboard;

public class KeyBindConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Use custom keybinds while holding a farming tool or Daedalus Axe in the hand.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Exclude Barn", desc = "Disable this feature while on the barn plot.")
    @ConfigEditorBoolean
    public boolean excludeBarn = false;

    @ConfigOption(name = "Disable All", desc = "Disable all keys.")
    @ConfigEditorButton(buttonText = "Disable")
    public Runnable presetDisable = GardenCustomKeybinds::disableAll;

    @ConfigOption(name = "Set Default", desc = "Reset all keys to default.")
    @ConfigEditorButton(buttonText = "Default")
    public Runnable presetDefault = GardenCustomKeybinds::defaultAll;

    @Expose
    @ConfigOption(name = "Attack", desc = "")
    @ConfigEditorKeybind(defaultKey = KeyboardManager.LEFT_MOUSE)
    public Property<Integer> attack = Property.of(KeyboardManager.LEFT_MOUSE);

    @Expose
    @ConfigOption(name = "Use Item", desc = "")
    @ConfigEditorKeybind(defaultKey = KeyboardManager.RIGHT_MOUSE)
    public Property<Integer> useItem = Property.of(KeyboardManager.RIGHT_MOUSE);

    @Expose
    @ConfigOption(name = "Move Left", desc = "")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_A)
    public Property<Integer> left = Property.of(Keyboard.KEY_A);

    @Expose
    @ConfigOption(name = "Move Right", desc = "")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_D)
    public Property<Integer> right = Property.of(Keyboard.KEY_D);

    @Expose
    @ConfigOption(name = "Move Forward", desc = "")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_W)
    public Property<Integer> forward = Property.of(Keyboard.KEY_W);

    @Expose
    @ConfigOption(name = "Move Back", desc = "")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_S)
    public Property<Integer> back = Property.of(Keyboard.KEY_S);

    @Expose
    @ConfigOption(name = "Jump", desc = "")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_SPACE)
    public Property<Integer> jump = Property.of(Keyboard.KEY_SPACE);

    @Expose
    @ConfigOption(name = "Sneak", desc = "")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_LSHIFT)
    public Property<Integer> sneak = Property.of(Keyboard.KEY_LSHIFT);
}
