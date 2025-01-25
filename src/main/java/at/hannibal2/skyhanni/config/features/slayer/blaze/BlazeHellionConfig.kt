package at.hannibal2.skyhanni.config.features.slayer.blaze;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BlazeHellionConfig {
    @Expose
    @ConfigOption(name = "Colored Mobs", desc = "Color the Blaze Slayer boss and the demons in the right hellion shield color.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean coloredMobs = false;

    @Expose
    @ConfigOption(name = "Blaze Daggers", desc = "Faster and permanent display for the Blaze Slayer daggers.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean daggers = false;

    @Expose
    @ConfigOption(name = "Right Dagger", desc = "Mark the right dagger to use for Blaze Slayer in the dagger overlay.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean markRightHellionShield = false;

    @Expose
    @ConfigOption(name = "First Dagger", desc = "Select the first, left sided dagger for the display.")
    @ConfigEditorDropdown
    public FirstDaggerEntry firstDagger = FirstDaggerEntry.SPIRIT_OR_CRYSTAL;

    public enum FirstDaggerEntry implements HasLegacyId {
        SPIRIT_OR_CRYSTAL("Spirit/Crystal", 0),
        ASHEN_OR_AURIC("Ashen/Auric", 1),
        ;
        private final String str;
        private final int legacyId;

        FirstDaggerEntry(String str, int legacyId) {
            this.str = str;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        FirstDaggerEntry(String str) {
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
    @ConfigOption(name = "Hide Chat", desc = "Remove the wrong Blaze Slayer dagger messages from chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideDaggerWarning = false;

    @Expose
    @ConfigLink(owner = BlazeHellionConfig.class, field = "daggers")
    public Position positionTop = new Position(-475, 173, 4.4f, true);

    @Expose
    @ConfigLink(owner = BlazeHellionConfig.class, field = "daggers")
    public Position positionBottom = new Position(-475, 230, 3.2f, true);
}
