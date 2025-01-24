package at.hannibal2.skyhanni.config.features.combat.ghostcounter;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.combat.ghosttracker.GhostTracker;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GhostProfitTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enables the Ghost Profit Tracker.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Display Text",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public List<GhostTracker.GhostTrackerLines> ghostTrackerText = new ArrayList<>(Arrays.asList(
        GhostTracker.GhostTrackerLines.KILLS,
        GhostTracker.GhostTrackerLines.GHOSTS_SINCE_SORROW,
        GhostTracker.GhostTrackerLines.MAX_KILL_COMBO,
        GhostTracker.GhostTrackerLines.COMBAT_XP_GAINED,
        GhostTracker.GhostTrackerLines.AVERAGE_MAGIC_FIND,
        GhostTracker.GhostTrackerLines.BESTIARY_KILLS
    ));

    @Expose
    @ConfigOption(name = "Max Bestiary", desc = "§7This feature will currently not work properly when having max Ghost Bestiary.")
    @ConfigEditorInfoText(infoTitle = "Warning")
    public String useless;

    @Expose
    @ConfigLink(owner = GhostProfitTrackerConfig.class, field = "enabled")
    public Position position = new Position(50, 50, false, true);
}
