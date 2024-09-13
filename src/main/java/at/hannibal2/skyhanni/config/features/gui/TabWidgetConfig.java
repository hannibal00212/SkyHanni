package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.PositionList;
import at.hannibal2.skyhanni.features.gui.TabWidgetDisplay;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class TabWidgetConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enables the gui elements for the selected widgets.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @ConfigOption(name = "Not working Info", desc = "If the information isn't shown in the tablist it won't show anything. Use /widget to turn on the information you need.")
    @ConfigEditorInfoText
    public String text1;

    @ConfigOption(name = "Enable Info", desc = "Drag only one new value at time into the list, since the default locations are all the same.")
    @ConfigEditorInfoText
    public String text2;

    @Expose
    @ConfigOption(name = "Widgets", desc = "")
    @ConfigEditorDraggableList
    public List<TabWidgetDisplay> display = new ArrayList<>();

    @Expose
    @ConfigLink(owner = TabWidgetConfig.class, field = "enabled")
    public PositionList displayPositions = new PositionList(TabWidgetDisplay.getEntries().size());
}
