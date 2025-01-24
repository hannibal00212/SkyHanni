package at.hannibal2.skyhanni.config.features.misc.cosmetic;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CosmeticConfig {

    @Expose
    @ConfigOption(name = "Following Line", desc = "")
    @Accordion
    public FollowingLineConfig followingLine = new FollowingLineConfig();

    @Expose
    @ConfigOption(name = "Arrow Trail", desc = "")
    @Accordion
    public ArrowTrailConfig arrowTrail = new ArrowTrailConfig();
}
