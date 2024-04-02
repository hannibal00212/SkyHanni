package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import at.hannibal2.skyhanni.features.gui.customscoreboard.ChunkedStats;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class ChunkedStatsConfig {

    @Expose
    @ConfigOption(
        name = "ChunkedStats",
        desc = "Select the stats you want to display chunked on the scoreboard."
    )
    @ConfigEditorDraggableList
    public List<ChunkedStats> chunkedStats =  new ArrayList<>(ChunkedStats.getEntries());

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
