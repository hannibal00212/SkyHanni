package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class TotemOfCorruptionConfig {

    @Expose
    @ConfigOption(name = "Show Overlay", desc = "Show the Totem of Corruption overlay." +
        "\nShows the totem, in which effective area you are in, with the longest time left." +
        "\n§eThis needs to be enabled for the other options to work.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> showOverlay = Property.of(true);

    @Expose
    @ConfigOption(name = "Distance Threshold", desc = "The minimum distance to the Totem of Corruption for the overlay." +
        "\nThe effective distance of the totem is 16." +
        "\n§cLimited by how far you can see the nametags.")
    @ConfigEditorSlider(minValue = 0, maxValue = 100, minStep = 1)
    public int distanceThreshold = 16;

    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hide the particles of the Totem of Corruption.\n" +
        "§eRequires the Overlay to be active.")
    @ConfigEditorBoolean
    public boolean hideParticles = true;

    @Expose
    @ConfigOption(name = "Show Effective Area", desc = "Show the effective area (16 blocks) of the Totem of Corruption.")
    @ConfigEditorDropdown
    public OutlineType outlineType = OutlineType.FILLED;

    public enum OutlineType {
        NONE("No Outline"),
        FILLED("Filled"),
        WIREFRAME("Wireframe"),
        ;

        private final String str;

        OutlineType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Color of the area", desc = "The color of the area of the Totem of Corruption.")
    @ConfigEditorColour
    public String color = "0:153:18:159:85";

    @Expose
    @ConfigOption(name = "Warn when about to expire", desc = "Select the time in seconds when the totem is about to expire to warn you." +
        "\nSelect 0 to disable.")
    @ConfigEditorSlider(minValue = 0, maxValue = 60, minStep = 1)
    public int warnWhenAboutToExpire = 5;

    @Expose
    @ConfigLink(owner = TotemOfCorruptionConfig.class, field = "showOverlay")
    public Position position = new Position(50, 20, false, true);
}
