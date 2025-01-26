package at.hannibal2.skyhanni.features.garden.fortuneguide.pages

import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.fortuneguide.FFStats
import at.hannibal2.skyhanni.features.garden.fortuneguide.FarmingItems
import at.hannibal2.skyhanni.features.garden.fortuneguide.FortuneStats
import at.hannibal2.skyhanni.utils.CollectionUtils.split
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.guide.GuideTablePage
import at.hannibal2.skyhanni.utils.renderables.Renderable

class CropPage(val crop0: () -> CropType, sizeX: Int, sizeY: Int, paddingX: Int = 15, paddingY: Int = 7) :
    GuideTablePage(
        sizeX, sizeY, paddingX, paddingY,
    ) {

    val crop get() = crop0()

    override fun onEnter() {
        val item = crop.farmingItem
        FFStats.getCropStats(crop, item.getItemOrNull())

        FarmingItems.resetClickState()
        val toolLines = toolLines().split().map { Renderable.verticalContainer(it, 2) }
        update(
            listOf(
                header(),
                listOf(
                    toolLines[0],
                    equipDisplay(),
                    toolLines[1],
                ),
            ),
            emptyList(),
        )
    }

    private fun header(): List<Renderable> = buildList {
        add(FortuneStats.BASE.getFarmingBar())
        add(FortuneStats.CROP_TOTAL.getFarmingBar(110))
        add(FortuneStats.CROP_UPGRADE.getFarmingBar())
    }

    private fun FortuneStats.getFarmingBar(
        width: Int = 90,
    ) = Renderable.clickable(
        GuiRenderUtils.getFarmingBar(label(crop), tooltip(crop), current, max, width),
        { onClick(crop) },
    )

    private fun toolLines(): List<Renderable> =
        FortuneStats.entries.filter { it.isActive() && it !in headers }.map { it.getFarmingBar() }

    private fun equipDisplay(): Renderable =
        Renderable.fixedSizeColumn(
            Renderable.verticalContainer(
                listOf(
                    crop.farmingItem.getDisplay(),
                    Renderable.horizontalContainer(
                        listOf(
                            Renderable.verticalContainer(FarmingItems.getArmorDisplay(), 2),
                            Renderable.verticalContainer(FarmingItems.getEquipmentDisplay(), 2),
                        ),
                        2,
                        horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
                    ),
                    Renderable.horizontalContainer(FarmingItems.getPetsDisplay(true), 2),
                ),
                2,
                verticalAlign = RenderUtils.VerticalAlignment.BOTTOM,
            ),
            sizeY - 36,
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
            verticalAlign = RenderUtils.VerticalAlignment.BOTTOM,
        )

    companion object {
        private val headers = setOf(FortuneStats.BASE, FortuneStats.CROP_TOTAL, FortuneStats.CROP_UPGRADE)
    }
}
