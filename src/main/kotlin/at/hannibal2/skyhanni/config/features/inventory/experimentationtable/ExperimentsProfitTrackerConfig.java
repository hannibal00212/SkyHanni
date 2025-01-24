package at.hannibal2.skyhanni.config.features.inventory.experimentationtable;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentMessages;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class ExperimentsProfitTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Tracker for drops/XP you get from experiments.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Hide Messages", desc = "Change the messages to be hidden after completing Add-on/Main experiments.")
    @ConfigEditorDraggableList
    public List<ExperimentMessages> hideMessages = new ArrayList<>();

    @Expose
    @ConfigOption(name = "Time displayed", desc = "Time displayed after completing an experiment.")
    @ConfigEditorSlider(minValue = 5, maxValue = 60, minStep = 1)
    public int timeDisplayed = 30;

    @Expose
    @ConfigLink(owner = ExperimentsProfitTrackerConfig.class, field = "enabled")
    public Position position = new Position(20, 20, false, true);
}
