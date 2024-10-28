package at.hannibal2.skyhanni.data.maxwell

import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.MaxwellPowersJson
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.skyblock.MaxwellUpdateEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardConfigElement
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

@SkyHanniModule
object MaxwellAPI {

    private val storage get() = ProfileStorageData.profileSpecific?.maxwell

    var currentPower: MaxwellPower
        get() = storage?.currentPower?.let(::getPowerByInternalNameOrNull) ?: NO_POWER
        private set(value) {
            if (storage?.currentPower == value.internalName) return
            storage?.currentPower = value.internalName
            MaxwellUpdateEvent.Power(value).post()
        }

    var magicalPower: Int
        get() = storage?.magicalPower ?: 0
        private set(value) {
            if (storage?.magicalPower == value) return
            storage?.magicalPower = value
            MaxwellUpdateEvent.MagicalPower(value).post()
        }

    var tunings: List<MaxwellTunings>
        get() = storage?.tunings.orEmpty()
        private set(value) {
            if (storage?.tunings == value) return
            storage?.tunings = value
            MaxwellUpdateEvent.Tuning(value).post()
        }

    var favoritePowers: List<MaxwellPower>
        get() = storage?.favoritePowers?.mapNotNull(::getPowerByInternalNameOrNull).orEmpty()
        set(value) {
            storage?.favoritePowers = value.map(MaxwellPower::internalName)
        }

    var inInventory: Boolean = false
        private set

    var inTuningGui: Boolean = false
        private set

    fun getPowerByNameOrNull(name: String): MaxwellPower? = powers.values.find { it.name == name }

    fun getPowerByInternalNameOrNull(name: String): MaxwellPower? = powers[name]

    val NO_POWER = MaxwellPower("No Power", "NO_POWER")

    private var powers = mapOf<String, MaxwellPower>()
    private const val THAUMATURGY_TUNINGS_SLOT = 51
    private const val THAUMATURGY_MP_SLOT = 48

    //region Patterns
    private val patternGroup = RepoPattern.group("data.maxwell")
    private val chatPowerPattern by patternGroup.pattern(
        "chat.power",
        "§eYou selected the §a(?<power>.*) §e(power )?for your §aAccessory Bag§e!",
    )
    private val chatPowerUnlockedPattern by patternGroup.pattern(
        "chat.power.unlocked",
        "§eYour selected power was set to (?:§r)*§a(?<power>.*)(?:§r)*§e!",
    )
    private val inventoryPowerPattern by patternGroup.pattern(
        "inventory.power",
        "§7Selected Power: §a(?<power>.*)",
    )
    private val inventoryMPPattern by patternGroup.pattern(
        "inventory.magicalpower",
        "§7Magical Power: §6(?<mp>[\\d,]+)",
    )
    private val thaumaturgyGuiPattern by patternGroup.pattern(
        "gui.thaumaturgy",
        "Accessory Bag Thaumaturgy",
    )
    private val thaumaturgyStartPattern by patternGroup.pattern(
        "gui.thaumaturgy.start",
        "§7Your tuning:",
    )
    private val thaumaturgyDataPattern by patternGroup.pattern(
        "gui.thaumaturgy.data",
        "§(?<color>.)\\+(?<amount>[^ ]+)(?<icon>.) (?<name>.+)",
    )
    private val thaumaturgyMagicalPowerPattern by patternGroup.pattern(
        "gui.thaumaturgy.magicalpower",
        "§7Total: §6(?<mp>[\\d.,]+) Magical Power",
    )
    private val statsTuningGuiPattern by patternGroup.pattern(
        "gui.thaumaturgy.statstuning",
        "Stats Tuning",
    )
    private val statsTuningDataPattern by patternGroup.pattern(
        "thaumaturgy.statstuning",
        "§7You have: .+ §7\\+ §(?<color>.)(?<amount>[^ ]+) (?<icon>.)",
    )
    private val tuningNamePattern by patternGroup.pattern(
        "gui.thaumaturgy.tunings.name",
        "§.. (?<name>.+)"
    )
    private val tuningAutoAssignedPattern by patternGroup.pattern(
        "tuningpoints.chat.autoassigned",
        "§aYour §r§eTuning Points §r§awere auto-assigned as convenience!",
    )
    private val yourBagsGuiPattern by patternGroup.pattern(
        "gui.yourbags",
        "Your Bags",
    )
    private val powerSelectedPattern by patternGroup.pattern(
        "gui.selectedpower",
        "§aPower is selected!",
    )
    private val noPowerSelectedPattern by patternGroup.pattern(
        "gui.noselectedpower",
        "(?:§.)*Visit Maxwell in the Hub to learn",
    )
    private val accessoryBagStack by patternGroup.pattern(
        "stack.accessorybag",
        "§.Accessory Bag",
    )
    private val redstoneCollectionRequirementPattern by patternGroup.pattern(
        "collection.redstone.requirement",
        "(?:§.)*Requires (?:§.)*Redstone Collection I+(?:§.)*\\.",
    )
    //endregion

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val message = event.message.trimWhiteSpace().removeResets()

        chatPowerPattern.tryReadPower(message)
        chatPowerUnlockedPattern.tryReadPower(message)
        if (!tuningAutoAssignedPattern.matches(event.message)) return
        if (tunings.isEmpty()) return
        with(CustomScoreboard.config) {
            if (!enabled.get() || ScoreboardConfigElement.TUNING !in scoreboardEntries.get()) return
            ChatUtils.chat("Talk to Maxwell and open the Tuning Page again to update the tuning data in scoreboard.")
        }
    }

    private fun Pattern.tryReadPower(message: String) {
        val power = matchGroup(message, "power") ?: return
        currentPower = getPowerByName(
            power,
            "power" to power,
            "message" to message,
        )
    }

    // load earlier, so that other features can already use the api in this event
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return

        inInventory = thaumaturgyGuiPattern.matches(event.inventoryName)
        inTuningGui = statsTuningGuiPattern.matches(event.inventoryName)

        if (inInventory) {
            loadThaumaturgyCurrentPower(event.inventoryItems)
            loadThaumaturgyTunings(event.inventoryItems)
            loadThaumaturgyMagicalPower(event.inventoryItems)
        }

        if (yourBagsGuiPattern.matches(event.inventoryName)) {
            for (stack in event.inventoryItems.values) {
                if (accessoryBagStack.matches(stack.displayName)) processStack(stack)
            }
        }
        if (inTuningGui) {
            loadThaumaturgyTuningsFromTuning(event.inventoryItems)
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        inTuningGui = false
    }

    private fun loadThaumaturgyTuningsFromTuning(inventoryItems: Map<Int, ItemStack>) {
        tunings = buildList {
            for (stack in inventoryItems.values) {
                for (line in stack.getLore()) {
                    val tuning = statsTuningDataPattern.readTuningFromLine(line) ?: continue
                    tuning.name = tuningNamePattern.matchGroup(stack.name, "name") 
                        ?: ErrorManager.skyHanniError(
                            "found no name in thaumaturgy",
                            "stack name" to stack.name,
                            "line" to line,
                        )
                    add(tuning)
                }
            }
        }
    }

    private fun Pattern.readTuningFromLine(line: String): MaxwellTunings? {
        return matchMatcher(line) {
            val color = "§" + group("color")
            val icon = group("icon")
            val name = groupOrNull("name") ?: "<missing>"
            val value = group("amount")
            MaxwellTunings(value, color, name, icon)
        }
    }

    private fun loadThaumaturgyCurrentPower(inventoryItems: Map<Int, ItemStack>) {
        val selectedPowerStack =
            inventoryItems.values.find {
                powerSelectedPattern.matches(it.getLore().lastOrNull())
            } ?: return
        val displayName = selectedPowerStack.displayName.removeColor().trim()

        currentPower = getPowerByName(
            displayName,
            "displayName" to displayName,
            "lore" to selectedPowerStack.getLore(),
        )
    }

    private fun loadThaumaturgyTunings(inventoryItems: Map<Int, ItemStack>) {
        // Only load those rounded values if we don't have any values at all
        if (tunings.isNotEmpty()) return

        val item = inventoryItems[THAUMATURGY_TUNINGS_SLOT] ?: return
        var active = false
        tunings = buildList {
            for (line in item.getLore()) {
                if (thaumaturgyStartPattern.matches(line)) {
                    active = true
                    continue
                }
                if (!active) continue
                if (line.isEmpty()) break
                thaumaturgyDataPattern.readTuningFromLine(line)?.let {
                    add(it)
                }
            }
        }
    }

    private fun loadThaumaturgyMagicalPower(inventoryItems: Map<Int, ItemStack>) {
        val item = inventoryItems[THAUMATURGY_MP_SLOT] ?: return
        thaumaturgyMagicalPowerPattern.firstMatcher(item.getLore()) {
            magicalPower = group("mp").formatInt()
        }
    }

    private fun processStack(stack: ItemStack) {
        var foundMagicalPower = false
        for (line in stack.getLore()) {
            if (redstoneCollectionRequirementPattern.matches(line)) {
                if (magicalPower == 0 && currentPower == NO_POWER) return
                ChatUtils.chat(
                    "Seems like you don't have the Requirement for the Accessory Bag yet, " +
                        "setting power to No Power and magical power to 0.",
                )
                currentPower = NO_POWER
                magicalPower = 0
                tunings = listOf()
                return
            }

            if (noPowerSelectedPattern.matches(line)) currentPower = NO_POWER

            inventoryMPPattern.matchMatcher(line) {
                foundMagicalPower = true
                // MagicalPower is boosted in catacombs
                if (DungeonAPI.inDungeon()) return@matchMatcher

                magicalPower = group("mp").formatInt()
            }

            inventoryPowerPattern.matchMatcher(line) {
                val powerName = group("power")
                currentPower = getPowerByName(
                    powerName,
                    "displayName" to stack.displayName,
                    "lore" to stack.getLore(),
                )
            }
        }

        // If Magical Power isn't in the lore
        if (!foundMagicalPower) {
            magicalPower = 0
            tunings = listOf()
        }
    }

    @Suppress("SpreadOperator")
    private fun getPowerByName(
        name: String,
        vararg data: Pair<String, Any?>
    ): MaxwellPower {
        return getPowerByNameOrNull(name) ?: run {
            ErrorManager.logErrorWithData(
                UnknownMaxwellPower("Unkonwn power: $name"),
                "Unknown power: $name",
                *data,
                noStackTrace = true,
            )
            return NO_POWER
        }
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        val storage = storage ?: return
        if (storage.hasMigrated) return
        storage.currentPower = getPowerByNameOrNull(storage.currentPower)?.internalName
        storage.favoritePowers = storage.favoritePowers.mapNotNull { getPowerByNameOrNull(it)?.internalName }
        storage.hasMigrated = true
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && storage != null

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<MaxwellPowersJson>("MaxwellPowers")
        this.powers = buildMap {
            put(NO_POWER.internalName, NO_POWER)
            data.maxwellPowers.entries.forEach { (internalName, power) ->
                put(internalName, MaxwellPower(power.name, internalName))
            }
        }
    }

    private class UnknownMaxwellPower(message: String) : Exception(message)

}
