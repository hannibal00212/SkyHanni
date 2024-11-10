package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.inventory.chocolatefactory.ChocolateFactoryRabbitWarningConfig.StrayTypeEntry
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityTextureHandler
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.specialRabbitTextures
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryDataLoader.clickMeGoldenRabbitPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryDataLoader.clickMeRabbitPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColorInt
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.sin

@SkyHanniModule
object ChocolateFactoryScreenFlash {

    private val config get() = ChocolateFactoryAPI.config
    private var flashScreen = false

    @SubscribeEvent
    fun onTick(event: SecondPassedEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        flashScreen = InventoryUtils.getItemsInOpenChest().any {
            when (config.rabbitWarning.flashScreenType) {
                StrayTypeEntry.SPECIAL -> isSpecial(it)

                StrayTypeEntry.LEGENDARY_P -> isRarityOrHigher(it, LorenzRarity.LEGENDARY)
                StrayTypeEntry.EPIC_P -> isRarityOrHigher(it, LorenzRarity.EPIC)
                StrayTypeEntry.RARE_P -> isRarityOrHigher(it, LorenzRarity.RARE)
                StrayTypeEntry.UNCOMMON_P -> isRarityOrHigher(it, LorenzRarity.UNCOMMON)

                StrayTypeEntry.ALL -> {
                    clickMeRabbitPattern.matches(it.stack.name) || isSpecial(it)
                }

                StrayTypeEntry.NONE -> false
            }
        }
    }

    @HandleEvent
    fun onRabbitFound(event: RabbitFoundEvent) {
        if (event.eggType != HoppityEggType.STRAY) return
        flashScreen = false
    }

    fun isRarityOrHigher(stack: ItemStack, rarity: LorenzRarity) =
        stack.getSkullTexture()?.let { skullTexture ->
            HoppityTextureHandler.getRarityBySkullId(skullTexture)?.let { skullRarity ->
                skullRarity.ordinal >= rarity.ordinal
            } ?: false
        } ?: false
    private fun isRarityOrHigher(slot: Slot, rarity: LorenzRarity) =
        slot.stack?.let { isRarityOrHigher(it, rarity) } ?: false


    fun isSpecial(stack: ItemStack) =
        clickMeGoldenRabbitPattern.matches(stack.name) || stack.getSkullTexture() in specialRabbitTextures
    private fun isSpecial(slot: Slot) =
        slot.stack?.let { isSpecial(it) } ?: false

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        if (!flashScreen) return
        val minecraft = Minecraft.getMinecraft()
        val alpha = ((2 + sin(System.currentTimeMillis().toDouble() / 1000)) * 255 / 4).toInt().coerceIn(0..255)
        Gui.drawRect(
            0,
            0,
            minecraft.displayWidth,
            minecraft.displayHeight,
            (alpha shl 24) or (config.rabbitWarning.flashColor.toChromaColorInt() and 0xFFFFFF),
        )
        GlStateManager.color(1F, 1F, 1F, 1F)
    }
}
