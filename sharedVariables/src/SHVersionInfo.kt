package at.skyhanni.sharedvariables

object SHVersionInfo {
    val gitHash by lazy {
        val proc = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .start()
        proc.waitFor()
        proc.inputStream.readBytes().decodeToString().trim()
    }
}
