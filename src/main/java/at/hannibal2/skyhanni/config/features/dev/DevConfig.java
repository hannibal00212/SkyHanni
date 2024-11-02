package at.hannibal2.skyhanni.config.features.dev;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.features.dev.minecraftconsole.MinecraftConsoleConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class DevConfig {

    @Expose
    @ConfigOption(name = "Repository", desc = "")
    @Accordion
    public RepositoryConfig repo = new RepositoryConfig();

    @Expose
    @ConfigOption(name = "Debug", desc = "")
    @Accordion
    public DebugConfig debug = new DebugConfig();

    @Expose
    @ConfigOption(name = "Repo Pattern", desc = "")
    @Accordion
    public RepoPatternConfig repoPattern = new RepoPatternConfig();

    @Expose
    @ConfigOption(name = "Log Expiry Time", desc = "Deletes your SkyHanni logs after this time period in days.")
    @ConfigEditorSlider(minValue = 1, maxValue = 30, minStep = 1)
    public int logExpiryTime = 14;

    @Expose
    @ConfigOption(
        name = "Chat History Length",
        desc = "The number of messages to keep in memory for §e/shchathistory§7.\n" +
            "§cExcessively large values may cause memory allocation issues."
    )
    @ConfigEditorSlider(minValue = 100, maxValue = 5000, minStep = 10)
    public int chatHistoryLength = 100;

    @Expose
    @ConfigOption(name = "Slot Number", desc = "Show slot number in inventory while pressing this key.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int showSlotNumberKey = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "World Edit", desc = "Use wood axe or command /shworldedit to render a box, similar like the WorldEdit plugin.")
    @ConfigEditorBoolean
    public boolean worldEdit = false;

    @ConfigOption(name = "Parkour Waypoints", desc = "")
    @Accordion
    @Expose
    public WaypointsConfig waypoint = new WaypointsConfig();

    // Does not have a config element!
    @Expose
    public Position debugPos = new Position(10, 10, false, true);

    // Does not have a config element!
    @Expose
    public Position debugLocationPos = new Position(1, 160, false, true);

    // Does not have a config element!
    @Expose
    public Position debugItemPos = new Position(90, 70);

    @Expose
    @ConfigLink(owner = DebugConfig.class, field = "raytracedOreblock")
    public Position debugOrePos = new Position(1, 200, false, true);

    @Expose
    @ConfigOption(
        name = "Fancy Contributors",
        desc = "Marks §cSkyHanni's contributors §7fancy in the tab list. " +
            "§eThose are the folks that coded the mod for you for free :)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean fancyContributors = true;

    @Expose
    @ConfigOption(
        name = "Contributor Nametags",
        desc = "Makes SkyHanni contributors' nametags fancy too. "
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean contributorNametags = true;

    @Expose
    @ConfigOption(
        name = "Flip Contributors",
        desc = "Make SkyHanni contributors appear upside down in the world.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean flipContributors = true;

    @Expose
    @ConfigOption(
        name = "Spin Contributors",
        desc = "Make SkyHanni contributors spin around when you are looking at them. " +
            "§eRequires 'Flip Contributors' to be enabled.")
    @ConfigEditorBoolean
    public boolean rotateContributors = false;

    @Expose
    @ConfigOption(
        name = "SBA Contributors",
        desc = "Mark SBA Contributors the same way as SkyHanni contributors.")
    @ConfigEditorBoolean
    public boolean fancySbaContributors = false;

    @Expose
    @ConfigOption(
        name = "Number Format Override",
        desc = "Forces the number format to use the en_US locale.")
    @ConfigEditorBoolean
    public boolean numberFormatOverride = false;

    @Expose
    @Category(name = "Minecraft Console", desc = "Minecraft Console Settings")
    public MinecraftConsoleConfig minecraftConsoles = new MinecraftConsoleConfig();

    @Expose
    @Category(name = "Dev Tools", desc = "Tooling for devs")
    public DevToolConfig devTool = new DevToolConfig();

    @Expose
    @Category(name = "Debug Mob", desc = "Every Debug related to the Mob System")
    public DebugMobConfig mobDebug = new DebugMobConfig();

}
