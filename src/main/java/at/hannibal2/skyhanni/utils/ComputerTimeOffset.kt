package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ComputerTimeOffset {
    private var offsetMillis: Duration? = null

    private val config get() = SkyHanniMod.feature.misc.warnAboutPcTimeOffset

    private val offsetFixLinks by lazy {
        when {
            OSUtils.isWindows -> {
                "https://support.microsoft.com/en-us/windows/how-to-set-your-time-and-time-zone-dfaa7122-479f-5b98-2a7b-fa0b6e01b261"
            }

            OSUtils.isLinux -> "https://unix.stackexchange.com/a/79116"
            OSUtils.isMac -> "https://support.apple.com/guide/mac-help/set-the-date-and-time-automatically-mchlp2996/mac"
            else -> null
        }
    }

    init {
        thread {
            while (true) {
                Thread.sleep(1000)
                detectTimeChange()
            }
        }
    }

    private fun checkOffset() {
        val wasOffsetBefore = (offsetMillis?.absoluteValue ?: 0.seconds) > 5.seconds
        SkyHanniMod.coroutineScope.launch {
            offsetMillis = getNtpOffset("time.google.com")
            offsetMillis?.let {
                tryDisplayOffset(wasOffsetBefore)
            }
        }
    }

    private fun getNtpOffset(ntpServer: String): Duration? = try {
        val client = NTPUDPClient()
        val address = InetAddress.getByName(ntpServer)
        val timeInfo = client.getTime(address)

        timeInfo.computeDetails()
        timeInfo.offset.milliseconds
    } catch (e: Exception) {
        if (LorenzUtils.inSkyBlock && config) ErrorManager.logErrorWithData(
            e, "Failed to get NTP offset",
            "server" to ntpServer,
        )
        else {
            @Suppress("PrintStackTrace")
            e.printStackTrace()
        }
        null
    }

    private var lastSystemTime = System.currentTimeMillis()

    private fun detectTimeChange() {
        val currentSystemTime = System.currentTimeMillis()
        val timeDifference = (currentSystemTime - lastSystemTime).milliseconds
        lastSystemTime = currentSystemTime

        val expectedDuration = 1.seconds
        val deviation = timeDifference - expectedDuration

        if (deviation.absoluteValue > 1.seconds) {
            checkOffset()
        }
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        DelayedRun.runDelayed(5.seconds) {
            checkOffset()
        }
    }

    private fun tryDisplayOffset(wasOffsetBefore: Boolean) {
        if (!config || !LorenzUtils.onHypixel) return
        val offsetMillis = offsetMillis ?: return
        if (offsetMillis.absoluteValue < 5.seconds) {
            if (wasOffsetBefore) {
                ChatUtils.chat("Congratulations! Your computer's clock is now accurate.")
            }
            return
        }

        ChatUtils.clickableLinkChat(
            "Your computer's clock is off by ${offsetMillis.absoluteValue.format()}.\n" +
                "§ePlease update your time settings. Many features may not function correctly until you do.\n" +
                "§eClick here for instructions on how to fix your clock.",
            offsetFixLinks ?: return,
            prefixColor = "§c",
        )
    }

    @SubscribeEvent
    fun onDebugCollect(event: DebugDataCollectEvent) {
        event.title("Time Offset")
        val offset = offsetMillis ?: run {
            event.addIrrelevant("not calculated yet")
            return
        }

        val relevant = offset.absoluteValue > 500.milliseconds
        if (relevant) {
            event.addData(offset.toString())
        } else {
            event.addIrrelevant(offset.toString())
        }
    }
}
