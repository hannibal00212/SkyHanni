package at.hannibal2.skyhanni.config.features.inventory.customwardrobe;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class KeybindConfig {

    @Expose
    @ConfigOption(
        name = "Slot Keybinds Toggle",
        desc = "Enable/Disable the slot keybinds.\n§cThis only works inside the Custom Wardrobe GUI."
    )
    @ConfigEditorBoolean
    public boolean slotKeybindsToggle = true;

    @Expose
    @ConfigOption(
        name = "Slot 1",
        desc = "Keybind for slot 1"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_1)
    public int slot1 = Keyboard.KEY_1;

    @Expose
    @ConfigOption(
        name = "Slot 2",
        desc = "Keybind for slot 2"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_2)
    public int slot2 = Keyboard.KEY_2;

    @Expose
    @ConfigOption(
        name = "Slot 3",
        desc = "Keybind for slot 3"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_3)
    public int slot3 = Keyboard.KEY_3;

    @Expose
    @ConfigOption(
        name = "Slot 4",
        desc = "Keybind for slot 4"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_4)
    public int slot4 = Keyboard.KEY_4;

    @Expose
    @ConfigOption(
        name = "Slot 5",
        desc = "Keybind for slot 5"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_5)
    public int slot5 = Keyboard.KEY_5;

    @Expose
    @ConfigOption(
        name = "Slot 6",
        desc = "Keybind for slot 6"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_6)
    public int slot6 = Keyboard.KEY_6;

    @Expose
    @ConfigOption(
        name = "Slot 7",
        desc = "Keybind for slot 7"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_7)
    public int slot7 = Keyboard.KEY_7;

    @Expose
    @ConfigOption(
        name = "Slot 8",
        desc = "Keybind for slot 8"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_8)
    public int slot8 = Keyboard.KEY_8;

    @Expose
    @ConfigOption(
        name = "Slot 9",
        desc = "Keybind for slot 9"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_9)
    public int slot9 = Keyboard.KEY_9;

}
