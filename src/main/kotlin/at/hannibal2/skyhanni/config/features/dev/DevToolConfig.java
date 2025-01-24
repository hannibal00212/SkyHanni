package at.hannibal2.skyhanni.config.features.dev;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DevToolConfig {

    @Expose
    @ConfigOption(name = "Graph Tools", desc = "")
    @Accordion
    public GraphConfig graph = new GraphConfig();

}
