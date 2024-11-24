package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ElectionAPI
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.garden.pests.PestSpawnEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isSprayExpired
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.seconds
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PestWarning {
    private val config get() = PestAPI.config.pestTimer

    private const val BASE_PEST_COOLDOWN = 300.0

    private var SprayMultiplier: Double = 1.0
    private var Cooldown: Double? = null
    private var lastPestSpawnTime = SimpleTimeMark.farPast()
    private var warningShown = false

    val storage get() = GardenAPI.storage

    var equipmentPestCooldown: Int
        get() = storage?.equipmentPestCooldown ?: 0
        set(value) {
            storage?.equipmentPestCooldown = value
        }

    private val equipmentPattern by RepoPattern.pattern(
        "chat.pest.equipment",
        "§aYou equipped a §r§.(?<reforge>\\S+)? (?<item>.*)§r§a!"
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        val message = event.message
        equipmentPattern.matchMatcher(message) {
            group("reforge")?.let {
                if (it.contains("Squeaky")) {
                    equipmentPestCooldown += 1
                }
            }
            group("item")?.let {
                equipmentPestCooldown = when (it) {
                    "Pesthunter's Gloves" -> equipmentPestCooldown + 1
                    "Pest Vest" -> equipmentPestCooldown + 20
                    else -> equipmentPestCooldown
                }
            }
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName == "Your Equipment and Stats" && LorenzUtils.inSkyBlock) {
            equipmentPestCooldown = checkEquipment(event.inventoryItems)
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        SprayMultiplier = checkSpray()
        Cooldown = BASE_PEST_COOLDOWN * SprayMultiplier * (1 - (equipmentPestCooldown?.div(100.0) ?: 0.0))
    }

    @HandleEvent
    fun onPestSpawn(event: PestSpawnEvent) {
        lastPestSpawnTime = SimpleTimeMark.now()
        warningShown = false
    }

    private fun checkEquipment(equippedItems: Map<Int, ItemStack>): Int {
        val slotsToCheck = setOf(10, 19, 28, 37)
        var totalReduction = 0

        for ((slot, itemStack) in equippedItems) {
            if (slot !in slotsToCheck) continue

            val internalName = itemStack.getInternalName().asString()
            val reforgeName = itemStack.getReforgeName()

            if (internalName == "PESTHUNTERS_GLOVES") totalReduction += 1
            if (internalName == "PEST_VEST") totalReduction += 20
            if (reforgeName == "squeaky") totalReduction += 1
        }
        return totalReduction
    }

    private fun checkSpray(): Double {
        val plot = GardenPlotAPI.getCurrentPlot() ?: return 1.0
        return if (plot.isSprayExpired) 1.0 else if (checkFinnegan()) 0.25 else 0.5
    }

    private fun checkFinnegan(): Boolean {
        val currentMayor = ElectionAPI.currentMayor
        val currentMinister = ElectionAPI.currentMinister
        return (currentMayor?.mayorName == "Finnegan" || currentMinister?.mayorName == "Finnegan") &&
            (currentMayor?.activePerks?.any { it.perkName == "Pest Eradicator" } == true ||
                currentMinister?.activePerks?.any { it.perkName == "Pest Eradicator" } == true)
    }

    @SubscribeEvent
    fun warn(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (LorenzUtils.inSkyBlock && Cooldown != null && !warningShown && config.pestSpawnWarning) {
            val timeSinceLastPest = lastPestSpawnTime.passedSince().inWholeSeconds
            if (timeSinceLastPest >= Cooldown!! - config.pestSpawnWarningTime) {
                SoundUtils.createSound("random.orb", 0.5f).playSound()
                LorenzUtils.sendTitle("§cPests Cooldown Expired!", duration = 3.seconds)
                ChatUtils.chat("§cPests cooldown has expired")
                warningShown = true
            }
        }
    }

    fun isEnabled() = GardenAPI.inGarden() && config.pestSpawnWarning
}
