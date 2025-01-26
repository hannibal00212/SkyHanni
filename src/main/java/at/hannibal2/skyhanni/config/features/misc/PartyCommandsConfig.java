package at.hannibal2.skyhanni.config.features.misc;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.jetbrains.annotations.NotNull;

public class PartyCommandsConfig {

    @Expose
    @ConfigEditorDropdown
    @ConfigOption(name = "Party Command Trust Level", desc = "Choose who can run party chat commands.")
    public @NotNull TrustedUser defaultRequiredTrustLevel = TrustedUser.FRIENDS;

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(name = "Party Transfer", desc = "Automatically transfer the party to people who type §b!ptme")
    public boolean transferCommand = false;

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(name = "Party Warp", desc = "Automatically warp the party if someone types §b!warp")
    public boolean warpCommand = false;

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(name = "Party All Invite", desc = "Automatically turn on allinvite if someone types §b!allinv")
    public boolean allInviteCommand = false;

    public enum TrustedUser {
        BEST_FRIENDS("Best Friends"),
        FRIENDS("Friends"),
        ANYONE("Everyone"),
        NO_ONE("No One"),
        ;
        final String label;

        TrustedUser(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(name = "Show reminder", desc = "Show a reminder when an unauthorized player tries to run a command.")
    public boolean showIgnoredReminder = true;
}
