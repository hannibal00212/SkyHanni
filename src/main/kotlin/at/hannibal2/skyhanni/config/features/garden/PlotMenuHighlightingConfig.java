package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.utils.LorenzColor;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlotMenuHighlightingConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight plots based on their status.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Statuses", desc = "Change which statuses are enabled, and the hierarchy of them.")
    @ConfigEditorDraggableList
    public List<PlotStatusType> deskPlotStatusTypes = new ArrayList<>(Arrays.asList(
        PlotStatusType.CURRENT,
        PlotStatusType.PESTS,
        PlotStatusType.SPRAYS,
        PlotStatusType.LOCKED
    ));

    public enum PlotStatusType {
        PESTS("§cPests", LorenzColor.RED),
        SPRAYS("§6Sprays", LorenzColor.GOLD),
        LOCKED("§7Locked", LorenzColor.DARK_GRAY),
        CURRENT("§aCurrent plot", LorenzColor.GREEN),
        PASTING("§ePasting", LorenzColor.YELLOW),
        ;

        public final String name;
        public final LorenzColor highlightColor;

        PlotStatusType(String name, LorenzColor highlightColor) {
            this.name = name;
            this.highlightColor = highlightColor;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
