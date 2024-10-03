package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.test.command.ErrorManager
import java.awt.Desktop
import java.io.IOException
import java.net.URI

object OSUtils {

    val isWindows: Boolean
    val isMac: Boolean
    val isLinux: Boolean

    init {
        val os = System.getProperty("os.name")
        isWindows = os.contains("win", ignoreCase = true)
        isMac = os.contains("mac", ignoreCase = true)
        isLinux = os.contains("linux", ignoreCase = true)
    }

    @JvmStatic
    fun openBrowser(url: String) {
        val desktopSupported = Desktop.isDesktopSupported()
        val supportedActionBrowse = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        if (desktopSupported && supportedActionBrowse) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (e: IOException) {
                ErrorManager.logErrorWithData(
                    e, "Error while opening website.",
                    "url" to url,
                )
            }
        } else {
            copyToClipboard(url)
            ErrorManager.logErrorStateWithData(
                "Cannot open website! Copied url to clipboard instead", "Web browser is not supported",
                "url" to url,
                "desktopSupported" to desktopSupported,
                "supportedActionBrowse" to supportedActionBrowse,
            )
        }
    }

    fun copyToClipboard(text: String) {
        ClipboardUtils.copyToClipboard(text)
    }

    suspend fun readFromClipboard() = ClipboardUtils.readFromClipboard()
}
