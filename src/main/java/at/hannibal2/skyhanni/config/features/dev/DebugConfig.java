package at.hannibal2.skyhanni.config.features.dev;

import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.data.Mayor;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.input.Keyboard;

public class DebugConfig {
    @Expose
    @ConfigOption(name = "Enable Debug", desc = "Enable Test logic")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Command Logging", desc = "Logs stack trace information into the console when a command gets sent to Hypixel. (by any mod or the player)")
    @ConfigEditorBoolean
    public boolean commandLogs = false;

    @Expose
    @ConfigOption(
        name = "Mod Menu Log",
        desc = "Enable debug messages when the currently opened GUI changes, with the path to the gui class. " +
            "Useful for adding more mods to quick mod menu switch."
    )
    @ConfigEditorBoolean
    public boolean modMenuLog = false;

    @Expose
    @ConfigOption(name = "Show Internal Name", desc = "Show internal names in item lore.")
    @ConfigEditorBoolean
    public boolean showInternalName = false;

    @Expose
    @ConfigOption(name = "Show Empty Internal Names", desc = "Shows internal name even for items with none.")
    @ConfigEditorBoolean
    public boolean showEmptyNames = false;

    @Expose
    @ConfigOption(name = "Show Item Rarity", desc = "Show item rarities in item lore.")
    @ConfigEditorBoolean
    public boolean showItemRarity = false;

    @Expose
    @ConfigOption(name = "Show Item Category", desc = "Show item categories in item lore.")
    @ConfigEditorBoolean
    public boolean showItemCategory = false;

    @Expose
    @ConfigOption(name = "Show Item Name", desc = "Show the SkyHanni item name for an item.")
    @ConfigEditorBoolean
    public boolean showItemName = false;

    @Expose
    @ConfigOption(name = "Copy Internal Name", desc = "Copies the internal name of an item on key press in the clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyInternalName = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Show NPC Price", desc = "Show NPC price in item lore.")
    @ConfigEditorBoolean
    public boolean showNpcPrice = false;

    @Expose
    @ConfigOption(name = "Show Craft Price", desc = "Show craft price in item lore.")
    @ConfigEditorBoolean
    public boolean showCraftPrice = false;

    @Expose
    @ConfigOption(name = "Show BZ Price", desc = "Show BZ price in item lore.")
    @ConfigEditorBoolean
    public boolean showBZPrice = false;

    @Expose
    @ConfigOption(name = "Show Item UUID", desc = "Show the Unique Identifier of items in the lore.")
    @ConfigEditorBoolean
    public boolean showItemUuid = false;

    @Expose
    @ConfigOption(name = "Copy Item Data", desc = "Copies item NBT data on key press in a GUI to clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyItemData = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Copy Compressed Item Data", desc = "Copies compressed item NBT data on key press in a GUI to clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyItemDataCompressed = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Copy RNG Meter", desc = "Copies internal names and maxed XP needed from RNG meter inventories as json to clipboard.")
    @ConfigEditorBoolean
    public boolean copyRngMeter = false;

    @Expose
    @ConfigOption(name = "Copy Bestiary Data", desc = "Copies the bestiary data from the inventory as json to clipboard.")
    @ConfigEditorBoolean
    public boolean copyBestiaryData = false;

    @Expose
    @ConfigOption(name = "Highlight Missing Repo Items", desc = "Highlights each item in the current inventory that is not in your current NEU repo.")
    @ConfigEditorBoolean
    public boolean highlightMissingRepo = false;

    @Expose
    @ConfigOption(name = "Hot Swap Detection", desc = "Show chat messages when Hot Swap starts and ends.")
    @ConfigEditorBoolean
    public boolean hotSwapDetection = false;

    @Expose
    @ConfigOption(name = "Always Outdated", desc = "For the sake of the auto updater, act like you are always outdated.")
    @ConfigEditorBoolean
    public boolean alwaysOutdated = false;

    @Expose
    @ConfigOption(name = "SkyHanni Event Counter", desc = "Count once per second how many skyhanni events gets triggered, " +
        "show the total amount in console output.")
    @ConfigEditorBoolean
    public boolean eventCounter = false;

    @Expose
    @ConfigOption(name = "Bypass Advanced Tab List", desc = "The Advanced Player Tab list is disabled while pressing this hotkey.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int bypassAdvancedPlayerTabList = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "SkyBlock Area", desc = "Show your current area in SkyBlock while F3 is open.")
    @ConfigEditorBoolean
    public boolean currentAreaDebug = true;

    @Expose
    @ConfigOption(name = "OreBlock Name", desc = "Show the OreBlock you are currently looking at.")
    @ConfigEditorBoolean
    // TODO rename to rayTracedOreBlock
    public boolean raytracedOreblock = true;

    @Expose
    @ConfigOption(name = "Ore Event Messages", desc = "Shows debug messages every time the Ore Event happens.")
    @ConfigEditorBoolean
    public boolean oreEventMessages = false;

    @Expose
    @ConfigOption(name = "Powder Messages", desc = "Shows debug messages every time Hotm Powder changes.")
    @ConfigEditorBoolean
    public boolean powderMessages = false;

    @Expose
    @ConfigOption(name = "Assume Mayor", desc = "Select a mayor to assume.")
    @ConfigEditorDropdown
    public Property<Mayor> assumeMayor = Property.of(Mayor.DISABLED);

    @Expose
    @ConfigOption(name = "Always April Fools", desc = "Always show April fools jokes.")
    @ConfigEditorBoolean
    public boolean alwaysFunnyTime = false;

    @Expose
    @ConfigOption(name = "Never April Fools", desc = "Admit it, you hate fun.")
    @ConfigEditorBoolean
    public boolean neverFunnyTime = false;

    @Expose
    @ConfigOption(name = "Always Hoppity's", desc = "Always act as if Hoppity's Hunt is active.")
    @ConfigEditorBoolean
    public boolean alwaysHoppitys = false;

    @Expose
    @ConfigOption(name = "Always Great Spook", desc = "Assumes the Great Spook is always active.")
    @ConfigEditorBoolean
    public Property<Boolean> forceGreatSpook = Property.of(false);

    // Does not have a config element!
    @Expose
    public Position trackSoundPosition = new Position(0, 0);

    // Also does not have a config element!
    @Expose
    public Position trackParticlePosition = new Position(0, 0);
}
