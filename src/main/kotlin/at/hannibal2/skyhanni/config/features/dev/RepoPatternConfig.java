package at.hannibal2.skyhanni.config.features.dev;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class RepoPatternConfig {
    @Expose
    @ConfigOption(name = "Force Local Loading", desc = "Force loading local patterns.")
    @ConfigEditorBoolean
    public Property<Boolean> forceLocal = Property.of(false);

    @Expose
    @ConfigOption(name = "Tolerate Duplicate Usages", desc = "Don't crash when two or more code locations use the same RepoPattern key")
    @ConfigEditorBoolean
    public boolean tolerateDuplicateUsage = false;

    @Expose
    @ConfigOption(name = "Tolerate Late Registration", desc = "Don't crash when a RepoPattern is obtained after preinitialization.")
    @ConfigEditorBoolean
    public boolean tolerateLateRegistration = false;
}
