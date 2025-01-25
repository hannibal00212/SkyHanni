package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.input.Keyboard;

public class SensitivityReducerConfig {
    @Expose
    @ConfigOption(
        name = "Mode",
        desc = "Lower mouse sensitivity while in the garden.")
    @ConfigEditorDropdown
    public Mode mode = Mode.OFF;

    public enum Mode {
        OFF("Disabled"),
        TOOL("Holding farming tool"),
        KEYBIND("Holding Keybind");
        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Keybind", desc = "When selected above, press this key to reduce the mouse sensitivity.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_N)
    public int keybind = Keyboard.KEY_N;

    @Expose
    @ConfigOption(name = "Reducing factor", desc = "Change by how much the sensitivity is lowered by.")
    @ConfigEditorSlider(minValue = 1, maxValue = 50, minStep = 1)
    public Property<Float> reducingFactor = Property.of(15.0F);

    @Expose
    @ConfigOption(
        name = "Show GUI",
        desc = "Show the GUI element while the feature is enabled.")
    @ConfigEditorBoolean
    public boolean showGUI = true;

    @Expose
    @ConfigOption(
        name = "Only in Ground",
        desc = "Lower sensitivity when standing on the ground.")
    @ConfigEditorBoolean
    public Property<Boolean> onGround = Property.of(false);

    @Expose
    @ConfigOption(
        name = "Disable in Barn",
        desc = "Disable reduced sensitivity in barn plot.")
    @ConfigEditorBoolean
    public Property<Boolean> onlyPlot = Property.of(true);

    @Expose
    @ConfigLink(owner = SensitivityReducerConfig.class, field = "showGUI")
    public Position position = new Position(400, 400, 0.8f);
}
