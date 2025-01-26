package at.hannibal2.skyhanni.config.features.combat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.COINS_MADE;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.ENCHANTED_ENDER_PEARL;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.ENCHANTED_END_STONE;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.ENCHANTED_OBSIDIAN;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.ENDERMAN_PET;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.ENDERMITE_NEST;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.ENDER_ARMOR;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.GRAND_XP_BOTTLE;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.MAGICAL_RUNE_I;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.MITE_GEL;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.NODES_MINED;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.SHRIMP_THE_FISH;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.SPACER_1;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.SPACER_2;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.TITANIC_XP_BOTTLE;
import static at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry.TITLE;

public class EnderNodeConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Tracks all of your drops from mining Ender Nodes in the End.\n" +
            "Also tracks drops from Endermen."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Only While Holding Tool",
        desc = "Only show the tracker if holding a pickaxe, drill or gauntlet in hand."
    )
    @ConfigEditorBoolean
    public boolean onlyPickaxe = false;

    @Expose
    @ConfigOption(
        name = "Text Format",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public Property<List<EnderNodeDisplayEntry>> textFormat = Property.of(new ArrayList<>(Arrays.asList(
        TITLE,
        NODES_MINED,
        COINS_MADE,
        SPACER_1,
        ENDERMITE_NEST,
        ENCHANTED_END_STONE,
        ENCHANTED_OBSIDIAN,
        ENCHANTED_ENDER_PEARL,
        GRAND_XP_BOTTLE,
        TITANIC_XP_BOTTLE,
        MAGICAL_RUNE_I,
        MITE_GEL,
        SHRIMP_THE_FISH,
        SPACER_2,
        ENDER_ARMOR,
        ENDERMAN_PET)
    ));

    public enum EnderNodeDisplayEntry implements HasLegacyId {
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

        private final String displayName;
        private final int legacyId;

        EnderNodeDisplayEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        EnderNodeDisplayEntry(String displayName) {
            this(displayName, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigLink(owner = EnderNodeConfig.class, field = "enabled")
    public Position position = new Position(10, 80, false, true);
}
