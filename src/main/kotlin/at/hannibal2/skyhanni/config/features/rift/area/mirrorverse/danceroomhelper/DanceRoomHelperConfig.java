package at.hannibal2.skyhanni.config.features.rift.area.mirrorverse.danceroomhelper;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.features.rift.area.mirrorverse.danceroomhelper.danceroomformatting.DanceRoomFormattingConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DanceRoomHelperConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Help to solve the dance room in the Mirrorverse by showing multiple tasks at once.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Lines to Show", desc = "How many tasks you should see.")
    @ConfigEditorSlider(minStep = 1, maxValue = 49, minValue = 1)
    public int lineToShow = 3;

    @Expose
    @ConfigOption(name = "Space", desc = "Change the space between each line.")
    @ConfigEditorSlider(minStep = 1, maxValue = 10, minValue = -5)
    public int extraSpace = 0;

    @Expose
    @ConfigOption(name = "Hide Other Players", desc = "Hide other players inside the dance room.")
    @ConfigEditorBoolean
    public boolean hidePlayers = false;

    @Expose
    @ConfigOption(name = "Hide Title", desc = "Hide Instructions, \"§aIt's happening!\" §7and \"§aKeep it up!\" §7titles.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideOriginalTitle = false;

    @Expose
    @ConfigOption(name = "Formatting", desc = "")
    @Accordion
    public DanceRoomFormattingConfig danceRoomFormatting = new DanceRoomFormattingConfig();

    @Expose
    @ConfigLink(owner = DanceRoomHelperConfig.class, field = "enabled")
    public Position position = new Position(442, 239, false, true);
}
