package at.hannibal2.skyhanni.config.features.commands;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ReversePartyTransferConfig {

    @Expose
    @ConfigOption(name = "Command", desc = "Adds §e/rpt §7to transfer a party back to its previous leader.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean command = true;

    @Expose
    @ConfigOption(name = "Clickable Message", desc = "Allows transfer message to be clicked to transfer a party back to its previous leader if it has been transferred to you.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean clickable = false;

    @Expose
    @ConfigOption(name = "Response Message", desc = "Sends a custom message to party chat when the party is reverse transferred.")
    @ConfigEditorText
    public String message = "Nuh Uh";
}
