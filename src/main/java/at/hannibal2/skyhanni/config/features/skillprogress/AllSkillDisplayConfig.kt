package at.hannibal2.skyhanni.config.features.skillprogress

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class AllSkillDisplayConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a display with all skills progress.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Text", desc = "Choose what skills you want to see in the display.")
    @ConfigEditorDraggableList
    var skillEntryList: MutableList<SkillType> = mutableListOf(
        SkillType.COMBAT,
        SkillType.FARMING,
        SkillType.FISHING,
        SkillType.MINING,
        SkillType.FORAGING,
        SkillType.ENCHANTING,
        SkillType.ALCHEMY,
        SkillType.CARPENTRY,
        SkillType.TAMING
    )
}
