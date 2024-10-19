package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.currency.CurrencyChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats
import at.hannibal2.skyhanni.features.garden.CropType.Companion.getCropType
import at.hannibal2.skyhanni.features.garden.composter.ComposterOverlay
import at.hannibal2.skyhanni.features.garden.contest.FarmingContestAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenBestCropTime
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed
import at.hannibal2.skyhanni.features.garden.fortuneguide.FFGuideGUI
import at.hannibal2.skyhanni.features.garden.fortuneguide.FarmingItems
import at.hannibal2.skyhanni.features.garden.inventory.SkyMartCopperPrice
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateShopPrice
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.isBabyCrop
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.addItemIcon
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getCultivatingCounter
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHoeCounter
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenAPI {

    var toolInHand: String? = null
    var itemInHand: ItemStack? = null
    var cropInHand: CropType? = null
    val mushroomCowPet
        get() = PetAPI.isCurrentPet("Mooshroom Cow") &&
            storage?.fortune?.farmingItems?.get(FarmingItems.MOOSHROOM_COW)
                ?.let { it.getItemRarityOrNull()?.isAtLeast(LorenzRarity.RARE) } ?: false
    private var inBarn = false
    val onBarnPlot get() = inBarn && inGarden()
    val storage get() = ProfileStorageData.profileSpecific?.garden
    val config get() = SkyHanniMod.feature.garden
    var totalAmountVisitorsExisting = 0
    var gardenExp: Long?
        get() = storage?.experience
        set(value) {
            value?.let {
                storage?.experience = it
            }
        }

    var copper: Long = 0
        private set

    private val patternGroup = RepoPattern.group("garden")

    val copperPattern by patternGroup.pattern(
        "copper",
        "(?:§.)*Copper: (?:§.)*(?<copper>[\\d,]+).*",
    )

    private val barnArea = AxisAlignedBB(35.5, 70.0, -4.5, -32.5, 100.0, -46.5)

    // TODO USE SH-REPO
    private val otherToolsList = listOf(
        "DAEDALUS_AXE",
        "STARRED_DAEDALUS_AXE",
        "BASIC_GARDENING_HOE",
        "ADVANCED_GARDENING_AXE",
        "BASIC_GARDENING_AXE",
        "ADVANCED_GARDENING_HOE",
        "ROOKIE_HOE",
        "BINGHOE",
    )

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onSendPacket(event: PacketSentEvent) {
        if (event.packet !is C09PacketHeldItemChange) return
        checkItemInHand()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!inGarden()) return
        checkItemInHand()
        DelayedRun.runDelayed(500.milliseconds) {
            if (inGarden()) {
                checkItemInHand()
            }
        }
    }

    @SubscribeEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (!inGarden()) return
        copperPattern.firstMatcher(event.added) {
            val newCopper = group("copper").formatLong()
            val difference = (newCopper - copper).toInt()
            if (difference == 0) return
            copper = newCopper
            CurrencyChangeEvent.Copper(difference, copper).post()
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!inGarden()) return
        if (event.isMod(10, 1)) {
            inBarn = barnArea.isPlayerInside()

            // We ignore random hypixel moments
            Minecraft.getMinecraft().currentScreen ?: return
            checkItemInHand()
        }
    }

    // TODO use IslandChangeEvent
    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        DelayedRun.runDelayed(2.seconds) {
            if (inGarden()) {
                checkItemInHand()
            }
        }
    }

    private fun updateGardenTool() {
        GardenToolChangeEvent(cropInHand, itemInHand).postAndCatch()
    }

    private fun checkItemInHand() {
        val toolItem = InventoryUtils.getItemInHand()
        val crop = toolItem?.getCropType()
        val newTool = getToolInHand(toolItem, crop)
        if (toolInHand != newTool) {
            toolInHand = newTool
            cropInHand = crop
            itemInHand = toolItem
            updateGardenTool()
        }
    }

    private fun getToolInHand(toolItem: ItemStack?, crop: CropType?): String? {
        if (crop != null) return crop.cropName

        val internalName = toolItem?.getInternalName() ?: return null
        return if (isOtherTool(internalName)) internalName.asString() else null
    }

    private fun isOtherTool(internalName: NEUInternalName): Boolean {
        return internalName.asString() in otherToolsList
    }

    fun inGarden() = IslandType.GARDEN.isInIsland()

    fun isCurrentlyFarming() = inGarden() && GardenCropSpeed.averageBlocksPerSecond > 0.0 && hasFarmingToolInHand()

    fun hasFarmingToolInHand() = InventoryUtils.getItemInHand()?.let {
        val crop = it.getCropType()
        getToolInHand(it, crop) != null
    } ?: false

    fun ItemStack.getCropType(): CropType? {
        val internalName = getInternalName()
        return CropType.entries.firstOrNull { internalName.startsWith(it.toolName) }
    }

    fun readCounter(itemStack: ItemStack): Long = itemStack.getHoeCounter() ?: itemStack.getCultivatingCounter() ?: -1L

    @Deprecated("use renderable list instead", ReplaceWith(""))
    fun MutableList<Any>.addCropIcon(
        crop: CropType,
        scale: Double = NEUItems.itemFontSize,
        highlight: Boolean = false,
    ) =
        addItemIcon(crop.icon.copy(), highlight, scale = scale)

    // TODO rename to addCropIcon
    fun MutableList<Renderable>.addCropIconRenderable(
        crop: CropType,
        scale: Double = NEUItems.itemFontSize,
        highlight: Boolean = false,
    ) {
        addItemStack(crop.icon.copy(), highlight = highlight, scale = scale)
    }

    fun hideExtraGuis() = ComposterOverlay.inInventory ||
        AnitaMedalProfit.inInventory ||
        SkyMartCopperPrice.inInventory ||
        FarmingContestAPI.inInventory ||
        VisitorAPI.inInventory ||
        FFGuideGUI.isInGui() ||
        ChocolateShopPrice.inInventory ||
        ChocolateFactoryAPI.inChocolateFactory ||
        ChocolateFactoryAPI.chocolateFactoryPaused ||
        HoppityCollectionStats.inInventory

    fun resetCropSpeed() {
        storage?.cropsPerSecond?.clear()
        GardenBestCropTime.reset()
        updateGardenTool()
        ChatUtils.chat("Manually reset all crop speed data!")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        GardenBestCropTime.reset()
    }

    fun getCurrentlyFarmedCrop(): CropType? {
        val brokenCrop = if (toolInHand != null) GardenCropSpeed.lastBrokenCrop else null
        return cropInHand ?: brokenCrop
    }

    private var lastLocation: LorenzVec? = null

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onBlockClick(event: BlockClickEvent) {
        val blockState = event.getBlockState
        val cropBroken = blockState.getCropType() ?: return
        if (cropBroken.multiplier == 1 && blockState.isBabyCrop()) return

        val position = event.position
        if (lastLocation == position) {
            return
        }

        lastLocation = position
        CropClickEvent(position, cropBroken, blockState, event.clickType, event.itemInHand).postAndCatch()
    }

    fun getExpForLevel(requestedLevel: Int): Long {
        var totalExp = 0L
        var tier = 0
        for (tierExp in gardenExperience) {
            totalExp += tierExp
            tier++
            if (tier == requestedLevel) {
                return totalExp
            }
        }

        while (tier < requestedLevel) {
            totalExp += gardenOverflowExp
            tier++
            if (tier == requestedLevel) {
                return totalExp
            }
        }
        return 0
    }

    fun getGardenLevel(overflow: Boolean = true): Int {
        val gardenExp = this.gardenExp ?: return 0
        var tier = 0
        var totalExp = 0L
        for (tierExp in gardenExperience) {
            totalExp += tierExp
            if (totalExp > gardenExp) {
                return tier
            }
            tier++
        }
        if (overflow) {
            totalExp += gardenOverflowExp

            while (totalExp < gardenExp) {
                tier++
                totalExp += gardenOverflowExp
            }
        }
        return tier
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<GardenJson>("Garden")
        gardenExperience = data.gardenExp
        totalAmountVisitorsExisting = data.visitors.size
    }

    private var gardenExperience = listOf<Int>()
    private const val gardenOverflowExp = 10000
}
