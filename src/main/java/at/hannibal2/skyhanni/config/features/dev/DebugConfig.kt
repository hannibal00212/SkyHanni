package at.hannibal2.skyhanni.config.features.dev

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.ElectionCandidate
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import org.lwjgl.input.Keyboard

class DebugConfig {
    @Expose
    @ConfigOption(name = "Enable Debug", desc = "Enable Test logic")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Command Logging",
        desc = "Logs stack trace information into the console when a command gets sent to Hypixel. (by any mod or the player)"
    )
    @ConfigEditorBoolean
    var commandLogs: Boolean = false

    @Expose
    @ConfigOption(
        name = "Mod Menu Log",
        desc = "Enable debug messages when the currently opened GUI changes, with the path to the gui class. " +
            "Useful for adding more mods to quick mod menu switch."
    )
    @ConfigEditorBoolean
    var modMenuLog: Boolean = false

    @Expose
    @ConfigOption(name = "Show Internal Name", desc = "Show internal names in item lore.")
    @ConfigEditorBoolean
    var showInternalName: Boolean = false

    @Expose
    @ConfigOption(name = "Show Empty Internal Names", desc = "Shows internal name even for items with none.")
    @ConfigEditorBoolean
    var showEmptyNames: Boolean = false

    @Expose
    @ConfigOption(name = "Show Item Rarity", desc = "Show item rarities in item lore.")
    @ConfigEditorBoolean
    var showItemRarity: Boolean = false

    @Expose
    @ConfigOption(name = "Show Item Category", desc = "Show item categories in item lore.")
    @ConfigEditorBoolean
    var showItemCategory: Boolean = false

    @Expose
    @ConfigOption(name = "Show Item Name", desc = "Show the SkyHanni item name for an item.")
    @ConfigEditorBoolean
    var showItemName: Boolean = false

    @Expose
    @ConfigOption(
        name = "Copy Internal Name",
        desc = "Copies the internal name of an item on key press in the clipboard."
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var copyInternalName: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigOption(name = "Show NPC Price", desc = "Show NPC price in item lore.")
    @ConfigEditorBoolean
    var showNpcPrice: Boolean = false

    @Expose
    @ConfigOption(name = "Show Base Values", desc = "Show item base values in item lore.")
    @ConfigEditorBoolean
    var showBaseValues: Boolean = false

    @Expose
    @ConfigOption(name = "Show Craft Price", desc = "Show craft price in item lore.")
    @ConfigEditorBoolean
    var showCraftPrice: Boolean = false

    @Expose
    @ConfigOption(name = "Show BZ Price", desc = "Show BZ price in item lore.")
    @ConfigEditorBoolean
    var showBZPrice: Boolean = false

    @Expose
    @ConfigOption(name = "Show Item UUID", desc = "Show the Unique Identifier of items in the lore.")
    @ConfigEditorBoolean
    var showItemUuid: Boolean = false

    @Expose
    @ConfigOption(name = "Copy Item Data", desc = "Copies item NBT data on key press in a GUI to clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var copyItemData: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigOption(
        name = "Copy Compressed Item Data",
        desc = "Copies compressed item NBT data on key press in a GUI to clipboard."
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var copyItemDataCompressed: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigOption(
        name = "Copy RNG Meter",
        desc = "Copies internal names and maxed XP needed from RNG meter inventories as json to clipboard."
    )
    @ConfigEditorBoolean
    var copyRngMeter: Boolean = false

    @Expose
    @ConfigOption(
        name = "Copy Bestiary Data",
        desc = "Copies the bestiary data from the inventory as json to clipboard."
    )
    @ConfigEditorBoolean
    var copyBestiaryData: Boolean = false

    @Expose
    @ConfigOption(
        name = "Highlight Missing Repo Items",
        desc = "Highlights each item in the current inventory that is not in your current NEU repo."
    )
    @ConfigEditorBoolean
    var highlightMissingRepo: Boolean = false

    @Expose
    @ConfigOption(name = "Hot Swap Detection", desc = "Show chat messages when Hot Swap starts and ends.")
    @ConfigEditorBoolean
    var hotSwapDetection: Boolean = false

    @Expose
    @ConfigOption(
        name = "Always Outdated",
        desc = "For the sake of the auto updater, act like you are always outdated."
    )
    @ConfigEditorBoolean
    var alwaysOutdated: Boolean = false

    @Expose
    @ConfigOption(
        name = "SkyHanni Event Counter",
        desc = "Count once per second how many skyhanni events gets triggered, show the total amount in console output."
    )
    @ConfigEditorBoolean
    var eventCounter: Boolean = false

    @Expose
    @ConfigOption(
        name = "Bypass Advanced Tab List",
        desc = "The Advanced Player Tab list is disabled while pressing this hotkey."
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var bypassAdvancedPlayerTabList: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigOption(name = "SkyBlock Area", desc = "Show your current area in SkyBlock while F3 is open.")
    @ConfigEditorBoolean
    var currentAreaDebug: Boolean = true

    // TODO rename to rayTracedOreBlock
    @Expose
    @ConfigOption(name = "OreBlock Name", desc = "Show the OreBlock you are currently looking at.")
    @ConfigEditorBoolean
    var raytracedOreblock: Boolean = true

    @Expose
    @ConfigOption(name = "Ore Event Messages", desc = "Shows debug messages every time the Ore Event happens.")
    @ConfigEditorBoolean
    var oreEventMessages: Boolean = false

    @Expose
    @ConfigOption(name = "Assume Mayor", desc = "Select a mayor to assume.")
    @ConfigEditorDropdown
    var assumeMayor: Property<ElectionCandidate> = Property.of(ElectionCandidate.DISABLED)

    @Expose
    @ConfigOption(name = "Always April Fools", desc = "Always show April fools jokes.")
    @ConfigEditorBoolean
    var alwaysFunnyTime: Boolean = false

    @Expose
    @ConfigOption(name = "Never April Fools", desc = "Admit it, you hate fun.")
    @ConfigEditorBoolean
    var neverFunnyTime: Boolean = false

    @Expose
    @ConfigOption(name = "Always Hoppity's", desc = "Always act as if Hoppity's Hunt is active.")
    @ConfigEditorBoolean
    var alwaysHoppitys: Boolean = false

    @Expose
    @ConfigOption(name = "Always Great Spook", desc = "Assumes the Great Spook is always active.")
    @ConfigEditorBoolean
    var forceGreatSpook: Property<Boolean> = Property.of(false)

    // Does not have a config element!
    @Expose
    var trackSoundPosition: Position = Position(0, 0)

    // Also does not have a config element!
    @Expose
    var trackParticlePosition: Position = Position(0, 0)
}
