package at.hannibal2.skyhanni.config.features.misc.pets;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PetConfig {
    @Expose
    @ConfigOption(name = "Pet Display", desc = "Show the currently active pet.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = false;

    @Expose
    @ConfigLink(owner = PetConfig.class, field = "display")
    public Position displayPos = new Position(-330, -15, false, true);

    @Expose
    @ConfigOption(name = "Pet Experience Tooltip", desc = "")
    @Accordion
    public PetExperienceToolTipConfig petExperienceToolTip = new PetExperienceToolTipConfig();

    @Expose
    @ConfigOption(name = "Pet Nametag", desc = "")
    @Accordion
    public PetNametagConfig nametag = new PetNametagConfig();

    @Expose
    @ConfigOption(name = "Hide Autopet Messages", desc = "Hide the autopet messages from chat.\n" +
        "§eRequires the display to be enabled.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideAutopet = false;

    @Expose
    @ConfigOption(name = "Show Pet Item", desc = "Specify the pet items for which icons should be displayed next to pets.")
    @ConfigEditorDraggableList
    public List<PetItemsDisplay> petItemDisplay = new ArrayList<>(Arrays.asList(
        PetItemsDisplay.XP_SHARE,
        PetItemsDisplay.TIER_BOOST
    ));

    @Expose
    @ConfigOption(name = "Pet Item Scale", desc = "The scale at which the Pet Item will be displayed.")
    @ConfigEditorSlider(minValue = 0.7f, maxValue = 1.5f, minStep = 0.05f)
    public float petItemDisplayScale = 0.9f;


    public enum PetItemsDisplay {
        XP_SHARE("§5⚘", "Exp Share", "PET_ITEM_EXP_SHARE"),
        TIER_BOOST("§c●", "Tier Boost", "PET_ITEM_TIER_BOOST"),
        ;
        private final String str;
        public final String icon;
        public final String item;

        PetItemsDisplay(String icon, String name, String item) {
            this.icon = icon;
            this.item = item;
            this.str = icon + " §ffor " + name;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
