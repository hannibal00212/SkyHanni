package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.inventory.chocolatefactory.ChocolateFactoryRabbitWarningConfig.StrayTypeEntry
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityTextureHandler
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.caughtRabbitPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.specialRabbitTextures
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryDataLoader.clickMeGoldenRabbitPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryDataLoader.clickMeRabbitPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getSingleLineLore
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColorInt
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.sin

@SkyHanniModule
object ChocolateFactoryStrayWarning {

    private val config get() = ChocolateFactoryAPI.config
    private val warningConfig get() = config.rabbitWarning

    private var flashScreen = false
    private var clickableStraySlots: Set<Int> = setOf()

    private fun isRarityOrHigher(stack: ItemStack, rarity: LorenzRarity) =
        stack.getSkullTexture()?.let { skullTexture ->
            HoppityTextureHandler.getRarityBySkullId(skullTexture)?.let { skullRarity ->
                skullRarity.ordinal >= rarity.ordinal
            } ?: false
        } ?: false

    private fun isSpecial(stack: ItemStack) =
        clickMeGoldenRabbitPattern.matches(stack.name) || stack.getSkullTexture() in specialRabbitTextures

    private fun shouldWarnAboutStray(item: ItemStack) = when (config.rabbitWarning.rabbitWarningLevel) {
        StrayTypeEntry.SPECIAL -> isSpecial(item)

        StrayTypeEntry.LEGENDARY_P -> isRarityOrHigher(item, LorenzRarity.LEGENDARY)
        StrayTypeEntry.EPIC_P -> isRarityOrHigher(item, LorenzRarity.EPIC)
        StrayTypeEntry.RARE_P -> isRarityOrHigher(item, LorenzRarity.RARE)
        StrayTypeEntry.UNCOMMON_P -> isRarityOrHigher(item, LorenzRarity.UNCOMMON)

        StrayTypeEntry.ALL -> clickMeRabbitPattern.matches(item.name) || isSpecial(item)

        StrayTypeEntry.NONE -> false
        else -> false
    }

    private fun handleRabbitWarnings(item: ItemStack) {
        if (caughtRabbitPattern.matches(item.getSingleLineLore())) return
        val isGoldenRabbit = clickMeGoldenRabbitPattern.matches(item.name)

        if (!clickMeRabbitPattern.matches(item.name) && !isGoldenRabbit) return
        if (shouldWarnAboutStray(item)) {
            if (isGoldenRabbit || item.getSkullTexture() in specialRabbitTextures) {
                SoundUtils.repeatSound(100, warningConfig.repeatSound, ChocolateFactoryAPI.warningSound)
            } else SoundUtils.playBeepSound()
        }
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        InventoryUtils.getItemsInOpenChest().filter {
            it.slotNumber in clickableStraySlots
        }.forEach {
            it highlight warningConfig.inventoryHighlightColor.toSpecialColor()
        }
    }

    @SubscribeEvent
    fun onInventoryUpdate(event: InventoryUpdatedEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) {
            flashScreen = false
            return
        }
        val activeStrays = event.inventoryItems.filter {
            it.value.hasDisplayName() && it.value.displayName.isNotEmpty()
                && !caughtRabbitPattern.matches(it.value.getSingleLineLore())
        }
        clickableStraySlots = activeStrays.filter {
            clickMeRabbitPattern.matches(it.value.name) || clickMeGoldenRabbitPattern.matches(it.value.name)
        }.keys
        flashScreen = activeStrays.any {
            val stack = it.value
            when (config.rabbitWarning.flashScreenLevel) {
                StrayTypeEntry.SPECIAL -> isSpecial(stack)

                StrayTypeEntry.LEGENDARY_P -> isRarityOrHigher(stack, LorenzRarity.LEGENDARY)
                StrayTypeEntry.EPIC_P -> isRarityOrHigher(stack, LorenzRarity.EPIC)
                StrayTypeEntry.RARE_P -> isRarityOrHigher(stack, LorenzRarity.RARE)
                StrayTypeEntry.UNCOMMON_P -> isRarityOrHigher(stack, LorenzRarity.UNCOMMON)

                StrayTypeEntry.ALL -> {
                    clickMeRabbitPattern.matches(it.value.name) || isSpecial(stack)
                }

                StrayTypeEntry.NONE -> false
                else -> false
            }
        }
        activeStrays.forEach { handleRabbitWarnings(it.value) }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        flashScreen = false
    }

    @HandleEvent
    fun onRabbitFound(event: RabbitFoundEvent) {
        if (event.eggType != HoppityEggType.STRAY) return
        flashScreen = false
    }

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        if (!flashScreen) return
        val minecraft = Minecraft.getMinecraft()
        val alpha = ((2 + sin(System.currentTimeMillis().toDouble() / 1000)) * 255 / 4).toInt().coerceIn(0..255)
        val color = (alpha shl 24) or (config.rabbitWarning.flashColor.toSpecialColorInt() and 0xFFFFFF)
        Gui.drawRect(0, 0, minecraft.displayWidth, minecraft.displayHeight, color)
        GlStateManager.color(1F, 1F, 1F, 1F)
    }
}
