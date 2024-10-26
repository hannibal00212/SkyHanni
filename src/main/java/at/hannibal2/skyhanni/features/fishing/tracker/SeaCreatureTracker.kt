package at.hannibal2.skyhanni.features.fishing.tracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.FishingBobberCastEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SeaCreatureFishEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.features.fishing.SeaCreatureManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.CollectionUtils.sumAllValues
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.addButton
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SeaCreatureTracker {
    private var needMigration = true

    private val config get() = SkyHanniMod.feature.fishing.seaCreatureTracker

    private val tracker = SkyHanniTracker("Sea Creature Tracker", { Data() }, { it.fishing.seaCreatureTracker }) {
        drawDisplay(it)
    }

    class Data : TrackerData() {

        override fun reset() {
            amount.clear()
        }

        @Expose
        var amount: MutableMap<String, Int> = mutableMapOf()
    }

    @SubscribeEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!isEnabled()) return

        tracker.modify {
            val amount = if (event.doubleHook && config.countDouble) 2 else 1
            it.amount.addOrPut(event.seaCreature.name, amount)
        }

        if (config.hideChat) {
            event.chatEvent.blockedReason = "sea_creature_tracker"
        }
    }

    private const val NAME_ALL: CategoryName = "All"
    private var currentCategory: CategoryName = NAME_ALL

    private fun getCurrentCategories(data: Data): Map<CategoryName, Int> {
        val map = mutableMapOf<CategoryName, Int>()
        map[NAME_ALL] = data.amount.size
        for ((category, names) in SeaCreatureManager.allVariants) {
            val amount = names.count { it in data.amount }
            if (amount > 0) {
                map[category] = amount
            }
        }

        return map
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        needMigration = true
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        // manually migrating from "Phlhlegblast" to "Plhlegblast" when the new name is in the repo
        tryToMigrate(data.amount)

        addSearchString("§7Sea Creature Tracker:")

        val filter: (String) -> Boolean = addCategories(data)
        val realAmount = data.amount.filter { filter(it.key) }

        val total = realAmount.sumAllValues()
        for ((name, amount) in realAmount.entries.sortedByDescending { it.value }) {
            val displayName = SeaCreatureManager.allFishingMobs[name]?.displayName ?: run {
                ErrorManager.logErrorStateWithData(
                    "Sea Creature Tracker can not display a name correctly",
                    "Could not find sea creature by name",
                    "SeaCreatureManager.allFishingMobs.keys" to SeaCreatureManager.allFishingMobs.keys,
                    "name" to name,
                )
                name
            }

            val percentageSuffix = if (config.showPercentage.get()) {
                val percentage = LorenzUtils.formatPercentage(amount.toDouble() / total)
                " §7$percentage"
            } else ""

            addSearchString(" §7- §e${amount.addSeparators()} $displayName$percentageSuffix", displayName)
        }
        addSearchString(" §7- §e${total.addSeparators()} §7Total Sea Creatures")
    }

    private fun tryToMigrate(
        data: MutableMap<String, Int>,
    ) {
        if (!needMigration) return
        needMigration = false

        val oldName = "Phlhlegblast"
        val newName = "Plhlegblast"

        // only migrate once the repo contains the new name
        if (SeaCreatureManager.allFishingMobs.containsKey(newName)) {
            data[oldName]?.let {
                ChatUtils.debug("Sea Creature Tracker migrated $it $oldName to $newName")
                data[newName] = it
                data.remove(oldName)
            }
        }
    }

    private fun MutableList<Searchable>.addCategories(data: Data): (String) -> Boolean {
        val amounts = getCurrentCategories(data)
        val list = amounts.keys.toList()
        if (currentCategory !in list) {
            currentCategory = NAME_ALL
        }

        if (tracker.isInventoryOpen()) {
            addButton(
                prefix = "§7Category: ",
                getName = currentCategory.allLettersFirstUppercase() + " §7(" + amounts[currentCategory] + ")",
                onChange = {
                    val id = list.indexOf(currentCategory)
                    currentCategory = list[(id + 1) % list.size]
                    tracker.update()
                },
            )
        }

        return if (currentCategory == NAME_ALL) {
            { true }
        } else filterCurrentCategory()
    }

    private fun filterCurrentCategory(): (String) -> Boolean {
        val items = SeaCreatureManager.allVariants[currentCategory] ?: run {
            ErrorManager.logErrorStateWithData(
                "Sea Creature Tracker can not find all sea creature variants",
                "Sea creature variant is not found",
                "SeaCreatureManager.allVariants.keys" to SeaCreatureManager.allVariants.keys,
                "currentCategory" to currentCategory,
            )
            return { true }
        }
        return { it in items }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.showPercentage) {
            tracker.update()
        }
    }

    @SubscribeEvent
    fun onBobberThrow(event: FishingBobberCastEvent) {
        tracker.firstUpdate()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (!FishingAPI.isFishing(checkRodInHand = false)) return

        tracker.renderDisplay(config.position)
    }

    fun resetCommand() {
        tracker.resetCommand()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && !FishingAPI.wearingTrophyArmor && !LorenzUtils.inKuudraFight
}
