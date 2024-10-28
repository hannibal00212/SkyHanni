package at.hannibal2.skyhanni.features.gui.electionviewer

import at.hannibal2.skyhanni.data.ElectionAPI
import at.hannibal2.skyhanni.data.MayorAPI
import at.hannibal2.skyhanni.features.gui.electionviewer.ElectionViewerUtils.getFakeCandidateRenderable
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import java.awt.Color

@SkyHanniModule
object CurrentElectionScreen : ElectionViewerScreen() {

    override val posLabel = "Election Viewer - Current Election"

    override fun updateDisplay() {
        val currentElection = ElectionAPI.rawMayorData?.current ?: return
        val votesHidden = currentElection.candidates.sumOf { it.votes } == 0

        val sortedCandidates = currentElection.candidates.sortedByDescending { it.votes }

        val candidateWithRank = if (votesHidden) {
            sortedCandidates.map { it to null }
        } else {
            sortedCandidates.mapIndexed { index, candidate ->
                candidate to when (index) {
                    0 -> Color.YELLOW
                    1 -> Color.LIGHT_GRAY
                    else -> null
                }
            }
        }

        val candidateRenderables = currentElection.candidates.map { candidate ->
            val (_, rankColor) = candidateWithRank.first { it.first == candidate }
            val color = ElectionAPI.mayorNameToColorCode(candidate.name)
            val votesText = if (votesHidden) "§7????" else "§7" + candidate.votes.addSeparators()

            val candidateContent = Renderable.verticalContainer(
                listOf(
                    Renderable.string(color + candidate.name),
                    Renderable.string(votesText),
                ),
                spacing = 10,
                verticalAlign = VerticalAlignment.CENTER,
                horizontalAlign = HorizontalAlignment.CENTER,
            )

            val candidateContainer = Renderable.horizontalContainer(
                listOf(
                    getFakeCandidateRenderable(candidate),
                    candidateContent,
                ),
            )

            val perksContainer = Renderable.verticalContainer(
                candidate.perks.map {
                    Renderable.hoverTips(
                        Renderable.wrappedString(
                            buildString {
                                if (it.minister) append("§6✯ ")
                                append(color + it.name)
                            },
                            candidateContainer.width,
                            horizontalAlign = HorizontalAlignment.CENTER,
                            internalAlign = HorizontalAlignment.CENTER,
                        ),
                        listOf(Renderable.wrappedString("§7" + it.description, 200)),
                        bypassChecks = true,
                    )
                },
                spacing = 5,
                verticalAlign = VerticalAlignment.CENTER,
                horizontalAlign = HorizontalAlignment.CENTER,
            )

            val fullCandidateContainer = Renderable.verticalContainer(
                listOf(
                    candidateContainer,
                    perksContainer,
                ),
                spacing = 10,
                verticalAlign = VerticalAlignment.TOP,
                horizontalAlign = HorizontalAlignment.CENTER,
            )

            rankColor?.withAlpha(200).let {
                Renderable.drawInsideRoundedRectWithOutline(
                    fullCandidateContainer,
                    Color.BLACK.addAlpha(0),
                    topOutlineColor = it ?: Color.BLACK.withAlpha(0),
                    bottomOutlineColor = it ?: Color.BLACK.withAlpha(0),
                    borderOutlineThickness = 4,
                )
            }
        }

        display = Renderable.verticalContainer(
            listOf(
                Renderable.string("Current Election", horizontalAlign = HorizontalAlignment.CENTER),
                Renderable.horizontalContainer(candidateRenderables, spacing = 10),
            ),
            verticalAlign = VerticalAlignment.CENTER,
            horizontalAlign = HorizontalAlignment.CENTER,
            spacing = 20,
        )
    }


    override fun isInGui() = Minecraft.getMinecraft().currentScreen is CurrentElectionScreen
}
