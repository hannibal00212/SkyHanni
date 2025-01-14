package at.hannibal2.skyhanni.config.features.event.hoppity

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HoppityChatConfig {
    @Expose
    @ConfigOption(name = "Compact Chat", desc = "Compact chat events when finding a Hoppity Egg.")
    @ConfigEditorBoolean
    @FeatureToggle
    var compact: Boolean = false

    @Expose
    @ConfigOption(name = "Compacted Rarity", desc = "Show rarity of found rabbit in Compacted chat messages.")
    @ConfigEditorDropdown
    var rarityInCompact: CompactRarityTypes = CompactRarityTypes.NEW

    enum class CompactRarityTypes(private val displayName: String) {
        NONE("Neither"),
        NEW("New Rabbits"),
        DUPE("Duplicate Rabbits"),
        BOTH("New & Duplicate Rabbits"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(
        name = "Compact Hitman",
        desc = "Show a summary message instead of individual messages for Hitman \"Claim All\" menu.\n" +
            "§cRequires Compact Chat enabled to work."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var compactHitman: Boolean = false

    @Expose
    @ConfigOption(
        name = "Show Duplicate Count",
        desc = "Show the number of previous finds of a duplicate Hoppity rabbit in chat messages."
    )
    @ConfigEditorBoolean
    var showDuplicateNumber: Boolean = false

    @Expose
    @ConfigOption(
        name = "Recolor Time-Towered Chocolate",
        desc = "Recolor raw chocolate gain from duplicate rabbits while Time Tower is active."
    )
    @ConfigEditorBoolean
    var recolorTTChocolate: Boolean = false

    @Expose
    @ConfigOption(
        name = "Time in Chat",
        desc = "When the Egglocator can't find an egg, show the time until the next Hoppity event or egg spawn."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var eggLocatorTimeInChat: Boolean = true
}
