package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.fortuneguide.pages.CropPage
import at.hannibal2.skyhanni.features.garden.fortuneguide.pages.OverviewPage
import at.hannibal2.skyhanni.features.garden.fortuneguide.pages.UpgradePage
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.guide.GuideGUI
import at.hannibal2.skyhanni.utils.guide.GuideTab
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

class FFGuideGUI : GuideGUI<FFGuideGUI.FortuneGuidePage>(FortuneGuidePage.OVERVIEW) {

    override val sizeX = 360
    override val sizeY = 180

    companion object {

        /** Value for which crop page is active */
        var currentCrop: CropType? = null

        fun isInGui() = Minecraft.getMinecraft().currentScreen is FFGuideGUI

        private val fallbackItems = mutableMapOf<FarmingItems, ItemStack>()

        fun getFallbackItem(item: FarmingItems) = fallbackItems.getOrPut(item) {
            val name = "§cNo saved ${item.name.lowercase().replace("_", " ")}"
            ItemStack(Blocks.barrier).setStackDisplayName(name)
        }

        fun isFallbackItem(item: ItemStack) = item.name.startsWith("§cNo saved ")

        fun open() {
            CaptureFarmingGear.captureFarmingGear()
            SkyHanniMod.screenToOpen = FFGuideGUI()
        }

        fun updateDisplay() {
            with(Minecraft.getMinecraft().currentScreen) {
                if (this !is FFGuideGUI) return
                this.refreshPage()
            }
        }
    }

    init {
        FFStats.loadFFData()
        FortuneUpgrades.generateGenericUpgrades()

        FarmingItems.setDefaultPet()

        pageList = mapOf(
            FortuneGuidePage.OVERVIEW to OverviewPage(sizeX, sizeY),
            FortuneGuidePage.CROP to CropPage(sizeX, sizeY),
            FortuneGuidePage.UPGRADES to UpgradePage(sizeX, sizeY - 2),
        )
        verticalTabs = listOf(
            vTab(ItemStack(Items.gold_ingot), Renderable.string("§eBreakdown")) {
                currentPage = if (currentCrop == null) FortuneGuidePage.OVERVIEW else FortuneGuidePage.CROP
            },
            vTab(ItemStack(Items.map), Renderable.string("§eUpgrades")) {
                currentPage = FortuneGuidePage.UPGRADES
            })
        horizontalTabs = buildList {
            add(
                hTab(ItemStack(Blocks.grass), Renderable.string("§eOverview")) {
                    currentCrop = null

                    it.pageSwitchHorizontal()
                }
            )
            for (crop in CropType.entries) {
                add(
                    hTab(crop.icon, Renderable.string("§e${crop.cropName}")) {
                        currentCrop = crop

                        it.pageSwitchHorizontal()
                    }
                )
            }
        }
        horizontalTabs.firstOrNull()?.fakeClick()
        verticalTabs.firstOrNull()?.fakeClick()

    }

    private fun GuideTab.pageSwitchHorizontal() {
        if (isSelected()) {
            verticalTabs.first { it != lastVerticalTabWrapper.tab }.fakeClick() // Double Click Logic
        } else {
            lastVerticalTabWrapper.tab?.fakeClick() // First Click Logic
        }
    }

    enum class FortuneGuidePage {
        OVERVIEW,
        CROP,
        UPGRADES,
    }
}
