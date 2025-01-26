package at.hannibal2.skyhanni.config.features.inventory.helper;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class HarpConfigKeyBinds {
    @Expose
    @ConfigOption(name = "Key 1", desc = "Key for the first node")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_1)
    public int key1 = Keyboard.KEY_1;
    @Expose
    @ConfigOption(name = "Key 2", desc = "Key for the second node")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_2)
    public int key2 = Keyboard.KEY_2;
    @Expose
    @ConfigOption(name = "Key 3", desc = "Key for the third node")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_3)
    public int key3 = Keyboard.KEY_3;
    @Expose
    @ConfigOption(name = "Key 4", desc = "Key for the fourth node")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_4)
    public int key4 = Keyboard.KEY_4;
    @Expose
    @ConfigOption(name = "Key 5", desc = "Key for the fifth node")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_5)
    public int key5 = Keyboard.KEY_5;
    @Expose
    @ConfigOption(name = "Key 6", desc = "Key for the sixth node")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_6)
    public int key6 = Keyboard.KEY_6;
    @Expose
    @ConfigOption(name = "Key 7", desc = "Key for the seventh node")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_7)
    public int key7 = Keyboard.KEY_7;
}
