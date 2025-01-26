package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class SeaCreatureTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Count the different sea creatures you catch.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigLink(owner = SeaCreatureTrackerConfig.class, field = "enabled")
    public Position position = new Position(20, 20, false, true);

    @Expose
    @ConfigOption(name = "Show Percentage", desc = "Show percentage how often what sea creature got caught.")
    @ConfigEditorBoolean
    public Property<Boolean> showPercentage = Property.of(false);

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide the chat messages when catching a sea creature.")
    @ConfigEditorBoolean
    public boolean hideChat = false;

    @Expose
    @ConfigOption(name = "Count Double", desc = "Count double hook catches as two catches.")
    @ConfigEditorBoolean
    public boolean countDouble = true;
}
