package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class BreakDownConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "When the keybind is pressed, show a breakdown of all the mining fortune sources on an item."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean toggleBreakDownStats = true;

    @Expose
    @ConfigOption(
        name = "Hotkey",
        desc = "Press this key to show a break down of the stats"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_LSHIFT)
    public int statTooltipKeybind = Keyboard.KEY_LSHIFT;

}
