package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class DicerRngDropTrackerConfig {
    @Expose
    @ConfigOption(name = "Enable Tracker", desc = "Track RNG drops for Melon Dicer and Pumpkin Dicer.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = true;

    @Expose
    @ConfigOption(name = "Compact Format", desc = "Compact the Dicer RNG Drop Tracker Display.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> compact = Property.of(false);

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide the chat message when dropping a RNG Dicer drop.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideChat = false;

    @Expose
    @ConfigLink(owner = DicerRngDropTrackerConfig.class, field = "display")
    // TODO rename to "positon"
    public Position pos = new Position(16, -232, false, true);
}
