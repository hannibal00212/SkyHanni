package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HoppityEventSummaryLiveDisplayConfig {

    @Expose
    @ConfigOption(name = "Show Display", desc = "Show a hoppity stats card in a GUI element.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Note",
        desc = "§cNote§7: This card will mirror the stat list that is defined in the Hoppity Event Summary config."
    )
    @ConfigEditorInfoText
    public boolean mirrorConfigNote = false;

    public enum HoppityDateTimeDisplayType {
        CURRENT("Current Event"),
        PAST_EVENTS("Past Events"),
        NEXT_EVENT("Next Event"),
        ;

        private final String displayName;

        HoppityDateTimeDisplayType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
        name = "Date/Time Display",
        desc = "Display the date and time of the event in the header, for the current event, past events, or the next event.\n" +
            "§cNote§7: The Next Event option will only appear if Next Event is added here."
    )
    @ConfigEditorDraggableList
    public List<HoppityDateTimeDisplayType> dateTimeDisplay = new ArrayList<>(Collections.singletonList(
        HoppityDateTimeDisplayType.CURRENT
    ));

    public enum HoppityDateTimeFormat {
        RELATIVE("Relative"),
        ABSOLUTE("Absolute"),
        ;

        private final String displayName;

        HoppityDateTimeFormat(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Date Time Format", desc = "The format of the date and time.")
    @ConfigEditorDropdown
    public HoppityDateTimeFormat dateTimeFormat = HoppityDateTimeFormat.RELATIVE;

    @Expose
    @ConfigOption(name = "Card Toggle Keybind", desc = "Toggle the GUI element with this keybind.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int toggleKeybind = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(
        name = "Specific Inventories",
        desc = "§cOnly§r show the card while in certain inventories." +
            "\n§eIf the list is empty, the card will show in all inventories."
    )
    @ConfigEditorDraggableList
    public List<HoppityLiveDisplayInventoryType> specificInventories = new ArrayList<>(Arrays.asList(
        HoppityLiveDisplayInventoryType.NO_INVENTORY,
        HoppityLiveDisplayInventoryType.CHOCOLATE_FACTORY
    ));

    public enum HoppityLiveDisplayInventoryType {
        NO_INVENTORY("No Inventory"),
        OWN_INVENTORY("Own Inventory"),
        CHOCOLATE_FACTORY("Chocolate Factory"),
        HOPPITY("Hoppity"),
        MEAL_EGGS("Meal Eggs"),
        ;

        private final String displayName;

        HoppityLiveDisplayInventoryType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Only During Event", desc = "§cOnly§r show the card while Hoppity's Hunt is active.")
    @ConfigEditorBoolean
    public boolean onlyDuringEvent = true;

    @Expose
    @ConfigOption(name = "Only Holding Egglocator", desc = "§cOnly§r show the card when holding an Egglocator.")
    @ConfigEditorBoolean
    public boolean mustHoldEggLocator = false;

    @Expose
    @ConfigOption(
        name = "Only Hoppity Islands",
        desc = "§cOnly§r show the card while on Islands that spawn Hoppity Eggs (will not show on Garden, Island, Dungeons etc.)."
    )
    @ConfigEditorBoolean
    public boolean onlyHoppityIslands = false;
}
