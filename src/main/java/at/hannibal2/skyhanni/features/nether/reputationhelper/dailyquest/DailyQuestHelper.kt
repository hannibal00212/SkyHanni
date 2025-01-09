package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.SackAPI.getAmountInSacksOrNull
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraTier
import at.hannibal2.skyhanni.features.nether.reputationhelper.CrimsonIsleReputationHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.FactionType
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.DojoQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.FetchQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.KuudraQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.MiniBossQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.ProgressQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.Quest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.QuestCategory
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.QuestState
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.RescueMissionQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.TrophyFishQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.UnknownQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss.CrimsonMiniBoss
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeWordsAtEnd
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class DailyQuestHelper(val reputationHelper: CrimsonIsleReputationHelper) {

    private val townBoardMage = LorenzVec(-138, 92, -755)
    private val townBoardBarbarian = LorenzVec(-572, 100, -687)

    private val questLoader = QuestLoader(this)
    val quests = mutableListOf<Quest>()
    var greatSpook = false

    val patternGroup = RepoPattern.group("crimson.reputationhelper.quest")

    /**
     * REGEX-TEST: §7Kill the §cAshfang §7miniboss §a2 §7times!
     * REGEX-TEST: §7Kill the §cMage Outlaw §7miniboss §a1 §7time!
     * REGEX-TEST: §7miniboss §a1 §7time!
     * REGEX-TEST: §7Kill the §cBarbarian Duke X §7miniboss §a2
     */
    val minibossAmountPattern by patternGroup.pattern(
        "minibossamount",
        "(?:§7Kill the §c.+ §7|.*)miniboss §a(?<amount>\\d)(?: §7times?!)?",
    )

    /**
     * REGEX-TEST: §a§lCOMPLETE
     */
    val completedPattern by patternGroup.pattern(
        "complete",
        "(?:§.)*COMPLETE",
    )


    private val config get() = SkyHanniMod.feature.crimsonIsle.reputationHelper

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return

        questLoader.checkInventory(event)
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.enabled) {
            if (IslandType.CRIMSON_ISLE.isInIsland()) {
                questLoader.loadFromTabList()
            }
        }
    }

    @HandleEvent
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.FACTION_QUESTS)) return
        if (!isEnabled()) return

        questLoader.loadFromTabList()
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (event.repeatSeconds(3)) {
            checkInventoryForFetchItem()
        }
    }

    fun update() {
        reputationHelper.update()
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return

        if (event.gui !is GuiChest) return
        val chest = event.gui.inventorySlots as ContainerChest
        val chestName = chest.getInventoryName()

        if (chestName == "Challenges") {
            if (LorenzUtils.skyBlockArea != "Dojo") return
            val dojoQuest = getQuest<DojoQuest>() ?: return
            if (dojoQuest.state != QuestState.ACCEPTED) return

            for ((slot, stack) in chest.getUpperItems()) {
                if (stack.name.contains(dojoQuest.dojoName)) {
                    slot highlight LorenzColor.AQUA
                }
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        val message = event.message
        if (message == "§aYou completed your Dojo quest! Visit the Town Board to claim the rewards.") {
            val dojoQuest = getQuest<DojoQuest>() ?: return
            dojoQuest.state = QuestState.READY_TO_COLLECT
            update()
        }
        if (message == "§aYou completed your rescue quest! Visit the Town Board to claim the rewards,") {
            val rescueMissionQuest = getQuest<RescueMissionQuest>() ?: return
            rescueMissionQuest.state = QuestState.READY_TO_COLLECT
            update()
        }

        if (message.contains("§6§lTROPHY FISH! §r§bYou caught a")) {
            val fishQuest = getQuest<TrophyFishQuest>() ?: return
            if (fishQuest.state != QuestState.ACCEPTED && fishQuest.state != QuestState.READY_TO_COLLECT) return
            val fishName = fishQuest.fishName

            if (message.contains(fishName)) {
                updateProcessQuest(fishQuest, fishQuest.haveAmount + 1)
            }
        }
    }

    inline fun <reified T : Quest> getQuest() = quests.filterIsInstance<T>().firstOrNull()

    private fun checkInventoryForFetchItem() {
        val fetchQuest = getQuest<FetchQuest>() ?: return
        if (fetchQuest.state != QuestState.ACCEPTED && fetchQuest.state != QuestState.READY_TO_COLLECT) return

        val itemName = fetchQuest.itemName

        val count = InventoryUtils.countItemsInLowerInventory { it.name.contains(itemName) }
        updateProcessQuest(fetchQuest, count)
    }

    private fun updateProcessQuest(quest: ProgressQuest, newAmount: Int) {
        var count = newAmount
        val needAmount = quest.needAmount
        if (count > needAmount) {
            count = needAmount
        }
        if (quest.haveAmount == count) return
        ChatUtils.chat("${quest.displayName} progress: $count/$needAmount")

        quest.haveAmount = count
        quest.state = if (count == needAmount) QuestState.READY_TO_COLLECT else QuestState.ACCEPTED
        update()
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (!reputationHelper.showLocations()) return

        for (quest in quests) {
            if (quest is MiniBossQuest) continue
            if (quest.state != QuestState.ACCEPTED) continue
            val location = quest.location ?: continue

            event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
            event.drawDynamicText(location, quest.displayName, 1.5)
        }

        renderTownBoard(event)
    }

    private fun renderTownBoard(event: LorenzRenderWorldEvent) {
        if (!quests.any { it.needsTownBoardLocation() }) return
        val location = when (reputationHelper.factionType) {
            FactionType.BARBARIAN -> townBoardBarbarian
            FactionType.MAGE -> townBoardMage

            FactionType.NONE -> return
        }
        event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
        event.drawDynamicText(location, "Town Board", 1.5)
    }

    private fun Quest.needsTownBoardLocation(): Boolean = state.let { state ->
        state == QuestState.READY_TO_COLLECT || (this is RescueMissionQuest && state == QuestState.ACCEPTED)
    }

    fun render(display: MutableList<List<Any>>) {
        if (greatSpook) {
            display.addAsSingletonList("")
            display.addAsSingletonList("§7Daily Quests (§cdisabled§7)")
            display.addAsSingletonList(" §5§lThe Great Spook §7happened :O")
            return
        }
        val done = quests.count { it.state == QuestState.COLLECTED }
        display.addAsSingletonList("")
        display.addAsSingletonList("§7Daily Quests (§e$done§8/§e5 collected§7)")
        if (done != 5) {
            val filteredQuests = quests.filter { !config.hideComplete.get() || it.state != QuestState.COLLECTED }
            filteredQuests.mapTo(display) { renderQuest(it) }
        }
    }

    private fun renderQuest(quest: Quest): List<Any> {
        val category = quest.category
        val state = quest.state.displayName
        val stateColor = quest.state.color

        val progressText = if (quest is ProgressQuest && quest.state != QuestState.COLLECTED) {
            val haveAmount = quest.haveAmount
            val needAmount = quest.needAmount
            " §e$haveAmount§8/§e$needAmount"
        } else {
            ""
        }

        val sacksText = if (quest is FetchQuest && quest.state != QuestState.COLLECTED) {
            quest.displayItem.getAmountInSacksOrNull()?.let {
                val color = if (it >= quest.needAmount) {
                    "§a"
                } else {
                    "§c"
                }
                " §7($color${it.addSeparators()} §7in sacks)"
            } ?: " §7(§eSack data outdated/missing§7)"
        } else {
            ""
        }

        val stateText = if (quest !is UnknownQuest && quest.state != QuestState.ACCEPTED) {
            "$stateColor[$state] §f"
        } else {
            ""
        }

        val result = mutableListOf<Any>()
        val item = quest.displayItem.getItemStack()

        val displayName = if (category == QuestCategory.FETCH || category == QuestCategory.FISHING) {
            val name = item.name
            if (category == QuestCategory.FISHING) {
                name.removeWordsAtEnd(1)
            } else name
        } else quest.displayName

        val categoryName = category.displayName

        result.add("  $stateText$categoryName: ")
        result.add(item)
        result.add("§f$displayName$progressText$sacksText")
        return result
    }

    fun finishMiniBoss(miniBoss: CrimsonMiniBoss) {
        val miniBossQuest = getQuest<MiniBossQuest>() ?: return
        if (miniBossQuest.miniBoss == miniBoss && miniBossQuest.state == QuestState.ACCEPTED) {
            updateProcessQuest(miniBossQuest, miniBossQuest.haveAmount + 1)
        }
    }

    fun finishKuudra(kuudraTier: KuudraTier) {
        val kuudraQuest = getQuest<KuudraQuest>() ?: return
        // TODO make inline method for this two lines
        if (kuudraQuest.kuudraTier == kuudraTier && kuudraQuest.state == QuestState.ACCEPTED) {
            kuudraQuest.state = QuestState.READY_TO_COLLECT
        }
    }

    fun reset() {
        quests.clear()
    }

    fun load(storage: ProfileSpecificStorage.CrimsonIsleStorage) {
        reset()
        questLoader.loadConfig(storage)
    }

    fun saveConfig(storage: ProfileSpecificStorage.CrimsonIsleStorage) {
        storage.quests.clear()
        for (quest in quests) {
            val builder = StringBuilder()
            val internalName = quest.internalName
            builder.append(internalName)
            builder.append(":")
            val state = quest.state
            builder.append(state)

            if (quest is ProgressQuest) {
                val need = quest.needAmount
                val have = quest.haveAmount

                builder.append(":")
                builder.append(need)
                builder.append(":")
                builder.append(have)
            } else {
                builder.append(":0")
            }
            storage.quests.add(builder.toString())
        }
    }

    private fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.enabled.get()
}
