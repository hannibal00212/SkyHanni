package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.mining.FlowstateElements;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.annotations.SearchTag;

import java.util.ArrayList;
import java.util.List;

public class FlowstateHelperConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Shows stats for the Flowstate enchantment on Mining Tools.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Appearance", desc = "Drag text to change the appearance.")
    @ConfigEditorDraggableList()
    public List<FlowstateElements> appearance = new ArrayList<>(FlowstateElements.defaultOption);

    @Expose
    @ConfigOption(name = "Dynamic Color", desc = "Makes the timer's color dynamic.")
    @ConfigEditorBoolean
    public boolean colorfulTimer = false;

    @Expose
    @ConfigOption(name = "Auto Hide", desc = "Automatically hides the GUI after being idle, in seconds.")
    @SearchTag("autohide")
    @ConfigEditorSlider(
        minValue = -1,
        maxValue = 30,
        minStep = 1
    )
    public int autoHide = 10;

    @Expose
    @ConfigLink(owner = FlowstateHelperConfig.class, field = "enabled")
    public Position position = new Position(-110 , 9);
}
