package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.TrophyFishInfo
import at.hannibal2.skyhanni.data.jsonobjects.repo.TrophyFishJson
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.NeuProfileDataLoadedEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyFishMessages.getInternalName
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle

@SkyHanniModule
object TrophyFishManager {
    private val config get() = SkyHanniMod.feature.fishing.trophyFishing

    private val patternGroup = RepoPattern.group("fishing.trophyfish")

    /**
     * REGEX-TEST: §6Gold §a✔§7 (1)
     */
    private val odgerRankPattern by patternGroup.pattern(
        "odger.rank",
        "§.(?<rarity>.*) §a✔§7 \\((?<amount>.*)\\)",
    )

    /**
     * REGEX-TEST: §bDiamond §c✖
     */
    private val odgerRankEmptyPattern by patternGroup.pattern(
        "odger.rank.empty",
        "§.(?<rarity>.*) §c✖",
    )

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<TrophyFishJson>("TrophyFish")
        trophyFishInfo = data.trophyFish
    }

    val fish: MutableMap<String, MutableMap<TrophyRarity, Int>>?
        get() = ProfileStorageData.profileSpecific?.crimsonIsle?.trophyFishes

    private var loadedNeu = false

    @HandleEvent
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
                "Click here to load Trophy Fishing data from NEU PV!",
                onClick = {
                    updateFromNeuPv(savedFishes, neuData)
                },
                "§eClick to load!",
                oneTimeClick = true,
            )
        }
    }

    // Fetch when talking with Odger
    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Trophy Fishing") return

        val savedFishes = fish ?: return
        var updatedFishes = 0
        for (stack in event.inventoryItems.values) {
            val internalName = getInternalName(stack.name.replace("§k", ""))

            fun getRarity(rawRarity: String, line: String): TrophyRarity =
                TrophyRarity.getByName(rawRarity) ?: ErrorManager.skyHanniError(
                    "unknown trophy fish rarity in odger inventory",
                    "rawRarity" to rawRarity,
                    "line" to line,
                    "stack.name" to stack.name,
                    "internalName" to internalName,
                )

            var updated = false
            for (line in stack.getLore()) {
                val (rarity, amount) = odgerRankPattern.matchMatcher(line) {
                    val rarity = getRarity(group("rarity"), line)
                    val amount = group("amount").formatInt()
                    rarity to amount
                } ?: odgerRankEmptyPattern.matchMatcher(line) {
                    val rarity = getRarity(group("rarity"), line)
                    rarity to 0
                } ?: continue

                val stored = savedFishes[internalName]?.get(rarity) ?: -1
                if (amount != stored) {
                    updated = true
                    savedFishes.getOrPut(internalName) { mutableMapOf() }[rarity] = amount
                }
            }
            if (updated) {
                updatedFishes++
            }
        }

        if (updatedFishes > 0) {
            ChatUtils.chat("Updated $updatedFishes Trophy Fishes from Odger.")
            TrophyFishDisplay.update()
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
        val display = TrophyFishApi.hoverInfo(internalName) ?: return null
        return ChatStyle().setChatHoverEvent(
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(display)),
        )
    }
}
