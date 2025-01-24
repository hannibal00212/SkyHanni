package at.hannibal2.skyhanni.config.features.rift.area.mirrorverse;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class LavaMazeConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Help solve the lava maze in the Mirrorverse by showing the correct way.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Look Ahead", desc = "Change how many platforms should be shown in front of you.")
    @ConfigEditorSlider(minStep = 1, maxValue = 30, minValue = 1)
    public Property<Integer> lookAhead = Property.of(3);

    @Expose
    @ConfigOption(name = "Rainbow Color", desc = "Show the rainbow color effect instead of a boring monochrome.")
    @ConfigEditorBoolean
    public Property<Boolean> rainbowColor = Property.of(true);

    @Expose
    @ConfigOption(name = "Monochrome Color", desc = "Set a boring monochrome color for the parkour platforms.")
    @ConfigEditorColour
    public Property<String> monochromeColor = Property.of("0:60:0:0:255");

    @Expose
    @ConfigOption(name = "Hide Others Players", desc = "Hide other players while doing the lava maze.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hidePlayers = false;
}
