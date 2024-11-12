package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.events.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.features.event.diana.DianaAPI
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SlayerQuestWarning {

    private val config get() = SkyHanniMod.feature.slayer

    private var lastWeaponUse = SimpleTimeMark.farPast()
    private val voidItem = "ASPECT_OF_THE_VOID".toInternalName()
    private val endItem = "ASPECT_OF_THE_END".toInternalName()

    private val outsideRiftData = SlayerData()
    private val insideRiftData = SlayerData()

    class SlayerData {
        var currentSlayerState: String? = null
        var lastSlayerType: SlayerType? = null
    }

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        val slayerType = event.full.nextAfter("Slayer Quest")
        val slayerProgress = event.full.nextAfter("Slayer Quest", skip = 2) ?: "no slayer"
        val new = slayerProgress.removeColor()
        val slayerData = getSlayerData()

        if (slayerData.currentSlayerState == new) return

        slayerData.currentSlayerState?.let {
            change(it, new)
        }
        slayerData.currentSlayerState = new
        slayerType?.let {
            slayerData.lastSlayerType = SlayerType.getByName(it)
        }
    }

    private fun getSlayerData() = if (RiftAPI.inRift()) outsideRiftData else insideRiftData

    private fun String.inCombat() = contains("Combat") || contains("Kills")
    private fun String.inBoss() = this == "Slay the boss!"
    private fun String?.bossSlain() = this == "Boss slain!"
    private fun String.noSlayer() = this == "no slayer"

    private fun change(old: String, new: String) {
        if (!old.inCombat() && new.inCombat()) {
            needSlayerQuest = false
        }
        if (old.inBoss() && new.noSlayer()) {
            needNewQuest("The old slayer quest has failed!")
        }
        if (new.bossSlain()) {
            DelayedRun.runDelayed(2.seconds) {
                if (getSlayerData().currentSlayerState.bossSlain()) {
                    needNewQuest("You have no Auto-Slayer active!")
                }
            }
        }
    }

    private var needSlayerQuest = false
    private var lastWarning = SimpleTimeMark.farPast()
    private var currentReason = ""

    private fun needNewQuest(reason: String) {
        currentReason = reason
        needSlayerQuest = true
    }

    private fun tryWarn() {
        if (!needSlayerQuest) return
        warn("New Slayer Quest!", "Start a new slayer quest! $currentReason")
    }

    private fun warn(titleMessage: String, chatMessage: String) {
        if (!config.questWarning) return
        if (lastWarning.passedSince() < 10.seconds) return

        if (DianaAPI.isDoingDiana()) return
        // prevent warnings when mobs are hit by other players
        if (lastWeaponUse.passedSince() > 500.milliseconds) return

        lastWarning = SimpleTimeMark.now()
        ChatUtils.chat(chatMessage)

        if (config.questWarningTitle) {
            LorenzUtils.sendTitle("§e$titleMessage", 2.seconds)
        }
    }

    @SubscribeEvent
    fun onEntityHealthUpdate(event: EntityHealthUpdateEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val entity = event.entity
        if (entity.getLorenzVec().distanceToPlayer() < 6 && isSlayerMob(entity)) {
            tryWarn()
        }
    }

    private fun isSlayerMob(entity: EntityLivingBase): Boolean {
        val slayerType = SlayerAPI.getSlayerTypeForCurrentArea() ?: return false

        // workaround for rift mob that is unrelated to slayer
        if (entity.name == "Oubliette Guard") return false
        // workaround for Bladesoul in  Crimson Isle
        if (LorenzUtils.skyBlockArea == "Stronghold" && entity.name == "Skeleton") return false

        val isSlayer = slayerType.clazz.isInstance(entity)
        if (!isSlayer) return false

        SlayerAPI.activeSlayer?.let {
            if (slayerType != it) {
                val activeSlayerName = it.displayName
                val slayerName = slayerType.displayName
                SlayerAPI.latestWrongAreaWarning = SimpleTimeMark.now()
                warn(
                    "Wrong Slayer!",
                    "Wrong slayer selected! You have $activeSlayerName selected and you are in an $slayerName area!",
                )
            }
        }

        return getSlayerData().lastSlayerType == slayerType
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onItemClick(event: ItemClickEvent) {
        val internalName = event.itemInHand?.getInternalNameOrNull()

        if (event.clickType == ClickType.RIGHT_CLICK) {
            if (internalName == voidItem || internalName == endItem) {
                // ignore harmless teleportation
                return
            }
            if (internalName == null) {
                // ignore harmless right click
                return
            }
        }
        lastWeaponUse = SimpleTimeMark.now()
    }
}
