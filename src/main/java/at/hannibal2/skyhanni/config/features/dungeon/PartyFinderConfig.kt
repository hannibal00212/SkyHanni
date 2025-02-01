package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PartyFinderConfig {
    @Expose
    @ConfigOption(name = "Colored Class Level", desc = "Color class levels in Party Finder.")
    @ConfigEditorBoolean
    @FeatureToggle
    var coloredClassLevel: Boolean = true

    @Expose
    @ConfigOption(name = "Floor Stack Size", desc = "Display the party finder floor as the item stack size.")
    @ConfigEditorBoolean
    @FeatureToggle
    var floorAsStackSize: Boolean = true

    @Expose
    @ConfigOption(
        name = "Mark Paid Carries",
        desc = "Highlight paid carries with a red background to make them easier to find/skip."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var markPaidCarries: Boolean = true

    @Expose
    @ConfigOption(
        name = "Mark Perm/VC Parties",
        desc = "Highlight perm parties and parties that require a VC with a purple background to make them easier to find/skip."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var markNonPugs: Boolean = true

    @Expose
    @ConfigOption(
        name = "Mark Low Levels",
        desc = "Highlight groups with players at or below the specified class level to make them easier to find/skip."
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 50f, minStep = 1f)
    var markBelowClassLevel: Int = 0

    @Expose
    @ConfigOption(name = "Mark Ineligible Groups", desc = "Highlight groups with requirements that you do not meet.")
    @ConfigEditorBoolean
    @FeatureToggle
    var markIneligibleGroups: Boolean = true

    @Expose
    @ConfigOption(
        name = "Mark Missing Class",
        desc = "Highlight groups that don't currently have any members of your selected dungeon class."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var markMissingClass: Boolean = true

    @Expose
    @ConfigOption(name = "Show Missing Classes", desc = "Show missing classes in a party in the tooltip.")
    @ConfigEditorBoolean
    @FeatureToggle
    var showMissingClasses: Boolean = true
}
