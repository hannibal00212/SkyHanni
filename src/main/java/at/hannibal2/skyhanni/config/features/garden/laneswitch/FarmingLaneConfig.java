package at.hannibal2.skyhanni.config.features.garden.laneswitch;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.garden.CropType;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class FarmingLaneConfig {

    @Expose
    @ConfigOption(name = "Create Lanes", desc = "In order for those features to work, you first need to create a lane with §e/shlanedetection§7!")
    @ConfigEditorInfoText(infoTitle = "Tutorial")
    public boolean tutorial = false;

    @Expose
    @ConfigOption(name = "Lane Switch Notification", desc = "")
    @Accordion
    public LaneSwitchNotificationConfig laneSwitchNotification = new LaneSwitchNotificationConfig();

    @Expose
    @ConfigOption(name = "Distance Display", desc = "Show the remaining distance and time until you reach the end of the current lane.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean distanceDisplay = false;

    @Expose
    @ConfigLink(owner = FarmingLaneConfig.class, field = "distanceDisplay")
    public Position distanceDisplayPosition = new Position(0, 200, false, true);

    @Expose
    @ConfigOption(name = "Corner Waypoints", desc = "Show the corner for the current lane in the world.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean cornerWaypoints = false;

    @Expose
    @ConfigOption(
        name = "Ignored Crops",
        desc = "Add the crops you wish to not setup a lane for."
    )
    @ConfigEditorDraggableList()
    public List<CropType> ignoredCrops = new ArrayList<>();
}
