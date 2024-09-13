package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import org.apache.http.HttpEntity
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object APIUtil {

    private val parser = JsonParser()
    private var showApiErrors = false

    data class ApiResponse(val success: Boolean, val message: String?, val data: JsonObject)

    enum class SkinBodyPart(val path: String) {
        FRONT("body/front"),
        LEFT("body/left"),
        BACK("body/back"),
        RIGHT("body/right"),
        FACE("face"),
        HEAD("head");

        override fun toString(): String {
            return "§9${name.lowercase().firstLetterUppercase()}"
        }
    }

    private val builder: HttpClientBuilder =
        HttpClients.custom().setUserAgent("SkyHanni/${SkyHanniMod.version}")
            .setDefaultHeaders(
                mutableListOf(
                    BasicHeader("Pragma", "no-cache"),
                    BasicHeader("Cache-Control", "no-cache"),
                ),
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .build(),
            )
            .useSystemProperties()

    /**
     * TODO
     * make suspend
     * use withContext(Dispatchers.IO) { APIUtil.getJSONResponse(url) }.asJsonObject
     */
    fun getJSONResponse(urlString: String, silentError: Boolean = false) =
        getJSONResponseAsElement(urlString, silentError) as JsonObject

    fun getJSONResponseAsElement(
        urlString: String,
        silentError: Boolean = false,
        apiName: String = "Hypixel API",
    ): JsonElement {
        val client = builder.build()
        try {
            client.execute(HttpGet(urlString)).use { response ->
                val entity = response.entity
                if (entity != null) {
                    val retSrc = EntityUtils.toString(entity)
                    try {
                        return parser.parse(retSrc)
                    } catch (e: JsonSyntaxException) {
                        val name = e.javaClass.name
                        val message = "$name: ${e.message}"
                        if (e.message?.contains("Use JsonReader.setLenient(true)") == true) {
                            println("MalformedJsonException: Use JsonReader.setLenient(true)")
                            println(" - getJSONResponse: '$urlString'")
                            ChatUtils.debug("MalformedJsonException: Use JsonReader.setLenient(true)")
                        } else if (retSrc.contains("<center><h1>502 Bad Gateway</h1></center>")) {
                            if (showApiErrors && apiName == "Hypixel API") {
                                ChatUtils.clickableChat(
                                    "Problems with detecting the Hypixel API. §eClick here to hide this message for now.",
                                    onClick = { toggleApiErrorMessages() },
                                    "§eClick to run /shtogglehypixelapierrors!",
                                )
                            }
                            ErrorManager.skyHanniError(
                                "SkyHanni Connection Error",
                                "error message" to "$message(502 Bad Gateway)",
                                "apiName" to apiName,
                                "urlString" to urlString,
                                "returnedData" to retSrc,
                            )
                        } else {
                            ErrorManager.skyHanniError(
                                "SkyHanni Connection Error",
                                "error message" to message,
                                "apiName" to apiName,
                                "urlString" to urlString,
                                "returnedData" to retSrc,
                            )
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            if (silentError) {
                throw e
            }
            val name = e.javaClass.name
            val message = "$name: ${e.message}"
            ErrorManager.skyHanniError(
                "SkyHanni Connection Error",
                "error message" to message,
                "urlString" to urlString,
            )
        } finally {
            client.close()
        }
        return JsonObject()
    }

    fun postJSON(urlString: String, body: String, silentError: Boolean = false): ApiResponse {
        val client = builder.build()

        try {
            val method = HttpPost(urlString)
            method.entity = StringEntity(body, ContentType.APPLICATION_JSON)

            client.execute(method).use { response ->
                val status = response.statusLine
                val entity = response.entity

                if (status.statusCode in 200..299) {
                    val data = readResponse(entity)
                    return ApiResponse(true, "Request successful", data)
                }

                val message = "POST request to '$urlString' returned status ${status.statusCode}"
                ErrorManager.logErrorStateWithData(
                    "Error communicating with API", "APIUtil POST request returned an error code",
                    "statusCode" to status.statusCode,
                    "urlString" to urlString,
                    "body" to body,
                )
                return ApiResponse(false, message, JsonObject())
            }
        } catch (throwable: Throwable) {
            if (silentError) {
                throw throwable
            }
            ErrorManager.logErrorWithData(
                throwable, "SkyHanni ran into an ${throwable::class.simpleName ?: "error"} whilst sending a resource",
                "urlString" to urlString,
                "body" to body,
            )
            return ApiResponse(false, throwable.message, JsonObject())
        } finally {
            client.close()
        }
    }

    private fun readResponse(entity: HttpEntity): JsonObject {
        val retSrc = EntityUtils.toString(entity) ?: return JsonObject()
        val parsed = parser.parse(retSrc)
        if (parsed.isJsonNull) return JsonObject()
        return parsed as JsonObject
    }

    fun postJSONIsSuccessful(url: String, body: String, silentError: Boolean = false): Boolean {
        val response = postJSON(url, body, silentError)

        if (response.success) {
            return true
        }

        ErrorManager.logErrorStateWithData(
            "An error occurred during the API request",
            "unsuccessful API response",
            "url" to url,
            "body" to body,
            "message" to response.message,
            "response" to response,
        )

        return false
    }

    fun patchJSON(urlString: String, body: String, silentError: Boolean = false): ApiResponse {
        val client = builder.build()

        try {
            val method = HttpPatch(urlString)
            method.entity = StringEntity(body, ContentType.APPLICATION_JSON)

            client.execute(method).use { response ->
                val status = response.statusLine
                val entity = response.entity

                if (status.statusCode in 200..299) {
                    val data = readResponse(entity)
                    return ApiResponse(true, "Request successful", data)
                }

                val message = "PATCH request to '$urlString' returned status ${status.statusCode}"
                ErrorManager.logErrorStateWithData(
                    "Error communicating with API", "APIUtil PATCH request returned an error code",
                    "statusCode" to status.statusCode,
                    "urlString" to urlString,
                    "body" to body,
                )
                return ApiResponse(false, message, JsonObject())
            }
        } catch (throwable: Throwable) {
            if (silentError) {
                throw throwable
            }
            ErrorManager.logErrorWithData(
                throwable, "SkyHanni ran into an ${throwable::class.simpleName ?: "error"} whilst sending a resource",
                "urlString" to urlString,
                "body" to body,
            )
            return ApiResponse(false, throwable.message, JsonObject())
        } finally {
            client.close()
        }
    }

    fun getPlayerSkin(part: SkinBodyPart, scale: Int) =
        "https://api.mineatar.io/${part.path}/${LorenzUtils.getPlayerUuid()}?scale=$scale"

    fun readFile(file: File): BufferedReader {
        return BufferedReader(InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8))
    }

    // TODO remove command, use clickable chat message instead
    fun toggleApiErrorMessages() {
        showApiErrors = !showApiErrors
        ChatUtils.chat("Hypixel API error messages " + if (showApiErrors) "§chidden" else "§ashown")
    }
}
