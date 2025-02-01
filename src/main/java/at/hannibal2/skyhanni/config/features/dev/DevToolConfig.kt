package at.hannibal2.skyhanni.config.features.dev

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DevToolConfig {
    @Expose
    @ConfigOption(name = "Graph Tools", desc = "")
    @Accordion
    var graph: GraphConfig = GraphConfig()
}
