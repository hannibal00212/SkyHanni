package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.fortuneguide.pages.CropPage
import at.hannibal2.skyhanni.features.garden.fortuneguide.pages.OverviewPage
import at.hannibal2.skyhanni.features.garden.fortuneguide.pages.UpgradePage
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.guide.GuideGUI
import at.hannibal2.skyhanni.utils.guide.GuideTab
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

class FFGuideGUI : GuideGUI<FFGuideGUI.FortuneGuidePage>(FortuneGuidePage.OVERVIEW) {

    override val sizeX = 360
    override val sizeY = 200

    companion object {

        @JvmStatic
        fun onCommand() {
            if (!LorenzUtils.inSkyBlock) {
                ChatUtils.userError("Join SkyBlock to open the fortune guide!")
            } else {
                open()
            }
        }

        fun isInGui() = Minecraft.getMinecraft().currentScreen is FFGuideGUI

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

    /** Value for which crop page is active */
    private var currentCrop: CropType? = null

    init {
        FFStats.loadFFData()
        FortuneUpgrades.generateGenericUpgrades()

        FarmingItems.setDefaultPet()

        pageList = mapOf(
            FortuneGuidePage.OVERVIEW to OverviewPage(sizeX, sizeY),
            FortuneGuidePage.CROP to CropPage({ currentCrop!! }, sizeX, sizeY),
            FortuneGuidePage.UPGRADES to UpgradePage({ currentCrop }, sizeX, sizeY - 2),
        )
        verticalTabs = listOf(
            vTab(ItemStack(Items.gold_ingot), Renderable.string("§eBreakdown")) {
                currentPage = if (currentCrop == null) FortuneGuidePage.OVERVIEW else FortuneGuidePage.CROP
            },
            vTab(
                ItemStack(Items.map),
                Renderable.string("§eUpgrades")
            ) {
                currentPage = FortuneGuidePage.UPGRADES
            }
        )
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
