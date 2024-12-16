package at.hannibal2.skyhanni.config.features.event.gifting;

import at.hannibal2.skyhanni.config.features.event.winter.GiftingOpportunitiesConfig;
import at.hannibal2.skyhanni.config.features.event.winter.UniqueGiftConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GiftingConfig {

    @Expose
    @ConfigOption(name = "Gift Profit Tracker", desc = "")
    @Accordion
    public GiftTrackerConfig giftProfitTracker = new GiftTrackerConfig();

    @Expose
    @ConfigOption(name = "Unique Gifting Opportunities", desc = "Highlight players who you haven't given gifts to yet.")
    @Accordion
    public GiftingOpportunitiesConfig giftingOpportunities = new GiftingOpportunitiesConfig();

    @Accordion
    @Expose
    @ConfigOption(name = "Unique Gift Counter", desc = "Keep track of how many unique players you have given gifts to.")
    public UniqueGiftConfig uniqueGiftCounter = new UniqueGiftConfig();
}
