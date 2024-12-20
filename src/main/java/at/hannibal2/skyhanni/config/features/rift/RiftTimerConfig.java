package at.hannibal2.skyhanni.config.features.rift;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class RiftTimerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the remaining rift time, max time, percentage, and extra time changes.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Max Time", desc = "Show max time.")
    @ConfigEditorBoolean
    public Property<Boolean> maxTime = Property.of(true);

    @Expose
    @ConfigOption(name = "Percentage", desc = "Show percentage.")
    @ConfigEditorBoolean
    public Property<Boolean> percentage = Property.of(true);

    @Expose
    @ConfigLink(owner = RiftTimerConfig.class, field = "enabled")
    public Position timerPosition = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Nametag Format", desc = "Format the remaining rift time for other players in their nametag.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean nametag = true;

}
