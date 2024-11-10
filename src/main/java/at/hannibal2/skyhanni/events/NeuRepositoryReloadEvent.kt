
package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.enoughupdates.EnoughUpdatesManager
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.repo.RepoError
import at.hannibal2.skyhanni.data.repo.RepoUtils
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.json.getJson
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import java.io.File
import java.lang.reflect.Type

class NeuRepositoryReloadEvent : LorenzEvent() {
    fun getConstant(file: String): JsonObject? = File(EnoughUpdatesManager.repoLocation, "constants/$file.json").getJson()

    inline fun <reified T : Any> readConstant(file: String, gson: Gson = ConfigManager.gson): T {
        val data = getConstant(file) ?: ErrorManager.skyHanniError("$file failed to load from neu repo!")
        return try {
            gson.fromJson<T>(data)
        } catch (e: JsonSyntaxException) {
            ErrorManager.logErrorWithData(
                e, "$file failed to read from neu repo!",
                "data" to data,
            )
            throw e
        }
    }

    inline fun <reified T : Any> getConstant(constant: String, type: Type? = null, gson: Gson = ConfigManager.gson): T = try {
        if (!EnoughUpdatesManager.repoLocation.exists()) throw RepoError("NEU-Repo folder does not exist!")
        RepoUtils.getConstant(EnoughUpdatesManager.repoLocation, constant, gson, T::class.java, type)
    } catch (e: Exception) {
        throw RepoError("Repo parsing error while trying to read constant '$constant'", e)
    }
}
