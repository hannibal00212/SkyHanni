package at.hannibal2.skyhanni.config.features.garden.visitor;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.ACCEPTED;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.COINS_SPENT;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.COPPER;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.COPPER_DYE;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.DEDICATION_IV;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.DENIED;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.FARMING_EXP;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.GREEN_BANDANA;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.OVERGROWN_GRASS;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.SPACER_1;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.TITLE;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.TOTAL_VISITORS;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.VISITORS_BY_RARITY;

public class DropsStatisticsConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Tally statistics about visitors and the rewards you have received from them."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Text Format",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public List<DropsStatisticsTextEntry> textFormat = new ArrayList<>(Arrays.asList(
        TITLE,
        TOTAL_VISITORS,
        VISITORS_BY_RARITY,
        ACCEPTED,
        DENIED,
        SPACER_1,
        COPPER,
        FARMING_EXP,
        COINS_SPENT,
        OVERGROWN_GRASS,
        GREEN_BANDANA,
        DEDICATION_IV,
        COPPER_DYE
    ));

    /**
     * Do not change the order of the enums added to that list! New items are to be synced up with the implementation in GardenVisitorDropStatistics.drawDisplay.
     * Generic non VisitorReward stuff belongs in front of the first VisitorReward.
     */
    public enum DropsStatisticsTextEntry implements HasLegacyId {
        // generic stuff
        TITLE("§e§lVisitor Statistics", 0),
        TOTAL_VISITORS("§e1,636 Total", 1),
        VISITORS_BY_RARITY("§a1,172§f-§9382§f-§681§f-§d2§f-§c1", 2),
        ACCEPTED("§21,382 Accepted", 3),
        DENIED("§c254 Denied", 4),
        SPACER_1(" ", 5),
        COPPER("§c62,072 Copper", 6),
        FARMING_EXP("§33.2m Farming EXP", 7),
        COINS_SPENT("§647.2m Coins Spent", 8),
        SPACER_2(" ", 17),
        GARDEN_EXP("§212,600 Garden EXP", 18),
        BITS("§b4.2k Bits", 19),
        MITHRIL_POWDER("§220k Mithril Powder", 20),
        GEMSTONE_POWDER("§d18k Gemstone Powder", 21),

        // VisitorReward items
        FLOWERING_BOUQUET("§b23 §9Flowering Bouquet", 9),
        OVERGROWN_GRASS("§b4 §9Overgrown Grass", 10),
        GREEN_BANDANA("§b2 §5Green Bandana", 11),
        DEDICATION_IV("§b1 §9Dedication IV", 12),
        MUSIC_RUNE_I("§b6 §b◆ Music Rune I", 13),
        SPACE_HELMET("§b1 §cSpace Helmet", 14),
        CULTIVATING_I("§b1 §9Cultivating I", 15),
        REPLENISH_I("§b1 §9Replenish I", 16),
        DELICATE("§b1 §9Delicate V"),
        COPPER_DYE("§b1 §8Copper Dye"),
        ;

        private final String str;
        private final int legacyId;

        DropsStatisticsTextEntry(String str, int legacyId) {
            this.str = str;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        DropsStatisticsTextEntry(String str) {
            this(str, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Display Numbers First", desc = "Whether the number or drop name displays first.\n" +
        "§eNote: Will not update the preview above!")
    @ConfigEditorBoolean
    public boolean displayNumbersFirst = true;

    @Expose
    @ConfigOption(name = "Display Icons", desc = "Replace the drop names with icons.\n" +
        "§eNote: Will not update the preview above!")
    @ConfigEditorBoolean
    public boolean displayIcons = false;

    @Expose
    @ConfigOption(name = "Only on Barn Plot", desc = "Only show the overlay while on the Barn plot.")
    @ConfigEditorBoolean
    public boolean onlyOnBarn = true;

    @Expose
    @ConfigLink(owner = DropsStatisticsConfig.class, field = "enabled")
    public Position pos = new Position(5, 20, false, true);
}
