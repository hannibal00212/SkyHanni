package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.features.chat.SoundResponseTypes
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SoundResponseConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Enable sound responses which play animal sounds when they are said in chat.",
    )
    @FeatureToggle
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Sound Responses", desc = "Add animal sounds to play when certain words are said in chat.")
    @ConfigEditorDraggableList
    var soundResponses: List<SoundResponseTypes> = ArrayList<SoundResponseTypes>(SoundResponseTypes.entries)
}
