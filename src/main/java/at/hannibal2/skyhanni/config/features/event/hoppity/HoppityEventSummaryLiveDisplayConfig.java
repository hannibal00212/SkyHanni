package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
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

        private final String str;

        HoppityLiveDisplayInventoryType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
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
}
