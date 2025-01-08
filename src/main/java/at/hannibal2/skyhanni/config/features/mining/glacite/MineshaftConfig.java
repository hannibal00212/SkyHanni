package at.hannibal2.skyhanni.config.features.mining.glacite;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MineshaftConfig {

    @Expose
    @ConfigOption(
        name = "Profit Per Corpse",
        desc = "Show profit/loss in chat after each looted corpse in the Mineshaft. Also includes breakdown information on hover."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean profitPerCorpseLoot = true;
}
