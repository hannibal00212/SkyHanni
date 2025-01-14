package at.hannibal2.skyhanni.config.features.garden.cropmilestones;

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

import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.MushroomPetPerkConfig.MushroomTextEntry.MUSHROOM_TIER;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.MushroomPetPerkConfig.MushroomTextEntry.NUMBER_OUT_OF_TOTAL;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.MushroomPetPerkConfig.MushroomTextEntry.TIME;
import static at.hannibal2.skyhanni.config.features.garden.cropmilestones.MushroomPetPerkConfig.MushroomTextEntry.TITLE;

// TODO moulconfig runnable support
public class MushroomPetPerkConfig {
    @Expose
    @ConfigOption(
        name = "Display Enabled",
        desc = "Show the progress and ETA for mushroom crops when farming other crops because of the Mooshroom Cow perk.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Mushroom Text",
        desc = "Drag text to change the appearance of the overlay.\n" +
            "Hold a farming tool to show the overlay."
    )
    @ConfigEditorDraggableList
    public List<MushroomTextEntry> text = new ArrayList<>(Arrays.asList(
        TITLE,
        MUSHROOM_TIER,
        NUMBER_OUT_OF_TOTAL,
        TIME
    ));

    public enum MushroomTextEntry implements HasLegacyId {
        TITLE("§6Mooshroom Cow Perk", 0),
        // TODO change to mushroom milestone
        MUSHROOM_TIER("§7Mushroom Milestone 8", 1),
        NUMBER_OUT_OF_TOTAL("§e6,700§8/§e15,000", 2),
        TIME("§7In §b12m 34s", 3),
        PERCENTAGE("§7Percentage: §e12.34%", 4),
        ;

        private final String displayName;
        private final int legacyId;

        MushroomTextEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        MushroomTextEntry(String displayName) {
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
    @ConfigLink(owner = MushroomPetPerkConfig.class, field = "enabled")
    public Position pos = new Position(-112, -143, false, true);
}
