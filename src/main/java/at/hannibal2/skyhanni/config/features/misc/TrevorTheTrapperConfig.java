package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.ELUSIVE;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.ENDANGERED;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.KILLED;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.PELTS_PER_HOUR;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.QUESTS_STARTED;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.SPACER_1;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.TITLE;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.TOTAL_PELTS;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.TRACKABLE;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.UNDETECTED;
import static at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry.UNTRACKABLE;

public class TrevorTheTrapperConfig {

    @Expose
    @ConfigOption(
        name = "Enable Data Tracker",
        desc = "Track all of your data from doing Trevor Quests.\n" +
            "Shows based on the setting below."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean dataTracker = true;

    @Expose
    @ConfigOption(
        name = "Show Between Quests",
        desc = "Show the tracker during and between quests otherwise it will only show during them.\n" +
            "Will show in the Trapper's Den regardless. §cToggle 'Enable Data Tracker' above."
    )
    @ConfigEditorBoolean
    public boolean displayType = true;

    @Expose
    @ConfigOption(
        name = "Text Format",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public List<TrackerEntry> textFormat = new ArrayList<>(Arrays.asList(
        TITLE,
        QUESTS_STARTED,
        TOTAL_PELTS,
        PELTS_PER_HOUR,
        SPACER_1,
        KILLED,
        TRACKABLE,
        UNTRACKABLE,
        UNDETECTED,
        ENDANGERED,
        ELUSIVE
    ));

    public enum TrackerEntry implements HasLegacyId {
        TITLE("§b§lTrevor Data Tracker", 0),
        QUESTS_STARTED("§b1,428 §9Quests Started", 1),
        TOTAL_PELTS("§b11,281 §5Total Pelts Gained", 2),
        PELTS_PER_HOUR("§b2,448 §5Pelts Per Hour", 3),
        SPACER_1("", 4),
        KILLED("§b850 §cKilled Animals", 5),
        SELF_KILLING("§b153 §cSelf Killing Animals", 6),
        TRACKABLE("§b788 §fTrackable Animals", 7),
        UNTRACKABLE("§b239 §aUntrackable Animals", 8),
        UNDETECTED("§b115 §9Undetected Animals", 9),
        ENDANGERED("§b73 §5Endangered Animals", 10),
        ELUSIVE("§b12 §6Elusive Animals", 11),
        ;

        private final String displayName;
        private final int legacyId;

        TrackerEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        TrackerEntry(String displayName) {
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
    @ConfigLink(owner = TrevorTheTrapperConfig.class, field = "dataTracker")
    public Position position = new Position(10, 80, false, true);

    @Expose
    @ConfigOption(name = "Trapper Solver", desc = "Assist in finding Trevor's mobs.\n" +
        "§eNote: May not always work as expected.\n" +
        "§cWill not help you to find rabbits or sheep in the Oasis!")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean trapperSolver = true;

    @Expose
    @ConfigOption(name = "Mob Dead Warning", desc = "Show a message when Trevor's mob dies.")
    @ConfigEditorBoolean
    public boolean trapperMobDiedMessage = true;

    @Expose
    @ConfigOption(name = "Warp to Trapper", desc = "Warp to Trevor's Den. Works only inside the Farming Islands.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean warpToTrapper = false;

    @Expose
    @ConfigOption(name = "Accept Trapper Quest", desc = "Click this key after the chat prompt to accept Trevor's quest.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean acceptQuest = false;

    @Expose
    @ConfigOption(name = "Trapper Hotkey", desc = "Press this key to warp to Trevor's Den or to accept the quest. " +
        "§eRequires the relevant above settings to be toggled")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int keyBindWarpTrapper = Keyboard.KEY_NONE;


    @Expose
    @ConfigOption(name = "Trapper Cooldown", desc = "Change the color of Trevor and adds a cooldown over his head.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean trapperTalkCooldown = true;

    @Expose
    @ConfigOption(
        name = "Trapper Cooldown GUI",
        desc = "Show the cooldown on screen in an overlay (intended for Abiphone users)."
    )
    @ConfigEditorBoolean
    public boolean trapperCooldownGui = false;

    @Expose
    @ConfigLink(owner = TrevorTheTrapperConfig.class, field = "trapperCooldownGui")
    public Position trapperCooldownPos = new Position(10, 10, false, true);
}
