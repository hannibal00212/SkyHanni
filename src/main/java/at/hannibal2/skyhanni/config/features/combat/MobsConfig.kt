package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MobsConfig {
    @Expose
    @ConfigOption(
        name = "Area Boss Highlighter",
        desc = "Highlight Golden Ghoul, Old Wolf, Voidling Extremist, Millenia-Aged Blaze and Soul of the Alpha."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var areaBossHighlight: Boolean = true

    @Expose
    @ConfigOption(name = "Arachne Keeper", desc = "Highlight the Arachne Keeper in the Spider's Den in purple color.")
    @ConfigEditorBoolean
    @FeatureToggle
    var arachneKeeperHighlight: Boolean = true

    @Expose
    @ConfigOption(name = "Corleone", desc = "Highlight Boss Corleone in the Crystal Hollows.")
    @ConfigEditorBoolean
    @FeatureToggle
    var corleoneHighlighter: Boolean = true

    @Expose
    @ConfigOption(name = "Zealot", desc = "Highlight Zealots and Bruisers in The End.")
    @ConfigEditorBoolean
    @FeatureToggle
    var zealotBruiserHighlighter: Boolean = false

    @Expose
    @ConfigOption(name = "Zealot with Chest", desc = "Highlight Zealots holding a Chest in a different color.")
    @ConfigEditorBoolean
    @FeatureToggle
    var chestZealotHighlighter: Boolean = false

    @Expose
    @ConfigOption(
        name = "Special Zealots",
        desc = "Highlight Special Zealots (the ones that drop Summoning Eyes) in the End."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var specialZealotHighlighter: Boolean = true

    @Expose
    @ConfigOption(name = "Corrupted Mob", desc = "Highlight corrupted mobs in purple color.")
    @ConfigEditorBoolean
    @FeatureToggle
    var corruptedMobHighlight: Boolean = false

    @Expose
    @ConfigOption(name = "Arachne Boss", desc = "Highlight the Arachne boss in red and mini-bosses in orange.")
    @ConfigEditorBoolean
    @FeatureToggle
    var arachneBossHighlighter: Boolean = true

    @Expose
    @ConfigOption(name = "Line to Arachne", desc = "Draw a line pointing to where Arachne is currently at.")
    @ConfigEditorBoolean
    @FeatureToggle
    var lineToArachne: Boolean = false

    @Expose
    @ConfigOption(name = "Line to Arachne Width", desc = "The width of the line pointing to where Arachne is at.")
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 10f)
    var lineToArachneWidth: Int = 5

    @Expose
    @ConfigOption(
        name = "Area Boss Timer",
        desc = "Show a timer when Area Bosses respawn.\n" +
            "§eMay take 20 - 30 seconds to calibrate correctly."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var areaBossRespawnTimer: Boolean = false

    @Expose
    @ConfigOption(
        name = "Arachne Spawn Timer",
        desc = "Show a timer when Arachne fragments or crystals are placed to indicate how long until the boss will spawn. " +
            "§eTimer may be 1 - 2 seconds off."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showArachneSpawnTimer: Boolean = true

    @Expose
    @ConfigOption(
        name = "Arachne Kill Timer",
        desc = "Shows how long it took to kill Arachne after the fight ends. " +
            "§cDoes not show if you were not in the Sanctuary when it spawned."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var arachneKillTimer: Boolean = true

    @Expose
    @ConfigOption(name = "Enderman TP Hider", desc = "Stops the Enderman Teleportation animation.")
    @ConfigEditorBoolean
    @FeatureToggle
    var endermanTeleportationHider: Boolean = true

    @Expose
    @ConfigOption(name = "Arachne Minis Hider", desc = "Hides the nametag above Arachne minis.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideNameTagArachneMinis: Boolean = true
}
