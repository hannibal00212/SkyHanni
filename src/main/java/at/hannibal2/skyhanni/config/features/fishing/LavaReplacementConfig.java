package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.fishing.LavaReplacement.IslandsToReplace;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.List;

public class LavaReplacementConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Replace the lava texture with the water texture.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Replace Everywhere", desc = "Replace the lava texture In All Islands regardless of List Below.")
    @ConfigEditorBoolean
    public Property<Boolean> everywhere = Property.of(true);

    @Expose
    @ConfigOption(name = "Islands", desc = "Islands to Replace Lava In.")
    @ConfigEditorDraggableList
    public Property<List<IslandsToReplace>> islands = Property.of(new ArrayList<>());

}
