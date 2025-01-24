package at.hannibal2.skyhanni.config.features.rift.area.mirrorverse;

import at.hannibal2.skyhanni.config.features.rift.area.mirrorverse.danceroomhelper.DanceRoomHelperConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MirrorVerseConfig {

    // Four Levers

    @ConfigOption(name = "Lava Maze", desc = "")
    @Accordion
    @Expose
    public LavaMazeConfig lavaMazeConfig = new LavaMazeConfig();

    @ConfigOption(name = "Crafting Room", desc = "")
    @Accordion
    @Expose
    public CraftingRoomConfig craftingRoom = new CraftingRoomConfig();

    @ConfigOption(name = "Upside-Down Parkour", desc = "")
    @Accordion
    @Expose
    public UpsideDownParkourConfig upsideDownParkour = new UpsideDownParkourConfig();

    // Red-Green Puzzle

    @ConfigOption(name = "Dance Room Helper", desc = "")
    @Accordion
    @Expose
    public DanceRoomHelperConfig danceRoomHelper = new DanceRoomHelperConfig();

    @ConfigOption(name = "Tubulator", desc = "")
    @Accordion
    @Expose
    public TubulatorConfig tubulatorConfig = new TubulatorConfig();
}
