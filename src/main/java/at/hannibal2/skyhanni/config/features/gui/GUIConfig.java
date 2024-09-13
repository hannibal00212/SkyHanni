package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.features.chroma.ChromaConfig;
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.CustomScoreboardConfig;
import at.hannibal2.skyhanni.config.features.markedplayer.MarkedPlayerConfig;
import at.hannibal2.skyhanni.config.features.misc.DiscordRPCConfig;
import at.hannibal2.skyhanni.config.features.misc.compacttablist.CompactTabListConfig;
import at.hannibal2.skyhanni.config.features.misc.cosmetic.CosmeticConfig;
import at.hannibal2.skyhanni.data.GuiEditManager;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class GUIConfig {

    @Expose
    @Category(name = "Compact Tab List", desc = "Compact Tab List Settings")
    @Accordion
    public CompactTabListConfig compactTabList = new CompactTabListConfig();

    @Expose
    @Category(name = "Custom Scoreboard", desc = "Custom Scoreboard Settings")
    public CustomScoreboardConfig customScoreboard = new CustomScoreboardConfig();

    @Expose
    @Category(name = "Chroma", desc = "Settings for Chroma text (Credit to SBA).")
    @Accordion
    public ChromaConfig chroma = new ChromaConfig();

    @ConfigOption(name = "Edit GUI Locations", desc = "Change the position of SkyHanni's overlays.")
    @ConfigEditorButton(buttonText = "Edit")
    public Runnable positions = () -> GuiEditManager.openGuiPositionEditor(true);

    @Expose
    @ConfigOption(name = "Open Hotkey", desc = "Press this key to open the GUI Editor.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int keyBindOpen = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Global GUI Scale", desc = "Globally scale all SkyHanni GUIs.")
    @ConfigEditorSlider(minValue = 0.1F, maxValue = 10, minStep = 0.05F)
    public float globalScale = 1F;

    @Expose
    @ConfigOption(name = "Time Format", desc = "Change SkyHanni to use 24h time instead of 12h time.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean timeFormat24h = false;

    @Expose
    @ConfigOption(name = "Discord Rich Presence", desc = "")
    @Accordion
    public DiscordRPCConfig discordRPC = new DiscordRPCConfig();

    @Expose
    @ConfigOption(name = "Hotbar", desc = "Settings for adjusting the hotbar")
    @Accordion
    public HotbarConfig hotbar = new HotbarConfig();

    @Expose
    @ConfigOption(name = "Xp Bar", desc = "Settings for adjusting the xp bar")
    @Accordion
    public XPBarConfig xpBar = new XPBarConfig();

    @Expose
    @ConfigOption(name = "Marked Players", desc = "Players that got marked with §e/shmarkplayer§7.")
    @Accordion
    public MarkedPlayerConfig markedPlayers = new MarkedPlayerConfig();

    @Expose
    @ConfigOption(name = "Modify Visual Words", desc = "")
    @Accordion
    public ModifyWordsConfig modifyWords = new ModifyWordsConfig();

    @Expose
    @ConfigOption(name = "Custom Text Box", desc = "")
    @Accordion
    public TextBoxConfig customTextBox = new TextBoxConfig();

    @Expose
    @ConfigOption(name = "Tab Widget", desc = "")
    @Accordion
    public TabWidgetConfig tabWidget = new TabWidgetConfig();

    @Expose
    @ConfigOption(name = "In-Game Date", desc = "")
    @Accordion
    public InGameDateConfig inGameDate = new InGameDateConfig();

    @Expose
    @ConfigOption(name = "Beacon Power", desc = "Display the current beacon power duration and what stat is boosted.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean beaconPower = false;

    @Expose
    @ConfigOption(name = "Show Beacon Stat", desc = "Show what stat is being boosted by your beacon.")
    @ConfigEditorBoolean
    public boolean beaconPowerStat = true;

    @Expose
    @ConfigLink(owner = GUIConfig.class, field = "beaconPower")
    public Position beaconPowerPosition = new Position(10, 10);

    @Expose
    @ConfigOption(name = "Real Time", desc = "Display the current computer time, a handy feature when playing in full-screen mode.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean realTime = false;

    @Expose
    @ConfigOption(name = "Real Time 12h Format", desc = "Display the current computer time in 12hr Format rather than 24h Format.")
    @ConfigEditorBoolean
    public boolean realTimeFormatToggle = false;

    @Expose
    @ConfigLink(owner = GUIConfig.class, field = "realTime")
    public Position realTimePosition = new Position(10, 10, false, true);

    @Expose
    @Category(name = "Cosmetic", desc = "Cosmetics Settings")
    public CosmeticConfig cosmetic = new CosmeticConfig();

    @Expose
    @ConfigOption(name = "TPS Display", desc = "Show the TPS of the current server, like in Soopy.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean tpsDisplay = false;

    @Expose
    @ConfigLink(owner = GUIConfig.class, field = "tpsDisplay")
    public Position tpsDisplayPosition = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Config Button", desc = "Add a button to the pause menu to configure SkyHanni.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean configButtonOnPause = true;
}
