package at.hannibal2.skyhanni.config.features.dungeon;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ObjectHiderConfig {
    @Expose
    @ConfigOption(name = "Hide Superboom TNT", desc = "Hide Superboom TNT laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideSuperboomTNT = false;

    @Expose
    @ConfigOption(name = "Hide Blessings", desc = "Hide Blessings laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideBlessing = false;

    @Expose
    @ConfigOption(name = "Hide Revive Stones", desc = "Hide Revive Stones laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideReviveStone = false;

    @Expose
    @ConfigOption(name = "Hide Premium Flesh", desc = "Hide Premium Flesh laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hidePremiumFlesh = false;

    @Expose
    @ConfigOption(name = "Hide Journal Entry", desc = "Hide Journal Entry pages laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideJournalEntry = false;

    @Expose
    @ConfigOption(name = "Hide Skeleton Skull", desc = "Hide Skeleton Skulls laying around in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideSkeletonSkull = true;

    @Expose
    @ConfigOption(name = "Hide Healer Orbs", desc = "Hide the damage, ability damage and defensive orbs that spawn when the Healer kills mobs.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideHealerOrbs = false;

    @Expose
    @ConfigOption(name = "Hide Healer Fairy", desc = "Hide the Golden Fairy that follows the Healer in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideHealerFairy = false;

    @Expose
    @ConfigOption(
        name = "Hide Soulweaver Skulls",
        desc = "Hide the annoying soulweaver skulls that float around you if you have the soulweaver gloves equipped.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideSoulweaverSkulls = false;

}
