package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GardenUptimeConfig {
    @Expose
    @ConfigOption(name = "Enable Tracker", desc = "Track Garden Uptime")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = true;

    @Expose
    @ConfigLink(owner = GardenUptimeConfig.class, field = "display")
    public Position pos = new Position(5, -180, false, true);

    @Expose
    @ConfigOption(name = "Include Visitors", desc = "Include doing visitors in active farming time")
    public boolean includeVisitors = true;

    @Expose
    @ConfigOption(name = "Include Pests", desc = "Include doing pests in active farming time")
    public boolean includePests = true;

    @Expose
    @ConfigOption(name = "AFK Timeout", desc = "Stop tracking player uptime after this amount of time afk")
    @ConfigEditorSlider(
        minValue = 5,
        maxValue = 60,
        minStep = 1
    )
    public double afkTimeout = 15;


}
