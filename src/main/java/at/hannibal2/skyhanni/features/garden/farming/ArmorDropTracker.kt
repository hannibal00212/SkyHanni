package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.ArmorDropInfo
import at.hannibal2.skyhanni.data.jsonobjects.repo.ArmorDropsJson
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ArmorDropTracker {

    private val config get() = GardenAPI.config.farmingArmorDrop

    private val armorPattern by RepoPattern.pattern(
        "garden.armordrops.armor",
        "(FERMENTO|CROPIE|SQUASH|MELON)_(LEGGINGS|CHESTPLATE|BOOTS|HELMET)"
    )

    private var hasArmor = false

    private val tracker = SkyHanniTracker("Armor Drop Tracker", { Data() }, { it.garden.armorDropTracker })
    { drawDisplay(it) }

    class Data : TrackerData() {

        override fun reset() {
            drops.clear()
        }

        @Expose
        var drops: MutableMap<ArmorDropType, Int> = mutableMapOf()
    }

    // Todo use repo pattern
    enum class ArmorDropType(val dropName: String, val chatMessage: String) {
        CROPIE("§9Cropie", "§6§lRARE CROP! §r§f§r§9Cropie §r§b(Armor Set Bonus)"),
        SQUASH("§5Squash", "§6§lRARE CROP! §r§f§r§5Squash §r§b(Armor Set Bonus)"),
        FERMENTO("§6Fermento", "§6§lRARE CROP! §r§f§r§6Fermento §r§b(Armor Set Bonus)"),
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        hasArmor = false
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        for (dropType in ArmorDropType.entries) {
            if (dropType.chatMessage == event.message) {
                addDrop(dropType)
                if (config.hideChat) {
                    event.blockedReason = "farming_armor_drops"
                }
            }
        }
    }

    private fun addDrop(drop: ArmorDropType) {
        tracker.modify {
            it.drops.addOrPut(drop, 1)
        }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§7Armor Drop Tracker:")
        for ((drop, amount) in data.drops.sortedDesc()) {
            val dropName = drop.dropName
            addSearchString(" §7- §e${amount.addSeparators()}x $dropName", dropName)
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.enabled) return
        if (!hasArmor) return
        if (!GardenAPI.hasFarmingToolInHand()) return

        tracker.renderDisplay(config.pos)
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.GARDEN) {
            tracker.firstUpdate()
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.enabled) return

        checkArmor()
    }

    private fun checkArmor() {
        val armorPieces = InventoryUtils.getArmor()
            .mapNotNull { it?.getInternalName()?.asString() }
            .count { armorPattern.matcher(it).matches() }
        hasArmor = armorPieces > 1
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ArmorDropsJson>("ArmorDrops")
        armorDropInfo = data.specialCrops
    }

    private var armorDropInfo = mapOf<String, ArmorDropInfo>()
    private var currentArmorDropChance = 0.0
    private var lastCalculationTime = SimpleTimeMark.farPast()

    fun getDropsPerHour(crop: CropType?): Double {
        if (crop == null) return 0.0

        if (lastCalculationTime.passedSince() > 5.seconds) {
            lastCalculationTime = SimpleTimeMark.now()

            val armorDropName = crop.specialDropType
            val armorName = armorDropInfo[armorDropName]?.armorType ?: return 0.0
            val pieceCount = InventoryUtils.getArmor()
                .mapNotNull { it?.getInternalName()?.asString() }
                .count { it.contains(armorName) || it.contains("FERMENTO") }

            val dropRates = armorDropInfo[armorDropName]?.chance ?: return 0.0
            var dropRate = 0.0
            if (pieceCount > 0 && dropRates.size >= pieceCount) {
                dropRate = dropRates[pieceCount - 1]
            }
            currentArmorDropChance = (dropRate * 60 * 60.0) / 100
        }
        return currentArmorDropChance
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.farmingArmorDropsEnabled", "garden.farmingArmorDrop.enabled")
        event.move(3, "garden.farmingArmorDropsHideChat", "garden.farmingArmorDrop.hideChat")
        event.move(3, "garden.farmingArmorDropsPos", "garden.farmingArmorDrop.pos")

        event.move(8, "#profile.garden.farmArmorDrops", "#profile.garden.armorDropTracker") { old ->
            val new = JsonObject()
            new.add("drops", old)
            new
        }
    }

    fun resetCommand() {
        tracker.resetCommand()
    }
}
