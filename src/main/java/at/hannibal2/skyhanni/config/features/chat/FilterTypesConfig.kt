package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FilterTypesConfig {
    @Expose
    @ConfigOption(name = "Powder Mining", desc = "")
    @Accordion
    var powderMiningFilter: PowderMiningFilterConfig = PowderMiningFilterConfig()

    @Expose
    @ConfigOption(name = "Stash Messages", desc = "")
    @Accordion
    var stashMessages: StashConfig = StashConfig()

    @Expose
    @ConfigOption(
        name = "Hypixel Lobbies",
        desc = "Hide announcements in Hypixel lobbies " +
            "(player joins, loot boxes, prototype lobby messages, radiating generosity, Hypixel tournaments)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hypixelHub: Boolean = false

    @Expose
    @ConfigOption(name = "Empty", desc = "Hide all empty messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var empty: Boolean = false

    @Expose
    @ConfigOption(name = "Warping", desc = "Hide 'Sending request to join...' and 'Warping...' messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var warping: Boolean = false

    @Expose
    @ConfigOption(name = "Welcome", desc = "Hide the 'Welcome to SkyBlock' message.")
    @ConfigEditorBoolean
    @FeatureToggle
    var welcome: Boolean = false

    @Expose
    @ConfigOption(name = "Guild EXP", desc = "Hide Guild EXP messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var guildExp: Boolean = false

    @Expose
    @ConfigOption(name = "Friend Join/Left", desc = "Hide friend join/left messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var friendJoinLeft: Boolean = false

    @Expose
    @ConfigOption(name = "Winter Gifts", desc = "Hide pointless Winter Gift messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var winterGift: Boolean = false

    @Expose
    @ConfigOption(name = "Kill Combo", desc = "Hide messages about your Kill Combo from the Grandma Wolf pet.")
    @ConfigEditorBoolean
    @FeatureToggle
    var killCombo: Boolean = false

    @Expose
    @ConfigOption(
        name = "Watchdog",
        desc = "Hide the message where Hypixel flexes about how many players they have banned over the last week."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var watchDog: Boolean = false

    @Expose
    @ConfigOption(name = "Profile Join", desc = "Hide 'You are playing on profile' and 'Profile ID' chat messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var profileJoin: Boolean = false

    @Expose
    @ConfigOption(name = "Fire Sale", desc = "Hide the repeating fire sale reminder chat messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var fireSale: Boolean = false

    @Expose
    @ConfigOption(name = "Event Level Up", desc = "Hide event level up messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var eventLevelUp: Boolean = false

    @Expose
    @ConfigOption(
        name = "Diana",
        desc = "Hide chat messages around griffin burrow chains, griffin feather drops, and coin drops."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var diana: Boolean = false

    @Expose
    @ConfigOption(
        name = "Factory Upgrade",
        desc = "Hide §nHypixel's§r Chocolate Factory upgrade and employee promotion messages.\n" +
            "§eTo turn off SkyHanni's upgrade messages, search §lUpgrade Warning"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var factoryUpgrade: Boolean = false

    @Expose
    @ConfigOption(name = "Sacrifice", desc = "Hide other players' sacrifice messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var sacrifice: Boolean = false

    @Expose
    @ConfigOption(name = "Garden Pest", desc = "Hide the message of no pests on garden.")
    @ConfigEditorBoolean
    @FeatureToggle
    var gardenNoPest: Boolean = false

    @Expose
    @ConfigOption(name = "Block Alpha Achievements", desc = "Hide achievement messages while on the Alpha network.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideAlphaAchievements: Boolean = false

    @Expose
    @ConfigOption(
        name = "Parkour Messages",
        desc = "Hide parkour messages (starting, stopping, reaching a checkpoint)."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var parkour: Boolean = false

    @Expose
    @ConfigOption(name = "Teleport Pad Messages", desc = "Hide annoying messages when using teleport pads.")
    @ConfigEditorBoolean
    @FeatureToggle
    var teleportPads: Boolean = false

    // TODO remove
    @Expose
    @ConfigOption(name = "Others", desc = "Hide other annoying messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var others: Boolean = false
}
