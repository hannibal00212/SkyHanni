package at.hannibal2.skyhanni.utils

object LorenzDebug {

    private val logger = LorenzLogger("debug")

    fun log(text: String) {
        logger.log(text)
        LorenzUtils.consoleLog("debug logger: $text")
    }

    fun chatAndLog(text: String) {
        ChatUtils.debug(text)
        log(text)
    }
}
