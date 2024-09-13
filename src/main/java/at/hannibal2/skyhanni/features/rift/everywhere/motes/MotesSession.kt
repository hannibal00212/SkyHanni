package at.hannibal2.skyhanni.features.rift.everywhere.motes

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.inPartialHours
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object MotesSession {

    private val config get() = SkyHanniMod.feature.rift.motes.motesPerSession

    private var initialMotes: Long? = null
    private var currentMotes: Long? = null
    private var enterRiftTime = SimpleTimeMark.farPast()

    private val repoGroup = RepoPattern.group("rift.everywhere.motes")

    /**
     * REGEX-TEST:  Lifetime Motes: §r§d593,922
     */
    private val lifetimeMotesPattern by repoGroup.pattern(
        "lifetime",
        "\\s+Lifetime Motes: §r§d(?<motes>[\\d,.]+)",
    )

    @SubscribeEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.RIFT_INFO)) return
        lifetimeMotesPattern.firstMatcher(event.widget.lines) {
            val amount = group("motes").formatLong()
            if (initialMotes == null) {
                initialMotes = amount
                enterRiftTime = SimpleTimeMark.now()
            }
            // TODO move into RiftAPI, rename to lifetimeMotes, reuse in custom scoreboard maybe?
            currentMotes = amount
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.oldIsland == IslandType.THE_RIFT) {
            sendMotesInfo()
            initialMotes = null
            currentMotes = null
        }
    }

    private fun sendMotesInfo() {
        if (!config) return
        val initial = initialMotes ?: return
        val current = currentMotes ?: return
        val gained = current - initial
        if (gained == 0L) return
        val timeInRift = enterRiftTime.passedSince()
        val motesPerHour = (gained / timeInRift.inPartialHours).toLong()
        val hover = buildList {
            add("§7Gained: §d${gained.addSeparators()} motes")
            add("§7Time spent: §d${timeInRift.format()}")
            add("§7Motes/h: §d${motesPerHour.addSeparators()}")
        }
        ChatUtils.hoverableChat(
            "Gained §d${gained.addSeparators()} motes §ethis Rift session! (§d${motesPerHour.shortFormat()}/h§e)",
            hover,
        )
    }
}
