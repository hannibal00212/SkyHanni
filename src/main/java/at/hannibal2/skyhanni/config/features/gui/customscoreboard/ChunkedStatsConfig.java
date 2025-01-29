package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import at.hannibal2.skyhanni.features.gui.customscoreboard.ChunkedStatsLine;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class ChunkedStatsConfig {

    @Expose
    @ConfigOption(
        name = "Chunked Stats",
        desc = "Select the stats you want to display chunked on the scoreboard."
    )
    @ConfigEditorDraggableList
    public List<ChunkedStatsLine> chunkedStats = new ArrayList<>(ChunkedStatsLine.getEntries());

    @Expose
    @ConfigOption(
        name = "Max Stats per Line",
        desc = "The maximum amount of stats that will be displayed in one line."
    )
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 10,
        minStep = 1)
    public int maxStatsPerLine = 3;
}
