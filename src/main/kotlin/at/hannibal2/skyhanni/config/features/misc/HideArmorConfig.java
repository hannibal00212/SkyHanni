package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.HasLegacyId;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HideArmorConfig {

    @Expose
    @ConfigOption(name = "Mode", desc = "Hide the armor of players.")
    @ConfigEditorDropdown
    public ModeEntry mode = ModeEntry.OFF;

    public enum ModeEntry implements HasLegacyId {
        ALL("All", 0),
        OWN("Own Armor", 1),
        OTHERS("Other's Armor", 2),
        OFF("Off", 3);
        private final String str;
        private final int legacyId;

        ModeEntry(String str, int legacyId) {
            this.str = str;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        ModeEntry(String str) {
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
    @ConfigOption(name = "Only Helmet", desc = "Only hide the helmet.")
    @ConfigEditorBoolean
    public Boolean onlyHelmet = false;

}
