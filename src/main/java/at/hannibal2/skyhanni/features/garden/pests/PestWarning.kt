package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.garden.pests.PestSpawnEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isSprayExpired
import at.hannibal2.skyhanni.features.inventory.wardrobe.WardrobeAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PestWarning {
    private val config get() = PestAPI.config.pestTimer

    private const val BASE_PEST_COOLDOWN = 300.0

    var repellentMultiplier: Int = 1
    private var sprayMultiplier: Double = 1.0
    private var cooldown: Double? = null
    private var warningShown = false

    private var wardrobeOpened = false
    private var lastOpened = SimpleTimeMark.farPast()

    private val storage get() = GardenAPI.storage

    private var equipmentPestCooldown: Int
        get() = storage?.equipmentPestCooldown ?: 0
        set(value) {
            storage?.equipmentPestCooldown = value
        }

    // TODO : move to EquipmentAPI
    /**
     * REGEX-TEST: §aYou equipped a §r§5Rooted Pest Vest§r§a!
     * REGEX-TEST: §aYou equipped a §r§5Rooted Lotus Necklace§r§a!
     * REGEX-TEST: §aYou equipped a §r§aSqueaky Pesthunter's Gloves§r§a!
     */
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
        if (!LorenzUtils.inSkyBlock) return

        if (event.inventoryName == "Your Equipment and Stats") {
            equipmentPestCooldown = checkEquipment(event.inventoryItems)
        }

        WardrobeAPI.inventoryPattern.matchMatcher(event.inventoryName) {
            wardrobeOpened = true
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        sprayMultiplier = checkSpray()
        cooldown = BASE_PEST_COOLDOWN * sprayMultiplier * (1 - equipmentPestCooldown.div(100.0)) * repellentMultiplier
    }

    @HandleEvent
    fun onPestSpawn(event: PestSpawnEvent) {
        warningShown = false
        wardrobeOpened = false
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
        return if (plot.isSprayExpired) 1.0 else if (Perk.PEST_ERADICATOR.isActive) 0.25 else 0.5
    }

    @SubscribeEvent
    fun warn(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (cooldown == null) return
        if (warningShown) return

        val timeSinceLastPest = PestSpawnTimer.lastSpawnTime.passedSince().inWholeSeconds
        val cooldownValue = cooldown ?: return
        if (timeSinceLastPest >= cooldownValue - config.pestSpawnWarningTime) {
            LorenzUtils.sendTitle("§cPests Cooldown Expired!", duration = 3.seconds)

            ChatUtils.clickableChat(
                "§cPest spawn cooldown has expired",
                onClick = {
                    HypixelCommands.wardrobe()
                },
                "§eClick to open your wardrobe!"
            )

            playUserSound()

            warningShown = true
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!config.sound.repeatSound) return
        if (!event.isMod(config.sound.repeatDuration)) return

        if (!wardrobeOpened && warningShown) {
            playUserSound()
        }
    }

    @HandleEvent
    fun onKeyPress(event: KeyPressEvent) {
        if (!warningShown) return
        if (lastOpened.passedSince() < 200.milliseconds) return

        if (event.keyCode == config.keyBindWardrobe) {
            HypixelCommands.wardrobe()
            lastOpened = SimpleTimeMark.now()
        }
    }

    @JvmStatic
    fun playUserSound() {
        with(config.sound) {
            SoundUtils.createSound(name, pitch).playSound()
        }
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Pest Warning")
        event.addIrrelevant {
            add("Repellent Multiplier: $repellentMultiplier")
            add("Spray Multiplier: $sprayMultiplier")
            add("Equipment Pest Cooldown: $equipmentPestCooldown")
            add("Cooldown: ${cooldown ?: "Unknown"}")
            add("Warning Shown: $warningShown")
            add("Wardrobe Open: $wardrobeOpened")
        }
    }

    fun isEnabled() = GardenAPI.inGarden() && config.pestSpawnWarning
}
