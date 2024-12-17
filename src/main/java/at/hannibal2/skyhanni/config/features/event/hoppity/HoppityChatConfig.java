package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityChatConfig {

    @Expose
    @ConfigOption(name = "Compact Chat", desc = "Compact chat events when finding a Hoppity Egg.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compact = false;

    @Expose
    @ConfigOption(name = "Compacted Rarity", desc = "Show rarity of found rabbit in Compacted chat messages.")
    @ConfigEditorDropdown
    public CompactRarityTypes rarityInCompact = CompactRarityTypes.NEW;

    public enum CompactRarityTypes {
        NONE("Neither"),
        NEW("New Rabbits"),
        DUPE("Duplicate Rabbits"),
        BOTH("New & Duplicate Rabbits"),
        ;

        private final String name;

        CompactRarityTypes(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(
        name = "Compact Hitman",
        desc = "Show a summary message instead of individual messages for mass (>1) Hitman claims." +
            "\n§cRequires Compact Chat enabled to work."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactHitman = false;

    @Expose
    @ConfigOption(name = "Show Duplicate Count", desc = "Show the number of previous finds of a duplicate Hoppity rabbit in chat messages.")
    @ConfigEditorBoolean
    public boolean showDuplicateNumber = false;

    @Expose
    @ConfigOption(name = "Recolor Time-Towered Chocolate", desc = "Recolor raw chocolate gain from duplicate rabbits while Time Tower is active.")
    @ConfigEditorBoolean
    public boolean recolorTTChocolate = false;

    @Expose
    @ConfigOption(name = "Time in Chat", desc = "When the Egglocator can't find an egg, show the time until the next Hoppity event or egg spawn.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean eggLocatorTimeInChat = true;
}
