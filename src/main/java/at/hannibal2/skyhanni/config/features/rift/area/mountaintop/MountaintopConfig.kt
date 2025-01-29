package at.hannibal2.skyhanni.config.features.rift.area.mountaintop

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MountaintopConfig {
    @Expose
    @ConfigOption(name = "Timite", desc = "")
    @Accordion
    var timite: TimiteConfig = TimiteConfig()
}
