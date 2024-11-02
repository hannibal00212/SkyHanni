package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.filter.AbstractFilter
import org.apache.logging.log4j.message.Message

@Suppress("CommentWrapping")
class MinecraftConsoleFilter(private val loggerConfigName: String) : AbstractFilter(
    /* onMatch = */ Filter.Result.ACCEPT,
    /* onMismatch = */ Filter.Result.DENY,
) {

    private val config get() = SkyHanniMod.feature.dev.minecraftConsoles
    private val filterConfig get() = config.consoleFilter

    private val loggerFiltered = LorenzLogger("debug/mc_console/filtered")
    private val loggerUnfiltered = LorenzLogger("debug/mc_console/unfiltered")

    private val patternBiomeIdBounds = "Biome ID is out of bounds: (\\d+), defaulting to 0 \\(Ocean\\)".toPattern()

    companion object {

        fun initLogging() {
            val ctx: LoggerContext = LogManager.getContext(false) as LoggerContext

            for (loggerConfig in ctx.configuration.loggers.values) {
                val loggerName = loggerConfig.name
                loggerConfig.addFilter(MinecraftConsoleFilter(loggerName))
            }
        }
    }

    override fun filter(event: LogEvent?): Filter.Result {
        if (event == null) return Filter.Result.ACCEPT

        val loggerName = event.loggerName
        if (loggerName == "SkyHanni") return Filter.Result.ACCEPT

        val message = event.message
        val formattedMessage = message.formattedMessage
        val thrown = event.thrown

        if (filterConfig.filterChat && formattedMessage.startsWith("[CHAT] ")) {
            filterConsole("chat")
            return Filter.Result.DENY
        }
        if (filterConfig.filterGrowBuffer && formattedMessage.startsWith("Needed to grow BufferBuilder buffer: Old size ")) {
            filterConsole("Grow BufferBuilder buffer")
            return Filter.Result.DENY
        }
        if (filterConfig.filterUnknownSound && formattedMessage.startsWith("Unable to play unknown soundEvent: minecraft:")) {
            filterConsole("Unknown soundEvent (minecraft:)")
            return Filter.Result.DENY
        }
        // TODO testing
        if (filterConfig.filterParticleVillagerHappy && formattedMessage == "Could not spawn particle effect VILLAGER_HAPPY") {
            filterConsole("particle VILLAGER_HAPPY")
            return Filter.Result.DENY
        }

        if (filterConfig.filterOptiFine) {
            if (formattedMessage.startsWith("[OptiFine] CustomItems: ")) {
                filterConsole("OptiFine CustomItems")
                return Filter.Result.DENY
            }
            if (formattedMessage.startsWith("[OptiFine] ConnectedTextures: ")) {
                filterConsole("OptiFine ConnectedTextures")
                return Filter.Result.DENY
            }
        }
        if (loggerName == "AsmHelper" && filterConfig.filterAmsHelperTransformer) {
            if (formattedMessage.startsWith("Transforming class ")) {
                filterConsole("AsmHelper Transforming")
                return Filter.Result.DENY
            }
            if (filterConfig.filterAsmHelperApplying && formattedMessage.startsWith("Applying AsmWriter ModifyWriter")) {
                filterConsole("AsmHelper Applying AsmWriter")
                return Filter.Result.DENY
            }
        }

        // TODO find a way to load the filter earlier to filter these messages too
//        if (loggerName == "LaunchWrapper") {
//            //The jar file C:\Users\Lorenz\AppData\Roaming\.minecraft\libraries\org\lwjgl\lwjgl\lwjgl\2.9.4-nightly-20150209\lwjgl-2.9.4-nightly-20150209.jar has a security seal for path org.lwjgl.opengl, but that path is defined and not secure
//            if (formattedMessage.startsWith("The jar file ")) {
//                if (formattedMessage.endsWith(
//                        ".jar has a security seal for path org.lwjgl.opengl, " +
//                                "but that path is defined and not secure"
//                    )
//                ) {
//                    filterConsole("LaunchWrapper org.lwjgl.opengl security seal")
//                    return Filter.Result.DENY
//                }
//                if (formattedMessage.endsWith(
//                        ".jar has a security seal for path org.lwjgl, " +
//                                "but that path is defined and not secure"
//                    )
//                ) {
//                    filterConsole("LaunchWrapper org.lwjgl security seal")
//                    return Filter.Result.DENY
//                }
//            }
//        }
//        if (loggerName == "mixin") {
//            if (formattedMessage.startsWith("Mixing ") && formattedMessage.contains(" into ")) {
//                filterConsole("Mixing")
//                return Filter.Result.DENY
//            }
//        }
        if (filterConfig.filterBiomeIdBounds) {
            patternBiomeIdBounds.matchMatcher(formattedMessage) {
                filterConsole("Biome ID bounds")
                return Filter.Result.DENY
            }
        }

        if (thrown != null && filterConfig.filterScoreboardErrors) {
            val cause = thrown.cause
            if (cause != null && cause.stackTrace.isNotEmpty()) {
                val first = cause.stackTrace[0]
                val firstName = first.toString()
                if (firstName == "net.minecraft.scoreboard.Scoreboard.removeTeam(Scoreboard.java:229)" ||
                    firstName == "net.minecraft.scoreboard.Scoreboard.removeTeam(Scoreboard.java:262)" ||
                    firstName == "net.minecraft.scoreboard.Scoreboard.removeTeam(Scoreboard.java:240)"
                ) {
                    filterConsole("NullPointerException at Scoreboard.removeTeam")
                    return Filter.Result.DENY
                }
                if (firstName == "net.minecraft.scoreboard.Scoreboard.createTeam(Scoreboard.java:218)") {
                    filterConsole("IllegalArgumentException at Scoreboard.createTeam")
                    return Filter.Result.DENY
                }
                if (firstName == "net.minecraft.scoreboard.Scoreboard.removeObjective(Scoreboard.java:179)" ||
                    firstName == "net.minecraft.scoreboard.Scoreboard.removeObjective(Scoreboard.java:198)" ||
                    firstName == "net.minecraft.scoreboard.Scoreboard.removeObjective(Scoreboard.java:186)"
                ) {
                    filterConsole("IllegalArgumentException at Scoreboard.removeObjective")
                    return Filter.Result.DENY
                }
            }
            if (thrown.toString().contains(" java.lang.IllegalArgumentException: A team with the name '")) {
                filterConsole("IllegalArgumentException because scoreboard team already exists")
                return Filter.Result.DENY
            }
        }

        if (!config.printUnfilteredDebugs) return Filter.Result.ACCEPT
        if (!config.printUnfilteredDebugsOutsideSkyBlock && !LorenzUtils.inSkyBlock) return Filter.Result.ACCEPT
        if (formattedMessage == "filtered console: ") return Filter.Result.ACCEPT

        debug(" ")
        debug("filter 4/event ('$loggerConfigName'/'$loggerName')")
        debug("formattedMessage: '$formattedMessage'")
        val threadName = event.threadName
        debug("threadName: '$threadName'")
        val level = event.level
        debug("level: '$level'")
        val marker = event.marker
        if (marker != null) {
            val name = marker.name
            debug("marker name: '$name'")
        } else {
            debug("marker is null")
        }
        debug("thrown: '$thrown'")
        if (thrown != null && thrown.stackTrace.isNotEmpty()) {
            var element = thrown.stackTrace[0]
            debug("thrown first element: '$element'")
            val cause = thrown.cause
            if (cause != null) {
                debug("throw cause: '$cause'")
                if (cause.stackTrace.isNotEmpty()) {
                    element = cause.stackTrace[0]
                    debug("thrown cause first element: '$element'")
                } else {
                    debug("thrown cause has no elements")
                }
            }
        }
        debug(" ")

        return Filter.Result.ACCEPT
    }

    private fun debug(text: String) {
        if (config.logUnfilteredFile) {
            loggerUnfiltered.log(text)
        } else {
            LorenzUtils.consoleLog(text)
        }
    }

    private fun filterConsole(message: String) {
        loggerFiltered.log(message)
        if (config.printFilteredReason) {
            LorenzUtils.consoleLog("filtered console: $message")
        }
    }

    override fun filter(
        logger: Logger?,
        level: Level?,
        marker: Marker?,
        msg: String?,
        vararg params: Any?,
    ): Filter.Result {
        return Filter.Result.ACCEPT
    }

    override fun filter(
        logger: Logger?,
        level: Level?,
        marker: Marker?,
        msg: Any?,
        t: Throwable?,
    ): Filter.Result {
        return Filter.Result.ACCEPT
    }

    override fun filter(
        logger: Logger?,
        level: Level?,
        marker: Marker?,
        msg: Message?,
        t: Throwable?,
    ): Filter.Result {
        return Filter.Result.ACCEPT
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "dev.printUnfilteredDebugs", "dev.minecraftConsoles.printUnfilteredDebugs")
        event.move(3, "dev.logUnfilteredFile", "dev.minecraftConsoles.logUnfilteredFile")
        event.move(
            3,
            "dev.printUnfilteredDebugsOutsideSkyBlock",
            "dev.minecraftConsoles.printUnfilteredDebugsOutsideSkyBlock"
        )
        event.move(3, "dev.printFilteredReason", "dev.minecraftConsoles.printFilteredReason")
        event.move(3, "dev.filterChat", "dev.minecraftConsoles.consoleFilter.filterChat")
        event.move(3, "dev.filterGrowBuffer", "dev.minecraftConsoles.consoleFilter.filterGrowBuffer")
        event.move(3, "dev.filterUnknownSound", "dev.minecraftConsoles.consoleFilter.filterUnknownSound")
        event.move(
            3,
            "dev.filterParticleVillagerHappy",
            "dev.minecraftConsoles.consoleFilter.filterParticleVillagerHappy"
        )
        event.move(
            3,
            "dev.filterAmsHelperTransformer",
            "dev.minecraftConsoles.consoleFilter.filterAmsHelperTransformer"
        )
        event.move(3, "dev.filterAsmHelperApplying", "dev.minecraftConsoles.consoleFilter.filterAsmHelperApplying")
        event.move(3, "dev.filterBiomeIdBounds", "dev.minecraftConsoles.consoleFilter.filterBiomeIdBounds")
        event.move(3, "dev.filterScoreboardErrors", "dev.minecraftConsoles.consoleFilter.filterScoreboardErrors")
        event.move(3, "dev.filterOptiFine", "dev.minecraftConsoles.consoleFilter.filterOptiFine")
    }
}
