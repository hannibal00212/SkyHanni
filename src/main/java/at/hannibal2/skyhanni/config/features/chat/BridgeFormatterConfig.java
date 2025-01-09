package at.hannibal2.skyhanni.config.features.chat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.chat.BridgeFormatter;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BridgeFormatterConfig {

    @Expose
    @ConfigOption(
        name = "What is a Bridge?",
        desc = "A bridge is an account that forwards Hypixel guild chat to a Discord channel and vice versa." +
            "Guilds can use this to communicate with their members on Discord from within the game."
    )
    @ConfigEditorInfoText
    public String bridgeInfo = "§eBridge Info";

    @Expose
    @ConfigOption(
        name = "Enable Bridge Reformatting",
        desc = "Reformat guild bridge messages with your own format."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Bridge Bot Name",
        desc = "IGN of the Minecraft account acting as the bridge. Case sensitive."
    )
    @ConfigEditorText
    public String bridgeName = "BotNameHere";

    @Expose
    @ConfigOption(
        name = "Format Info",
        desc = "§7For the setting below, use §e%ign% §7for sender and §e%msg% §7for message. Use & for color codes."
    )
    @ConfigEditorInfoText
    public String info = "";

    @Expose
    @ConfigOption(
        name = "New Bridge Format",
        desc = "New message format for bridge messages."
    )
    @ConfigEditorText
    public String bridgeFormat = "&c[Discord] &6&l%ign%&r: %msg%";

    @Expose
    @ConfigOption(
        name = "Separator Info",
        desc = "For this guild message sent by bridge bot:\n" +
            "§2Guild > §7Bridge §e[rank]§f: Discord > Message\n" +
            "§e> §7would be the separator."
    )
    @ConfigEditorInfoText
    public String separatorInfo = "";

    @Expose
    @ConfigOption(
        name = "Bridge Incoming Separator",
        desc = "The character that separates the Discord name and the Discord message sent by the bot."
    )
    @ConfigEditorText
    public String separator = ":";

    @ConfigOption(
        name = "Test Format",
        desc = "Test the current format."
    )
    @ConfigEditorButton(buttonText = "Test")
    public Runnable testFormat = BridgeFormatter.INSTANCE::testFormat;
}
