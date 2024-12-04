package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PesthunterShopConfig {

    @Expose
    @ConfigOption(
        name = "Enable",
        desc = "Enable the Pesthunter Profit display."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean pesthunterProfitEnabled = false;

    @Expose
    @ConfigLink(owner = PesthunterShopConfig.class, field = "pesthunterProfitEnabled")
    public Position pesthunterProfitPos = new Position(206, 158, false, true);
}
