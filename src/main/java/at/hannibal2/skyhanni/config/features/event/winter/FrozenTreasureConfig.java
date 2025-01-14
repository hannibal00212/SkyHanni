package at.hannibal2.skyhanni.config.features.event.winter;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.COMPACT_PROCS;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.ENCHANTED_ICE;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.ENCHANTED_PACKED_ICE;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.GLACIAL_FRAGMENT;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.GLACIAL_TALISMAN;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.GREEN_GIFT;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.ICE_PER_HOUR;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.RED_GIFT;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.SPACER_1;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.TITLE;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.TOTAL_ICE;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.TREASURES_MINED;
import static at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry.WHITE_GIFT;

public class FrozenTreasureConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Track all of your drops from Frozen Treasure in the Glacial Caves.\n" +
            "§eIce calculations are an estimate but are relatively accurate."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Text Format",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public List<FrozenTreasureDisplayEntry> textFormat = new ArrayList<>(Arrays.asList(
        TITLE,
        TREASURES_MINED,
        TOTAL_ICE,
        ICE_PER_HOUR,
        COMPACT_PROCS,
        SPACER_1,
        WHITE_GIFT,
        GREEN_GIFT,
        RED_GIFT,
        ENCHANTED_ICE,
        ENCHANTED_PACKED_ICE,
        GLACIAL_FRAGMENT,
        GLACIAL_TALISMAN
    ));

    public enum FrozenTreasureDisplayEntry implements HasLegacyId {
        TITLE("§e§lFrozen Treasure Tracker", 0),
        TREASURES_MINED("§61,636 Treasures Mined", 1),
        TOTAL_ICE("§33.2m Total Ice", 2),
        ICE_PER_HOUR("§3342,192 Ice/hr", 3),
        COMPACT_PROCS("§81,002 Compact Procs", 4),
        SPACER_1(" ", 5),
        WHITE_GIFT("§b182 §fWhite Gift", 6),
        GREEN_GIFT("§b94 §aGreen Gift", 7),
        RED_GIFT("§b17 §9§cRed Gift", 8),
        PACKED_ICE("§b328 §fPacked Ice", 9),
        ENCHANTED_ICE("§b80 §aEnchanted Ice", 10),
        ENCHANTED_PACKED_ICE("§b4 §9Enchanted Packed Ice", 11),
        ICE_BAIT("§b182 §aIce Bait", 12),
        GLOWY_CHUM_BAIT("§b3 §aGlowy Chum Bait", 13),
        GLACIAL_FRAGMENT("§b36 §5Glacial Fragment", 14),
        GLACIAL_TALISMAN("§b6 §fGlacial Talisman", 15),
        FROZEN_BAIT("§b20 §9Frozen Bait"),
        EINARY_RED_HOODIE("§b1 §cEinary's Red Hoodie"),
        SPACER_2(" ", 16);

        private final String displayName;
        private final int legacyId;

        FrozenTreasureDisplayEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        FrozenTreasureDisplayEntry(String displayName) {
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
    @ConfigOption(name = "Only in Glacial Cave", desc = "Only show the overlay while in the Glacial Cave.")
    @ConfigEditorBoolean
    public boolean onlyInCave = true;

    @Expose
    @ConfigOption(name = "Show as Drops", desc = "Multiply the numbers on the display by the base drop.\n" +
        "E.g. 3 Ice Bait -> 48 Ice Bait")
    @ConfigEditorBoolean
    public boolean showAsDrops = false;

    @Expose
    @ConfigOption(name = "Hide Chat Messages", desc = "Hide the chat messages from Frozen Treasures.")
    @ConfigEditorBoolean
    public boolean hideMessages = false;

    @Expose
    @ConfigLink(owner = FrozenTreasureConfig.class, field = "enabled")
    public Position position = new Position(10, 80, false, true);
}
