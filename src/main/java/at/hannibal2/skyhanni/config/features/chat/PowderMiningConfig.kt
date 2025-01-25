package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PowderMiningConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Hide messages while opening chests in the Crystal Hollows.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Powder",
        desc = "Hide §dGemstone §7and §aMithril §7Powder rewards under a certain amount.\n" +
            "§a0§7: §aShow all\n" +
            "§c60000§7: §cHide all"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 60000f, minStep = 500f)
    var powderThreshold: Int = 1000

    @Expose
    @ConfigOption(
        name = "Essence",
        desc = "Hide §6Gold §7and §bDiamond §7Essence rewards under a certain amount.\n" +
            "§a0§7: §aShow all\n" +
            "§c20§7: §cHide all"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 20f, minStep = 1f)
    var essenceThreshold: Int = 5

    enum class SimplePowderMiningRewardTypes(val displayName: String) {
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

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Common Items", desc = "Hide reward messages for listed items.")
    @ConfigEditorDraggableList
    var simplePowderMiningTypes: List<SimplePowderMiningRewardTypes> = listOf(
        SimplePowderMiningRewardTypes.ASCENSION_ROPE,
        SimplePowderMiningRewardTypes.WISHING_COMPASS,
        SimplePowderMiningRewardTypes.OIL_BARREL,
        SimplePowderMiningRewardTypes.JUNGLE_HEART,
        SimplePowderMiningRewardTypes.SLUDGE_JUICE,
        SimplePowderMiningRewardTypes.YOGGIE,
        SimplePowderMiningRewardTypes.TREASURITE
    )

    @Expose
    @ConfigOption(name = "Goblin Egg", desc = "Hide Goblin Egg rewards that are below a certain rarity.")
    @ConfigEditorDropdown
    var goblinEggs: GoblinEggEntry = GoblinEggEntry.YELLOW_UP

    enum class GoblinEggEntry(val displayName: String) {
        SHOW_ALL("Show all"),
        HIDE_ALL("Hide all"),
        GREEN_UP("Show §aGreen §7and up"),
        YELLOW_UP("Show §eYellow §7and up"),
        RED_UP("Show §cRed §7and up"),
        BLUE_ONLY("Show §3Blue §7only");

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Gemstones", desc = "")
    @Accordion
    var gemstone: PowderMiningGemstoneConfig = PowderMiningGemstoneConfig()
}
