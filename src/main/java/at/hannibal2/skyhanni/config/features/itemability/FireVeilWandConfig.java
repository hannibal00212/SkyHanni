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
        private final String displayName;
        private final int legacyId;

        DisplayEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        DisplayEntry(String displayName) {
            this(displayName, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return displayName;
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
