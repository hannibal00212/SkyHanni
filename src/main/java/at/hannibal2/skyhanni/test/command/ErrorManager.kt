package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.jsonobjects.repo.RepoErrorData
import at.hannibal2.skyhanni.data.jsonobjects.repo.RepoErrorJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object ErrorManager {

    // random id -> error message
    private val errorMessages = mutableMapOf<String, String>()
    private val fullErrorMessages = mutableMapOf<String, String>()
    private val cache = TimeLimitedSet<Pair<String, Int>>(10.minutes)
    private var repoErrors: List<RepoErrorData> = emptyList()

    private val breakAfter = listOf(
        "at at.hannibal2.skyhanni.config.commands.Commands\$createCommand",
        "at net.minecraftforge.fml.common.eventhandler.EventBus.post",
        "at at.hannibal2.skyhanni.mixins.hooks.NetHandlerPlayClientHookKt.onSendPacket",
        "at net.minecraft.client.main.Main.main",
    )

    private val replace = mapOf(
        "at.hannibal2.skyhanni" to "SH",
        "io.moulberry.notenoughupdates" to "NEU",
        "net.minecraft." to "MC.",
        "net.minecraftforge.fml." to "FML.",
    )

    private val ignored = listOf(
        "at java.lang.Thread.run",
        "at java.util.concurrent.",
        "at java.lang.reflect.",
        "at net.minecraft.network.",
        "at net.minecraft.client.Minecraft.addScheduledTask(",
        "at net.minecraftforge.fml.common.network.handshake.",
        "at net.minecraftforge.fml.common.eventhandler.",
        "at net.fabricmc.devlaunchinjector.",
        "at io.netty.",
        "at com.google.gson.internal.",
        "at sun.reflect.",

        "at at.hannibal2.skyhanni.config.commands.SimpleCommand.",
        "at at.hannibal2.skyhanni.config.commands.Commands\$createCommand\$1.processCommand",
        "at at.hannibal2.skyhanni.test.command.ErrorManager.logError",
        "at at.hannibal2.skyhanni.events.LorenzEvent.postAndCatch",
        "at at.hannibal2.skyhanni.api.event.SkyHanniEvent.post",
        "at at.hannibal2.skyhanni.api.event.EventHandler.post",
        "at net.minecraft.launchwrapper.",
    )

    fun resetCache() {
        cache.clear()
    }

    fun skyHanniError(message: String, vararg extraData: Pair<String, Any?>): Nothing {
        val exception = IllegalStateException(message)
        println("silent SkyHanni error:")
        println("message: '$message'")
        println("extraData: \n${buildExtraDataString(extraData)}")
        throw exception
    }

    private fun copyError(errorId: String) {
        val fullErrorMessage = KeyboardManager.isModifierKeyDown()
        val errorMessage = if (fullErrorMessage) {
            fullErrorMessages[errorId]
        } else {
            errorMessages[errorId]
        }
        val name = if (fullErrorMessage) "Full error" else "Error"
        ChatUtils.chat(
            errorMessage?.let {
                OSUtils.copyToClipboard(it)
                "$name copied into the clipboard, please report it on the SkyHanni discord!"
            } ?: "Error id not found!",
        )
    }

    fun logErrorStateWithData(
        userMessage: String,
        internalMessage: String,
        vararg extraData: Pair<String, Any?>,
        ignoreErrorCache: Boolean = false,
        noStackTrace: Boolean = false,
        betaOnly: Boolean = false,
        condition: () -> Boolean = { true },
    ) {
        logError(
            IllegalStateException(internalMessage),
            userMessage,
            ignoreErrorCache,
            noStackTrace,
            *extraData,
            betaOnly = betaOnly,
            condition = condition,
        )
    }

    fun logErrorWithData(
        throwable: Throwable,
        message: String,
        vararg extraData: Pair<String, Any?>,
        ignoreErrorCache: Boolean = false,
        noStackTrace: Boolean = false,
        betaOnly: Boolean = false,
    ) {
        logError(throwable, message, ignoreErrorCache, noStackTrace, *extraData, betaOnly = betaOnly)
    }

    private fun logError(
        throwable: Throwable,
        message: String,
        ignoreErrorCache: Boolean,
        noStackTrace: Boolean,
        vararg extraData: Pair<String, Any?>,
        betaOnly: Boolean = false,
        condition: () -> Boolean = { true },
    ) {
        if (betaOnly && !LorenzUtils.isBetaVersion()) return
        if (!ignoreErrorCache) {
            val pair = if (throwable.stackTrace.isNotEmpty()) {
                throwable.stackTrace[0].let { (it.fileName ?: "<unknown>") to it.lineNumber }
            } else message to 0
            if (pair in cache) return
            cache.add(pair)
        }
        if (!condition()) return

        Error(message, throwable).printStackTrace()
        Minecraft.getMinecraft().thePlayer ?: return

        val fullStackTrace: String
        val stackTrace: String

        if (noStackTrace) {
            fullStackTrace = "<no stack trace>"
            stackTrace = "<no stack trace>"
        } else {
            fullStackTrace = throwable.getCustomStackTrace(true).joinToString("\n")
            stackTrace = throwable.getCustomStackTrace(false).joinToString("\n")
        }
        val randomId = StringUtils.generateRandomId()

        val extraDataString = buildExtraDataString(extraData)
        val rawMessage = message.removeColor()
        errorMessages[randomId] = "```\nSkyHanni ${SkyHanniMod.version}: $rawMessage\n \n$stackTrace\n$extraDataString```"
        fullErrorMessages[randomId] =
            "```\nSkyHanni ${SkyHanniMod.version}: $rawMessage\n(full stack trace)\n \n$fullStackTrace\n$extraDataString```"

        val finalMessage = buildFinalMessage(message) ?: return
        ChatUtils.clickableChat(
            "§c[SkyHanni-${SkyHanniMod.version}]: $finalMessage Click here to copy the error into the clipboard.",
            onClick = { copyError(randomId) },
            "§eClick to copy!",
            prefix = false,
        )
    }

    private fun buildFinalMessage(message: String): String? {
        var finalMessage = message
        val rawMessage = message.removeColor()

        var hideError = false
        for (repoError in repoErrors) {
            for (string in repoError.messageStartsWith) {
                if (rawMessage.startsWith(string)) {
                    hideError = true
                }
            }
            for (string in repoError.messageExact) {
                if (rawMessage == string) {
                    hideError = true
                }
            }
            if (hideError) {
                repoError.replaceMessage?.let {
                    finalMessage = it
                    hideError = false
                }
                repoError.customMessage?.let {
                    ChatUtils.userError(it)
                    return null
                }
                break
            }
        }

        if (finalMessage.last() !in ".?!") {
            finalMessage += "§c."
        }
        return if (hideError) null else finalMessage
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<RepoErrorJson>("ChangedChatErrors")
        val version = SkyHanniMod.version

        repoErrors = data.changedErrorMessages.filter { version in it.affectedVersions }
    }

    private fun buildExtraDataString(extraData: Array<out Pair<String, Any?>>): String {
        val extraDataString = if (extraData.isNotEmpty()) {
            val builder = StringBuilder()
            for ((key, value) in extraData) {
                builder.append(key)
                builder.append(": ")
                if (value is Iterable<*>) {
                    builder.append("\n")
                    for (line in value) {
                        builder.append(" - '$line'")
                        builder.append("\n")
                    }
                } else {
                    builder.append("'$value'")
                }
                builder.append("\n")
            }
            "\nExtra data:\n$builder"
        } else ""
        return extraDataString
    }

    private fun Throwable.getCustomStackTrace(
        fullStackTrace: Boolean,
        parent: List<String> = emptyList(),
    ): List<String> = buildList {
        add("Caused by ${this@getCustomStackTrace.javaClass.name}: $message")

        for (traceElement in stackTrace) {
            val text = "\tat $traceElement"
            if (!fullStackTrace && text in parent) {
                break
            }
            var visualText = text
            if (!fullStackTrace) {
                for ((from, to) in replace) {
                    visualText = visualText.replace(from, to)
                }
            }
            if (!fullStackTrace && breakAfter.any { text.contains(it) }) {
                add(visualText)
                break
            }
            if (ignored.any { text.contains(it) }) continue
            add(visualText)
        }

        if (this === cause) {
            add("<Infinite recurring causes>")
            return@buildList
        }

        cause?.let {
            addAll(it.getCustomStackTrace(fullStackTrace, this))
        }
    }
}
