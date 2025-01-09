package at.hannibal2.skyhanni.config.features.markedplayer;

import at.hannibal2.skyhanni.utils.LorenzColor;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class MarkedPlayerConfig {

    @Expose
    @ConfigOption(name = "Highlight in World", desc = "Highlight marked players in the world.")
    @ConfigEditorBoolean
    public boolean highlightInWorld = true;

    @Expose
    @ConfigOption(name = "Highlight in Chat", desc = "Highlight marked player names in chat.")
    @ConfigEditorBoolean
    public boolean highlightInChat = true;

    @Expose
    @ConfigOption(name = "Mark Own Name", desc = "Mark own player name.")
    @ConfigEditorBoolean
    public Property<Boolean> markOwnName = Property.of(false);

    @ConfigOption(name = "Marked Chat Color", desc = "Marked Chat Color. §eIf Chroma is gray, enable Chroma in Chroma settings.")
    @Expose
    @ConfigEditorDropdown
    public LorenzColor chatColor = LorenzColor.YELLOW;

    @ConfigOption(name = "Marked Entity Color", desc = "The color of the marked player in the world. §cDoes not yet support chroma.")
    @Expose
    @ConfigEditorDropdown
    public Property<LorenzColor> entityColor = Property.of(LorenzColor.YELLOW);

    @Expose
    @ConfigOption(name = "Join/Leave Message", desc = "")
    @Accordion
    public JoinLeaveMessage joinLeaveMessage = new JoinLeaveMessage();

    public static class JoinLeaveMessage {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the Join/Leave message for marked players.")
        @ConfigEditorBoolean
        public boolean enabled = false;

        @Expose
        @ConfigOption(name = "Players List", desc = "The list of players you want to be notified for.\n§cCase sensitive, separated by commas.")
        @ConfigEditorText
        public Property<String> playersList = Property.of("hypixel,Minikloon");

        @Expose
        @ConfigOption(name = "Use Prefix", desc = "Should the [SkyHanni] prefix be included in the join/leave message?")
        @ConfigEditorBoolean
        public boolean usePrefix = true;

        @Expose
        @ConfigOption(name = "Join Message", desc = "Configure the message when someone joins.\n&& is replaced with the minecraft color code §.\n%s is replaced with the player name.")
        @ConfigEditorText
        public String joinMessage = "&&b%s &&ajoined your lobby.";

        @Expose
        @ConfigOption(name = "Left Message", desc = "Configure the message when someone leaves.\n&& is replaced with the minecraft color code §.\n%s is replaced with the player name.")
        @ConfigEditorText
        public String leftMessage = "&&b%s &&cleft your lobby.";
    }

}
