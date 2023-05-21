package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.fortuneguide.pages.*
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SoundUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import org.lwjgl.input.Mouse
import java.io.IOException
import java.util.*

open class FFGuideGUI : GuiScreen() {
    companion object {
        val pages = mutableMapOf<FortuneGuidePages, FFGuidePage>()

        var guiLeft = 0
        var guiTop = 0
        var screenHeight = 0

        const val sizeX = 360
        const val sizeY = 180

        var selectedPage = FortuneGuidePages.OVERVIEW
        var breakdownMode = true
        var currentPet = 0
        var currentArmor = 0
        var currentEquipment = 0

        var mouseX = 0
        var mouseY = 0

        var tooltipToDisplay = mutableListOf<String>()
        val textLinesWithTooltip = mutableMapOf<Pair<String, String>, Pair<Int, Int>>()
        var items = mutableListOf<ItemStack>()

        fun isInGui() = Minecraft.getMinecraft().currentScreen is FFGuideGUI

        var farmingLevel = -1
        var plotsUnlocked = 0
        var communityUpgradeLevel = -1
        var cakeBuffTime = -1L
        var anitaBuff = -1
    }

    init {
        pages[FortuneGuidePages.OVERVIEW] = OverviewPage()
        pages[FortuneGuidePages.WHEAT] = WheatPage()
        pages[FortuneGuidePages.CARROT] = CarrotPage()
        pages[FortuneGuidePages.POTATO] = PotatoPage()
        pages[FortuneGuidePages.PUMPKIN] = PumpkinPage()
        pages[FortuneGuidePages.SUGAR_CANE] = CanePage()
        pages[FortuneGuidePages.MELON] = MelonPage()
        pages[FortuneGuidePages.CACTUS] = CactusPage()
        pages[FortuneGuidePages.COCOA_BEANS] = CocoaPage()
        pages[FortuneGuidePages.MUSHROOM] = MushroomPage()
        pages[FortuneGuidePages.NETHER_WART] = WartPage()

        farmingLevel = GardenAPI.config?.fortune?.farmingLevel ?: -1
        communityUpgradeLevel = SkyHanniMod.feature.storage.gardenCommunityUpgrade
        plotsUnlocked = GardenAPI.config?.fortune?.plotsUnlocked ?: -1
        anitaBuff = GardenAPI.config?.fortune?.anitaUpgrade ?: -1
        cakeBuffTime = GardenAPI.config?.fortune?.cakeExpiring ?: -1L

        for (item in GardenAPI.config?.fortune?.farmingItems!!) {
            items.add(NEUItems.loadNBTData(item))
        }
    }

    override fun drawScreen(unusedX: Int, unusedY: Int, partialTicks: Float) {
        super.drawScreen(unusedX, unusedY, partialTicks)
        drawDefaultBackground()
        screenHeight = height
        guiLeft = (width - sizeX) / 2
        guiTop = (height - sizeY) / 2

        mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth
        mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1

        GlStateManager.pushMatrix()
        drawRect(guiLeft, guiTop, guiLeft + sizeX, guiTop + sizeY, 0x50000000)
        renderTabs()

        //these buttons could be improved
        drawRect(guiLeft, guiTop + sizeY + 3, guiLeft + 40,
            guiTop + sizeY + 15, 0x50000000)
        RenderUtils.drawStringCentered("§6Mode:", guiLeft + 20, guiTop + sizeY + 9)

        drawRect(guiLeft + 45, guiTop + sizeY + 3, guiLeft + 125,
            guiTop + sizeY + 15, if (breakdownMode) 0x50555555 else 0x50000000)
        RenderUtils.drawStringCentered("§6Breakdown", guiLeft + 85, guiTop + sizeY + 9)

        drawRect(guiLeft + 130, guiTop + sizeY + 3, guiLeft + 210,
            guiTop + sizeY + 15, if (!breakdownMode) 0x50555555 else 0x50000000)
        RenderUtils.drawStringCentered("§6Improvements", guiLeft + 170, guiTop + sizeY + 9)

        if (breakdownMode) {
            if (selectedPage != FortuneGuidePages.OVERVIEW) {
                RenderUtils.renderItemAndTip(items[18], guiLeft + 152, guiTop + 160, mouseX, mouseY,
                    if (currentPet == 0) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[19], guiLeft + 172, guiTop + 160, mouseX, mouseY,
                    if (currentPet == 1) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[20], guiLeft + 192, guiTop + 160, mouseX, mouseY,
                    if (currentPet == 2) 0xFF00FF00.toInt() else 0xFF43464B.toInt())

                RenderUtils.renderItemAndTip(items[13], guiLeft + 162, guiTop + 80, mouseX, mouseY)
                RenderUtils.renderItemAndTip(items[12], guiLeft + 162, guiTop + 100, mouseX, mouseY)
                RenderUtils.renderItemAndTip(items[11], guiLeft + 162, guiTop + 120, mouseX, mouseY)
                RenderUtils.renderItemAndTip(items[10], guiLeft + 162, guiTop + 140, mouseX, mouseY)

                RenderUtils.renderItemAndTip(items[14], guiLeft + 182, guiTop + 80, mouseX, mouseY)
                RenderUtils.renderItemAndTip(items[15], guiLeft + 182, guiTop + 100, mouseX, mouseY)
                RenderUtils.renderItemAndTip(items[16], guiLeft + 182, guiTop + 120, mouseX, mouseY)
                RenderUtils.renderItemAndTip(items[17], guiLeft + 182, guiTop + 140, mouseX, mouseY)
            } else {
                RenderUtils.renderItemAndTip(items[13], guiLeft + 142, guiTop + 5, mouseX, mouseY,
                    if (currentArmor == 1) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[12], guiLeft + 162, guiTop + 5, mouseX, mouseY,
                    if (currentArmor == 2) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[11], guiLeft + 182, guiTop + 5, mouseX, mouseY,
                    if (currentArmor == 3) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[10], guiLeft + 202, guiTop + 5, mouseX, mouseY,
                    if (currentArmor == 4) 0xFF00FF00.toInt() else 0xFF43464B.toInt())

                RenderUtils.renderItemAndTip(items[14], guiLeft + 262, guiTop + 5, mouseX, mouseY,
                    if (currentEquipment == 1) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[15], guiLeft + 282, guiTop + 5, mouseX, mouseY,
                    if (currentEquipment == 2) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[16], guiLeft + 302, guiTop + 5, mouseX, mouseY,
                    if (currentEquipment == 3) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[17], guiLeft + 322, guiTop + 5, mouseX, mouseY,
                    if (currentEquipment == 4) 0xFF00FF00.toInt() else 0xFF43464B.toInt())

                RenderUtils.renderItemAndTip(items[18], guiLeft + 152, guiTop + 130, mouseX, mouseY,
                    if (currentPet == 0) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[19], guiLeft + 172, guiTop + 130, mouseX, mouseY,
                    if (currentPet == 1) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
                RenderUtils.renderItemAndTip(items[20], guiLeft + 192, guiTop + 130, mouseX, mouseY,
                    if (currentPet == 2) 0xFF00FF00.toInt() else 0xFF43464B.toInt())
            }
        }

        RenderUtils.drawStringCentered("§7SkyHanni", guiLeft + 334, guiTop + sizeY + 9)

        pages[selectedPage]?.drawPage(mouseX, mouseY, partialTicks)

        renderText(tooltipToDisplay)

        GlStateManager.popMatrix()

        if (tooltipToDisplay.isNotEmpty()) {
            RenderUtils.drawTooltip(tooltipToDisplay, mouseX, mouseY, height)
            tooltipToDisplay.clear()
        }
    }

    fun renderText(output: MutableList<String>, scale: Float = .7f) {
        for (line in textLinesWithTooltip) {
            val inverse = 1 /scale
            val str = line.key.first
            val tooltip = line.key.second
            val x = line.value.first
            val y = line.value.second

            val textWidth: Int = Minecraft.getMinecraft().fontRendererObj.getStringWidth(str) + 6
            val textHeight = 14
            GlStateManager.scale(scale, scale, scale)
            RenderUtils.drawString(str, (x + 3) * inverse, (y + 2) * inverse)
            GlStateManager.scale(inverse , inverse, inverse)
            if (tooltip == "") continue
            if (RenderUtils.isPointInRect(mouseX, mouseY, x, y, (textWidth * scale).toInt(), textHeight)) {
                val split = tooltip.split("\n")
                for (tooltipLine in split) {
//                    tooltipToDisplay.add(tooltipLine)
                    output.add(tooltipLine)
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun mouseClicked(originalX: Int, originalY: Int, mouseButton: Int) {
        super.mouseClicked(originalX, originalY, mouseButton)

        for (page in FortuneGuidePages.values()) {
            val x = guiLeft + (page.ordinal) * 30 + 15
            val y = guiTop - 28

            if (RenderUtils.isPointInRect(mouseX, mouseY, x, y, 25, 28)) {
                if (selectedPage != page) {
                    SoundUtils.playClickSound()
                    swapMode()
                    selectedPage = page
                    swapMode()
                }
            }
        }
        if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 45, guiTop + sizeY, 80, 15) && !breakdownMode) {
            SoundUtils.playClickSound()
            breakdownMode = true
            swapMode()
        }
        if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 130, guiTop + sizeY, 80, 15) && breakdownMode) {
            SoundUtils.playClickSound()
            breakdownMode = false
            swapMode()
        }
        if (selectedPage == FortuneGuidePages.OVERVIEW) {
            if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 152, guiTop + 130, 16, 16) && currentPet != 0) {
                SoundUtils.playClickSound()
                currentPet = 0
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 172, guiTop + 130, 16, 16) && currentPet != 1) {
                SoundUtils.playClickSound()
                currentPet = 1
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 192, guiTop + 130, 16, 16) && currentPet != 2) {
                SoundUtils.playClickSound()
                currentPet = 2
            }

            else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 142, guiTop + 5, 16, 16)) {
                SoundUtils.playClickSound()
                currentArmor = if (currentArmor == 1) 0 else 1
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 162, guiTop + 5, 16, 16)) {
                SoundUtils.playClickSound()
                currentArmor = if (currentArmor == 2) 0 else 2
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 182, guiTop + 5, 16, 16)) {
                SoundUtils.playClickSound()
                currentArmor = if (currentArmor == 3) 0 else 3
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 202, guiTop + 5, 16, 16)) {
            SoundUtils.playClickSound()
                currentArmor = if (currentArmor == 4) 0 else 4
            }

            else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 262, guiTop + 5, 16, 16)) {
                SoundUtils.playClickSound()
                currentEquipment = if (currentEquipment == 1) 0 else 1
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 282, guiTop + 5, 16, 16)) {
                SoundUtils.playClickSound()
                currentEquipment = if (currentEquipment == 2) 0 else 2
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 302, guiTop + 5, 16, 16)) {
                SoundUtils.playClickSound()
                currentEquipment = if (currentEquipment == 3) 0 else 3
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 322, guiTop + 5, 16, 16)) {
                SoundUtils.playClickSound()
                currentEquipment = if (currentEquipment == 4) 0 else 4
            }

        } else {
            if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 152, guiTop + 160, 16, 16) && currentPet != 0) {
                SoundUtils.playClickSound()
                currentPet = 0
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 172, guiTop + 160, 16, 16) && currentPet != 1) {
                SoundUtils.playClickSound()
                currentPet = 1
            } else if (RenderUtils.isPointInRect(mouseX, mouseY, guiLeft + 192, guiTop + 160, 16, 16) && currentPet != 2) {
                SoundUtils.playClickSound()
                currentPet = 2
            }
        }
    }

    private fun renderTabs() {
        for (page in FortuneGuidePages.values()) {
            val x = guiLeft + (page.ordinal) * 30 + 15
            val y = guiTop - 28
            drawRect(x, y, x + 25, y + 28, if (page == selectedPage) 0x50555555 else 0x50000000)

            if (page.crop != null) {
                RenderUtils.renderItemStack(page.crop.icon, x + 5, y + 5)
            } else RenderUtils.renderItemStack(ItemStack(Blocks.grass), x + 5, y + 5)

            if (RenderUtils.isPointInRect(mouseX, mouseY, x, y, 25, 25)) {
                tooltipToDisplay.add(page.pageName)
            }
        }
    }

    fun swapMode() {
        textLinesWithTooltip.clear()
    }

    enum class FortuneGuidePages(val pageName: String, val crop: CropType?) {
        OVERVIEW("§eOverview", null),
        WHEAT("§eWheat", CropType.WHEAT),
        CARROT("§eCarrot", CropType.CARROT),
        POTATO("§ePotato", CropType.POTATO),
        NETHER_WART("§eNether Wart", CropType.NETHER_WART),
        PUMPKIN("§ePumpkin", CropType.PUMPKIN),
        MELON("§eMelon", CropType.MELON),
        COCOA_BEANS("§eCocoa Beans", CropType.COCOA_BEANS),
        SUGAR_CANE("§eSugar Cane", CropType.SUGAR_CANE),
        CACTUS("§eCactus", CropType.CACTUS),
        MUSHROOM("§eMushroom", CropType.MUSHROOM),
    }

    abstract class FFGuidePage {
        abstract fun drawPage(mouseX: Int, mouseY: Int, partialTicks: Float)
    }
}

