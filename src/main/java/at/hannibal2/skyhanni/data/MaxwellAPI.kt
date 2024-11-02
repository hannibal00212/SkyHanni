package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.jsonobjects.repo.MaxwellPowersJson
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
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
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

@SkyHanniModule
object MaxwellAPI {

    private val storage get() = ProfileStorageData.profileSpecific

    var currentPower: String?
        get() = storage?.maxwell?.currentPower
        set(value) {
            storage?.maxwell?.currentPower = value ?: return
        }

    var magicalPower: Int?
        get() = storage?.maxwell?.magicalPower
        set(value) {
            storage?.maxwell?.magicalPower = value ?: return
        }

    var tunings: List<ThaumaturgyPowerTuning>?
        get() = storage?.maxwell?.tunings
        set(value) {
            storage?.maxwell?.tunings = value ?: return
        }

    var favoritePowers: List<String>
        get() = storage?.maxwell?.favoritePowers.orEmpty()
        set(value) {
            storage?.maxwell?.favoritePowers = value
        }

    private val NO_POWER by lazy { getPowerByNameOrNull("No Power") }
    private var powers = mutableListOf<String>()

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

    fun isThaumaturgyInventory(inventoryName: String) = thaumaturgyGuiPattern.matches(inventoryName)

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val message = event.message.trimWhiteSpace().removeResets()

        chatPowerPattern.tryReadPower(message)
        chatPowerUnlockedPattern.tryReadPower(message)
        if (!tuningAutoAssignedPattern.matches(event.message)) return
        if (tunings.isNullOrEmpty()) return
        with(CustomScoreboard.config) {
            if (!enabled.get() || ScoreboardConfigElement.TUNING !in scoreboardEntries.get()) return
            ChatUtils.chat("Talk to Maxwell and open the Tuning Page again to update the tuning data in scoreboard.")
        }
    }

    private fun Pattern.tryReadPower(message: String) {
        matchMatcher(message) {
            val power = group("power")
            currentPower = getPowerByNameOrNull(power) ?: return ErrorManager.logErrorWithData(
                UnknownMaxwellPower("Unknown power: $power"),
                "Unknown power: $power",
                "power" to power,
                "message" to message,
            )
        }
    }

    // load earlier, so that other features can already use the api in this event
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return

        if (isThaumaturgyInventory(event.inventoryName)) {
            loadThaumaturgyCurrentPower(event.inventoryItems)
            loadThaumaturgyTunings(event.inventoryItems)
            loadThaumaturgyMagicalPower(event.inventoryItems)
        }

        if (yourBagsGuiPattern.matches(event.inventoryName)) {
            for (stack in event.inventoryItems.values) {
                if (accessoryBagStack.matches(stack.displayName)) processStack(stack)
            }
        }
        if (statsTuningGuiPattern.matches(event.inventoryName)) {
            loadThaumaturgyTuningsFromTuning(event.inventoryItems)
        }
    }

    private fun loadThaumaturgyTuningsFromTuning(inventoryItems: Map<Int, ItemStack>) {
        val map = mutableListOf<ThaumaturgyPowerTuning>()
        for (stack in inventoryItems.values) {
            for (line in stack.getLore()) {
                statsTuningDataPattern.readTuningFromLine(line)?.let {
                    it.name = "§.. (?<name>.+)".toPattern().matchMatcher(stack.name) {
                        group("name")
                    } ?: ErrorManager.skyHanniError(
                        "found no name in thaumaturgy",
                        "stack name" to stack.name,
                        "line" to line,
                    )
                    map.add(it)
                }
            }
        }
        tunings = map
    }

    private fun Pattern.readTuningFromLine(line: String): ThaumaturgyPowerTuning? {
        return matchMatcher(line) {
            val color = "§" + group("color")
            val icon = group("icon")
            val name = groupOrNull("name") ?: "<missing>"
            val value = group("amount")
            ThaumaturgyPowerTuning(value, color, name, icon)
        }
    }

    private fun loadThaumaturgyCurrentPower(inventoryItems: Map<Int, ItemStack>) {
        val selectedPowerStack =
            inventoryItems.values.find {
                powerSelectedPattern.matches(it.getLore().lastOrNull())
            } ?: return
        val displayName = selectedPowerStack.displayName.removeColor().trim()

        currentPower = getPowerByNameOrNull(displayName)
            ?: return ErrorManager.logErrorWithData(
                UnknownMaxwellPower("Unknown power: $displayName"),
                "Unknown power: $displayName",
                "displayName" to displayName,
                "lore" to selectedPowerStack.getLore(),
                noStackTrace = true,
            )
    }

    private fun loadThaumaturgyTunings(inventoryItems: Map<Int, ItemStack>) {
        val tunings = tunings ?: return

        // Only load those rounded values if we don't have any values at all
        if (tunings.isNotEmpty()) return

        val item = inventoryItems[51] ?: return
        var active = false
        val map = mutableListOf<ThaumaturgyPowerTuning>()
        for (line in item.getLore()) {
            if (thaumaturgyStartPattern.matches(line)) {
                active = true
                continue
            }
            if (!active) continue
            if (line.isEmpty()) break
            thaumaturgyDataPattern.readTuningFromLine(line)?.let {
                map.add(it)
            }
        }
        this.tunings = map
    }

    private fun loadThaumaturgyMagicalPower(inventoryItems: Map<Int, ItemStack>) {
        val item = inventoryItems[48] ?: return
        item.getLore().matchFirst(thaumaturgyMagicalPowerPattern) {
            magicalPower = group("mp").formatInt()
        }
    }

    private fun processStack(stack: ItemStack) {
        var foundMagicalPower = false
        for (line in stack.getLore()) {
            redstoneCollectionRequirementPattern.matchMatcher(line) {
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
                val power = group("power")
                currentPower = getPowerByNameOrNull(power)
                    ?: return@matchMatcher ErrorManager.logErrorWithData(
                        UnknownMaxwellPower("Unknown power: ${stack.displayName}"),
                        "Unknown power: ${stack.displayName}",
                        "displayName" to stack.displayName,
                        "lore" to stack.getLore(),
                        noStackTrace = true,
                    )
            }
        }

        // If Magical Power isn't in the lore
        if (!foundMagicalPower) {
            magicalPower = 0
            tunings = listOf()
        }
    }

    fun getPowerByNameOrNull(name: String) = powers.find { it == name }

    private fun isEnabled() = LorenzUtils.inSkyBlock && storage != null

    // Load powers from repo
    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<MaxwellPowersJson>("MaxwellPowers")
        powers = data.powers
    }

    class UnknownMaxwellPower(message: String) : Exception(message)

    class ThaumaturgyPowerTuning(
        @Expose val value: String,
        @Expose val color: String,
        @Expose var name: String,
        @Expose val icon: String,
    )
}
