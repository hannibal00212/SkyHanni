package at.hannibal2.skyhanni.config.features.garden.visitor;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class VisitorConfig {
    @Expose
    @ConfigOption(name = "Visitor Timer", desc = "")
    @Accordion
    public TimerConfig timer = new TimerConfig();

    @Expose
    @ConfigOption(name = "Visitor Shopping List", desc = "")
    @Accordion
    public ShoppingListConfig shoppingList = new ShoppingListConfig();

    @Expose
    @ConfigOption(name = "Visitor Inventory", desc = "")
    @Accordion
    public InventoryConfig inventory = new InventoryConfig();

    @Expose
    @ConfigOption(name = "Visitor Reward Warning", desc = "")
    @Accordion
    public RewardWarningConfig rewardWarning = new RewardWarningConfig();

    @Expose
    @ConfigOption(name = "Notification Chat", desc = "Show in chat when a new visitor is visiting your island.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean notificationChat = true;

    @Expose
    @ConfigOption(name = "Compact Chat", desc = "Compact reward summary messages when you accept an offer.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactRewardChat = false;

    @Expose
    @ConfigOption(name = "Notification Title", desc = "Show a title when a new visitor is visiting your island.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean notificationTitle = true;

    @Expose
    @ConfigOption(name = "Highlight Status", desc = "Highlight the status for visitors with a text above or with color.")
    @ConfigEditorDropdown
    public HighlightMode highlightStatus = HighlightMode.BOTH;

    public enum HighlightMode implements HasLegacyId {
        COLOR("Color Only", 0),
        NAME("Name Only", 1),
        BOTH("Both", 2),
        DISABLED("Disabled", 3);
        private final String displayName;
        private final int legacyId;

        HighlightMode(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        HighlightMode(String displayName) {
            this(displayName, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Hypixel Message", desc = "Hide the chat message from Hypixel that a new visitor has arrived at your garden.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hypixelArrivedMessage = true;

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide chat messages from the visitors in the garden. (Except Beth, Maeve, and Spaceman)")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideChat = true;

    @Expose
    @ConfigOption(name = "Visitor Drops Statistics Counter", desc = "")
    @Accordion
    public DropsStatisticsConfig dropsStatistics = new DropsStatisticsConfig();

    @Expose
    @ConfigOption(
        name = "Accept Hotkey",
        desc = "Accept a visitor when you press this keybind while in the visitor GUI.\n" +
            "§eUseful for getting Ephemeral Gratitudes during the 2023 Halloween event."
    )
    @ConfigEditorKeybind(
        defaultKey = Keyboard.KEY_NONE
    )
    public int acceptHotkey = Keyboard.KEY_NONE;


    @Expose
    @ConfigOption(
        name = "Highlight Visitors in SkyBlock",
        desc = "Highlight visitors outside of the Garden."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightVisitors = false;

    @Expose
    @ConfigOption(
        name = "Block Interacting with Visitors",
        desc = "Prevent interacting with / unlocking Visitors to allow for Dedication Cycling."
    )
    @ConfigEditorDropdown
    public VisitorBlockBehaviour blockInteracting = VisitorBlockBehaviour.DONT;

    public enum VisitorBlockBehaviour {
        DONT("Don't"), ALWAYS("Always"), ONLY_ON_BINGO("Only on Bingo");

        final String displayName;

        VisitorBlockBehaviour(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

}
