package at.hannibal2.skyhanni.test.garden

import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.features.garden.visitor.VisitorApi
import at.hannibal2.skyhanni.features.garden.visitor.VisitorListener
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VisitorListenerTest {
    private lateinit var listener: VisitorListener

    @BeforeEach
    fun setUp() {
        mockkObject(GardenApi)
        every { GardenApi.inGarden() } returns true

        mockkObject(LorenzUtils)
        every { LorenzUtils.getPlayerName() } returns "ThePlayerName"

        mockkObject(VisitorApi)
        every { VisitorApi.addVisitor(any()) } returns true

        listener = VisitorListener
    }

    @Test
    fun `onTablistUpdate it should add new visitors to the list`() {
        listener.onWidgetUpdate(
            fakeTabWidget(
                mutableListOf(
                    "§b§lVisitors: §r§f(3)",
                    " §r§cSpaceman",
                    " §r§6Madame Eleanor Q. Goldsworth III §r§fCarrot §r§c333 C §r§3107k FXP §r§275 GXP",
                    " §r§fJacob",
                    "ThePlayerName",
                    "",
                ),
            ),
        )

        verify { VisitorApi.addVisitor("§fJacob") }
        verify { VisitorApi.addVisitor("§cSpaceman") }
        verify { VisitorApi.addVisitor("§6Madame Eleanor Q. Goldsworth III") }
    }

    @Test
    fun `onTablistUpdate it should remove visitors from the list`() {
        every { VisitorApi.getVisitors() } returns listOf(
            mockk { every { visitorName } returns "§fJacob" },
        )

        listener.onWidgetUpdate(
            fakeTabWidget(
                mutableListOf("§b§lVisitors: §r§f(0)", ""),
            ),
        )

        verify { VisitorApi.removeVisitor("§fJacob") }
    }

    @Test
    fun `onTablistUpdate it should not remove visitors if the timeout is not hit`() {
        every { VisitorApi.getVisitors() } returns listOf(
            mockk { every { visitorName } returns "§fJacob" },
        )

        every { LorenzUtils.lastWorldSwitch } returns SimpleTimeMark.now()

        listener.onWidgetUpdate(
            fakeTabWidget(
                mutableListOf("§b§lVisitors: §r§f(0)", ""),
            ),
        )

        verify(exactly = 0) { VisitorApi.removeVisitor("§fJacob") }
    }

    private fun fakeTabWidget(lines: List<String>): WidgetUpdateEvent {
        return WidgetUpdateEvent(TabWidget.VISITORS, lines)
    }
}
