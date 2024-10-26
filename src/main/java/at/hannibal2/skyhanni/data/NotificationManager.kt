package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import io.github.notenoughupdates.moulconfig.internal.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object NotificationManager {

    private val notificationQueue = mutableListOf<SkyHanniNotification>()

    private var currentNotification: SkyHanniNotification? = null
    private var lastNotificationClosed = SimpleTimeMark.farPast()

    private const val CLOSE_TEXT = "§c[X] Close"

    @SubscribeEvent
    fun onKeyClick(event: LorenzKeyPressEvent) {
        currentNotification ?: return
        if (lastNotificationClosed.passedSince() < 200.milliseconds) return
        if (event.keyCode != Keyboard.KEY_X) return
        currentNotification = null
        lastNotificationClosed = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        val notification = getCurrentNotification() ?: return

        if (InventoryUtils.inInventory() && !notification.showOverInventory) return

        val midX = GuiScreenUtils.scaledWindowWidth / 2
        val topY = (GuiScreenUtils.scaledWindowHeight * 0.75 - notification.height / 2).toInt()

        RenderUtils.drawFloatingRectDark(midX - notification.width / 2, topY, notification.width, notification.height)
        val closeTextWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(CLOSE_TEXT)

        GuiRenderUtils.drawString(CLOSE_TEXT, midX + notification.width / 2 - 3 - closeTextWidth, topY + 4)

        if (notification.length.isFinite()) {
            val remainingTime = "§8" + notification.endTime.timeUntil().format()
            GuiRenderUtils.drawString(remainingTime, midX - notification.width / 2 + 4, topY + 4)
        }

        notification.message.forEachIndexed { index, line ->
            GuiRenderUtils.drawStringCentered("§7$line", midX, topY + 19 + index * 10)
        }
    }

    private fun getCurrentNotification(): SkyHanniNotification? {
        currentNotification?.let {
            if (it.endTime.isInPast()) currentNotification = null
        }
        if (currentNotification == null) {
            currentNotification = notificationQueue.removeFirstOrNull()
            currentNotification?.setEndTime()
        }
        return currentNotification
    }

    fun queueNotification(notification: SkyHanniNotification) {
        notificationQueue.add(notification)
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shtestnotification") {
            description = "Shows a test notification"
            category = CommandCategory.DEVELOPER_TEST
            callback {
                val testingText = it.joinToString(" ").replace("\\n", "\n")
                queueNotification(SkyHanniNotification(testingText, Duration.INFINITE))
            }
        }
    }
}

data class SkyHanniNotification(
    val message: List<String>,
    val length: Duration,
    val showOverInventory: Boolean = false,
) {
    constructor(message: String, length: Duration, showOverInventory: Boolean = false) : this(
        message.lines(),
        length,
        showOverInventory,
    )

    var endTime = SimpleTimeMark.farFuture()

    val width by lazy { (message.maxOfOrNull { Minecraft.getMinecraft().fontRendererObj.getStringWidth(it) } ?: 0) + 8 }
    val height by lazy { message.size * 10 + 18 }

    fun setEndTime() {
        if (length.isInfinite()) return
        endTime = SimpleTimeMark.now() + length
    }
}
