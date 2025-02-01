package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.HasLegacyId
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import java.util.*

class EnderNodeConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Tracks all of your drops from mining Ender Nodes in the End.\n" +
            "Also tracks drops from Endermen."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Only While Holding Tool",
        desc = "Only show the tracker if holding a pickaxe, drill or gauntlet in hand."
    )
    @ConfigEditorBoolean
    var onlyPickaxe: Boolean = false

    @Expose
    @ConfigOption(name = "Text Format", desc = "Drag text to change the appearance of the overlay.")
    @ConfigEditorDraggableList
    var textFormat: Property<MutableList<EnderNodeDisplayEntry>> = Property.of(
        mutableListOf(
            EnderNodeDisplayEntry.TITLE,
            EnderNodeDisplayEntry.NODES_MINED,
            EnderNodeDisplayEntry.COINS_MADE,
            EnderNodeDisplayEntry.SPACER_1,
            EnderNodeDisplayEntry.ENDERMITE_NEST,
            EnderNodeDisplayEntry.ENCHANTED_END_STONE,
            EnderNodeDisplayEntry.ENCHANTED_OBSIDIAN,
            EnderNodeDisplayEntry.ENCHANTED_ENDER_PEARL,
            EnderNodeDisplayEntry.GRAND_XP_BOTTLE,
            EnderNodeDisplayEntry.TITANIC_XP_BOTTLE,
            EnderNodeDisplayEntry.MAGICAL_RUNE_I,
            EnderNodeDisplayEntry.MITE_GEL,
            EnderNodeDisplayEntry.SHRIMP_THE_FISH,
            EnderNodeDisplayEntry.SPACER_2,
            EnderNodeDisplayEntry.ENDER_ARMOR,
            EnderNodeDisplayEntry.ENDERMAN_PET
        )
    )

    enum class EnderNodeDisplayEntry(private val displayName: String, private val legacyId: Int = -1) : HasLegacyId {
        TITLE("§5§lEnder Node Tracker", 0),
        NODES_MINED("§d1,303 Ender Nodes Mined", 1),
        COINS_MADE("§615.3M Coins Made", 2),
        SPACER_1(" ", 3),
        ENDERMITE_NEST("§b123 §cEndermite Nest", 4),
        ENCHANTED_END_STONE("§b832 §aEnchanted End Stone", 5),
        ENCHANTED_OBSIDIAN("§b230 §aEnchanted Obsidian", 6),
        ENCHANTED_ENDER_PEARL("§b1630 §aEnchanted Ender Pearl", 7),
        GRAND_XP_BOTTLE("§b85 §aGrand Experience Bottle", 8),
        TITANIC_XP_BOTTLE("§b4 §9Titanic Experience Bottle", 9),
        END_STONE_SHULKER("§b15 §9End Stone Shulker", 10),
        END_STONE_GEODE("§b53 §9End Stone Geode", 11),
        MAGICAL_RUNE_I("§b10 §d◆ Magical Rune I", 12),
        ENDER_GAUNTLET("§b24 §5Ender Gauntlet", 13),
        MITE_GEL("§b357 §5Mite Gel", 14),
        SHRIMP_THE_FISH("§b2 §cShrimp The Fish", 15),
        SPACER_2(" ", 16),
        ENDER_ARMOR("§b200 §5Ender Armor", 17),
        ENDER_HELMET("§b24 §5Ender Helmet", 18),
        ENDER_CHESTPLATE("§b24 §5Ender Chestplate", 19),
        ENDER_LEGGINGS("§b24 §5Ender Leggings", 20),
        ENDER_BOOTS("§b24 §5Ender Boots", 21),
        ENDER_NECKLACE("§b24 §5Ender Necklace", 22),
        ENDERMAN_PET("§f10§7-§a8§7-§93§7-§52§7-§61 §fEnderman Pet", 23),
        ;

        override fun getLegacyId() = legacyId
        override fun toString() = displayName
    }

    @Expose
    @ConfigLink(owner = EnderNodeConfig::class, field = "enabled")
    var position: Position = Position(10, 80, false, true)
}
