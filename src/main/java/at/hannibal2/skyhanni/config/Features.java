package at.hannibal2.skyhanni.config;

import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.config.features.About;
import at.hannibal2.skyhanni.config.features.chat.ChatConfig;
import at.hannibal2.skyhanni.config.features.combat.CombatConfig;
import at.hannibal2.skyhanni.config.features.crimsonisle.CrimsonIsleConfig;
import at.hannibal2.skyhanni.config.features.dev.DevConfig;
import at.hannibal2.skyhanni.config.features.dungeon.DungeonConfig;
import at.hannibal2.skyhanni.config.features.event.EventConfig;
import at.hannibal2.skyhanni.config.features.fishing.FishingConfig;
import at.hannibal2.skyhanni.config.features.garden.GardenConfig;
import at.hannibal2.skyhanni.config.features.gui.GUIConfig;
import at.hannibal2.skyhanni.config.features.inventory.InventoryConfig;
import at.hannibal2.skyhanni.config.features.mining.MiningConfig;
import at.hannibal2.skyhanni.config.features.misc.MiscConfig;
import at.hannibal2.skyhanni.config.features.rift.RiftConfig;
import at.hannibal2.skyhanni.config.features.skillprogress.SkillProgressConfig;
import at.hannibal2.skyhanni.config.features.slayer.SlayerConfig;
import at.hannibal2.skyhanni.config.storage.Storage;
import at.hannibal2.skyhanni.utils.LorenzUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.Social;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.gui.HorizontalAlign;
import io.github.notenoughupdates.moulconfig.processor.ProcessedCategory;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;


public class Features extends Config {
    public static final ResourceLocation DISCORD = new ResourceLocation("notenoughupdates:social/discord.png");
    public static final ResourceLocation GITHUB = new ResourceLocation("notenoughupdates:social/github.png");
    public static final ResourceLocation PATREON = new ResourceLocation("notenoughupdates:social/patreon.png");

    @Override
    public boolean shouldAutoFocusSearchbar() {
        return true;
    }

    @Override
    public HorizontalAlign alignCategory(ProcessedCategory category, boolean isSelected) {
        if (LorenzUtils.INSTANCE.isAprilFoolsDay())
            return HorizontalAlign.RIGHT;
        return super.alignCategory(category, isSelected);
    }

    @Override
    public List<Social> getSocials() {
        return Arrays.asList(
            Social.forLink("Discord", DISCORD, "https://discord.com/invite/skyhanni-997079228510117908"),
            Social.forLink("GitHub", GITHUB, "https://github.com/hannibal002/SkyHanni"),
            Social.forLink("Patreon", PATREON, "https://www.patreon.com/hannibal2")
        );
    }

    @Override
    public void saveNow() {
        SkyHanniMod.configManager.saveConfig(ConfigFileType.FEATURES, "close-gui");
    }

    @Override
    public String getTitle() {
        String modName = "SkyHanni";
        if (LorenzUtils.INSTANCE.isAprilFoolsDay())
            modName = new StringBuilder().append("اسکای هانی").reverse().toString(); // Minecraft does not render RTL strings very nicely, so we reverse the string here. Not authentic, but close enough.

        return modName + " " + SkyHanniMod.VERSION + " by §channibal2§r, config by §5Moulberry §rand §5nea89";
    }

    /*
     * If you are adding a new category, please insert it alphabetically
     * The only exceptions to this are About and GUI, which are pinned to the top
     * and Misc and Dev, which are to be at the bottom. Thanks!
     */


    // Top

    @Expose
    @Category(name = "About", desc = "Information about SkyHanni and updates.")
    public About about = new About();

    @Expose
    @Category(name = "GUI", desc = "Change the locations of GUI elements (§e/sh gui§7).")
    public GUIConfig gui = new GUIConfig();

    // Islands

    @Expose
    @Category(name = "Garden", desc = "Features for the Garden island.")
    public GardenConfig garden = new GardenConfig();

    @Expose
    @Category(name = "Crimson Isle", desc = "Things to do on the Crimson Isle/Nether island.")
    public CrimsonIsleConfig crimsonIsle = new CrimsonIsleConfig();

    @Expose
    @Category(name = "The Rift", desc = "Features for The Rift dimension.")
    public RiftConfig rift = new RiftConfig();

    // Skills

    @Expose
    @Category(name = "Fishing", desc = "Fishing stuff.")
    public FishingConfig fishing = new FishingConfig();

    @Expose
    @Category(name = "Mining", desc = "Features that help you break blocks.")
    public MiningConfig mining = new MiningConfig();

    // Combat like

    @Expose
    @Category(name = "Combat", desc = "Everything combat and PvE related.")
    public CombatConfig combat = new CombatConfig();

    @Expose
    @Category(name = "Slayer", desc = "Slayer features.")
    public SlayerConfig slayer = new SlayerConfig();

    @Expose
    @Category(name = "Dungeon", desc = "Features that change the Dungeons experience in The Catacombs.")
    public DungeonConfig dungeon = new DungeonConfig();

    // Misc

    @Expose
    @Category(name = "Inventory", desc = "Change the behavior of items and the inventory.")
    public InventoryConfig inventory = new InventoryConfig();

    @Expose
    @Category(name = "Events", desc = "Stuff that is not always available.")
    public EventConfig event = new EventConfig();

    @Expose
    @Category(name = "Skill Progress", desc = "Skill Progress related config options.")
    public SkillProgressConfig skillProgress = new SkillProgressConfig();

    @Expose
    @Category(name = "Chat", desc = "Change how the chat looks.")
    public ChatConfig chat = new ChatConfig();

    @Expose
    @Category(name = "Misc", desc = "Settings without a category.")
    public MiscConfig misc = new MiscConfig();

    // Bottom

    @Expose
    @Category(name = "Dev", desc = "Debug and test stuff. Developers are cool.")
    public DevConfig dev = new DevConfig();

    @Expose
    public Storage storage = new Storage();

    @Expose
    public int lastVersion = ConfigUpdaterMigrator.CONFIG_VERSION;

}
