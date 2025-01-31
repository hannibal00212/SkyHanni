package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.SackApi.getAmountInSacksOrNull
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
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
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeWordsAtEnd
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object DailyQuestHelper {

    private val townBoardMage = LorenzVec(-138, 92, -755)
    private val townBoardBarbarian = LorenzVec(-572, 100, -687)

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

        QuestLoader.checkInventory(event)
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.enabled) {
            if (IslandType.CRIMSON_ISLE.isInIsland()) {
                QuestLoader.loadFromTabList()
            }
        }
    }

    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.FACTION_QUESTS)) return
        if (!isEnabled()) return

        QuestLoader.loadFromTabList()
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (event.repeatSeconds(3)) {
            checkInventoryForFetchItem()
        }
    }

    fun update() {
        CrimsonIsleReputationHelper.update()
    }

    @HandleEvent
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

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
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

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (!CrimsonIsleReputationHelper.showLocations()) return

        for (quest in quests) {
            if (quest is MiniBossQuest) continue
            if (quest.state != QuestState.ACCEPTED) continue
            val location = quest.location ?: continue

            event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
            event.drawDynamicText(location, quest.displayName, 1.5)
        }

        renderTownBoard(event)
    }

    private fun renderTownBoard(event: SkyHanniRenderWorldEvent) {
        if (!quests.any { it.needsTownBoardLocation() }) return
        val location = when (CrimsonIsleReputationHelper.factionType ?: return) {
            FactionType.BARBARIAN -> townBoardBarbarian
            FactionType.MAGE -> townBoardMage
        }
        event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
        event.drawDynamicText(location, "Town Board", 1.5)
    }

    private fun Quest.needsTownBoardLocation() =
        state == QuestState.READY_TO_COLLECT ||
            (state == QuestState.ACCEPTED && (this is FetchQuest || this is RescueMissionQuest))

    fun MutableList<Renderable>.addQuests() {
        if (greatSpook) {
            addString("")
            addString("§7Daily Quests (§cdisabled§7)")
            addString(" §5§lThe Great Spook §7happened :O")
            return
        }
        val done = quests.count { it.state == QuestState.COLLECTED }
        addString("")
        addString("§7Daily Quests (§e$done§8/§e5 collected§7)")
        if (done != 5) {
            val filteredQuests = quests.filter { !config.hideComplete.get() || it.state != QuestState.COLLECTED }
            addAll(filteredQuests.map { renderQuest(it) })
        }
    }

    private fun renderQuest(quest: Quest): Renderable {
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

        val item = quest.displayItem.getItemStack()

        val displayName = if (category == QuestCategory.FETCH || category == QuestCategory.FISHING) {
            val name = item.name
            if (category == QuestCategory.FISHING) {
                name.removeWordsAtEnd(1)
            } else name
        } else quest.displayName

        val categoryName = category.displayName

        return Renderable.line {
            addString("  $stateText$categoryName: ")
            addItemStack(item)
            addString("§f$displayName$progressText$sacksText")
        }
    }

    fun finishMiniBoss(miniBoss: CrimsonMiniBoss) {
        val miniBossQuest = getQuest<MiniBossQuest>() ?: return
        if (miniBossQuest.miniBoss == miniBoss && miniBossQuest.state == QuestState.ACCEPTED) {
            updateProcessQuest(miniBossQuest, miniBossQuest.haveAmount + 1)
            if (miniBossQuest.haveAmount == 1) {
                fixMiniBossByTabWidget(miniBossQuest)
            }
        }
    }

    private fun fixMiniBossByTabWidget(oldQuest: MiniBossQuest) {
        oldQuest.state = QuestState.ACCEPTED
        DelayedRun.runDelayed(5.seconds) {
            if (oldQuest.state == QuestState.ACCEPTED) {
                ChatUtils.debug(
                    "Daily Minibosss Quest is still not ready to accept even though we have one miniboss kill," +
                        "we now assume there are two to kill.",
                )
                val newQuest = MiniBossQuest(oldQuest.miniBoss, oldQuest.state, 2)
                newQuest.haveAmount = oldQuest.haveAmount
                DelayedRun.runNextTick {
                    quests.remove(oldQuest)
                    quests.add(newQuest)
                    ChatUtils.chat("Fixed wrong miniboss amount from Tab Widget.")
                    update()
                }
            } else {
                oldQuest.state = QuestState.READY_TO_COLLECT
            }
            CrimsonIsleReputationHelper.update()
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
        QuestLoader.loadConfig(storage)
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
