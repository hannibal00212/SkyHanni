package at.hannibal2.skyhanni.config.features.rift.motes;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class RiftInventoryValueConfig {
    @Expose
    @ConfigOption(name = "Inventory Value", desc = "Show total Motes NPC price for the current opened inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Number Format Type", desc = "Short: 1.2M\n" +
        "Long: 1,200,000")
    @ConfigEditorDropdown
    public NumberFormatEntry formatType = NumberFormatEntry.SHORT;

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
    @ConfigLink(owner = RiftInventoryValueConfig.class, field = "enabled")
    public Position position = new Position(126, 156, false, true);
}
