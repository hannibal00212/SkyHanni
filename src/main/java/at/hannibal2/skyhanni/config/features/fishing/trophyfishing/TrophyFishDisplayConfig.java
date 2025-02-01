package at.hannibal2.skyhanni.config.features.fishing.trophyfishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrophyFishDisplayConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show a display of all trophy fishes ever caught."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "When Show", desc = "Change when the trophy fish display should be visible in Crimson Isle.")
    @ConfigEditorDropdown
    public Property<WhenToShow> whenToShow = Property.of(WhenToShow.ALWAYS);

    public enum WhenToShow {
        ALWAYS("Always"),
        ONLY_IN_INVENTORY("In inventory"),
        ONLY_WITH_ROD_IN_HAND("Rod in hand"),
        ONLY_WITH_KEYBIND("On keybind"),
        ;

        private final String displayName;

        WhenToShow(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Keybind", desc = "")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int keybind = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(
        name = "Hunter Armor",
        desc = "Only show when wearing a full Hunter Armor."
    )
    @ConfigEditorBoolean
    public Property<Boolean> requireHunterArmor = Property.of(false);

    @Expose
    @ConfigOption(
        name = "Highlight New",
        desc = "Highlight new trophies green for couple seconds."
    )
    @ConfigEditorBoolean
    public Property<Boolean> highlightNew = Property.of(true);

    @Expose
    @ConfigOption(name = "Extra space", desc = "Space between each line of text.")
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 10,
        minStep = 1)
    public Property<Integer> extraSpace = Property.of(1);

    @Expose
    @ConfigOption(name = "Sorted By", desc = "Sorting type of items in sack.")
    @ConfigEditorDropdown
    public Property<TrophySorting> sortingType = Property.of(TrophySorting.ITEM_RARITY);

    public enum TrophySorting {
        ITEM_RARITY("Item Rarity"),
        TOTAL_AMOUNT("Total Amount"),
        BRONZE_AMOUNT("Bronze Amount"),
        SILVER_AMOUNT("Silver Amount"),
        GOLD_AMOUNT("Gold Amount"),
        DIAMOND_AMOUNT("Diamond Amount"),
        HIGHEST_RARITY("Highest Rarity"),
        NAME("Name Alphabetical"),
        ;

        private final String displayName;

        TrophySorting(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
        name = "Reverse Order",
        desc = "Reverse the sorting order."
    )
    @ConfigEditorBoolean
    public Property<Boolean> reverseOrder = Property.of(false);

    @Expose
    @ConfigOption(
        name = "Text Order",
        desc = "Drag text to change the line format."
    )
    @ConfigEditorDraggableList
    public Property<List<TextPart>> textOrder = Property.of(new ArrayList<>(Arrays.asList(
        TextPart.NAME,
        TextPart.ICON,
        TextPart.TOTAL,
        TextPart.BRONZE,
        TextPart.SILVER,
        TextPart.GOLD,
        TextPart.DIAMOND
    )));

    public enum TextPart {
        ICON("Item Icon"),
        NAME("Item Name"),
        BRONZE("Amount Bronze"),
        SILVER("Amount Silver"),
        GOLD("Amount Gold"),
        DIAMOND("Amount Diamond"),
        TOTAL("Amount Total"),
        ;

        private final String displayName;

        TextPart(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
        name = "Show ✖",
        desc = "Instead of the number 0, show §c✖ §7if not found."
    )
    @ConfigEditorBoolean
    public Property<Boolean> showCross = Property.of(false);

    @Expose
    @ConfigOption(
        name = "Show ✔",
        desc = "Instead of the exact numbers, show §e§l✔ §7if found."
    )
    @ConfigEditorBoolean
    public Property<Boolean> showCheckmark = Property.of(false);

    @Expose
    @ConfigOption(name = "Only Show Missing", desc = "Only show Trophy Fish that are still missing at this tier.")
    @ConfigEditorDropdown
    public Property<HideCaught> onlyShowMissing = Property.of(HideCaught.NONE);

    @Expose
    @ConfigOption(
        name = "Show If Caught Higher Tier",
        desc = "Show Trophy Fish missing at the chosen tier even if a higher tier has already been caught."
    )
    @ConfigEditorBoolean
    public Property<Boolean> showCaughtHigher = Property.of(false);

    public enum HideCaught {
        NONE("Show All"),
        BRONZE("Bronze"),
        SILVER("Silver"),
        GOLD("Gold"),
        DIAMOND("Diamond"),
        ;

        private final String displayName;

        HideCaught(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigLink(owner = TrophyFishDisplayConfig.class, field = "enabled")
    public Position position = new Position(144, 139, false, true);
}
