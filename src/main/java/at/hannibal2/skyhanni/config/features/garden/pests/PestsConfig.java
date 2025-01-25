package at.hannibal2.skyhanni.config.features.garden.pests;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PestsConfig {

    @Expose
    @ConfigOption(name = "Pest Spawn", desc = "")
    @Accordion
    public PestSpawnConfig pestSpawn = new PestSpawnConfig();

    @Expose
    @ConfigOption(name = "Pest Trap", desc = "")
    @Accordion
    public PestTrapConfig pestTrap = new PestTrapConfig();

    @Expose
    @ConfigOption(name = "Pest Finder", desc = "")
    @Accordion
    public PestFinderConfig pestFinder = new PestFinderConfig();

    @Expose
    @ConfigOption(name = "Pest Waypoint", desc = "")
    @Accordion
    public PestWaypointConfig pestWaypoint = new PestWaypointConfig();

    @Expose
    @ConfigOption(name = "Pest Timer", desc = "")
    @Accordion
    public PestTimerConfig pestTimer = new PestTimerConfig();

    @Expose
    @ConfigOption(name = "Pest Profit Tracker", desc = "")
    @Accordion
    public PestProfitTrackerConfig pestProfitTacker = new PestProfitTrackerConfig();

    @Expose
    @ConfigOption(name = "Spray", desc = "")
    @Accordion
    public SprayConfig spray = new SprayConfig();

    @ConfigOption(name = "Stereo Harmony", desc = "")
    @Accordion
    @Expose
    public StereoHarmonyConfig stereoHarmony = new StereoHarmonyConfig();
}
