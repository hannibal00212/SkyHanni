package at.hannibal2.skyhanni.test.garden

import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
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
        mockkObject(GardenAPI)
        every { GardenAPI.inGarden() } returns true

        mockkObject(LorenzUtils)
        every { LorenzUtils.getPlayerName() } returns "ThePlayerName"

        mockkObject(VisitorAPI)
        every { VisitorAPI.addVisitor(any()) } returns true

        listener = VisitorListener
    }

    @Test
    fun `onTablistUpdate it should add new visitors to the list`() {
        listener.onTabListUpdate(
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

        verify { VisitorAPI.addVisitor("§fJacob") }
        verify { VisitorAPI.addVisitor("§cSpaceman") }
        verify { VisitorAPI.addVisitor("§6Madame Eleanor Q. Goldsworth III") }
    }

    @Test
    fun `onTablistUpdate it should remove visitors from the list`() {
        every { VisitorAPI.getVisitors() } returns listOf(
            mockk { every { visitorName } returns "§fJacob" },
        )

        listener.onTabListUpdate(
            fakeTabWidget(
                mutableListOf("§b§lVisitors: §r§f(0)", ""),
            ),
        )

        verify { VisitorAPI.removeVisitor("§fJacob") }
    }

    @Test
    fun `onTablistUpdate it should not remove visitors if the timeout is not hit`() {
        every { VisitorAPI.getVisitors() } returns listOf(
            mockk { every { visitorName } returns "§fJacob" },
        )

        every { LorenzUtils.lastWorldSwitch } returns SimpleTimeMark.now()

        listener.onTabListUpdate(
            fakeTabWidget(
                mutableListOf("§b§lVisitors: §r§f(0)", ""),
            ),
        )

        verify(exactly = 0) { VisitorAPI.removeVisitor("§fJacob") }
    }

    private fun fakeTabWidget(lines: List<String>): WidgetUpdateEvent {
        return WidgetUpdateEvent(TabWidget.VISITORS, lines)
    }
}
