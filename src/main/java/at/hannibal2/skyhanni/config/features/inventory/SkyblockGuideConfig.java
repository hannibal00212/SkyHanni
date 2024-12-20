package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SkyblockGuideConfig {

    @Expose
    @ConfigOption(name = "Menu Highlight",
        desc = "Highlight the top level of non-completed task in the SkyBlock guide.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean menuGuide = true;

    @Expose
    @ConfigOption(name = "Collection Highlight",
        desc = "Highlight missing collections.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean collectionGuide = false;

    @Expose
    @ConfigOption(name = "Abiphone Highlight",
        desc = "Highlight missing Abiphone contacts.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean abiphoneGuide = true;

    @Expose
    @ConfigOption(name = "Minion Highlight",
        desc = "Highlight non-maxed minions.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean minionGuide = true;

    @Expose
    @ConfigOption(name = "Essence Shop Highlight",
        desc = "Highlight missing essence shop upgrades.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean essenceGuide = false;

    @Expose
    @ConfigOption(name = "Consumable Highlight",
        desc = "Highlight non-fully consumed consumables.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean consumableGuide = true;

    @Expose
    @ConfigOption(name = "Jacob Contest Highlight",
        desc = "Highlight crop where no gold medal was earned.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean jacobGuide = true;

    @Expose
    @ConfigOption(name = "Story Highlight",
        desc = "Highlight uncompleted storylines, missing fast travel scrolls and non-100% completed harp songs.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean storyGuide = true;

    @Expose
    @ConfigOption(name = "One Time Completion Highlights",
        desc = "Highlight missing Kuudra defeats, Dungeon floor completions, spooky ranks, bank upgrades, rock/dolphin rarities, undefeated dragons, unobtained dojo belts.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean oneTimeCompletion = true;

}
