package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ObjectHiderConfig {
    @Expose
    @ConfigOption(name = "Hide Superboom TNT", desc = "Hide Superboom TNT laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideSuperboomTNT: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Blessings", desc = "Hide Blessings laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideBlessing: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Revive Stones", desc = "Hide Revive Stones laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideReviveStone: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Premium Flesh", desc = "Hide Premium Flesh laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hidePremiumFlesh: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Journal Entry", desc = "Hide Journal Entry pages laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideJournalEntry: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Skeleton Skull", desc = "Hide Skeleton Skulls laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideSkeletonSkull: Boolean = true

    @Expose
    @ConfigOption(
        name = "Hide Healer Orbs",
        desc = "Hide the damage, ability damage and defensive orbs that spawn when the Healer kills mobs."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideHealerOrbs: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Healer Fairy", desc = "Hide the Golden Fairy that follows the Healer in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideHealerFairy: Boolean = false

    @Expose
    @ConfigOption(
        name = "Hide Soulweaver Skulls",
        desc = "Hide the annoying soulweaver skulls that float around you if you have the soulweaver gloves equipped."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideSoulweaverSkulls: Boolean = false
}
