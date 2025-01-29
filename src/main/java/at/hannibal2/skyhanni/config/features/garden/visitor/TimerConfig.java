package at.hannibal2.skyhanni.config.features.garden.visitor;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class TimerConfig {
    @Expose
    @ConfigOption(name = "Visitor Timer", desc = "Timer for when the next visitor will appear, and a number for how many visitors are already waiting.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Sixth Visitor Estimate", desc = "Estimate when the sixth visitor in the queue will arrive.\n" +
        "§eMay be inaccurate with co-op members farming simultaneously.")
    @ConfigEditorBoolean
    public boolean sixthVisitorEnabled = true;

    @Expose
    @ConfigOption(name = "Sixth Visitor Warning", desc = "Notify when it is believed that the sixth visitor has arrived.\n" +
        "§eMay be inaccurate with co-op members farming simultaneously.")
    @ConfigEditorBoolean
    public boolean sixthVisitorWarning = true;

    @Expose
    @ConfigOption(name = "New Visitor Ping", desc = "Ping you when you are less than 10 seconds away from getting a new visitor.\n" +
        "§eUseful for getting Ephemeral Gratitudes during the 2023 Halloween event.")
    @ConfigEditorBoolean
    public boolean newVisitorPing = false;

    @Expose
    @ConfigLink(owner = TimerConfig.class, field = "enabled")
    // TODO rename to position
    public Position pos = new Position(-200, 40, false, true);
}
