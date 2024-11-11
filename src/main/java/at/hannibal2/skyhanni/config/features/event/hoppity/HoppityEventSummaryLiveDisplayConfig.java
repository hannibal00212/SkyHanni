package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class HoppityEventSummaryLiveDisplayConfig {

    @Expose
    @ConfigOption(name = "Show Display", desc = "Show the hoppity card in a GUI element.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Note", desc = "§cNote§7: This card will mirror the stat list that is defined in the Hoppity Event Summary config.")
    @ConfigEditorInfoText
    public boolean mirrorConfigNote = false;

    @Expose
    @ConfigOption(name = "Only During Event", desc = "Only show the display while Hoppity's Hunt is active.")
    @ConfigEditorBoolean
    public boolean onlyDuringEvent = true;

    @Expose
    @ConfigOption(name = "Card Toggle Keybind", desc = "Toggle the GUI element with this keybind.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int toggleKeybind = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Show Holding Egglocator", desc = "Always show the card when holding an Egglocator.")
    @ConfigEditorBoolean
    public boolean showHoldingEgglocator = false;
}
