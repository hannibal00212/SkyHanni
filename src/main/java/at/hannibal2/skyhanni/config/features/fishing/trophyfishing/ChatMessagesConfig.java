package at.hannibal2.skyhanni.config.features.fishing.trophyfishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ChatMessagesConfig {

    @Expose
    @ConfigOption(
        name = "Trophy Counter",
        desc = "Count Trophy messages from chat and tells you how many you have found."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Trophy Counter Design",
        desc = "§fStyle 1: §72. §6§lGOLD §5Moldfin\n" +
            "§fStyle 2: §bYou caught a §5Moldfin §6§lGOLD§b. §7(2)\n" +
            "§fStyle 3: §bYou caught your 2nd §6§lGOLD §5Moldfin§b."
    )
    @ConfigEditorDropdown
    public DesignFormat design = DesignFormat.STYLE_1;

    public enum DesignFormat implements HasLegacyId {
        STYLE_1("Style 1", 0),
        STYLE_2("Style 2", 1),
        STYLE_3("Style 3", 2);
        private final String str;
        private final int legacyId;

        DesignFormat(String str, int legacyId) {
            this.str = str;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        DesignFormat(String str) {
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
    @ConfigOption(name = "Show Total Amount", desc = "Show total amount of all rarities at the end of the chat message.")
    @ConfigEditorBoolean
    public boolean totalAmount = false;

    @Expose
    @ConfigOption(name = "Trophy Fish Info", desc = "Show information and stats about a Trophy Fish when hovering over a catch message in chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean tooltip = true;

    @Expose
    @ConfigOption(name = "Hide Repeated Catches", desc = "Delete past catches of the same Trophy Fish from chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean duplicateHider = false;

    @Expose
    @ConfigOption(name = "Bronze Duplicates", desc = "Hide duplicate messages for bronze Trophy Fishes from chat.")
    @ConfigEditorBoolean
    public boolean bronzeHider = false;

    @Expose
    @ConfigOption(name = "Silver Duplicates", desc = "Hide duplicate messages for silver Trophy Fishes from chat.")
    @ConfigEditorBoolean
    public boolean silverHider = false;

    @Expose
    @ConfigOption(name = "Gold Alert", desc = "Send an alert upon catching a gold Trophy Fish.")
    @ConfigEditorBoolean
    public boolean goldAlert = false;

    @Expose
    @ConfigOption(name = "Diamond Alert", desc = "Send an alert upon catching a diamond Trophy Fish.")
    @ConfigEditorBoolean
    public boolean diamondAlert = false;

    @Expose
    @ConfigOption(name = "Play Sound Alert", desc = "Play a sound effect when rare trophy fishes are caught.")
    @ConfigEditorBoolean
    public boolean playSound = true;
}
