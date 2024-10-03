package at.hannibal2.skyhanni.config.features.rift.area.westvillage;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class GunthersRaceConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the route for Gunther's rift race.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Look Ahead", desc = "Change how many waypoints should be shown in front of you.")
    @ConfigEditorSlider(minStep = 1, maxValue = 30, minValue = 1)
    public Property<Integer> lookAhead = Property.of(3);

    @Expose
    @ConfigOption(name = "Rainbow Color", desc = "Show the rainbow color effect instead of a boring monochrome.")
    @ConfigEditorBoolean
    public Property<Boolean> rainbowColor = Property.of(true);

    @Expose
    @ConfigOption(name = "Monochrome Color", desc = "Set a boring monochrome color for the guide waypoints.")
    @ConfigEditorColour
    public Property<String> monochromeColor = Property.of("0:60:0:0:255");
}
