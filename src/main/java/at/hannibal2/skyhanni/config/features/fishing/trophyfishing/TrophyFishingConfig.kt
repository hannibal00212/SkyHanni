package at.hannibal2.skyhanni.config.features.fishing.trophyfishing

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.features.crimsonisle.SulphurSkitterBoxConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class TrophyFishingConfig {
    @Expose
    @ConfigOption(name = "Trophy Fishing Chat Messages", desc = "")
    @Accordion
    var chatMessages: ChatMessagesConfig = ChatMessagesConfig()

    @Expose
    @ConfigOption(name = "Trophy Fishing Display", desc = "")
    @Accordion
    var display: TrophyFishDisplayConfig = TrophyFishDisplayConfig()

    @Expose
    @ConfigOption(name = "Geyser Fishing", desc = "")
    @Accordion
    var geyserOptions: GeyserFishingConfig = GeyserFishingConfig()

    @ConfigOption(name = "Sulphur Skitter Box", desc = "")
    @Accordion
    @Expose
    var sulphurSkitterBox: SulphurSkitterBoxConfig = SulphurSkitterBoxConfig()

    @Expose
    @ConfigOption(name = "Golden Fish Timer", desc = "")
    @Accordion
    var goldenFishTimer: GoldenFishTimerConfig = GoldenFishTimerConfig()

    @Expose
    @ConfigOption(name = "Fillet Tooltip", desc = "Show fillet value of Trophy Fish in tooltip.")
    @ConfigEditorBoolean
    @FeatureToggle
    var filletTooltip: Boolean = true

    @Expose
    @ConfigOption(
        name = "Odger Waypoint",
        desc = "Show the Odger waypoint when Trophy Fishes are in the inventory and no lava rod in hand.\n" +
            "§cOnly useful for users without Abiphone contact."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var odgerLocation: Boolean = true

    @Expose
    @ConfigOption(name = "Load from NEU PV", desc = "Load Trophy fishing data when opening NEU PV.")
    @ConfigEditorBoolean
    @FeatureToggle
    var loadFromNeuPV: Boolean = true
}
