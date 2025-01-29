package at.hannibal2.skyhanni.config.features.skillprogress

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class SkillProgressBarConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable or disable the progress bar.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Textured Bar", desc = "Use a textured progress bar.\n§eCan be changed with a resource pack.")
    @ConfigEditorBoolean
    var useTexturedBar: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(
        name = "Chroma",
        desc = "Use the SBA like chroma effect on the bar.\n§eIf enabled, ignore the Bar Color setting."
    )
    @ConfigEditorBoolean
    var useChroma: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Bar Color", desc = "Color of the progress bar.\n§eIgnored if Chroma is enabled.")
    @ConfigEditorColour
    var barStartColor: String = "0:255:255:0:0"

    @Expose
    @ConfigOption(name = "Textured Bar", desc = "")
    @Accordion
    var texturedBar: TexturedBar = TexturedBar()

    class TexturedBar {
        @Expose
        @ConfigOption(name = "Used Texture", desc = "Choose what texture to use.")
        @ConfigEditorDropdown
        var usedTexture: Property<UsedTexture> = Property.of(UsedTexture.MATCH_PACK)

        enum class UsedTexture(private val displayName: String, val path: String) {
            MATCH_PACK("Match Resource Pack", "minecraft:textures/gui/icons.png"),
            CUSTOM_1("Texture 1", SkyHanniMod.MODID + ":bars/1.png"),
            CUSTOM_2("Texture 2", SkyHanniMod.MODID + ":bars/2.png"),
            CUSTOM_3("Texture 3", SkyHanniMod.MODID + ":bars/3.png"),
            CUSTOM_4("Texture 4", SkyHanniMod.MODID + ":bars/4.png"),
            CUSTOM_5("Texture 5", SkyHanniMod.MODID + ":bars/5.png"),
            ;

            override fun toString() = displayName
        }

        @Expose
        @ConfigOption(
            name = "Width",
            desc = "Modify the width of the bar.\n" +
                "§eDefault: 182\n" +
                "§c!!Does not work for now!!"
        )
        @ConfigEditorSlider(minStep = 1f, minValue = 16f, maxValue = 1024f)
        var width: Int = 182

        @Expose
        @ConfigOption(
            name = "Height",
            desc = "Modify the height of the bar.\n" +
                "§eDefault: 5\n" +
                "§c!!Does not work for now!!"
        )
        @ConfigEditorSlider(minStep = 1f, minValue = 3f, maxValue = 16f)
        var height: Int = 5
    }

    @Expose
    @ConfigOption(name = "Regular Bar", desc = "")
    @Accordion
    var regularBar: RegularBar = RegularBar()

    class RegularBar {
        @Expose
        @ConfigOption(name = "Width", desc = "Modify the width of the bar.")
        @ConfigEditorSlider(minStep = 1f, minValue = 100f, maxValue = 1000f)
        var width: Int = 182

        @Expose
        @ConfigOption(name = "Height", desc = "Modify the height of the bar.")
        @ConfigEditorSlider(minStep = 1f, minValue = 3f, maxValue = 15f)
        var height: Int = 6
    }
}
