package at.hannibal2.skyhanni.config.features.inventory.customwardrobe;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class CustomWardrobeConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Enable the Custom Wardrobe GUI.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Follow mouse", desc = "Whether the \"players\" follow the movement of the mouse.")
    @ConfigEditorBoolean
    public boolean eyesFollowMouse = true;

    @Expose
    @ConfigOption(name = "Hide Empty Slots", desc = "Hide wardrobe slots with no armor.")
    @ConfigEditorBoolean
    public boolean hideEmptySlots = false;

    @Expose
    @ConfigOption(name = "Hide Locked Slots", desc = "Hide locked wardrobe slots.")
    @ConfigEditorBoolean
    public boolean hideLockedSlots = false;

    @Expose
    public boolean onlyFavorites = false;

    @Expose
    @ConfigOption(name = "Estimated Value", desc = "Show a §2$ §7sign you can hover to see the wardrobe slot value.")
    @ConfigEditorBoolean
    public boolean estimatedValue = true;

    @Expose
    @ConfigOption(name = "Loading text", desc = "Show a \"§cLoading...§7\" text when the wardrobe page hasn't fully loaded in yet.")
    @ConfigEditorBoolean
    public boolean loadingText = true;

    @Expose
    @ConfigOption(name = "Armor Tooltip Keybind", desc = "Only show the lore of the item hovered when holding a keybind.")
    @ConfigEditorBoolean
    public boolean showTooltipOnlyKeybind = false;

    @Expose
    @ConfigOption(name = "Tooltip Keybind", desc = "Press this key to show the item tooltip.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_LSHIFT)
    public int tooltipKeybind = Keyboard.KEY_LSHIFT;

    @Expose
    @ConfigOption(name = "Colors", desc = "Change the color settings.")
    @Accordion
    public ColorConfig color = new ColorConfig();

    @Expose
    @ConfigOption(name = "Spacing", desc = "")
    @Accordion
    public SpacingConfig spacing = new SpacingConfig();

    @Expose
    @ConfigOption(name = "Keybinds", desc = "")
    @Accordion
    public KeybindConfig keybinds = new KeybindConfig();
}
