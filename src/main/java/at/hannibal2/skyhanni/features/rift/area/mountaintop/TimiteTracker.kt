package at.hannibal2.skyhanni.features.rift.area.mountaintop

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.features.rift.RiftApi.motesNpcPrice
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TimiteTracker {

    private val config get() = SkyHanniMod.feature.rift.area.mountaintop.timite
    private val HIGHLITE = "HIGHLITE".toInternalName()


    class Data : ItemTrackerData() {
        override fun resetItems() {
            "detekt ur a stupid fucker"
        }

        override fun getDescription(timesGained: Long): List<String> {
            return emptyList()
        }

        override fun getCoinName(item: TrackedItem): String {
            return "§5Motes"
        }

        override fun getCoinDescription(item: TrackedItem): List<String> {
            return emptyList()
        }

        override fun getCustomPricePer(internalName: NeuInternalName): Double {
            return internalName.getItemStack().motesNpcPrice() ?: 0.0
        }

        fun getTime(): Int {
            this.items["TIMITE".toInternalName()]?.let {
                return it.totalAmount.toInt() * 2
            }
            return 0
        }

    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§9§lTimite Tracker")
        val profit = tracker.drawItems(data, { true }, this)


        val highliteRecipe = NeuItems.getRecipes(HIGHLITE).singleOrNull()
        if (highliteRecipe != null) {
            var craftableAmount = 0

            for (neededItem in ItemUtils.neededItems(highliteRecipe)) {
                if (neededItem.key in validItems) {
                    data.items[neededItem.key]?.let {
                        val amountCanCraft = it.totalAmount.toInt() / neededItem.value
                        if (craftableAmount == 0 || amountCanCraft < craftableAmount) {
                            craftableAmount = amountCanCraft
                        }
                    }
                }
            }
            val stack = HIGHLITE.getItemStack()
            val motes = stack.motesNpcPrice()?.times(craftableAmount)?.shortFormat() ?: "0"
            if (craftableAmount > 0) {
                add(
                    Renderable.string(
                        " §7${craftableAmount.shortFormat()}x ${stack.displayName} Craftable§7: §5$motes motes"
                    ).toSearchable()
                )
            }
        }

        add(Renderable.string("§aTime§7: §a${data.getTime().seconds.format()}ф").toSearchable())


        add(
            Renderable.string(
                "§dTotal Profit§7: §5${profit.toInt().shortFormat()} Motes"
            ).toSearchable()
        )

    }

    private val tracker =
        SkyHanniItemTracker("Timite Tracker", { Data() }, { it.timiteHelperTracker }) {
            drawDisplay(it)
        }

    private val validItems = listOf(
        "TIMITE".toInternalName(),
        "YOUNGITE".toInternalName(),
        "OBSOLITE".toInternalName(),
    )

    @HandleEvent
    fun onItem(event: ItemAddEvent) {

        if (validItems.contains(event.internalName)) {
            tracker.addItem(event.internalName, event.amount, event.source == ItemAddManager.Source.COMMAND)
        }
    }

    @HandleEvent
    fun onRender(event: GuiRenderEvent) {
        if (!isEnabled()) return

        tracker.renderDisplay(config.trackerPos)
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresettimitetracker") {
            description = "Resets the Timite Tracker."
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    private fun isEnabled() = RiftApi.inMountainTop() && config.timiteTracker

}
