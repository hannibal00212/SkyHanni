package at.hannibal2.skyhanni.features.gui.electionviewer

import at.hannibal2.skyhanni.data.ElectionAPI
import at.hannibal2.skyhanni.data.ElectionAPI.ELECTION_END_DAY
import at.hannibal2.skyhanni.data.ElectionAPI.ELECTION_END_MONTH
import at.hannibal2.skyhanni.data.ElectionCandidate
import at.hannibal2.skyhanni.features.gui.electionviewer.ElectionViewerUtils.getFakeMayorRenderable
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft

@SkyHanniModule
object SpecialMayorScreen : ElectionViewerScreen() {

    override val posLabel = "Election Viewer - Special Mayor"

    override fun updateDisplay() {
        val specialMayorRenderables = ElectionViewerUtils.getNextSpecialMayors(SkyBlockTime.now().year).let { nextMayors ->
            nextMayors.map { (name, year) ->
                val electionTime = SkyBlockTime(year, ELECTION_END_MONTH, day = ELECTION_END_DAY).asTimeMark()
                val color = ElectionAPI.mayorNameToColorCode(name)
                Renderable.horizontalContainer(
                    listOf(
                        getFakeMayorRenderable(ElectionCandidate.getMayorFromName(name) ?: ElectionCandidate.UNKNOWN),
                        Renderable.verticalContainer(
                            listOf(
                                "$color$name",
                                "in year $color$year",
                                "Election in $color${electionTime.timeUntil().format(maxUnits = 2)}",
                                "Election at $color${electionTime.formattedDate()}",
                            ).map { Renderable.wrappedString(it, 130) },
                            spacing = 5,
                            verticalAlign = VerticalAlignment.CENTER,
                        ),
                    ),
                    spacing = 10,
                )
            }
        }

        display = Renderable.verticalContainer(
            listOf(
                Renderable.string("Next Special Mayors", horizontalAlign = HorizontalAlignment.CENTER),
                Renderable.string(
                    "The next three special mayors and the year they will (most likely) be elected",
                    horizontalAlign = HorizontalAlignment.CENTER,
                ),
                Renderable.horizontalContainer(
                    specialMayorRenderables,
                    horizontalAlign = HorizontalAlignment.CENTER,
                    spacing = 10,
                ),
            ),
            spacing = 20,
            verticalAlign = VerticalAlignment.CENTER,
            horizontalAlign = HorizontalAlignment.CENTER,
        )
    }

    override fun isInGui() = Minecraft.getMinecraft().currentScreen is SpecialMayorScreen
}
