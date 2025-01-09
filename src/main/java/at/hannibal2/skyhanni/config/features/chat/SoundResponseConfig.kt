package at.hannibal2.skyhanni.config.features.chat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.chat.SoundResponseTypes;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class SoundResponseConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable sound responses which play animal sounds when they are said in chat.")
    @FeatureToggle
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Sound Responses", desc = "Add animal sounds to play when certain words are said in chat.")
    @ConfigEditorDraggableList
    public List<SoundResponseTypes> soundResponses = new ArrayList<>(SoundResponseTypes.getEntries());
}
