package at.hannibal2.skyhanni.config.features.inventory.chocolatefactory;

import at.hannibal2.skyhanni.utils.LorenzColor;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class ChocolateFactoryStrayRabbitWarningConfig {

    @Expose
    @ConfigOption(name = "Warning Level", desc = "Warn when stray rabbits of a certain tier appear.")
    @ConfigEditorDropdown
    public StrayTypeEntry rabbitWarningLevel = StrayTypeEntry.ALL;

    @Expose
    @ConfigOption(name = "Highlight Color", desc = "Choose the color that stray rabbits should be highlighted as.")
    @ConfigEditorColour
    public String inventoryHighlightColor = LorenzColor.RED.toConfigColor();

    @Expose
    @ConfigOption(name = "Warning Sound", desc = "The sound that plays for a special rabbit.\n" +
        "§eYou can use custom sounds, put it in the §bskyhanni/sounds §efolder in your resource pack.\n" +
        "§eThen write §bskyhanni:yourfilename\n" +
        "§cMust be a .ogg file")
    @ConfigEditorText
    public Property<String> specialRabbitSound = Property.of("note.pling");

    @Expose
    @ConfigOption(name = "Repeat Sound", desc = "How many times the sound should be repeated.")
    @ConfigEditorSlider(minValue = 1, maxValue = 20, minStep = 1)
    public int repeatSound = 20;

    @Expose
    @ConfigOption(name = "Flash Screen", desc = "Choose the stray rabbit type to flash the screen for.")
    @ConfigEditorDropdown
    public StrayTypeEntry flashScreenLevel = StrayTypeEntry.SPECIAL;

    public enum StrayTypeEntry {
        SPECIAL("Special Only"),
        LEGENDARY_P("§6Legendary§7+"),
        EPIC_P("§5Epic§7+"),
        RARE_P("§9Rare§7+"),
        UNCOMMON_P("§aUncommon§7+"),
        ALL("All"),
        NONE("None"),
        ;
        private final String str;

        StrayTypeEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Flash Color", desc = "Color of the screen when flashing")
    @ConfigEditorColour
    public String flashColor = "0:127:0:238:255";

    @ConfigOption(name = "Sounds", desc = "Click to open the list of available sounds.")
    @ConfigEditorButton(buttonText = "OPEN")
    public Runnable sounds = () -> OSUtils.openBrowser("https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments");

}
