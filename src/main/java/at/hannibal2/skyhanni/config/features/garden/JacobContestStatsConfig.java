package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JacobContestStatsConfig {

    @Expose
    @ConfigOption(
        name = "Contest Stats",
        desc = "Show contest stats during a contest and after a contest in chat."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean jacobContestSummary = true;

    @Expose
    @ConfigOption(name = "Contest Stats Text", desc = "Drag to change the order of the overlay."
    )
    @ConfigEditorDraggableList
    public List<ContestStatsTextEntry> text = new ArrayList<>(Arrays.asList(
        ContestStatsTextEntry.TITLE,
        ContestStatsTextEntry.START_TIME,
        ContestStatsTextEntry.BLOCKS_BROKEN,
        ContestStatsTextEntry.BPS,
        ContestStatsTextEntry.POSITION,
        ContestStatsTextEntry.PREDICTED_SCORE
    ));

    public enum ContestStatsTextEntry {
        TITLE("§e§lSugar Cane Contest Stats"),
        START_TIME("§7Started §b3s §7into contest"),
        PARTICIPATING_TIME("§7Participating for §b15m 25s"),
        BLOCKS_BROKEN("§7Blocks Broken: §e14,781"),
        BPS("§7Blocks per Second: §c19.94"),
        POSITION("§7Position: Top §b0.1%"),
        PREDICTED_SCORE("§7Predicted Score: §e915,430");

        private final String str;

        ContestStatsTextEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigLink(owner = JacobContestStatsConfig.class, field = "jacobContestSummary")
    public Position pos = new Position(0, 240, false, true);
}
