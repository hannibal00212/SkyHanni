package at.hannibal2.skyhanni.config.features.combat.damageindicator;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EnderSlayerConfig {

    @Expose
    @ConfigOption(name = "Laser Phase Timer", desc = "Show a timer for when the laser phase will end.")
    @ConfigEditorBoolean
    public boolean laserPhaseTimer = false;

    @Expose
    @ConfigOption(name = "Health During Laser", desc = "Show the health of Voidgloom Seraph 4 during the laser phase.")
    @ConfigEditorBoolean
    public boolean showHealthDuringLaser = false;
}
