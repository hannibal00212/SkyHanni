package at.hannibal2.skyhanni.config.features.dev

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.input.Keyboard

class WaypointsConfig {
    @Expose
    @ConfigOption(
        name = "Save Hotkey",
        desc = "Saves block location to a temporarily parkour and copies everything to your clipboard."
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var saveKey: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigOption(name = "Delete Hotkey", desc = "Deletes the last saved location for when you make a mistake.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var deleteKey: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigOption(name = "Show Platform Number", desc = "Show the index number over the platform for every parkour.")
    @ConfigEditorBoolean
    var showPlatformNumber: Boolean = false

    @Expose
    @ConfigOption(name = "Show Outside SkyBlock", desc = "Make parkour waypoints outside of SkyBlock too.")
    @ConfigEditorBoolean
    var parkourOutsideSB: Boolean = false
}
