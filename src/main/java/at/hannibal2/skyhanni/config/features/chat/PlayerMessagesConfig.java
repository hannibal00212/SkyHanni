package at.hannibal2.skyhanni.config.features.chat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerMessagesConfig {

    @Expose
    @ConfigOption(name = "Enable Chat Formatting", desc = "Enable player chat modifications. Required for all settings below.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enable = false;

    @Expose
    @ConfigOption(
        name = "Part Order",
        desc = "Drag text to change the chat message format order for chat messages."
    )
    @ConfigEditorDraggableList
    public List<MessagePart> partsOrder = new ArrayList<>(Arrays.asList(
        MessagePart.SKYBLOCK_LEVEL,
        MessagePart.PRIVATE_ISLAND_RANK,
        MessagePart.PRIVATE_ISLAND_GUEST,
        MessagePart.PLAYER_NAME,
        MessagePart.GUILD_RANK,
        MessagePart.EMBLEM
    ));

    public enum MessagePart {
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

        private final String str;

        MessagePart(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Hide Level Brackets", desc = "Hide the gray brackets in front of and behind the level numbers.")
    @ConfigEditorBoolean
    public boolean hideLevelBrackets = false;

    @Expose
    @ConfigOption(name = "Level Color As Name", desc = "Use the color of the SkyBlock level for the player color.")
    @ConfigEditorBoolean
    public boolean useLevelColorForName = false;

    @Expose
    @ConfigOption(name = "Player Rank Hider", desc = "Hide player ranks in all chat messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean playerRankHider = false;

    @Expose
    @ConfigOption(name = "Ignore YouTube Rank", desc = "Do not remove the YouTube rank from chat.")
    @ConfigEditorBoolean
    public boolean ignoreYouTube = false;

    @Expose
    @ConfigOption(name = "Chat Filter", desc = "Scan messages sent by players for blacklisted words and gray out the message if any are found.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean chatFilter = false;

    @Expose
    @ConfigOption(name = "Same Chat Color", desc = "Make all chat messages white regardless of rank.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean sameChatColor = true;
}
