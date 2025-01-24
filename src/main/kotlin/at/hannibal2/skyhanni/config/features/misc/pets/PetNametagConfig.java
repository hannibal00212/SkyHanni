package at.hannibal2.skyhanni.config.features.misc.pets;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PetNametagConfig {

    @Expose
    @ConfigOption(name = "Hide Pet Level", desc = "Hide the pet level above the pet.")
    @ConfigEditorBoolean
    public boolean hidePetLevel = false;

    @Expose
    @ConfigOption(name = "Hide Max Pet Level", desc = "Hide the pet level above the pet if it is max level.")
    @ConfigEditorBoolean
    public boolean hideMaxPetLevel = false;

}
