package at.hannibal2.skyhanni.config.features.mining.nucleus;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class CrystalNucleusTrackerConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Crystal Nucleus Tracker overlay.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigLink(owner = CrystalNucleusTrackerConfig.class, field = "enabled")
    public Position position = new Position(20, 20, false, true);

    @Expose
    @ConfigOption(name = "Hide in Chocolate Factory", desc = "Hide tracker while the Chocolate Factory is open.")
    @ConfigEditorBoolean
    public boolean hideInCf = true;

    @Expose
    @ConfigOption(name = "Show Outside of Nucleus", desc = "Show the tracker anywhere in the Crystal Hollows.")
    @ConfigEditorBoolean
    public boolean showOutsideNucleus = false;

    @Expose
    @ConfigOption(name = "Profit Per", desc = "Show profit summary message for the completed nucleus run.")
    @ConfigEditorBoolean
    public boolean profitPer = true;

    @Expose
    @ConfigOption(name = "Profit Per Minimum", desc = "Only show items above this coin amount in the summary message hover.")
    @ConfigEditorSlider(minValue = 0, maxValue = 1000000, minStep = 5000)
    public int profitPerMinimum = 20000;

    @Expose
    @ConfigOption(name = "Professor Usage", desc = "Determine how cost for Sapphire Crystal is calculated.")
    @ConfigEditorDropdown
    public Property<ProfessorUsageType> professorUsage = Property.of(ProfessorUsageType.ROBOT_PARTS);

    public enum ProfessorUsageType {
        ROBOT_PARTS("§9Robot Parts"),
        PRECURSOR_APPARATUS("§5Precursor Apparatus"),
        ;

        private final String str;

        ProfessorUsageType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
