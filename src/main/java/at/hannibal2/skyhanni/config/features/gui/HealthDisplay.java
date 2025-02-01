package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class HealthDisplay {
    @Expose
    @ConfigOption(name = "Textured", desc = "")
    @Accordion
    public Textured texture = new Textured();

    @Expose
    @ConfigOption(
        name = "enable bar",
        desc = "show bar"
    )
    @ConfigEditorBoolean
    public Boolean enabledBar = false;

    @Expose
    @ConfigLink(owner = HealthDisplay.class, field = "enabledBar")
    public Position positionBar = new Position(40, 40, 1.0f);

    @Expose
    @ConfigOption(
        name = "enable text",
        desc = "show text"
    )
    @ConfigEditorBoolean
    public Boolean enabledText = false;

    @Expose
    @ConfigLink(owner = HealthDisplay.class, field = "enabledText")
    public Position positionText = new Position(40, 40, 1.0f);

    @Expose
    @ConfigOption(
        name = "health predictor",
        desc = "faster; less precise"
    )
    @ConfigEditorBoolean
    public Boolean predictHealth = true;

    @Expose
    @ConfigOption(
        name = "health updates",
        desc = "it shows health updates"
    )
    @ConfigEditorBoolean
    public Boolean healthUpdates = false;

    @Expose
    @ConfigOption(
        name = "rift dynamic maxhp",
        desc = "it makes maxhp dynamic in rift (wow)"
    )
    @ConfigEditorBoolean
    public Boolean riftDynamicHP = false;

    @Expose
    @ConfigOption(
        name = "hide action bar",
        desc = "a"
    )
    @ConfigEditorBoolean
    public Boolean hideActionBar = false;

    @Expose
    @ConfigOption(
        name = "hide vanilla hp",
        desc = "b"
    )
    @ConfigEditorBoolean
    public Property<Boolean> hideVanillaHP = Property.of(false);

    @Expose
    @ConfigOption(name = "Width", desc = "Modify the width of the bar.\n" +
        "§eDefault: 182\n")
    @ConfigEditorSlider(minStep = 1, minValue = 16, maxValue = 1024)
    public int width = 182;

    @Expose
    @ConfigOption(name = "Height", desc = "Modify the height of the bar.\n" +
        "§eDefault: 5\n")
    @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 16)
    public int height = 5;

    public static class Textured {
        @Expose
        @ConfigOption(name = "Use Texture", desc = "Toggles between using a texture or a regular bar.")
        @ConfigEditorBoolean
        public Boolean enabled = false;

        @ConfigOption(name = "§cWarning", desc = "While \"Use Texture\" is enabled, the width and height are not adjustable.")
        @ConfigEditorInfoText
        public String warning = "";

        @Expose
        @ConfigOption(name = "Used Texture", desc = "Choose what texture to use.")
        @ConfigEditorDropdown
        public Property<UsedTexture> texture = Property.of(UsedTexture.MATCH_PACK);

        public enum UsedTexture {
            MATCH_PACK("Match Resource Pack", "minecraft:textures/gui/icons.png"),
            CUSTOM_1("Texture 1", SkyHanniMod.MODID + ":bars/1.png"),
            CUSTOM_2("Texture 2", SkyHanniMod.MODID + ":bars/2.png"),
            CUSTOM_3("Texture 3", SkyHanniMod.MODID + ":bars/3.png"),
            CUSTOM_4("Texture 4", SkyHanniMod.MODID + ":bars/4.png"),
            CUSTOM_5("Texture 5", SkyHanniMod.MODID + ":bars/5.png"),
            ;

            private final String str;
            private final String path;

            UsedTexture(String str, String path) {
                this.str = str;
                this.path = path;
            }

            public String getPath() {
                return path;
            }

            @Override
            public String toString() {
                return str;
            }
        }
    }
}
