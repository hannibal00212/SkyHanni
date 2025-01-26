package at.hannibal2.skyhanni.config.features.garden.cropmilestones;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.MilestoneTextEntry.BLOCKS_PER_SECOND;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.MilestoneTextEntry.CROPS_PER_MINUTE;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.MilestoneTextEntry.MILESTONE_TIER;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.MilestoneTextEntry.NUMBER_OUT_OF_TOTAL;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.MilestoneTextEntry.TIME;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.MilestoneTextEntry.TITLE;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.TimeFormatEntry.YEAR;

public class CropMilestonesConfig {
    @Expose
    @ConfigOption(
        name = "Progress Display",
        desc = "Show the progress and ETA until the next crop milestone is reached and the current crops/minute value.\n" +
            "§eRequires a tool with either a counter or Cultivating enchantment for full accuracy."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean progress = true;

    @Expose
    @ConfigOption(name = "Overflow", desc = "")
    @Accordion
    public CropMilestonesOverflowConfig overflow = new CropMilestonesOverflowConfig();

    @Expose
    @ConfigOption(
        name = "Warn When Close",
        desc = "Warn with title and sound when the next crop milestone upgrade happens in 5 seconds. " +
            "Useful for switching to a different pet for leveling.")
    @ConfigEditorBoolean
    public boolean warnClose = false;

    @Expose
    @ConfigOption(
        name = "Time Format",
        desc = "Change the highest time unit to show (1h30m vs 90min)")
    @ConfigEditorDropdown
    public Property<TimeFormatEntry> highestTimeFormat = Property.of(YEAR);

    public enum TimeFormatEntry implements HasLegacyId {
        YEAR("Year", 0),
        DAY("Day", 1),
        HOUR("Hour", 2),
        MINUTE("Minute", 3),
        SECOND("Second", 4),
        ;

        private final String displayName;
        private final int legacyId;

        TimeFormatEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        TimeFormatEntry(String displayName) {
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
    @ConfigOption(
        name = "Maxed Milestone",
        desc = "Calculate the progress and ETA till maxed milestone (46) instead of next milestone.")
    @ConfigEditorBoolean
    public Property<Boolean> bestShowMaxedNeeded = Property.of(false);

    @Expose
    @ConfigOption(
        name = "Milestone Text",
        desc = "Drag text to change the appearance of the overlay.\n" +
            "Hold a farming tool to show the overlay."
    )
    @ConfigEditorDraggableList
    public List<MilestoneTextEntry> text = new ArrayList<>(Arrays.asList(
        TITLE,
        MILESTONE_TIER,
        NUMBER_OUT_OF_TOTAL,
        TIME,
        CROPS_PER_MINUTE,
        BLOCKS_PER_SECOND
    ));

    public enum MilestoneTextEntry implements HasLegacyId {
        TITLE("§6Crop Milestones", 0),
        MILESTONE_TIER("§7Pumpkin Tier 22", 1),
        NUMBER_OUT_OF_TOTAL("§e12,300§8/§e100,000", 2),
        TIME("§7In §b12m 34s", 3),
        CROPS_PER_SECOND("§7Crops/Second§8: §e205.75"),
        CROPS_PER_MINUTE("§7Crops/Minute§8: §e12,345", 4),
        CROPS_PER_HOUR("§7Crops/Hour§8: §e740,700"),
        BLOCKS_PER_SECOND("§7Blocks/Second§8: §e19.85", 5),
        PERCENTAGE("§7Percentage: §e12.34%", 6),
        ;

        private final String displayName;
        private final int legacyId;

        MilestoneTextEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        MilestoneTextEntry(String displayName) {
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
    @ConfigOption(name = "Block Broken Precision", desc = "The amount of decimals displayed in blocks/second.")
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 6,
        minStep = 1
    )
    public int blocksBrokenPrecision = 2;

    @Expose
    @ConfigOption(name = "Seconds Before Reset", desc = "How many seconds of not farming until blocks/second resets.")
    @ConfigEditorSlider(
        minValue = 2,
        maxValue = 60,
        minStep = 1
    )
    public int blocksBrokenResetTime = 5;

    @Expose
    @ConfigLink(owner = CropMilestonesConfig.class, field = "progress")
    public Position progressDisplayPos = new Position(-400, -200, false, true);

    @Expose
    @ConfigOption(name = "Best Crop", desc = "")
    @Accordion
    public NextConfig next = new NextConfig();

    @Expose
    @ConfigOption(name = "Mushroom Pet Perk", desc = "")
    @Accordion
    public MushroomPetPerkConfig mushroomPetPerk = new MushroomPetPerkConfig();

}
