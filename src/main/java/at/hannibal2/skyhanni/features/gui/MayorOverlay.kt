package at.hannibal2.skyhanni.features.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.ElectionAPI
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.Perk.Companion.toPerk
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

private val config get() = SkyHanniMod.feature.gui.mayorOverlay

enum class MayorOverlay(private val configLine: String, private val createLines: () -> Renderable) {
    MAYOR(
        "Mayor",
        {
            val currentMayor = ElectionAPI.currentMayor
            renderPerson(
                "Mayor",
                currentMayor?.mayorName,
                currentMayor?.activePerks,
            )
        },
    ),
    MINISTER(
        "Minister",
        {
            val currentMinister = ElectionAPI.currentMinister
            renderPerson(
                "Minister",
                currentMinister?.mayorName,
                currentMinister?.activePerks,
            )
        },
    ),
    CANDIDATES(
        "Candidates",
        {
            val candidates = ElectionAPI.rawMayorData?.current?.candidates.orEmpty()

            Renderable.verticalContainer(
                candidates.map { candidate ->
                    renderPerson(
                        "Candidate",
                        candidate.name,
                        candidate.perks.mapNotNull { it.toPerk() },
                    )
                },
                spacing = config.candidateSpacing,
            )
        },
    ),
    NEW_MAYOR(
        "New Mayor Time",
        {
            Renderable.string("§7New Mayor in: §e${ElectionAPI.nextMayorTimestamp.timeUntil().format(showMilliSeconds = false)}")
        },
    ),
    ;

    override fun toString() = configLine

    @SkyHanniModule
    companion object {
        var display: Renderable? = null

        @SubscribeEvent
        fun onSecondPassed(event: SecondPassedEvent) {
            if (!isEnabled()) return
            with(config) {
                display = mayorOverlay.map { it.createLines() }.let { Renderable.verticalContainer(it, spacing = spacing) }
            }
        }

        @SubscribeEvent
        fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
            if (!isEnabled()) return
            display?.let { config.position.renderRenderable(it, posLabel = "Mayor Overlay") }
        }

        private fun isEnabled() = (LorenzUtils.inSkyBlock || OutsideSbFeature.MAYOR_OVERLAY.isSelected()) && config.enabled
    }
}

private fun renderPerson(title: String, name: String?, perks: List<Perk>?): Renderable {
    val colorCode = ElectionAPI.mayorNameToColorCode(name.orEmpty())
    val perkLines = perks?.takeIf { config.showPerks }?.map { perk ->
        "${if (perk.minister) "§6✯ " else ""}§e${perk.perkName}" to "§7${perk.description}"
    }.orEmpty()

    return Renderable.verticalContainer(
        buildMap {
            name?.let { put("$colorCode$title $it", null) }
            putAll(perkLines)
        }.map { (key, value) ->
            value?.let {
                Renderable.hoverTips(
                    Renderable.string(key),
                    listOf(Renderable.wrappedString(it, 200)),
                )
            } ?: Renderable.string(key)
        },
    )
}
