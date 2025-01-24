package at.hannibal2.skyhanni.config.features.event;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class PrimalFearSolverConfig {
    @Expose
    @ConfigOption(name = "Math", desc = "Sends a clickable message with the solution in chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean math = false;

    @Expose
    @ConfigOption(name = "Public Speaking", desc = "Sends a clickable message with a random string in chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean publicSpeaking = false;
}
