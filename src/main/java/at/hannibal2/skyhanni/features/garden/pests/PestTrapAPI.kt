package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.events.garden.pests.PestTrapDataUpdatedEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.features.garden.GardenPlotApi
import at.hannibal2.skyhanni.features.garden.pests.PestApi.pestTrapPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.common.cache.RemovalCause.EXPIRED
import com.google.gson.annotations.Expose
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C02PacketUseEntity
import java.util.regex.Matcher
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

private typealias ArmorStandNameChangeEvent = EntityCustomNameUpdateEvent<EntityArmorStand>

@SkyHanniModule
object PestTrapAPI {

    data class PestTrapData(
        @Expose var number: Int,
        @Expose var name: String? = null,
        @Expose var plotName: String? = null,
        @Expose var location: LorenzVec? = null,
        @Expose var trapType: PestTrapType? = PestTrapType.PEST_TRAP,
        @Expose var count: Int = 0,
        @Expose var baitCount: Int = -1,
        @Expose var baitType: SprayType? = null,
    ) {
        val index get() = number - 1
        val isFull get() = count >= MAX_PEST_COUNT_PER_TRAP
        val noBait get() = baitCount == 0
        val plot get() = plotName?.let { GardenPlotApi.getPlotByName(it) }
    }

    enum class PestTrapType(val displayName: String) {
        PEST_TRAP("§2Pest Trap§r"),
        MOUSE_TRAP("§9Mouse Trap§r"),
        ;

        override fun toString() = displayName
    }

    private const val BAIT_SLOT = 11
    private val PEST_SLOTS = 13..15
    private const val RELEASE_ALL_SLOT = 17
    private const val MAX_RELEASED_PESTS = 8

    private val patternGroup = RepoPattern.group("garden.pests.trap")
    private val storage get() = GardenApi.storage
    // Todo: Use this in the future to tell the user to enable the widget if it's disabled
    private val widgetEnabledAndVisible: TimeLimitedCache<TabWidget, Boolean> = baseWidgetStatus()

    var MAX_PEST_COUNT_PER_TRAP = 3
    private var lastTabHash: Int = 0
    private var lastTitleHash: Int = 0
    private var lastFullHash: Int = 0
    private var lastNoBaitHash: Int = 0
    private var lastTotalHash: Int = lastTitleHash + lastFullHash + lastNoBaitHash
    private var lastClickedIndex: Int = -1
    private var inIndex: Int = -1

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §2§lGOTCHA! §7Your traps caught a §2Pest §7in §aPlot §r§bM4§r§r§7!
     * REGEX-TEST: §2§lGOTCHA! §7Your traps caught a §2Pest §7in §aPlot §r§bFR1§r§7!
     */
    private val caughtChatPattern by patternGroup.pattern(
        "chat.caught",
        "(?:§.)+GOTCHA! §7Your traps caught a §.Pest §7in §.Plot (?:§.)+.*(?:§.)+!"
    )

    private val tabListPestTrapsPattern = TabWidget.PEST_TRAPS.pattern
    private val tabListFullTrapsPattern = TabWidget.FULL_TRAPS.pattern
    private val tabListNoBaitPattern = TabWidget.NO_BAIT.pattern

    /**
     * REGEX-TEST: Bait: §aTasty Cheese
     * REGEX-TEST: Bait: §c§lNO BAIT
     */
    private val entityBaitPattern by patternGroup.pattern(
        "entity.bait",
        "Bait: (?:§.)+(?:NO BAIT|(?<bait>.+))"
    )

    private val entityFullTrap by patternGroup.pattern(
        "entity.full",
        "§c§lFULL"
    )

    /**
     * REGEX-TEST: Mouse Trap
     * REGEX-TEST: Pest Trap
     */
    private val pestTrapInventoryName by patternGroup.pattern(
        "inventory.name",
        "(?<type>Mouse|Pest) Trap"
    )
    // </editor-fold>

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onClickEntity(event: PacketSentEvent) {
        if (!PestApi.isNearPestTrap()) return
        val world = Minecraft.getMinecraft().theWorld ?: return
        val packet = event.packet as? C02PacketUseEntity ?: return
        val entity = packet.getEntityFromWorld(world) ?: return
        val armorStandEntity = entity as? EntityArmorStand ?: return
        val entityLocation = armorStandEntity.getLorenzVec()

        // Find the closest trap to the clicked entity
        val closestTrap = storage?.pestTrapStatus?.minByOrNull {
            it.location?.distance(entityLocation)?.takeIf { distance ->
                distance < 2.0
            } ?: Double.MAX_VALUE
        } ?: return
        lastClickedIndex = closestTrap.index
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!PestApi.isNearPestTrap()) return
        if (lastClickedIndex == -1) return
        inIndex = lastClickedIndex
        pestTrapInventoryName.matchMatcher(event.inventoryName) {
            val inventoryTrapType = this.extractTrapType() ?: return
            val storage = storage ?: return
            val baitStack = event.inventoryItems[BAIT_SLOT]?.takeIf {
                it.displayName.isNotEmpty() && it.displayName != "§6Trap Bait"
            } ?: return
            val baitInternalName = baitStack.getInternalNameOrNull() ?: return
            storage.pestTrapStatus[lastClickedIndex].apply {
                trapType = inventoryTrapType
                baitType = SprayType.getByInternalName(baitInternalName)
                baitCount = baitStack.stackSize
            }
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inIndex = -1
    }

    @HandleEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (inIndex == -1) return
        val storage = storage ?: return
        val trap = storage.pestTrapStatus[inIndex].takeIf { it.count > 0 } ?: return
        val stack = event.slot?.stack ?: return
        when (event.slot.slotNumber) {
            in PEST_SLOTS ->  {
                if (stack.displayName == "§aPest Slot") return
                if (!canReleasePest()) return
                trap.apply { count-- }
            }
            RELEASE_ALL_SLOT -> {
                if (!stack.canReleaseAll() || !canReleasePest(trap.count)) return
                val existingPests = PestApi.scoreboardPests.takeIf { it < MAX_RELEASED_PESTS } ?: return
                val pestsToRelease = min(trap.count, MAX_RELEASED_PESTS - existingPests)
                trap.apply {
                    count -= pestsToRelease
                }
            }
            else -> return
        }
    }

    private fun canReleasePest(quantity: Int = 1): Boolean {
        val existingPests = PestApi.scoreboardPests.takeIf { it < MAX_RELEASED_PESTS } ?: return false
        val pestsToRelease = min(quantity, MAX_RELEASED_PESTS - existingPests)
        return pestsToRelease > 0
    }

    private fun ItemStack.canReleaseAll() =
        this.getLore().any { it == "§cThere are no §2Pests §cto release!" }

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        val storage = storage?.takeIf { it.pestTrapStatus.size == 0 } ?: return
        storage.pestTrapStatus = (1..MAX_PEST_COUNT_PER_TRAP).map { PestTrapData(it) }.toMutableList()
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onEntityChangeName(event: ArmorStandNameChangeEvent) {
        val entityName = event.entity.name
        pestTrapPattern.matchMatcher(entityName) { this.extractPositionNameType(event.entity) }
        entityBaitPattern.matchMatcher(entityName) { this.extractBaitType(event.entity) }
        entityFullTrap.matchMatcher(entityName) { extractIsFull(event.entity) }
    }

    private fun Matcher.extractTrapType() = when (groupOrNull("type")?.lowercase()) {
        "mouse" -> PestTrapType.MOUSE_TRAP
        "pest" -> PestTrapType.PEST_TRAP
        else -> null
    }

    private fun Matcher.extractPositionNameType(entity: EntityArmorStand) {
        val storage = storage ?: return
        val number = groupOrNull("number")?.toIntOrNull() ?: return
        storage.pestTrapStatus[number - 1].apply {
            location = entity.getLorenzVec()
            name = entity.cleanName()
            trapType = extractTrapType() ?: trapType
        }
    }

    private fun Matcher.extractBaitType(entity: EntityArmorStand) {
        val storage = storage ?: return
        val trap = storage.pestTrapStatus.firstOrNull { trap ->
            trap.location?.let { trapLocation ->
                entity.getLorenzVec().isBottomToTop(trapLocation)
            } ?: false
        } ?: return
        val baitString = groupOrNull("bait") ?: run {
            trap.baitCount = 0
            return
        }
        val sprayType = SprayType.getByName(baitString) ?: return
        trap.baitType = sprayType
    }

    private fun extractIsFull(entity: EntityArmorStand) {
        val storage = storage ?: return
        val trap = storage.pestTrapStatus.firstOrNull { trap ->
            trap.location?.isBottomToTop(entity.getLorenzVec()) ?: false
        } ?: return
        trap.count = MAX_PEST_COUNT_PER_TRAP
    }

    private fun LorenzVec.isBottomToTop(other: LorenzVec) = (this.y < other.y) &&
        (this.x == other.x) && (this.z == other.z)

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onChat(event: SkyHanniChatEvent) {
        caughtChatPattern.matchMatcher(event.message) {
            val storage = storage ?: return
            val plotName = groupOrNull("plot") ?: return
            storage.pestTrapStatus.filter {
                it.plotName == plotName && it.count < MAX_PEST_COUNT_PER_TRAP
            }.onEach {
                it.count++
                it.baitCount =
                    if (it.baitCount == -1) -1
                    else (it.baitCount - 1).coerceAtLeast(0)
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onTabListUpdate(event: TabListUpdateEvent) {
        val storage = storage ?: return
        lastTabHash = event.tabList.sumOf { it.hashCode() }.takeIf { it != lastTabHash } ?: return

        for (line in event.tabList) {
            line.updateDataFromTitle(storage)
            line.updateDataFromFull(storage)
            line.updateDataFromNoBait(storage)
        }

        lastTotalHash = (lastTitleHash + lastFullHash + lastNoBaitHash).takeIf { it != lastTotalHash } ?: return

        PestTrapDataUpdatedEvent(storage.pestTrapStatus).post()
    }

    private fun String.updateDataFromTitle(
        storage: ProfileSpecificStorage.GardenStorage
    ) = tabListPestTrapsPattern.matchMatcher(this@updateDataFromTitle) {
        widgetEnabledAndVisible[TabWidget.PEST_TRAPS] = true
        val thisHash = this@updateDataFromTitle.hashCode().takeIf { it != lastTitleHash } ?: return@matchMatcher
        lastTitleHash = thisHash

        val count = group("count").toIntOrNull() ?: return@matchMatcher
        val max = group("max").toIntOrNull() ?: return@matchMatcher

        MAX_PEST_COUNT_PER_TRAP = max(max, MAX_PEST_COUNT_PER_TRAP)
        val numberToTrack = min(count, MAX_PEST_COUNT_PER_TRAP)

        storage.pestTrapStatus = storage.pestTrapStatus.take(numberToTrack).toMutableList()
    }

    private fun Matcher.extractTrapList(): List<String?>? = listOf(
        groupOrNull("one"),
        groupOrNull("two"),
        groupOrNull("three"),
    ).takeIf { it.any { group -> group != null } }

    private fun String.updateDataFromFull(
        storage: ProfileSpecificStorage.GardenStorage
    ) = tabListFullTrapsPattern.matchMatcher(this@updateDataFromFull) {
        widgetEnabledAndVisible[TabWidget.FULL_TRAPS] = true
        lastFullHash = this@updateDataFromFull.hashCode().takeIf { it != lastFullHash } ?: return@matchMatcher

        val fullTraps = extractTrapList() ?: return@matchMatcher
        storage.pestTrapStatus.filter {
            fullTraps[it.index] != null
        }.onEach {
            it.count = MAX_PEST_COUNT_PER_TRAP
        }
    }

    private fun String.updateDataFromNoBait(
        storage: ProfileSpecificStorage.GardenStorage
    ) = tabListNoBaitPattern.matchMatcher(this@updateDataFromNoBait) {
        widgetEnabledAndVisible[TabWidget.NO_BAIT] = true
        lastNoBaitHash = this@updateDataFromNoBait.hashCode().takeIf { it != lastNoBaitHash } ?: return@matchMatcher

        val noBaitTraps = extractTrapList() ?: return@matchMatcher
        storage.pestTrapStatus.filter {
            noBaitTraps[it.index] != null
        }.onEach {
            it.baitType = null
            it.baitCount = 0
        }
    }

    @Suppress("UnstableApiUsage")
    private fun baseWidgetStatus() = TimeLimitedCache<TabWidget, Boolean>(
        expireAfterWrite = 60.seconds,
        removalListener = { key, _, removalCause ->
            if (key != null && GardenApi.inGarden() && removalCause == EXPIRED) {
                ChatUtils.userError(
                    "Could not read ${key.name.lowercase().replace("_", " ")} data from the tab list!",
                    replaceSameMessage = true
                )
            }
        }
    )
}
