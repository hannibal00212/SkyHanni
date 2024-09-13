package at.hannibal2.skyhanni.config.features.chat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.ASCENSION_ROPE;
import static at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.JUNGLE_HEART;
import static at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.OIL_BARREL;
import static at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.SLUDGE_JUICE;
import static at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.TREASURITE;
import static at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.WISHING_COMPASS;
import static at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.YOGGIE;

public class PowderMiningFilterConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Hide messages while opening chests in the Crystal Hollows.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Powder", desc = "Hide §dGemstone §7and §aMithril §7Powder rewards under a certain amount." +
        "\n§a0§7: §aShow all\n§c60000§7: §cHide all"
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 60000, minStep = 500)
    public int powderFilterThreshold = 1000;

    @Expose
    @ConfigOption(
        name = "Essence", desc = "Hide §6Gold §7and §bDiamond §7Essence rewards under a certain amount." +
        "\n§a0§7: §aShow all\n§c20§7: §cHide all"
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 20, minStep = 1)
    public int essenceFilterThreshold = 5;

    public enum SimplePowderMiningRewardTypes {

        ASCENSION_ROPE("§9Ascension Rope"),
        WISHING_COMPASS("§aWishing Compass"),
        OIL_BARREL("§aOil Barrel"),
        PREHISTORIC_EGG("§fPrehistoric Egg"),
        PICKONIMBUS("§5Pickonimbus 2000"),
        JUNGLE_HEART("§6Jungle Heart"),
        SLUDGE_JUICE("§aSludge Juice"),
        YOGGIE("§aYoggie"),
        ROBOT_PARTS("§9Robot Parts"),
        TREASURITE("§5Treasurite"),
        ;

        private final String name;

        SimplePowderMiningRewardTypes(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(name = "Common Items", desc = "Hide reward messages for listed items.")
    @ConfigEditorDraggableList
    public List<SimplePowderMiningRewardTypes> simplePowderMiningTypes = new ArrayList<>(Arrays.asList(
        ASCENSION_ROPE,
        WISHING_COMPASS,
        OIL_BARREL,
        JUNGLE_HEART,
        SLUDGE_JUICE,
        YOGGIE,
        TREASURITE
    ));

    @Expose
    @ConfigOption(name = "Goblin Egg", desc = "Hide Goblin Egg rewards that are below a certain rarity.")
    @ConfigEditorDropdown
    public GoblinEggFilterEntry goblinEggs = GoblinEggFilterEntry.YELLOW_UP;

    public enum GoblinEggFilterEntry {
        SHOW_ALL("Show all"),
        HIDE_ALL("Hide all"),
        GREEN_UP("Show §aGreen §7and up"),
        YELLOW_UP("Show §eYellow §7and up"),
        RED_UP("Show §cRed §7and up"),
        BLUE_ONLY("Show §3Blue §7only");

        private final String name;

        GoblinEggFilterEntry(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(name = "Gemstones", desc = "")
    @Accordion
    // TODO remove config
    public PowderMiningGemstoneFilterConfig gemstoneFilterConfig = new PowderMiningGemstoneFilterConfig();

}
