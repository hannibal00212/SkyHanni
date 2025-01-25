package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ChestValueConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable estimated value of chest.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Enabled in own Inventory", desc = "Enable the feature for your own inventory.")
    @ConfigEditorBoolean
    public boolean enableInOwnInventory = false;

    @Expose
    @ConfigOption(name = "Enabled in dungeons", desc = "Enable the feature in dungeons.")
    @ConfigEditorBoolean
    public boolean enableInDungeons = false;

    @Expose
    @ConfigOption(name = "Enable during Item Value", desc = "Show this display even if the Estimated Item Value is visible.")
    @ConfigEditorBoolean
    public boolean showDuringEstimatedItemValue = false;

    @Expose
    @ConfigOption(name = "Show Stacks", desc = "Show the item icon before name.")
    @ConfigEditorBoolean
    public boolean showStacks = true;

    @Expose
    @ConfigOption(name = "Display Type", desc = "Try to align everything to look nicer.")
    @ConfigEditorBoolean
    public boolean alignedDisplay = true;

    @Expose
    @ConfigOption(name = "Name Length", desc = "Reduce item name length to gain extra space on screen.\n§cCalculated in pixels!")
    @ConfigEditorSlider(minStep = 1, minValue = 100, maxValue = 150)
    public int nameLength = 100;

    @Expose
    @ConfigOption(name = "Highlight Slot", desc = "Highlight slot where the item is when you hover over it in the display.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enableHighlight = true;

    @Expose
    @ConfigOption(name = "Sorting Type", desc = "Price sorting type.")
    @ConfigEditorDropdown
    public SortingTypeEntry sortingType = SortingTypeEntry.DESCENDING;

    public enum SortingTypeEntry implements HasLegacyId {
        DESCENDING("Descending", 0),
        ASCENDING("Ascending", 1),
        ;
        private final String displayName;
        private final int legacyId;

        SortingTypeEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        SortingTypeEntry(String displayName) {
            this(displayName, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Value formatting Type", desc = "Format of the price.")
    @ConfigEditorDropdown
    public NumberFormatEntry formatType = NumberFormatEntry.SHORT;

    public enum NumberFormatEntry implements HasLegacyId {
        SHORT("Short", 0),
        LONG("Long", 1);

        private final String displayName;
        private final int legacyId;

        NumberFormatEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        NumberFormatEntry(String displayName) {
            this(displayName, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Item To Show", desc = "Choose how many items are displayed.\n" +
        "All items in the chest are still counted for the total value.")
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 54,
        minStep = 1
    )
    public int itemToShow = 15;

    @Expose
    @ConfigOption(name = "Hide below", desc = "Hide items with value below configured amount.\n" +
        "Items are still counted for the total value.")
    @ConfigEditorSlider(
        minValue = 50_000,
        maxValue = 10_000_000,
        minStep = 50_000
    )
    public int hideBelow = 100_000;

    @Expose
    @ConfigLink(owner = ChestValueConfig.class, field = "enabled")
    public Position position = new Position(107, 141, false, true);
}
