package at.hannibal2.skyhanni.features.gui.electionviewer

import at.hannibal2.skyhanni.data.ElectionAPI
import at.hannibal2.skyhanni.features.gui.electionviewer.ElectionViewerUtils.getFakeMayorRenderable
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft

@SkyHanniModule
object CurrentMayorScreen : ElectionViewerScreen() {

    override val posLabel = "Election Viewer - Current Mayor"

    override fun updateDisplay() {
        val mayor = ElectionAPI.currentMayor ?: return
        val minister = ElectionAPI.currentMinister
        val jerryMayor = ElectionAPI.jerryExtraMayor

        display = Renderable.verticalContainer(
            listOf(
                Renderable.string("Current Mayor & Minister", horizontalAlign = HorizontalAlignment.CENTER),
                Renderable.string(
                    "New Mayor in §e${ElectionAPI.nextMayorTimestamp.timeUntil().format(showMilliSeconds = false)}",
                    horizontalAlign = HorizontalAlignment.CENTER,
                ),
                Renderable.horizontalContainer(
                    listOfNotNull(
                        getMayorRenderable(mayor, "Mayor"),
                        getMayorRenderable(jerryMayor.first, "Jerry Mayor", jerryMayor.second),
                        getMayorRenderable(minister, "Minister"),
                    ),
                    spacing = 50,
                ),
            ),
            spacing = 20,
            verticalAlign = VerticalAlignment.CENTER,
            horizontalAlign = HorizontalAlignment.CENTER,
        )
    }

    private fun getMayorRenderable(mayor: ElectionCandidate?, type: String, time: SimpleTimeMark? = null): Renderable? {
        if (mayor == null) return null

        val fakePlayer = getFakeMayorRenderable(mayor)

        val mayorDescription = getMayorDescription(mayor, type, time)

        return if (type == "Mayor") {
            Renderable.horizontalContainer(
                listOf(fakePlayer, mayorDescription),
                spacing = 5,
            )
        } else {
            Renderable.horizontalContainer(
                listOf(mayorDescription, fakePlayer),
                spacing = 5,
            )
        }
    }

    private fun getMayorDescription(mayor: ElectionCandidate, type: String, time: SimpleTimeMark? = null): Renderable {
        val color = ElectionAPI.mayorNameToColorCode(mayor.mayorName)
        return Renderable.verticalContainer(
            buildList {
                add("$color$type ${mayor.mayorName}")
                add("")
                if (time != null) {
                    add("§7Time left: §e${time.timeUntil().format(showMilliSeconds = false)}")
                    add("")
                }
                mayor.activePerks.forEach {
                    add(color + it.perkName)
                    add("§7${it.description}")
                    add("")
                }
            }.map { Renderable.wrappedString(it, 150) },
        )
    }

    override fun isInGui() = Minecraft.getMinecraft().currentScreen is CurrentMayorScreen
}
