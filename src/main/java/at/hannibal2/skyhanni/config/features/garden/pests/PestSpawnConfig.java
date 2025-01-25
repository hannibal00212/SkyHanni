package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PestSpawnConfig {

    @Expose
    @ConfigOption(
        name = "Chat Message Format",
        desc = "Change how the pest spawn chat message should be formatted.")
    @ConfigEditorDropdown
    public ChatMessageFormatEntry chatMessageFormat = ChatMessageFormatEntry.HYPIXEL;

    public enum ChatMessageFormatEntry implements HasLegacyId {
        HYPIXEL("Hypixel Style", 0),
        COMPACT("Compact", 1),
        DISABLED("Disabled", 2);
        private final String displayName;
        private final int legacyId;

        ChatMessageFormatEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        ChatMessageFormatEntry(String displayName) {
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
        name = "Show Title",
        desc = "Show a Title when a pest spawns."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showTitle = true;
}
