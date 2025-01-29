package at.hannibal2.skyhanni.config.features.event.diana;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class DianaConfig {

    @Expose
    @ConfigOption(name = "Highlight Inquisitors", desc = "Highlight Inquisitors found from the Mythological Event perk.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightInquisitors = true;

    @Expose
    @ConfigOption(name = "Inquisitor Highlight", desc = "Color in which Inquisitors will be highlighted.")
    @ConfigEditorColour
    public String color = "0:127:85:255:255";

    @Expose
    @ConfigOption(name = "Guess Next Burrow", desc = "Use math from §eSoopy's Guess Logic §7to find the next burrow.\n" +
        "§eDoes not require SoopyV2 or ChatTriggers to be installed.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean burrowsSoopyGuess = false;

    @Expose
    @ConfigOption(name = "Nearby Detection", desc = "Show burrows near you.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean burrowsNearbyDetection = false;

    @Expose
    @ConfigOption(name = "Line To Next", desc = "Show a line to the closest burrow or guess location.\n" +
        "§eRequires Burrow particle detection.")
    @ConfigEditorBoolean
    public boolean lineToNext = true;

    @Expose
    @ConfigOption(name = "Nearest Warp", desc = "Warp to the nearest warp point on the hub, if closer to the next burrow.")
    @ConfigEditorBoolean
    public boolean burrowNearestWarp = false;

    @Expose
    @ConfigOption(name = "Warp Key", desc = "Press this key to warp to nearest burrow waypoint.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int keyBindWarp = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Ignored Warps", desc = "")
    @Accordion
    public IgnoredWarpsConfig ignoredWarps = new IgnoredWarpsConfig();

    @Expose
    @ConfigOption(name = "Inquisitor Waypoint Sharing", desc = "")
    @Accordion
    public InquisitorSharingConfig inquisitorSharing = new InquisitorSharingConfig();

    @Expose
    @ConfigOption(name = "Griffin Pet Warning", desc = "Warn when holding an Ancestral Spade if a Griffin Pet is not equipped.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean petWarning = true;

    @Expose
    @ConfigOption(name = "Diana Profit Tracker", desc = "")
    @Accordion
    public DianaProfitTrackerConfig dianaProfitTracker = new DianaProfitTrackerConfig();

    @Expose
    @ConfigOption(name = "Mythological Creature Tracker", desc = "")
    @Accordion
    // TODO rename mythologicalMobTracker
    public MythologicalMobTrackerConfig mythologicalMobtracker = new MythologicalMobTrackerConfig();

    @Expose
    @ConfigOption(name = "All Burrows List", desc = "")
    @Accordion
    public AllBurrowsListConfig allBurrowsList = new AllBurrowsListConfig();
}
