package at.hannibal2.skyhanni.config.features.crimsonisle;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.nether.AtomHitBox;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AtomHitBoxConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show Exes, Wais and Zees hitbox.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Select Atoms",
        desc = "Select atoms you want to show the hitbox for."
    )
    @ConfigEditorDraggableList
    public List<AtomHitBox.Atom> atomsEntries = new ArrayList<>(Arrays.asList(
        AtomHitBox.Atom.EXE,
        AtomHitBox.Atom.WAI,
        AtomHitBox.Atom.ZEE
    ));
}
