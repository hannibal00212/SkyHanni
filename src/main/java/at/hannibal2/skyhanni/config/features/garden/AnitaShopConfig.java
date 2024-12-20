package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class AnitaShopConfig {
    @Expose
    @ConfigOption(
        name = "Medal Prices",
        desc = "Help to identify profitable items to buy at the Anita item shop and potential profit from selling the item in the Auction House."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean medalProfitEnabled = false;

    @Expose
    @ConfigOption(
        name = "Extra Farming Fortune",
        desc = "Show current tier and cost to max out in the item tooltip.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean extraFarmingFortune = true;

    @Expose
    @ConfigLink(owner = AnitaShopConfig.class, field = "medalProfitEnabled")
    public Position medalProfitPos = new Position(206, 158, false, true);
}
