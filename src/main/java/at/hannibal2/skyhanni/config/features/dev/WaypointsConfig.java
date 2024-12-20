package at.hannibal2.skyhanni.config.features.dev;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class WaypointsConfig {

    @Expose
    @ConfigOption(name = "Save Hotkey", desc = "Saves block location to a temporarily parkour and copies everything to your clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int saveKey = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Delete Hotkey", desc = "Deletes the last saved location for when you make a mistake.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int deleteKey = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Show Platform Number", desc = "Show the index number over the platform for every parkour.")
    @ConfigEditorBoolean
    public boolean showPlatformNumber = false;

    @Expose
    @ConfigOption(name = "Show Outside SkyBlock", desc = "Make parkour waypoints outside of SkyBlock too.")
    @ConfigEditorBoolean
    public boolean parkourOutsideSB = false;
}
