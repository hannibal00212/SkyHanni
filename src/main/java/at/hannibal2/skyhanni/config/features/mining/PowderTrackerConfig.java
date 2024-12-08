package at.hannibal2.skyhanni.config.features.mining;

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

import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.AMBER;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.AMETHYST;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.DIAMOND_ESSENCE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.DOUBLE_POWDER;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.ELECTRON;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.FTX;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.GEMSTONE_POWDER;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.GOLD_ESSENCE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.HARD_STONE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.JADE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.ROBOTRON;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.RUBY;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.SAPPHIRE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.SPACER_1;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.SPACER_2;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.SPACER_3;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.TOPAZ;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.TOTAL_CHESTS;

public class PowderTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Powder Tracker overlay for mining.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Only when Grinding", desc = "Only show the overlay when powder grinding.")
    @ConfigEditorBoolean
    public boolean onlyWhenPowderGrinding = false;

    @Expose
    @ConfigOption(
        name = "Text Format",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public Property<List<PowderDisplayEntry>> textFormat = Property.of(new ArrayList<>(Arrays.asList(
        TOTAL_CHESTS,
        DOUBLE_POWDER,
        GEMSTONE_POWDER,
        SPACER_1,
        DIAMOND_ESSENCE,
        GOLD_ESSENCE,
        SPACER_2,
        HARD_STONE,
        SPACER_3,
        RUBY,
        SAPPHIRE,
        AMBER,
        AMETHYST,
        JADE,
        TOPAZ,
        FTX,
        ELECTRON,
        ROBOTRON
    )));

    public enum PowderDisplayEntry implements HasLegacyId {
        TOTAL_CHESTS("§d852 Total chests Picked §7(950/h)", 2),
        DOUBLE_POWDER("§bx2 Powder: §aActive!", 3),
        GEMSTONE_POWDER("§b250,420 §dGemstone Powder §7(350,000/h)", 5),
        SPACER_1("", 6),
        DIAMOND_ESSENCE("§b129 §bDiamond Essence §7(600/h)", 7),
        GOLD_ESSENCE("§b234 §6Gold Essence §7(700/h)", 8),
        SPACER_2("", 9),
        HARD_STONE("§b1000 §fHard Stone §bCompacted §7(500/h)"),
        SPACER_3(""),
        RUBY("§50§7-§90§7-§a0§f-0 §cRuby Gemstone", 10),
        SAPPHIRE("§50§7-§90§7-§a0§f-0 §bSapphire Gemstone", 11),
        AMBER("§50§7-§90§7-§a0§f-0 §6Amber Gemstone", 12),
        AMETHYST("§50§7-§90§7-§a0§f-0 §5Amethyst Gemstone", 13),
        JADE("§50§7-§90§7-§a0§f-0 §aJade Gemstone", 14),
        TOPAZ("§50§7-§90§7-§a0§f-0 §eTopaz Gemstone", 15),
        FTX("§b14 §9FTX 3070", 16),
        ELECTRON("§b14 §9Electron Transmitter", 17),
        ROBOTRON("§b14 §9Robotron Reflector", 18),
        SUPERLITE("§b14 §9Superlite Motor", 19),
        CONTROL_SWITCH("§b14 §9Control Switch", 20),
        SYNTHETIC_HEART("§b14 §9Synthetic Heart", 21),
        TOTAL_ROBOT_PARTS("§b14 §9Total Robot Parts", 22),
        GOBLIN_EGGS("§30§7-§c0§7-§e0§f-§a0§f-§90 §fGoblin Egg", 23),
        WISHING_COMPASS("§b12 §aWishing Compass", 24),
        SLUDGE_JUICE("§b320 §aSludge Juice", 25),
        ASCENSION_ROPE("§b2 §9Ascension Rope", 26),
        TREASURITE("§b6 §5Treasurite", 27),
        JUNGLE_HEART("§b4 §6Jungle Heart", 28),
        PICKONIMBUS("§b1 §5Pickonimbus 2000", 29),
        YOGGIE("§b14 §aYoggie", 30),
        PREHISTORIC_EGG("§b9 §fPrehistoric Egg", 31),
        OIL_BARREL("§b25 §aOil Barrel", 32),
        ;

        private final String str;
        private final int legacyId;

        PowderDisplayEntry(String str, int legacyId) {
            this.str = str;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        PowderDisplayEntry(String str) {
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
    @ConfigLink(owner = PowderTrackerConfig.class, field = "enabled")
    public Position position = new Position(-274, 0, false, true);

}
