package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.jsonobjects.repo.ArrowTypeJson
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.QuiverUpdateEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getExtraAttributes
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemBow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@SkyHanniModule
object QuiverAPI {
    private val storage get() = ProfileStorageData.profileSpecific
    var currentArrow: ArrowType?
        get() = storage?.arrows?.currentArrow?.toInternalName()?.let { getArrowByNameOrNull(it) } ?: NONE_ARROW_TYPE
        set(value) {
            storage?.arrows?.currentArrow = value?.toString() ?: return
        }
    var arrowAmount: MutableMap<NEUInternalName, Int>
        get() = storage?.arrows?.arrowAmount ?: mutableMapOf()
        set(value) {
            storage?.arrows?.arrowAmount = value
        }
    var currentAmount: Int
        get() = currentArrow?.amount ?: 0
        set(value) {
            currentArrow?.amount = value
        }
    var ArrowType.amount: Int
        get() = arrowAmount[this.internalName] ?: 0
        set(value) {
            arrowAmount[this.internalName] = value
        }

    private var arrows: List<ArrowType> = listOf()

    var wearingSkeletonMasterChestplate = false
        private set
    private var hasBow = false

    const val MAX_ARROW_AMOUNT = 2880
    private val SKELETON_MASTER_CHESTPLATE = "SKELETON_MASTER_CHESTPLATE".toInternalName()

    var NONE_ARROW_TYPE: ArrowType? = null
    private var FLINT_ARROW_TYPE: ArrowType? = null

    private val group = RepoPattern.group("data.quiver")
    private val chatGroup = group.group("chat")
    private val selectPattern by chatGroup.pattern("select", "§aYou set your selected arrow type to §.(?<arrow>.*)§a!")
    private val fillUpJaxPattern by chatGroup.pattern(
        "fillupjax",
        "(?:§.)*Jax forged (?:§.)*(?<type>.*?)(?:§.)* x(?<amount>[\\d,]+)(?: (?:§.)*for (?:§.)*(?<coins>[\\d,]+) Coins)?(?:§.)*!",
    )

    /**
     * REGEX-TEST: §aYou filled your quiver with §f1,253 §aextra arrows!
     */
    private val fillUpPattern by chatGroup.pattern(
        "fillup",
        "§aYou filled your quiver with §f(?<flintAmount>.*) §aextra arrows!",
    )
    private val clearedPattern by chatGroup.pattern(
        "cleared",
        "§aCleared your quiver!|§c§lYour quiver is now completely empty!",
    )
    private val arrowRanOutPattern by chatGroup.pattern(
        "ranout",
        "§c§lQUIVER! §cYou have run out of §f(?<type>.*)s§c!",
    )
    private val arrowResetPattern by chatGroup.pattern("arrowreset", "§cYour favorite arrow has been reset!")
    private val addedToQuiverPattern by chatGroup.pattern(
        "addedtoquiver",
        "(?:§.)*You've added (?:§.)*(?<type>.*) x(?<amount>.*) (?:§.)*to your quiver!",
    )

    // Bows that don't use the players arrows, checked using the SkyBlock ID
    /**
     * REGEX-TEST: BOSS_SPIRIT_BOW
     * REGEX-TEST: CRYPT_BOW
     */
    private val fakeBowsPattern by group.pattern("fakebows", "^(?:BOSS_SPIRIT_BOW|CRYPT_BOW)$")
    private val quiverInventoryNamePattern by group.pattern("quivername", "^Quiver$")

    /**
     * REGEX-TEST: §7Active Arrow: §fFlint Arrow §7(§e2880§7)
     */
    private val quiverInventoryPattern by group.pattern(
        "quiver.inventory",
        "§7Active Arrow: §.(?<type>.*) §7\\(§e(?<amount>.*)§7\\)",
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val message = event.message.trimWhiteSpace().removeResets()

        selectPattern.matchMatcher(message) {
            val type = group("arrow")
            currentArrow = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )
            postUpdateEvent()
            return
        }

        arrowRanOutPattern.matchMatcher(message) {
            val type = group("type")
            val ranOutType = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )
            ranOutType.amount = 0
            postUpdateEvent(ranOutType)
        }

        fillUpJaxPattern.matchMatcher(message) {
            val type = group("type")
            val amount = group("amount").formatInt()
            val filledUpType = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )

            filledUpType.amount += amount
            if (filledUpType == currentArrow) {
                postUpdateEvent()
            }
            return
        }

        fillUpPattern.matchMatcher(message) {
            val flintAmount = group("flintAmount").formatInt()

            FLINT_ARROW_TYPE?.let { it.amount += flintAmount }
            if (currentArrow == FLINT_ARROW_TYPE) {
                postUpdateEvent()
            }
            return
        }

        addedToQuiverPattern.matchMatcher(message) {
            val type = group("type")
            val amount = group("amount").formatInt()
            val filledUpType = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )

            filledUpType.amount += amount
            if (filledUpType == currentArrow) {
                postUpdateEvent()
            }
            return
        }

        clearedPattern.matchMatcher(message) {
            currentAmount = 0
            arrowAmount.clear()

            postUpdateEvent()
            return
        }

        arrowResetPattern.matchMatcher(message) {
            currentArrow = NONE_ARROW_TYPE
            currentAmount = 0

            postUpdateEvent()
            return
        }
    }

    @SubscribeEvent
    fun onInventoryFullyLoaded(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (!quiverInventoryNamePattern.matches(event.inventoryName)) return

        // clear to prevent duplicates
        currentAmount = 0
        arrowAmount.clear()

        val stacks = event.inventoryItems
        for (stack in stacks.values) {
            if (stack.getItemCategoryOrNull() != ItemCategory.ARROW) continue
            val arrow = stack.getInternalNameOrNull() ?: continue
            val arrowType = getArrowByNameOrNull(arrow) ?: continue
            arrowType.amount += stack.stackSize
        }
    }

    @SubscribeEvent
    fun onInventoryUpdate(event: OwnInventoryItemUpdateEvent) {
        if (!isEnabled() && event.slot != 44) return
        val stack = event.itemStack
        if (stack.getExtraAttributes()?.hasKey("quiver_arrow") == true) {
            for (line in stack.getLore()) {
                quiverInventoryPattern.matchMatcher(line) {
                    val type = group("type")
                    val amount = group("amount").formatInt()
                    val currentArrowType = getArrowByNameOrNull(type)
                        ?: return ErrorManager.logErrorWithData(
                            UnknownArrowType("Unknown arrow type: $type"),
                            "Unknown arrow type: $type",
                            "line" to line,
                        )
                    if (currentArrowType != currentArrow || amount != currentAmount) {
                        currentArrow = currentArrowType
                        currentAmount = amount
                        postUpdateEvent()
                    }
                }
            }
        }
    }

    fun Int.asArrowPercentage() = ((this.toFloat() / MAX_ARROW_AMOUNT) * 100).roundTo(1)

    fun hasBowInInventory() = hasBow

    fun isHoldingBow(): Boolean {
        InventoryUtils.getItemInHand()?.let {
            return it.item is ItemBow && !fakeBowsPattern.matches(it.getInternalName().asString())
        } ?: return false
    }

    fun getArrowByNameOrNull(name: String): ArrowType? {
        return arrows.firstOrNull { it.arrow == name }
    }

    fun getArrowByNameOrNull(internalName: NEUInternalName): ArrowType? {
        return arrows.firstOrNull { it.internalName == internalName }
    }

    private fun NEUInternalName.asArrowTypeOrNull() = getArrowByNameOrNull(this)

    fun isEnabled() = LorenzUtils.inSkyBlock && storage != null

    private fun checkBowInventory() {
        hasBow = InventoryUtils.getItemsInOwnInventory().any {
            it.item is ItemBow && !fakeBowsPattern.matches(it.getInternalName().asString())
        }
    }

    private fun checkChestplate() {
        val wasWearing = wearingSkeletonMasterChestplate
        wearingSkeletonMasterChestplate =
            InventoryUtils.getChestplate()?.getInternalName() == SKELETON_MASTER_CHESTPLATE
        if (wasWearing != wearingSkeletonMasterChestplate) {
            postUpdateEvent()
        }
    }

    private fun postUpdateEvent(arrowType: ArrowType? = currentArrow) {
        QuiverUpdateEvent(arrowType, currentAmount).postAndCatch()
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (event.repeatSeconds(2)) {
            checkChestplate()
            checkBowInventory()
        }
    }

    // Load arrows from repo
    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val arrowData = event.getConstant<ArrowTypeJson>("ArrowTypes")
        arrows = arrowData.arrows.map { ArrowType(it.value.arrow, it.key.toInternalName()) }

        NONE_ARROW_TYPE = getArrowByNameOrNull("NONE".toInternalName())
        FLINT_ARROW_TYPE = getArrowByNameOrNull("ARROW".toInternalName())
    }

    class UnknownArrowType(message: String) : Exception(message)
}
