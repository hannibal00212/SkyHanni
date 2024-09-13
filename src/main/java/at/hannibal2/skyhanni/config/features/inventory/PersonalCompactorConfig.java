package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class PersonalCompactorConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable showing what items are inside your personal compactor/deletor.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Visibility Mode", desc = "Choose when to show the overlay.")
    @ConfigEditorDropdown
    public VisibilityMode visibilityMode = VisibilityMode.EXCEPT_KEYBIND;

    public enum VisibilityMode {
        ALWAYS("Always"),
        KEYBIND("Keybind Held"),
        EXCEPT_KEYBIND("Except Keybind Held"),
        ;

        private final String name;

        VisibilityMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(name = "Keybind", desc = "The keybind to hold to show the overlay.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_LSHIFT)
    public int keybind = Keyboard.KEY_LSHIFT;

    @Expose
    @ConfigOption(name = "Show On/Off", desc = "Show whether the Personal Compactor/Deletor is currently turned on or off.")
    @ConfigEditorBoolean
    public boolean showToggle = true;
}
