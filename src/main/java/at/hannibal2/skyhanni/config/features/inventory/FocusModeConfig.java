package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class FocusModeConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "In focus mode you only see the name of the item instead of the whole description.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Toggle Key", desc = "Key to toggle the focus mode on and off.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int toggleKey = Keyboard.KEY_NONE;
}
