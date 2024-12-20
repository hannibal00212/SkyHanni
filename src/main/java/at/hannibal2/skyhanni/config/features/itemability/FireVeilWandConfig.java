package at.hannibal2.skyhanni.config.features.itemability;

import at.hannibal2.skyhanni.config.HasLegacyId;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FireVeilWandConfig {
    @Expose
    @ConfigOption(name = "Fire Veil Design", desc = "Change the flame particles of the Fire Veil Wand ability.")
    @ConfigEditorDropdown
    public DisplayEntry display = DisplayEntry.PARTICLES;

    public enum DisplayEntry implements HasLegacyId {
        PARTICLES("Particles", 0),
        LINE("Line", 1),
        OFF("Off", 2),
        ;
        private final String str;
        private final int legacyId;

        DisplayEntry(String str, int legacyId) {
            this.str = str;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        DisplayEntry(String str) {
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
    @ConfigOption(
        name = "Line Color",
        desc = "Change the color of the Fire Veil Wand line."
    )
    @ConfigEditorColour
    public String displayColor = "0:245:255:85:85";
}
