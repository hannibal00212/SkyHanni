package at.hannibal2.skyhanni.config.features.event.gifting;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GiftTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the gift profit tracker.")
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "§cNote",
        desc = "§cDue to the complexities of gifts leaving and re-entering the inventory or stash, gift usage is not auto-tracked. " +
            "§cUse §e/shaddusedgifts §cto manually add gifts used."
    )
    public String note = "";

    @Expose
    @ConfigOption(name = "Holding Gift", desc = "Only show the tracker while holding a gift.")
    public boolean holdingGift = false;

    @Expose
    @ConfigLink(owner = GiftTrackerConfig.class, field = "enabled")
    public Position position = new Position(-274, 0, false, true);
}
