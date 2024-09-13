package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.TrophyFishInfo
import at.hannibal2.skyhanni.data.jsonobjects.repo.TrophyFishJson
import at.hannibal2.skyhanni.events.NeuProfileDataLoadedEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TrophyFishManager {
    private val config get() = SkyHanniMod.feature.fishing.trophyFishing

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<TrophyFishJson>("TrophyFish")
        trophyFishInfo = data.trophyFish
    }

    val fish: MutableMap<String, MutableMap<TrophyRarity, Int>>?
        get() = ProfileStorageData.profileSpecific?.crimsonIsle?.trophyFishes

    private var loadedNeu = false

    @SubscribeEvent
    fun onNeuProfileDataLoaded(event: NeuProfileDataLoadedEvent) {
        if (loadedNeu || !config.loadFromNeuPV) return

        val caughtTrophyFish = event.getCurrentPlayerData()?.trophyFish?.caught ?: return

        loadedNeu = true

        val savedFishes = fish ?: return
        var changed = false

        val neuData = mutableListOf<Triple<String, TrophyRarity, Int>>()
        for ((fishName, apiAmount) in caughtTrophyFish) {
            val rarity = TrophyRarity.getByName(fishName) ?: continue
            val name = fishName.split("_").dropLast(1).joinToString("")

            val savedFishData = savedFishes.getOrPut(name) { mutableMapOf() }

            val currentSavedAmount = savedFishData[rarity] ?: 0
            neuData.add(Triple(name, rarity, apiAmount))
            if (apiAmount > currentSavedAmount) {
                changed = true
            }
        }
        if (changed) {
            ChatUtils.clickableChat(
                "Click here to load Trophy Fishing data from NEU PV!", onClick = {
                    updateFromNeuPv(savedFishes, neuData)
                },
                "§eClick to load!",
                oneTimeClick = true
            )
        }
    }

    private fun updateFromNeuPv(
        savedFishes: Map<String, MutableMap<TrophyRarity, Int>>,
        neuData: List<Triple<String, TrophyRarity, Int>>,
    ) {
        for ((name, rarity, newValue) in neuData) {
            val saved = savedFishes[name] ?: continue

            val current = saved[rarity] ?: 0
            if (newValue > current) {
                saved[rarity] = newValue
                ChatUtils.debug("Updated trophy fishing data from NEU PV:  $name $rarity: $current -> $newValue")
            }
        }
        TrophyFishDisplay.update()
        ChatUtils.chat("Updated Trophy Fishing data via NEU PV!")
    }

    private var trophyFishInfo = mapOf<String, TrophyFishInfo>()

    fun getInfo(internalName: String): TrophyFishInfo? = trophyFishInfo[internalName]

    fun getInfoByName(name: String) = trophyFishInfo.values.find { it.displayName == name }

    fun TrophyFishInfo.getFilletValue(rarity: TrophyRarity): Int {
        return fillet.getOrDefault(rarity, -1)
    }

    fun getTooltip(internalName: String): ChatStyle? {
        val display = TrophyFishAPI.hoverInfo(internalName) ?: return null
        return ChatStyle().setChatHoverEvent(
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(display))
        )
    }
}
