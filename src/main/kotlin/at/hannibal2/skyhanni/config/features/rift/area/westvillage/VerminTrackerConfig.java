package at.hannibal2.skyhanni.config.features.rift.area.westvillage;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class VerminTrackerConfig {
    @Expose
    @ConfigOption(name = "Show Counter", desc = "Count all §aSilverfish§7, §aSpiders, §7and §aFlies §7vacuumed.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Show Outside West Village", desc = "Show the Vermin Tracker in other areas of The Rift.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showOutsideWestVillage = false;

    @Expose
    @ConfigOption(name = "Show without Vacuum", desc = "Require having Turbomax Vacuum in your inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showWithoutVacuum = false;

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide the chat message when vacuuming a vermin.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideChat = false;

    @Expose
    @ConfigLink(owner = VerminTrackerConfig.class, field = "enabled")
    public Position position = new Position(16, -232, false, true);
}

