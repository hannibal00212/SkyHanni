package at.hannibal2.skyhanni.config.features.skillprogress;

import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class SkillProgressBarConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable or disable the progress bar.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Textured Bar", desc = "Use a textured progress bar.\n§eCan be changed with a resource pack.")
    @ConfigEditorBoolean
    public Property<Boolean> useTexturedBar = Property.of(false);

    @Expose
    @ConfigOption(name = "Chroma", desc = "Use the SBA like chroma effect on the bar.\n§eIf enabled, ignore the Bar Color setting.")
    @ConfigEditorBoolean
    public Property<Boolean> useChroma = Property.of(false);

    @Expose
    @ConfigOption(name = "Bar Color", desc = "Color of the progress bar.\n§eIgnored if Chroma is enabled.")
    @ConfigEditorColour
    public String barStartColor = "0:255:255:0:0";

    @Expose
    @ConfigOption(name = "Textured Bar", desc = "")
    @Accordion
    public TexturedBar texturedBar = new TexturedBar();

    public static class TexturedBar {

        @Expose
        @ConfigOption(name = "Used Texture", desc = "Choose what texture to use.")
        @ConfigEditorDropdown
        public Property<UsedTexture> usedTexture = Property.of(UsedTexture.MATCH_PACK);

        public enum UsedTexture {
            MATCH_PACK("Match Resource Pack", "minecraft:textures/gui/icons.png"),
            CUSTOM_1("Texture 1", SkyHanniMod.MODID + ":bars/1.png"),
            CUSTOM_2("Texture 2", SkyHanniMod.MODID + ":bars/2.png"),
            CUSTOM_3("Texture 3", SkyHanniMod.MODID + ":bars/3.png"),
            CUSTOM_4("Texture 4", SkyHanniMod.MODID + ":bars/4.png"),
            CUSTOM_5("Texture 5", SkyHanniMod.MODID + ":bars/5.png"),
            ;

            private final String displayName;
            private final String path;

            UsedTexture(String displayName, String path) {
                this.displayName = displayName;
                this.path = path;
            }

            public String getPath() {
                return path;
            }

            @Override
            public String toString() {
                return displayName;
            }
        }

        @Expose
        @ConfigOption(name = "Width", desc = "Modify the width of the bar.\n" +
            "§eDefault: 182\n" +
            "§c!!Do not work for now!!")
        @ConfigEditorSlider(minStep = 1, minValue = 16, maxValue = 1024)
        public int width = 182;

        @Expose
        @ConfigOption(name = "Height", desc = "Modify the height of the bar.\n" +
            "§eDefault: 5\n" +
            "§c!!Do not work for now!!")
        @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 16)
        public int height = 5;
    }

    @Expose
    @ConfigOption(name = "Regular Bar", desc = "")
    @Accordion
    public RegularBar regularBar = new RegularBar();

    public static class RegularBar {
        @Expose
        @ConfigOption(name = "Width", desc = "Modify the width of the bar.")
        @ConfigEditorSlider(minStep = 1, minValue = 100, maxValue = 1000)
        public int width = 182;

        @Expose
        @ConfigOption(name = "Height", desc = "Modify the height of the bar.")
        @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 15)
        public int height = 6;
    }
}
