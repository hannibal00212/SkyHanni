package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CompactPotionConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Shorten chat messages about player potion effects.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(
        name = "Clickable Chat Message",
        desc = "Make the Compact Potion message open the Potion effects menu on click."
    )
    @ConfigEditorBoolean
    var clickableChatMessage: Boolean = true
}
