package at.hannibal2.skyhanni.config.features.dev

import at.hannibal2.skyhanni.data.repo.RepoManager
import at.hannibal2.skyhanni.data.repo.RepoUtils
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class RepositoryConfig {
    @Expose
    @ConfigOption(
        name = "Repo Auto Update",
        desc = "Update the repository on every startup.\n" +
            "§cOnly disable this if you know what you are doing!"
    )
    @ConfigEditorBoolean
    var repoAutoUpdate: Boolean = true

    @ConfigOption(name = "Update Repo Now", desc = "Update your repository to the latest version")
    @ConfigEditorButton(buttonText = "Update")
    var updateRepo: Runnable = Runnable { RepoManager::updateRepo }

    @Expose
    @ConfigOption(name = "Repository Location", desc = "")
    @Accordion
    var location: RepositoryLocation = RepositoryLocation()

    class RepositoryLocation {
        @ConfigOption(name = "Reset Repository Location", desc = "Reset your repository location to the default.")
        @ConfigEditorButton(buttonText = "Reset")
        var resetRepoLocation: Runnable = Runnable { RepoUtils.resetRepoLocation() }

        @Expose
        @ConfigOption(name = "Repository User", desc = "The Repository Branch, default: hannibal002")
        @ConfigEditorText
        var user: String = "hannibal002"

        @Expose
        @ConfigOption(name = "Repository Name", desc = "The Repository Name, default: SkyHanni-REPO")
        @ConfigEditorText
        var name: String = "SkyHanni-REPO"

        @Expose
        @ConfigOption(name = "Repository Branch", desc = "The Repository Branch, default: main")
        @ConfigEditorText
        var branch: String = "main"
    }
}
