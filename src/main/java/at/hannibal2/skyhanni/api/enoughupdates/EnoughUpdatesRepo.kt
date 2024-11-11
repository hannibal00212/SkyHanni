package at.hannibal2.skyhanni.api.enoughupdates

import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.repo.RepoUtils
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.json.getJson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.URL

// The regular repo has too much extra stuff for it to be worth having more common logic
object EnoughUpdatesRepo {

    fun downloadRepo() {
        download()
        ChatUtils.chat("Updated NEU repo")
        EnoughUpdatesManager.reloadRepo()
    }

    private fun download() {
        val hash = getCommitHash() ?: return
        val currentCommit = getCurrentCommitHash()
        if (hash == currentCommit) return

        RepoUtils.recursiveDelete(EnoughUpdatesManager.repoLocation)
        EnoughUpdatesManager.repoLocation.mkdirs()
        val itemsZip = File(EnoughUpdatesManager.repoLocation, "neu-items-master.zip")
        try {
            itemsZip.createNewFile()
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Error creating neu repo zip file")
            return
        }

        val url = URL("https://github.com/NotEnoughUpdates/NotEnoughUpdates-Repo/archive/$hash.zip")
        val urlConnection = url.openConnection()
        urlConnection.connectTimeout = 15000
        urlConnection.readTimeout = 30000

        try {
            urlConnection.getInputStream().use { input ->
                itemsZip.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Error downloading neu repo zip",
                "URL" to url.toString(),
            )
            return
        }
        RepoUtils.unzipIgnoreFirstFolder(itemsZip.absolutePath, EnoughUpdatesManager.repoLocation.absolutePath)
        writeCurrentCommitHash(hash)
    }

    private fun getCommitHash(): String? {
        try {
            InputStreamReader(URL("https://api.github.com/repos/NotEnoughUpdates/NotEnoughUpdates-Repo/commits/master").openStream())
                .use { reader ->
                    val json = ConfigManager.gson.fromJson(reader, JsonObject::class.java)
                    return json["sha"].asString
                }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Error fetching repo commit hash")
            return null
        }
    }

    private fun getCurrentCommitHash(): String? {
        val currentCommitJSON: JsonObject? = File(EnoughUpdatesManager.configLocation, "currentCommit.json").getJson()
        return currentCommitJSON?.get("sha")?.asString
    }

    private fun writeCurrentCommitHash(hash: String) {
        val currentCommitJson = JsonObject()
        currentCommitJson.addProperty("sha", hash)
        try {
            FileWriter(File(EnoughUpdatesManager.configLocation, "currentCommit.json")).use { writer ->
                ConfigManager.gson.toJson(currentCommitJson, writer)
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Error writing current repo commit")
        }
    }
}
