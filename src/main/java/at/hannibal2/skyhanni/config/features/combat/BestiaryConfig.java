package at.hannibal2.skyhanni.config.features.combat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BestiaryConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Show Bestiary Data overlay in the Bestiary menu.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Number format", desc = "Short: 1.1k\nLong: 1.100")
    @ConfigEditorDropdown
    public NumberFormatEntry numberFormat = NumberFormatEntry.SHORT;

    public enum NumberFormatEntry implements HasLegacyId {
        SHORT("Short", 0),
        LONG("Long", 1);

        private final String displayName;
        private final int legacyId;

        NumberFormatEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        NumberFormatEntry(String displayName) {
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
    @ConfigOption(name = "Display type", desc = "Choose what the display should show")
    @ConfigEditorDropdown
    public DisplayTypeEntry displayType = DisplayTypeEntry.GLOBAL_MAX;

    public enum DisplayTypeEntry implements HasLegacyId {
        GLOBAL_MAX("Global to max", 0),
        GLOBAL_NEXT("Global to next tier", 1),
        LOWEST_TOTAL("Lowest total kills", 2),
        HIGHEST_TOTAL("Highest total kills", 3),
        LOWEST_MAX("Lowest kills needed to max", 4),
        HIGHEST_MAX("Highest kills needed to max", 5),
        LOWEST_NEXT("Lowest kills needed to next tier", 6),
        HIGHEST_NEXT("Highest kills needed to next tier", 7);

        private final String displayName;
        private final int legacyId;

        DisplayTypeEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        DisplayTypeEntry(String displayName) {
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
    @ConfigOption(name = "Hide maxed", desc = "Hide maxed mobs.")
    @ConfigEditorBoolean
    public boolean hideMaxed = false;

    @Expose
    @ConfigOption(name = "Replace Romans", desc = "Replace Roman numerals (IX) with regular numbers (9)")
    @ConfigEditorBoolean
    public boolean replaceRoman = false;

    @Expose
    @ConfigLink(owner = BestiaryConfig.class, field = "enabled")
    public Position position = new Position(100, 100, false, true);
}
