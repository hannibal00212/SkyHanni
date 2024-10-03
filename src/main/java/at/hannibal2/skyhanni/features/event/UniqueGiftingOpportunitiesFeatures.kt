package at.hannibal2.skyhanni.features.event

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.WinterAPI
import at.hannibal2.skyhanni.events.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.features.event.winter.UniqueGiftCounter
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object UniqueGiftingOpportunitiesFeatures {

    private val playerList: MutableSet<String>?
        get() = ProfileStorageData.playerSpecific?.winter?.playersThatHaveBeenGifted

    private val patternGroup = RepoPattern.group("event.winter.uniquegifts")
    private val giftedPattern by patternGroup.pattern(
        "gifted",
        "§6\\+1 Unique Gift given! To ([^§]+)§r§6!"
    )
    private val giftNamePattern by patternGroup.pattern(
        "giftname",
        "(?:WHITE|RED|GREEN)_GIFT\$"
    )

    private var holdingGift = false

    private fun hasGiftedPlayer(player: EntityPlayer) = playerList?.contains(player.name) == true

    private fun addGiftedPlayer(playerName: String) {
        playerList?.add(playerName)
    }

    private val config get() = SkyHanniMod.feature.event.winter.giftingOpportunities

    private fun isEnabled() = holdingGift

    private const val HAS_NOT_GIFTED_NAMETAG = "§a§lꤥ"
    private const val HAS_GIFTED_NAMETAG = "§c§lꤥ"

    private fun analyzeArmorStand(entity: EntityArmorStand) {
        if (!config.useArmorStandDetection) return
        if (entity.name != HAS_GIFTED_NAMETAG) return

        val matchedPlayer = EntityUtils.getEntitiesNearby<EntityPlayer>(entity.getLorenzVec(), 2.0)
            .singleOrNull { !it.isNPC() } ?: return
        addGiftedPlayer(matchedPlayer.name)
    }

    @SubscribeEvent
    fun onEntityChangeName(event: EntityCustomNameUpdateEvent) {
        val entity = event.entity as? EntityArmorStand ?: return
        analyzeArmorStand(entity)
    }

    @HandleEvent
    fun onEntityJoinWorld(event: EntityEnterWorldEvent<Entity>) {
        playerColor(event)
        val entity = event.entity as? EntityArmorStand ?: return
        analyzeArmorStand(entity)
    }

    private fun playerColor(event: EntityEnterWorldEvent<Entity>) {
        if (event.entity is EntityOtherPlayerMP) {
            val entity = event.entity
            if (entity.isNPC() || isIronman(entity) || isBingo(entity)) return

            RenderLivingEntityHelper.setEntityColor(
                entity,
                LorenzColor.DARK_GREEN.toColor().withAlpha(127)
            ) { isEnabled() && !hasGiftedPlayer(entity) }
        }
    }

    private fun isBingo(entity: EntityLivingBase) =
        !LorenzUtils.isBingoProfile && entity.displayName.formattedText.endsWith("Ⓑ§r")

    private fun isIronman(entity: EntityLivingBase) =
        !LorenzUtils.noTradeMode && entity.displayName.formattedText.endsWith("♲§r")

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        giftedPattern.matchMatcher(event.message) {
            addGiftedPlayer(group(1))
            UniqueGiftCounter.addUniqueGift()
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        holdingGift = false

        if (!LorenzUtils.inSkyBlock) return
        if (!config.enabled) return
        if (!WinterAPI.isDecember()) return

        holdingGift = !config.highlighWithGiftOnly || giftNamePattern.matches(InventoryUtils.itemInHandId.asString())
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        holdingGift = false
    }
}
