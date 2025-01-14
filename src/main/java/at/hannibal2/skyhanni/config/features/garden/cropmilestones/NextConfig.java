package at.hannibal2.skyhanni.config.features.garden.cropmilestones;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

// TODO moulconfig runnable support
public class NextConfig {
    @Expose
    @ConfigOption(
        name = "Best Crop Time",
        desc = "List all crops and their ETA till next milestone. Sorts for best crop for getting garden or SkyBlock levels.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean bestDisplay = false;

    // TODO moulconfig runnable support
    @Expose
    @ConfigOption(name = "Sort Type", desc = "Sort the crops by either garden or SkyBlock EXP.")
    @ConfigEditorDropdown
    public BestTypeEntry bestType = BestTypeEntry.GARDEN_EXP;

    public enum BestTypeEntry implements HasLegacyId {
        GARDEN_EXP("Garden Exp", 0),
        SKYBLOCK_EXP("SkyBlock Exp", 1),
        ;
        private final String displayName;
        private final int legacyId;

        BestTypeEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        BestTypeEntry(String displayName) {
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

    // TODO moulconfig runnable support
    @Expose
    @ConfigOption(name = "Only Show Top", desc = "Only show the top # crops.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 10,
        minStep = 1
    )
    public int showOnlyBest = 10;

    @Expose
    @ConfigOption(name = "Extend Top List", desc = "Add current crop to the list if its lower ranked than the set limit by extending the list.")
    @ConfigEditorBoolean
    public boolean showCurrent = true;

    // TODO moulconfig runnable support
    @Expose
    @ConfigOption(
        name = "Always On",
        desc = "Show the Best Display always while in the garden.")
    @ConfigEditorBoolean
    public boolean bestAlwaysOn = false;

    @Expose
    @ConfigOption(
        name = "Compact Display",
        desc = "A more compact best crop time: Removing the crop name and exp, hide the # number and using a more compact time format.")
    @ConfigEditorBoolean
    public boolean bestCompact = false;

    @Expose
    @ConfigOption(
        name = "Hide Title",
        desc = "Hide the 'Best Crop Time' line entirely.")
    @ConfigEditorBoolean
    public boolean bestHideTitle = false;

    @Expose
    @ConfigLink(owner = NextConfig.class, field = "bestDisplay")
    public Position displayPos = new Position(-200, -200, false, true);
}
