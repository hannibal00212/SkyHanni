package at.hannibal2.skyhanni.config.features.rift.area.mountaintop;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class TimiteConfig {

    @Expose
    @ConfigOption(name = "Timite Timer", desc = "Count down the time until Timite evolves with the time gun.")
    @ConfigEditorBoolean
    public boolean timiteTimer = true;

    @Expose
    @ConfigOption(name = "Timite Expiry Timer", desc = "Count down the time until Timite/Obsolite expires.")
    @ConfigEditorBoolean
    public boolean timiteExpiryTimer = true;

    @Expose
    @ConfigOption(name = "Timite Tracker", desc = "Tracks collected Timite ores and shows mote profit")
    @ConfigEditorBoolean
    public boolean timiteTracker = false;

    @Expose
    @ConfigLink(owner = TimiteConfig.class, field = "timiteTimer")
    public Position timerPos = new Position(421, -220, false, true);

    @Expose
    @ConfigLink(owner = TimiteConfig.class, field = "timiteTracker")
    public Position trackerPos = new Position(-201, -220, false, true);
}
