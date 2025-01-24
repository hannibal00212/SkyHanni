package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class GardenLevelConfig {
    @Expose
    @ConfigOption(name = "Display", desc = "Show the current Garden level and progress to the next level.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = false;

    @Expose
    @ConfigOption(name = "Overflow", desc = "Enable overflow Garden levels")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> overflow = Property.of(true);

    @Expose
    @ConfigOption(name = "Overflow Chat", desc = "Enable overflow Garden level up messages in chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean overflowChat = true;

    @Expose
    @ConfigLink(owner = GardenLevelConfig.class, field = "display")
    public Position pos = new Position(390, 40, false, true);
}
