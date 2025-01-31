package at.hannibal2.skyhanni.config.features.crimsonisle

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.crimsonisle.ashfang.AshfangConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CrimsonIsleConfig {
    @Category(name = "Ashfang", desc = "Ashfang settings")
    @Expose
    var ashfang: AshfangConfig = AshfangConfig()

    @ConfigOption(name = "Reputation Helper", desc = "")
    @Accordion
    @Expose
    var reputationHelper: ReputationHelperConfig = ReputationHelperConfig()

    @Expose
    @ConfigOption(name = "Matriarch Helper", desc = "Helper for Heavy Pearls")
    @Accordion
    var matriarchHelper: MatriarchHelperConfig = MatriarchHelperConfig()

    @Expose
    @ConfigOption(name = "Miniboss Respawn Timer", desc = "Shows a timer for when minibosses will respawn.")
    @ConfigEditorBoolean
    @FeatureToggle
    var minibossRespawnTimer: Boolean = false

    @Expose
    @ConfigLink(owner = CrimsonIsleConfig::class, field = "minibossRespawnTimer")
    var minibossTimerPosition: Position = Position(20, 50)

    @Expose
    @ConfigOption(
        name = "Pablo NPC Helper",
        desc = "Show a clickable message that grabs the flower needed from your sacks."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var pabloHelper: Boolean = false

    @Expose
    @ConfigOption(name = "Volcano Explosivity", desc = "Show a HUD of the current volcano explosivity level.")
    @ConfigEditorBoolean
    var volcanoExplosivity: Boolean = false

    @Expose
    @ConfigLink(owner = CrimsonIsleConfig::class, field = "volcanoExplosivity")
    var positionVolcano: Position = Position(20, 20, false, true)

    @Expose
    @ConfigOption(
        name = "Dojo Rank Display",
        desc = "Display your rank, score, actual belt, and points needed for the next belt " +
            "in the Challenges inventory on the Crimson Isles."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showDojoRankDisplay: Boolean = false

    @Expose
    @ConfigLink(owner = CrimsonIsleConfig::class, field = "showDojoRankDisplay")
    var dojoRankDisplayPosition: Position = Position(-378, 206, false, true)
}
