package at.hannibal2.skyhanni.config.features.event.winter;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GiftingOpportunitiesConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight players who you haven't given gifts to yet.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Only While Holding Gift", desc = "Only highlight players you haven't gifted while holding a gift.")
    @ConfigEditorBoolean
    // TODO highlightWithGiftOnly
    public boolean highlighWithGiftOnly = true;


    @Expose
    @ConfigOption(name = "Use Armor Stands", desc = "Make use of armor stands to stop highlighting players.\n" +
        "§eThis is a bit inaccurate, but it can help with people you gifted before this feature was used.")
    @ConfigEditorBoolean
    public boolean useArmorStandDetection = false;

}
