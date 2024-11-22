package at.hannibal2.skyhanni.config.features.dev;

import at.hannibal2.skyhanni.data.repo.RepoUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class RepositoryConfig {

    @Expose
    @ConfigOption(name = "Repo Auto Update", desc = "Update the repository on every startup.\n" +
        "§cOnly disable this if you know what you are doing!")
    @ConfigEditorBoolean
    public boolean repoAutoUpdate = true;

    @ConfigOption(name = "Update Repo Now", desc = "Update your repository to the latest version")
    @ConfigEditorButton(buttonText = "Update")
    public Runnable updateRepo = RepoUtils::updateRepo;

    @Expose
    @ConfigOption(name = "Repository Location", desc = "")
    @Accordion
    public RepositoryLocation location = new RepositoryLocation();

    public static class RepositoryLocation {

        @ConfigOption(name = "Reset Repository Location", desc = "Reset your repository location to the default.")
        @ConfigEditorButton(buttonText = "Reset")
        public Runnable resetRepoLocation = RepoUtils::resetRepoLocation;

        @Expose
        @ConfigOption(name = "Repository User", desc = "The Repository Branch, default: hannibal002")
        @ConfigEditorText
        public String user = "hannibal002";

        @Expose
        @ConfigOption(name = "Repository Name", desc = "The Repository Name, default: SkyHanni-REPO")
        @ConfigEditorText
        public String name = "SkyHanni-REPO";

        @Expose
        @ConfigOption(name = "Repository Branch", desc = "The Repository Branch, default: main")
        @ConfigEditorText
        public String branch = "main";
    }

}
