package at.hannibal2.skyhanni.data.repo

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.chat.Text.send
import com.google.gson.JsonObject
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes

class RepoManager(private val configLocation: File) {

    private val gson get() = ConfigManager.gson
    private var latestRepoCommit: String? = null
    val repoLocation: File = File(configLocation, "repo")
    private var error = false
    private var lastRepoUpdate = SimpleTimeMark.farPast()

    companion object {

        private val config get() = SkyHanniMod.feature.dev.repo

        val successfulConstants = mutableListOf<String>()
        val unsuccessfulConstants = mutableListOf<String>()

        private var lastConstant: String? = null

        fun setLastConstant(constant: String) {
            lastConstant?.let {
                successfulConstants.add(it)
            }
            lastConstant = constant
        }

        fun getRepoLocation(): String {
            return "${config.location.user}/${config.location.name}/${config.location.branch}"
        }
    }

    fun loadRepoInformation() {
        atomicShouldManuallyReload.set(true)
        if (config.repoAutoUpdate) {
            fetchRepository(false).thenRun(this::reloadRepository)
        } else {
            reloadRepository()
        }
    }

    private val atomicShouldManuallyReload = AtomicBoolean(false)// TODO remove the workaround

    fun updateRepo() {
        atomicShouldManuallyReload.set(true)
        checkRepoLocation()
        fetchRepository(true).thenRun { this.reloadRepository("Repo updated successfully.") }
    }

    fun reloadLocalRepo() {
        atomicShouldManuallyReload.set(true)
        reloadRepository("Repo loaded from local files successfully.")
    }

    private fun fetchRepository(command: Boolean): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            try {
                val currentCommitJSON: JsonObject? = getJsonFromFile(File(configLocation, "currentCommit.json"))
                latestRepoCommit = null
                try {
                    InputStreamReader(URL(getCommitApiUrl()).openStream())
                        .use { inReader ->
                            val commits: JsonObject = gson.fromJson(inReader, JsonObject::class.java)
                            latestRepoCommit = commits["sha"].asString
                        }
                } catch (e: Exception) {
                    ErrorManager.logErrorWithData(
                        e,
                        "Error while loading data from repo",
                        "command" to command,
                        "currentCommitJSON" to currentCommitJSON,
                    )
                }
                if (latestRepoCommit == null || latestRepoCommit!!.isEmpty()) return@supplyAsync false
                val file = File(configLocation, "repo")
                if (file.exists() &&
                    currentCommitJSON?.get("sha")?.asString == latestRepoCommit &&
                    unsuccessfulConstants.isEmpty() &&
                    lastRepoUpdate.passedSince() < 1.minutes
                ) {
                    if (command) {
                        ChatUtils.chat("§7The repo is already up to date!")
                        atomicShouldManuallyReload.set(false)
                    }
                    return@supplyAsync false
                }
                lastRepoUpdate = SimpleTimeMark.now()
                RepoUtils.recursiveDelete(repoLocation)
                repoLocation.mkdirs()
                val itemsZip = File(repoLocation, "sh-repo-main.zip")
                try {
                    itemsZip.createNewFile()
                } catch (e: IOException) {
                    return@supplyAsync false
                }
                val url = URL(getDownloadUrl(latestRepoCommit))
                val urlConnection = url.openConnection()
                urlConnection.connectTimeout = 15000
                urlConnection.readTimeout = 30000
                try {
                    urlConnection.getInputStream().use { `is` ->
                        FileUtils.copyInputStreamToFile(
                            `is`,
                            itemsZip,
                        )
                    }
                } catch (e: IOException) {
                    ErrorManager.logErrorWithData(
                        e,
                        "Failed to download SkyHanni Repo",
                        "url" to url,
                        "command" to command,
                    )
                    return@supplyAsync false
                }
                RepoUtils.unzipIgnoreFirstFolder(
                    itemsZip.absolutePath,
                    repoLocation.absolutePath,
                )
                if (currentCommitJSON == null || currentCommitJSON["sha"].asString != latestRepoCommit) {
                    val newCurrentCommitJSON = JsonObject()
                    newCurrentCommitJSON.addProperty("sha", latestRepoCommit)
                    try {
                        writeJson(newCurrentCommitJSON, File(configLocation, "currentCommit.json"))
                    } catch (ignored: IOException) {
                    }
                }
            } catch (e: Exception) {
                ErrorManager.logErrorWithData(
                    e,
                    "Failed to download SkyHanni Repo",
                    "command" to command,
                )
            }
            true
        }
    }

    private fun reloadRepository(answerMessage: String = ""): CompletableFuture<Void?> {
        val comp = CompletableFuture<Void?>()
        if (!atomicShouldManuallyReload.get()) return comp
        ErrorManager.resetCache()
        DelayedRun.onThread.execute {
            error = false
            successfulConstants.clear()
            unsuccessfulConstants.clear()
            lastConstant = null

            RepositoryReloadEvent(repoLocation, gson).postAndCatchAndBlock(ignoreErrorCache = true) {
                error = true
                lastConstant?.let {
                    unsuccessfulConstants.add(it)
                }
                lastConstant = null
            }
            comp.complete(null)
            if (answerMessage.isNotEmpty() && !error) {
                ChatUtils.chat("§a$answerMessage")
            }
            if (error) {
                ChatUtils.clickableChat(
                    "Error with the repo detected, try /shupdaterepo to fix it!",
                    onClick = {
                        SkyHanniMod.repo.updateRepo()
                    },
                    "§eClick to update the repo!",
                    prefixColor = "§c",
                )
                if (unsuccessfulConstants.isEmpty()) {
                    unsuccessfulConstants.add("All Constants")
                }
            }
        }
        return comp
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Repo Status")

        if (unsuccessfulConstants.isEmpty() && successfulConstants.isNotEmpty()) {
            event.addIrrelevant("Repo working fine")
            return
        }

        event.addData {
            add("Successful Constants (${successfulConstants.size}):")

            add("Unsuccessful Constants (${unsuccessfulConstants.size}):")

            for ((i, constant) in unsuccessfulConstants.withIndex()) {
                add("   - $constant")
                if (i == 5) {
                    add("...")
                    break
                }
            }
        }
    }

    fun displayRepoStatus(joinEvent: Boolean) {
        if (joinEvent) {
            if (unsuccessfulConstants.isNotEmpty()) {
                val text = mutableListOf<IChatComponent>()
                text.add(
                    ("§c[SkyHanni-${SkyHanniMod.version}] §7Repo Issue! Some features may not work. " +
                        "Please report this error on the Discord!").asComponent(),
                )
                text.add("§7Repo Auto Update Value: §c${config.repoAutoUpdate}".asComponent())
                text.add("§7If you have Repo Auto Update turned off, please try turning that on.".asComponent())
                text.add("§cUnsuccessful Constants §7(${unsuccessfulConstants.size}):".asComponent())

                for (constant in unsuccessfulConstants) {
                    text.add("   §e- §7$constant".asComponent())
                }
                Text.multiline(text).send()
            }
            return
        }
        if (unsuccessfulConstants.isEmpty() && successfulConstants.isNotEmpty()) {
            ChatUtils.chat("Repo working fine! Commit hash: $latestRepoCommit", prefixColor = "§a")
            return
        }
        ChatUtils.chat("Repo has errors! Commit has: ${latestRepoCommit ?: "null"}", prefixColor = "§c")
        if (successfulConstants.isNotEmpty()) ChatUtils.chat(
            "Successful Constants §7(${successfulConstants.size}):",
            prefixColor = "§a",
        )
        for (constant in successfulConstants) {
            ChatUtils.chat("   §a- §7$constant", false)
        }
        ChatUtils.chat("Unsuccessful Constants §7(${unsuccessfulConstants.size}):")
        for (constant in unsuccessfulConstants) {
            ChatUtils.chat("   §e- §7$constant", false)
        }
    }

    /**
     * Parses a file in to a JsonObject.
     */
    private fun getJsonFromFile(file: File?): JsonObject? {
        try {
            BufferedReader(
                InputStreamReader(
                    FileInputStream(file),
                    StandardCharsets.UTF_8,
                ),
            ).use { reader ->
                return gson.fromJson(reader, JsonObject::class.java)
            }
        } catch (e: java.lang.Exception) {
            return null
        }
    }

    private fun getCommitApiUrl(): String {
        val repoUser = config.location.user
        val repoName = config.location.name
        val repoBranch = config.location.branch
        return String.format("https://api.github.com/repos/%s/%s/commits/%s", repoUser, repoName, repoBranch)
    }

    private fun getDownloadUrl(commitId: String?): String {
        val repoUser = config.location.user
        val repoName = config.location.name
        return String.format("https://github.com/%s/%s/archive/%s.zip", repoUser, repoName, commitId)
    }

    @Throws(IOException::class)
    fun writeJson(json: JsonObject?, file: File) {
        file.createNewFile()
        BufferedWriter(
            OutputStreamWriter(
                FileOutputStream(file),
                StandardCharsets.UTF_8,
            ),
        ).use { writer -> writer.write(gson.toJson(json)) }
    }

    @SubscribeEvent
    fun onNeuRepoReload(event: io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent) {
        NeuRepositoryReloadEvent().postAndCatch()
    }

    fun resetRepositoryLocation(manual: Boolean = false) {
        val defaultUser = "hannibal002"
        val defaultName = "SkyHanni-Repo"
        val defaultBranch = "main"

        with(config.location) {
            if (user == defaultUser && name == defaultName && branch == defaultBranch) {
                if (manual) {
                    ChatUtils.chat("Repo settings are already on default!")
                }
                return
            }

            user = defaultUser
            name = defaultName
            branch = defaultBranch
            if (manual) {
                ChatUtils.clickableChat(
                    "Reset Repo settings to default. " +
                        "Click §aUpdate Repo Now §ein config or run /shupdaterepo to update!",
                    onClick = {
                        updateRepo()
                    },
                    "§eClick to update the repo!",
                )
            }
        }
    }

    private fun checkRepoLocation() {
        if (config.location.user.isEmpty() || config.location.name.isEmpty() || config.location.branch.isEmpty()) {
            ChatUtils.userError("Invalid Repo settings detected, resetting default settings.")
            resetRepositoryLocation()
        }
    }
}
