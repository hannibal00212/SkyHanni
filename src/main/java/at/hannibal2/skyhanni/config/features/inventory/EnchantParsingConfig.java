package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.utils.LorenzColor;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class EnchantParsingConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Toggle for coloring the enchants. Turn this off if you want to use enchant parsing from other mods.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> colorParsing = Property.of(true);

    @Expose
    @ConfigOption(name = "Format", desc = "The way the enchants are formatted in the tooltip.")
    @ConfigEditorDropdown
    public Property<EnchantFormat> format = Property.of(EnchantFormat.NORMAL);

    public enum EnchantFormat {
        NORMAL("Normal"),
        COMPRESSED("Compressed"),
        STACKED("Stacked");

        public final String displayName;

        EnchantFormat(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Perfect Enchantment Color", desc = "The color an enchantment will be at max level. " +
        "§eIf SkyHanni chroma is disabled this will default to §6Gold.")
    @ConfigEditorDropdown
    public Property<LorenzColor> perfectEnchantColor = Property.of(LorenzColor.CHROMA);

    @Expose
    @ConfigOption(name = "Great Enchantment Color", desc = "The color an enchantment will be at a great level.")
    @ConfigEditorDropdown
    public Property<LorenzColor> greatEnchantColor = Property.of(LorenzColor.GOLD);

    @Expose
    @ConfigOption(name = "Good Enchantment Color", desc = "The color an enchantment will be at a good level.")
    @ConfigEditorDropdown
    public Property<LorenzColor> goodEnchantColor = Property.of(LorenzColor.BLUE);

    @Expose
    @ConfigOption(name = "Poor Enchantment Color", desc = "The color an enchantment will be at a poor level.")
    @ConfigEditorDropdown
    public Property<LorenzColor> poorEnchantColor = Property.of(LorenzColor.GRAY);

    @Expose
    @ConfigOption(name = "Comma Format", desc = "Change the format of the comma after each enchant.")
    @ConfigEditorDropdown
    public Property<CommaFormat> commaFormat = Property.of(CommaFormat.COPY_ENCHANT);

    public enum CommaFormat {
        COPY_ENCHANT("Copy enchant format"),
        DEFAULT("Default (Blue)");

        public final String displayName;

        CommaFormat(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Hide Vanilla Enchants", desc = "Hide the regular vanilla enchants usually found in the first 1-2 lines of lore.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> hideVanillaEnchants = Property.of(true);

    @Expose
    @ConfigOption(name = "Hide Enchant Description", desc = "Hide the enchant description after each enchant if available.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> hideEnchantDescriptions = Property.of(false);
}
