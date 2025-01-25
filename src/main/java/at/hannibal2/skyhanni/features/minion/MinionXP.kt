package at.hannibal2.skyhanni.features.minion

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.jsonobjects.repo.MinionXPJson
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.MinionCloseEvent
import at.hannibal2.skyhanni.events.MinionOpenEvent
import at.hannibal2.skyhanni.events.MinionStorageOpenEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.enumMapOf
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.block.BlockChest
import net.minecraft.client.Minecraft
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.util.EnumMap

@SkyHanniModule
object MinionXP {

    private val config get() = SkyHanniMod.feature.misc.minions

    private val xpItemMap: MutableMap<PrimitiveItemStack, String> = mutableMapOf()
    private val collectItemXPList: MutableList<String> = mutableListOf()

    private var collectItem: Item? = null

    private val minionStorages = mutableListOf<MinionStorage>()

    private var xpInfoMap: Map<NeuInternalName, XPInfo> = hashMapOf()

    data class XPInfo(val type: SkillType, val amount: Double)

    private data class MinionStorage(val position: LorenzVec, val xpList: EnumMap<SkillType, Double>) {
        val timestamp: SimpleTimeMark = SimpleTimeMark.now()
    }

    private fun toPrimitiveItemStack(itemStack: ItemStack) =
        PrimitiveItemStack(itemStack.getInternalName(), itemStack.stackSize)

    @HandleEvent
    fun onMinionOpen(event: MinionOpenEvent) {
        if (!config.xpDisplay) return

        collectItem = event.inventoryItems[48]?.item
        collectItemXPList.clear()

        val xpTotal = handleItems(event.inventoryItems, true)

        val missesStorage = MinionFeatures.lastMinion?.let { minionPosition ->
            getStorageXPAndUpdateTotal(minionPosition, xpTotal)
        } ?: false

        collectItemXPList.addAll(xpTotal.map { (type, amount) -> collectMessage(type, amount) })
        if (missesStorage) {
            collectItemXPList.add("")
            collectItemXPList.add("§cError: No Minion Storage Data")
            collectItemXPList.add("§eOpen Storage to get Correct Value")
        }
        collectItemXPList.add("")
    }

    private fun getStorageXPAndUpdateTotal(
        minionPosition: LorenzVec,
        xpTotal: EnumMap<SkillType, Double>,
    ): Boolean {
        if (!getHasStorage(minionPosition)) return false
        val storage = minionStorages.firstOrNull {
            it.position.distanceSq(minionPosition) <= 2.5 && it.timestamp.passedSince().inWholeMinutes < 20
        }

        return if (storage != null) {
            for ((type, amount) in storage.xpList) {
                xpTotal.compute(type) { _, currentAmount -> (currentAmount ?: 0.0) + amount }
            }
            false
        } else {
            true
        }
    }

    // TODO find the correct name of the list
    private val listWithMissingName = listOf(21..26, 30..35, 39..44)

    private fun handleItems(inventoryItems: Map<Int, ItemStack>, isMinion: Boolean): EnumMap<SkillType, Double> {
        val xpTotal = enumMapOf<SkillType, Double>()
        val list = inventoryItems.filter {
            it.value.getLore().isNotEmpty() &&
                (!isMinion || it.key in listWithMissingName.flatten())
        }.values
            .map { toPrimitiveItemStack(it) }
        for (item in list) {
            val name = item.internalName
            val xp = xpInfoMap[name] ?: continue

            // TODO add wisdom and temporary skill exp (Events) to calculation
            val baseXP = xp.amount * item.amount
            val xpAmount = if (Perk.MOAR_SKILLZ.isActive) {
                baseXP * 1.5
            } else baseXP

            xpItemMap[item] = collectMessage(xp.type, xpAmount)
            xpTotal.compute(xp.type) { _, currentAmount -> (currentAmount ?: 0.0) + xpAmount }
        }
        return xpTotal
    }

    @HandleEvent
    fun onMinionStorageOpen(event: MinionStorageOpenEvent) {
        if (!config.xpDisplay) return

        val xpTotal = handleItems(event.inventoryItems, false)

        if (event.position == null) return
        minionStorages.removeIf { it.position == event.position }
        minionStorages.add(MinionStorage(event.position, xpTotal))
    }

    private fun collectMessage(type: SkillType, amount: Double) =
        "§7Collect to get: §b${amount.addSeparators()} §e${type.displayName} XP"

    private fun getHasStorage(minionPosition: LorenzVec): Boolean {
        val positionsToCheck = listOf(
            LorenzVec(1, 0, 0), LorenzVec(0, 0, 1),
            LorenzVec(-1, 0, 0), LorenzVec(0, 0, -1),
        )

        return positionsToCheck.any { position ->
            val pos = (minionPosition + position).toBlockPos()
            val block = Minecraft.getMinecraft().theWorld.getBlockState(pos).block
            block is BlockChest
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onToolTip(event: ToolTipEvent) {
        if (!config.xpDisplay) return
        when {
            MinionFeatures.minionInventoryOpen -> {
                addXPInfoToTooltip(event)
                if (collectItem == event.itemStack.item) {
                    collectItemXPList.forEachIndexed { i, item ->
                        event.toolTip.add(i + 1, item)
                    }
                }
            }

            MinionFeatures.minionStorageInventoryOpen -> {
                addXPInfoToTooltip(event)
            }
        }
    }

    private fun addXPInfoToTooltip(event: ToolTipEvent) {
        xpItemMap[toPrimitiveItemStack(event.itemStack)]?.let {
            event.toolTip.add("")
            event.toolTip.add(it)
        }
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        minionStorages.clear()
        xpItemMap.clear()
        collectItemXPList.clear()
    }

    @HandleEvent
    fun onMinionClose(event: MinionCloseEvent) {
        xpItemMap.clear()
        collectItemXPList.clear()
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        xpInfoMap = event.getConstant<MinionXPJson>("MinionXP").minionXP.mapNotNull { xpType ->
            xpType.value.mapNotNull { it.key.toInternalName() to XPInfo(SkillType.getByName(xpType.key), it.value) }
        }.flatten().toMap()
    }
}
