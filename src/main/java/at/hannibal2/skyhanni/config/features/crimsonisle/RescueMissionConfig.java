package at.hannibal2.skyhanni.config.features.crimsonisle;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class RescueMissionConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a path to the hostage based on your quest rank. (You must hover over the book that gives you the quest and stand near the agent for the solver to work). §cDoes not work with S rank!")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Look Ahead", desc = "Change how many waypoints should be shown in front of you.")
    @ConfigEditorSlider(minValue = 1, maxValue = 10, minStep = 1)
    public Property<Integer> lookAhead = Property.of(2);

    @Expose
    @ConfigOption(name = "Rainbow Color", desc = "Show the rainbow color effect.")
    @ConfigEditorBoolean
    public Property<Boolean> chroma = Property.of(true);

    @Expose
    @ConfigOption(name = "Single Color", desc = "Make the waypoints an unchanging color for slow computers.")
    @ConfigEditorColour
    public Property<String> solidColor = Property.of("0:60:0:0:255");
}
