package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuRNGScore
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.SlayerChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.removeWordsAtEnd
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SlayerRngMeterDisplay {

    private val config get() = SkyHanniMod.feature.slayer.rngMeterDisplay

    private val patternGroup = RepoPattern.group("slayer.rngmeter")
    private val inventoryNamePattern by patternGroup.pattern(
        "inventoryname",
        "(?<name>.*) RNG Meter"
    )
    private val slayerInventoryNamePattern by patternGroup.pattern(
        "inventoryname.slayer",
        "Slayer"
    )
    private val updatePattern by patternGroup.pattern(
        "update",
        " {3}§dRNG Meter §f- §d(?<exp>.*) Stored XP"
    )
    private val changedItemPattern by patternGroup.pattern(
        "changeditem",
        "§aYou set your §r.* RNG Meter §r§ato drop §r.*§a!"
    )
    /**
     * REGEX-TEST: §aEnchanted Book (§d§lDuplex I§a)
     */
    private val bookFormatPattern by patternGroup.pattern(
        "book.format",
        "§aEnchanted Book \\((?<name>.*)§a\\)"
    )

    private var display = emptyList<Renderable>()
    private var lastItemDroppedTime = SimpleTimeMark.farPast()

    var rngScore = mapOf<String, Map<NEUInternalName, Long>>()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (!lastItemDroppedTime.isFarPast() && lastItemDroppedTime.passedSince() > 4.seconds) {
            lastItemDroppedTime = SimpleTimeMark.farPast()
            update()
        }
    }

    @SubscribeEvent
    fun onSlayerChange(event: SlayerChangeEvent) {
        update()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        if (config.hideChat && SlayerAPI.isInCorrectArea) {
            changedItemPattern.matchMatcher(event.message) {
                event.blockedReason = "slayer_rng_meter"
            }
        }

        val currentMeter = updatePattern.matchMatcher(event.message) {
            group("exp").formatLong()
        } ?: return

        val storage = getStorage() ?: return
        val old = storage.currentMeter
        storage.currentMeter = currentMeter

        if (old != -1L) {
            val item = storage.itemGoal
            val hasItemSelected = item != "" && item != "?"
            if (!hasItemSelected && config.warnEmpty) {
                ChatUtils.userError("No Slayer RNG Meter Item selected!")
                LorenzUtils.sendTitle("§cNo RNG Meter Item!", 3.seconds)
            }
            var blockChat = config.hideChat && hasItemSelected
            val diff = currentMeter - old
            if (diff > 0) {
                storage.gainPerBoss = diff
            } else {
                storage.currentMeter = 0
                blockChat = false
                val from = old.addSeparators()
                val to = storage.goalNeeded.addSeparators()

                var rawPercentage = old.toDouble() / storage.goalNeeded
                if (rawPercentage > 1) rawPercentage = 1.0
                val percentage = LorenzUtils.formatPercentage(rawPercentage)
                ChatUtils.chat("§dRNG Meter §7dropped at §e$percentage §7XP ($from/$to§7)")
                lastItemDroppedTime = SimpleTimeMark.now()
            }
            if (blockChat) {
                event.blockedReason = "slayer_rng_meter"
            }
        }
        update()
    }

    private fun getStorage(): ProfileSpecificStorage.SlayerRngMeterStorage? {
        return ProfileStorageData.profileSpecific?.slayerRngMeter?.getOrPut(getCurrentSlayer()) {
            ProfileSpecificStorage.SlayerRngMeterStorage()
        }
    }

    private fun getCurrentSlayer() = SlayerAPI.latestSlayerCategory.removeWordsAtEnd(1).removeColor()

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return

        readRngMeterInventory(event)
        readSlayerInventory(event)
    }

    private fun readRngMeterInventory(event: InventoryFullyOpenedEvent) {
        val name = inventoryNamePattern.matchMatcher(event.inventoryName) {
            group("name")
        } ?: return

        if (name != getCurrentSlayer()) return

        val internalName = event.inventoryItems.values
            .find { item -> item.getLore().any { it.contains("§a§lSELECTED") } }
        setNewGoal(internalName?.getInternalName())
    }

    private fun readSlayerInventory(event: InventoryFullyOpenedEvent) {
        if (!slayerInventoryNamePattern.matches(event.inventoryName)) return
        val item = event.inventoryItems[35] ?: return
        val lore = item.getLore()
        val name = lore.firstOrNull()?.removeColor() ?: return

        if (name != getCurrentSlayer()) return

        val rawName = lore.nextAfter("§7Selected Drop") ?: return
        val itemName = bookFormatPattern.matchMatcher(rawName) {
            group("name")
        } ?: rawName
        val internalName = NEUInternalName.fromItemName(itemName)
        setNewGoal(internalName)
    }

    private fun setNewGoal(internalName: NEUInternalName?) {
        val storage = getStorage() ?: return
        if (internalName == null) {
            storage.itemGoal = ""
            storage.goalNeeded = -1
        } else {
            storage.itemGoal = internalName.itemName
            storage.goalNeeded = rngScore[getCurrentSlayer()]?.get(internalName)
                ?: ErrorManager.skyHanniError(
                    "RNG Meter goal setting failed",
                    "internalName" to internalName,
                    "currentSlayer" to getCurrentSlayer(),
                    "repo" to rngScore
                )
        }
        update()
    }

    @SubscribeEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        rngScore = event.readConstant<NeuRNGScore>("rngscore").slayer
    }

    private fun update() {
        display = listOf(makeLink(drawDisplay()))
    }

    private fun makeLink(text: String) =
        Renderable.clickAndHover(
            text, listOf("§eClick to open RNG Meter Inventory."),
            onClick = {
                HypixelCommands.showRng("slayer", SlayerAPI.activeSlayer?.rngName)
            },
        )

    fun drawDisplay(): String {
        val storage = getStorage() ?: return ""

        if (SlayerAPI.latestSlayerCategory.let {
                it.endsWith(" I") || it.endsWith(" II")
            }
        ) {
            return ""
        }
        val latestSlayerCategory = SlayerAPI.latestSlayerCategory
        latestSlayerCategory.endsWith(" I")

        with(storage) {
            if (itemGoal == "?") return "§cOpen RNG Meter Inventory!"
            if (itemGoal == "") {
                return if (!lastItemDroppedTime.isFarPast()) {
                    "§a§lRNG Item dropped!"
                } else {
                    "§eNo RNG Item selected!"
                }
            }
            if (currentMeter == -1L || gainPerBoss == -1L) return "§cKill the slayer boss 2 times!"

            val missing = goalNeeded - currentMeter + gainPerBoss
            var timesMissing = missing.toDouble() / gainPerBoss
            if (timesMissing < 1) timesMissing = 1.0
            timesMissing = ceil(timesMissing)

            return "$itemGoal §7in §e${timesMissing.toInt().addSeparators()} §7bosses!"
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (!SlayerAPI.isInCorrectArea) return
        if (!SlayerAPI.hasActiveSlayerQuest()) return

        config.pos.renderRenderables(display, posLabel = "RNG Meter Display")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
