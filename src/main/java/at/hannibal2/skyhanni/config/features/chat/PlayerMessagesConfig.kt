package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PlayerMessagesConfig {
    @Expose
    @ConfigOption(
        name = "Enable Chat Formatting",
        desc = "Enable player chat modifications. Required for all settings below."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enable: Boolean = false

    @Expose
    @ConfigOption(name = "Part Order", desc = "Drag text to change the chat message format order for chat messages.")
    @ConfigEditorDraggableList
    var partsOrder: MutableList<MessagePart> = mutableListOf(
        MessagePart.SKYBLOCK_LEVEL,
        MessagePart.PRIVATE_ISLAND_RANK,
        MessagePart.PRIVATE_ISLAND_GUEST,
        MessagePart.PLAYER_NAME,
        MessagePart.GUILD_RANK,
        MessagePart.EMBLEM
    )

    enum class MessagePart(private val displayName: String) {
        SKYBLOCK_LEVEL("SkyBlock Level"),
        EMBLEM("Emblem"),
        PLAYER_NAME("§bPlayer Name"),
        GUILD_RANK("Guild Rank"),
        PRIVATE_ISLAND_RANK("Private Island Rank"),
        PRIVATE_ISLAND_GUEST("Private Island Guest"),
        CRIMSON_FACTION("Crimson Faction"),
        MODE_IRONMAN("Ironman Mode"),
        BINGO_LEVEL("Bingo Level"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(
        name = "Hide Level Brackets",
        desc = "Hide the gray brackets in front of and behind the level numbers."
    )
    @ConfigEditorBoolean
    var hideLevelBrackets: Boolean = false

    @Expose
    @ConfigOption(name = "Level Color As Name", desc = "Use the color of the SkyBlock level for the player color.")
    @ConfigEditorBoolean
    var useLevelColorForName: Boolean = false

    @Expose
    @ConfigOption(name = "Player Rank Hider", desc = "Hide player ranks in all chat messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    var playerRankHider: Boolean = false

    @Expose
    @ConfigOption(name = "Ignore YouTube Rank", desc = "Do not remove the YouTube rank from chat.")
    @ConfigEditorBoolean
    var ignoreYouTube: Boolean = false

    @Expose
    @ConfigOption(
        name = "Chat Filter",
        desc = "Scan messages sent by players for blacklisted words and gray out the message if any are found."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var chatFilter: Boolean = false

    @Expose
    @ConfigOption(name = "Same Chat Color", desc = "Make all chat messages white regardless of rank.")
    @ConfigEditorBoolean
    @FeatureToggle
    var sameChatColor: Boolean = true
}
