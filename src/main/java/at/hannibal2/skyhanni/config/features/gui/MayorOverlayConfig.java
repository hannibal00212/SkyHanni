package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.gui.MayorOverlay;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class MayorOverlayConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Mayor Overlay.")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Appearance", desc = "Change the order of appearance of the Mayor Overlay.")
    @ConfigEditorDraggableList
    public List<MayorOverlay> mayorOverlay = new ArrayList<>(MayorOverlay.getEntries());

    @Expose
    @ConfigOption(name = "Show Perks", desc = "Show the perks of the mayor.")
    @ConfigEditorBoolean
    public boolean showPerks = true;

    @Expose
    @ConfigOption(name = "Spacing between UI Elements", desc = "Change the spacing between the UI element entries.")
    @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 1)
    public int spacing = 10;

    @Expose
    @ConfigOption(name = "Spacing between Candidates", desc = "Change the spacing between the candidates.")
    @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 1)
    public int candidateSpacing = 3;

    @Expose
    @ConfigLink(owner = MayorOverlayConfig.class, field = "enabled")
    public Position position = new Position(10, 10, false, true);

}
