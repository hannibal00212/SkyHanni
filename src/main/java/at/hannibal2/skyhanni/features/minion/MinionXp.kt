package at.hannibal2.skyhanni.features.minion

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.jsonobjects.repo.MinionXPJson
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.MinionCloseEvent
import at.hannibal2.skyhanni.events.MinionOpenEvent
import at.hannibal2.skyhanni.events.MinionStorageOpenEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.block.BlockChest
import net.minecraft.client.Minecraft
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.EnumMap

@SkyHanniModule
object MinionXp {

    private val config get() = SkyHanniMod.feature.misc.minions

    private val xpItemMap: MutableMap<PrimitiveItemStack, String> = mutableMapOf()
    private val collectItemXpList: MutableList<String> = mutableListOf()

    private var collectItem: Item? = null

    private val minionStorages = mutableListOf<MinionStorage>()

    private var xpInfoMap: Map<NEUInternalName, XpInfo> = hashMapOf()

    data class XpInfo(val type: SkillType, val amount: Double)

    private data class MinionStorage(val position: LorenzVec, val xpList: EnumMap<SkillType, Double>) {
        val timestamp: SimpleTimeMark = SimpleTimeMark.now()
    }

    private fun toPrimitiveItemStack(itemStack: ItemStack) =
        PrimitiveItemStack(itemStack.getInternalName(), itemStack.stackSize)

    @SubscribeEvent
    fun onMinionOpen(event: MinionOpenEvent) {
        if (!config.xpDisplay) return

        collectItem = event.inventoryItems[48]?.item
        collectItemXpList.clear()

        val xpTotal = handleItems(event.inventoryItems, true)

        val missesStorage = MinionFeatures.lastMinion?.let { minionPosition ->
            getStorageXpAndUpdateTotal(minionPosition, xpTotal)
        } ?: false

        collectItemXpList.addAll(xpTotal.map { (type, amount) -> collectMessage(type, amount) })
        if (missesStorage) {
            collectItemXpList.add("")
            collectItemXpList.add("§cError: No Minion Storage Data")
            collectItemXpList.add("§eOpen Storage to get Correct Value")
        }
        collectItemXpList.add("")
    }

    private fun getStorageXpAndUpdateTotal(
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
        val xpTotal = EnumMap<SkillType, Double>(SkillType::class.java)
        val list = inventoryItems.filter {
            it.value.getLore().isNotEmpty() &&
                (!isMinion || it.key in listWithMissingName.flatten())
        }.values
            .map { toPrimitiveItemStack(it) }
        for (item in list) {
            val name = item.internalName
            val xp = xpInfoMap[name] ?: continue

            // TODO add wisdom and temporary skill exp (Events) to calculation
            val baseXp = xp.amount * item.amount
            val xpAmount = if (Perk.MOAR_SKILLZ.isActive) {
                baseXp * 1.5
            } else baseXp

            xpItemMap[item] = collectMessage(xp.type, xpAmount)
            xpTotal.compute(xp.type) { _, currentAmount -> (currentAmount ?: 0.0) + xpAmount }
        }
        return xpTotal
    }

    @SubscribeEvent
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
            LorenzVec(-1, 0, 0), LorenzVec(0, 0, -1)
        )

        return positionsToCheck.any { position ->
            val pos = (minionPosition + position).toBlockPos()
            val block = Minecraft.getMinecraft().theWorld.getBlockState(pos).block
            block is BlockChest
        }
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.xpDisplay) return
        when {
            MinionFeatures.minionInventoryOpen -> {
                addXpInfoToTooltip(event)
                if (collectItem == event.itemStack.item) {
                    collectItemXpList.forEachIndexed { i, item ->
                        event.toolTip.add(i + 1, item)
                    }
                }
            }

            MinionFeatures.minionStorageInventoryOpen -> {
                addXpInfoToTooltip(event)
            }
        }
    }

    private fun addXpInfoToTooltip(event: LorenzToolTipEvent) {
        xpItemMap[toPrimitiveItemStack(event.itemStack)]?.let {
            event.toolTip.add("")
            event.toolTip.add(it)
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        minionStorages.clear()
        xpItemMap.clear()
        collectItemXpList.clear()
    }

    @SubscribeEvent
    fun onMinionClose(event: MinionCloseEvent) {
        xpItemMap.clear()
        collectItemXpList.clear()
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        xpInfoMap = event.getConstant<MinionXPJson>("MinionXP").minionXp.mapNotNull { xpType ->
            xpType.value.mapNotNull { it.key.toInternalName() to XpInfo(SkillType.getByName(xpType.key), it.value) }
        }.flatten().toMap()
    }
}
