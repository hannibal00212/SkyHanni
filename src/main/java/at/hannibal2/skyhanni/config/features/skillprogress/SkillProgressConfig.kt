package at.hannibal2.skyhanni.config.features.skillprogress

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.utils.RenderUtils
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class SkillProgressConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the Skill Progress Display.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Text Alignment", desc = "Align the display text with the progress bar.")
    @ConfigEditorDropdown
    var textAlignmentProperty: Property<TextAlignment> = Property.of(
        TextAlignment.CENTERED
    )

    enum class TextAlignment(private val displayName: String, val alignment: RenderUtils.HorizontalAlignment?) {
        NONE("None", null),
        CENTERED("Centered", RenderUtils.HorizontalAlignment.CENTER),
        LEFT("Left", RenderUtils.HorizontalAlignment.LEFT),
        RIGHT("Right", RenderUtils.HorizontalAlignment.RIGHT),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Hide In Action Bar", desc = "Hide the skill progress in the Hypixel action bar.")
    @ConfigEditorBoolean
    var hideInActionBar: Boolean = false

    @Expose
    @ConfigOption(name = "Always Show", desc = "Always show the skill progress.")
    @ConfigEditorBoolean
    var alwaysShow: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Show Action left", desc = "Show action left until you reach the next level.")
    @ConfigEditorBoolean
    var showActionLeft: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Use percentage", desc = "Use percentage instead of XP.")
    @ConfigEditorBoolean
    var usePercentage: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Use Icon", desc = "Show the skill icon in the display.")
    @ConfigEditorBoolean
    var useIcon: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Use Skill Name", desc = "Show the skill name in the display.")
    @ConfigEditorBoolean
    var useSkillName: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Show Level", desc = "Show your current level in the display.")
    @ConfigEditorBoolean
    var showLevel: Property<Boolean> = Property.of(true)

    @Expose
    @Category(name = "Progress Bar", desc = "Progress Bar Config.")
    var skillProgressBarConfig: SkillProgressBarConfig = SkillProgressBarConfig()

    @Expose
    @Category(name = "Overflow", desc = "Overflow Config.")
    var overflowConfig: SkillOverflowConfig = SkillOverflowConfig()

    @Expose
    @Category(name = "Custom Goal", desc = "Define a custom goal for each skills.")
    var customGoalConfig: CustomGoalConfig = CustomGoalConfig()

    @Expose
    @Category(name = "All Skill Display", desc = "All Skill Display Config.")
    var allSkillDisplayConfig: AllSkillDisplayConfig = AllSkillDisplayConfig()

    @Expose
    @Category(name = "ETA Display", desc = "ETA Display Config.")
    var skillETADisplayConfig: SkillETADisplayConfig = SkillETADisplayConfig()

    @Expose
    @ConfigLink(owner = SkillProgressConfig::class, field = "enabled")
    var displayPosition: Position = Position(384, -105, false, true)

    @Expose
    @ConfigLink(owner = SkillProgressBarConfig::class, field = "enabled")
    var barPosition: Position = Position(384, -87, false, true)

    @Expose
    @ConfigLink(owner = AllSkillDisplayConfig::class, field = "enabled")
    var allSkillPosition: Position = Position(5, 209, false, true)

    @Expose
    @ConfigLink(owner = SkillETADisplayConfig::class, field = "enabled")
    var etaPosition: Position = Position(5, 155, false, true)
}
