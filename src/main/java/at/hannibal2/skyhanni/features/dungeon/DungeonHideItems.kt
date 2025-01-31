package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.entity.EntityMoveEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.holdingSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import at.hannibal2.skyhanni.utils.compat.getStandHelmet
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.EnumParticleTypes

@SkyHanniModule
object DungeonHideItems {

    private val config get() = SkyHanniMod.feature.dungeon.objectHider

    private val hideParticles = mutableMapOf<EntityArmorStand, Long>()
    private val movingSkeletonSkulls = mutableMapOf<EntityArmorStand, Long>()

    private val SOUL_WEAVER_HIDER by lazy { SkullTextureHolder.getTexture("DUNGEONS_SOUL_WEAVER") }
    private val BLESSING_TEXTURE by lazy { SkullTextureHolder.getTexture("DUNGEONS_BLESSING") }
    private val REVIVE_STONE_TEXTURE by lazy { SkullTextureHolder.getTexture("DUNGEONS_REVIVE_STONE") }
    private val PREMIUM_FLESH_TEXTURE by lazy { SkullTextureHolder.getTexture("DUNGEONS_PREMIUM_FLESH") }
    private val ABILITY_ORB_TEXTURE by lazy { SkullTextureHolder.getTexture("DUNGEONS_ABILITY_ORB") }
    private val SUPPORT_ORB_TEXTURE by lazy { SkullTextureHolder.getTexture("DUNGEONS_SUPPORT_ORB") }
    private val DAMAGE_ORB_TEXTURE by lazy { SkullTextureHolder.getTexture("DUNGEONS_DAMAGE_ORB") }
    private val HEALER_FAIRY_TEXTURE by lazy { SkullTextureHolder.getTexture("DUNGEONS_HEALER_FAIRY") }

    private fun isSkeletonSkull(entity: EntityArmorStand): Boolean = entity.getStandHelmet()?.cleanName() == "Skeleton Skull"

    @HandleEvent(onlyOnIsland = IslandType.CATACOMBS)
    fun onCheckRender(event: CheckRenderEntityEvent<Entity>) {
        val entity = event.entity

        if (entity is EntityItem) {
            val stack = entity.entityItem
            if (config.hideReviveStone && stack.cleanName() == "Revive Stone") {
                event.cancel()
            }

            if (config.hideJournalEntry && stack.cleanName() == "Journal Entry") {
                event.cancel()
            }
        }

        if (entity !is EntityArmorStand) return

        val head = entity.getStandHelmet()
        val skullTexture = head?.getSkullTexture()
        if (config.hideSuperboomTNT) {
            if (entity.name.startsWith("§9Superboom TNT")) {
                event.cancel()
            }

            if (head != null && head.cleanName() == "Superboom TNT") {
                event.cancel()
                hideParticles[entity] = System.currentTimeMillis()
            }
        }

        if (config.hideBlessing) {
            if (entity.name.startsWith("§dBlessing of ")) {
                event.cancel()
            }

            if (skullTexture == BLESSING_TEXTURE) {
                event.cancel()
            }
        }

        if (config.hideReviveStone) {
            if (entity.name == "§6Revive Stone") {
                event.cancel()
            }

            if (skullTexture == REVIVE_STONE_TEXTURE) {
                event.cancel()
                hideParticles[entity] = System.currentTimeMillis()
            }
        }

        if (config.hidePremiumFlesh) {
            if (entity.name == "§9Premium Flesh") {
                event.cancel()
                hideParticles[entity] = System.currentTimeMillis()
            }

            if (skullTexture == PREMIUM_FLESH_TEXTURE) {
                event.cancel()
            }
        }

        if (isSkeletonSkull(entity)) {
            EntityMovementData.addToTrack(entity)
            if (config.hideSkeletonSkull) {
                val lastMove = movingSkeletonSkulls.getOrDefault(entity, 0)
                if (lastMove + 100 > System.currentTimeMillis()) {
                    return
                }
                event.cancel()
            }
        }

        if (config.hideHealerOrbs) {
            when {
                entity.name.startsWith("§c§lDAMAGE §e") -> event.cancel()
                entity.name.startsWith("§c§lABILITY DAMAGE §e") -> event.cancel()
                entity.name.startsWith("§a§lDEFENSE §e") -> event.cancel()
            }

            when (skullTexture) {
                ABILITY_ORB_TEXTURE,
                SUPPORT_ORB_TEXTURE,
                DAMAGE_ORB_TEXTURE,
                -> {
                    event.cancel()
                    hideParticles[entity] = System.currentTimeMillis()
                    return
                }
            }
        }

        if (config.hideHealerFairy) {
            if (entity.holdingSkullTexture(HEALER_FAIRY_TEXTURE)) {
                event.cancel()
                return
            }
        }

        if (config.hideSoulweaverSkulls) {
            if (skullTexture == SOUL_WEAVER_HIDER) {
                event.cancel()
                return
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.CATACOMBS)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!config.hideSuperboomTNT && !config.hideReviveStone) return

        val packetLocation = event.location
        for (armorStand in hideParticles.filter { it.value + 100 > System.currentTimeMillis() }.map { it.key }) {
            val distance = packetLocation.distance(armorStand.getLorenzVec())
            if (distance < 2) {
                if (event.type == EnumParticleTypes.FIREWORKS_SPARK) {
                    event.cancel()
                }
                if (event.type == EnumParticleTypes.REDSTONE) {
                    event.cancel()
                }
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.CATACOMBS)
    fun onEntityMove(event: EntityMoveEvent<EntityArmorStand>) {
        val entity = event.entity

        if (isSkeletonSkull(entity)) {
            movingSkeletonSkulls[entity] = System.currentTimeMillis()
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                entity,
                LorenzColor.GOLD.toColor().addAlpha(60),
            ) { shouldColorMovingSkull(entity) }
        }
    }

    private fun shouldColorMovingSkull(entity: Entity) =
        SkyHanniMod.feature.dungeon.highlightSkeletonSkull && movingSkeletonSkulls[entity]?.let {
            it + 200 > System.currentTimeMillis()
        } ?: false

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        hideParticles.clear()
        movingSkeletonSkulls.clear()
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "dungeon.hideSuperboomTNT", "dungeon.objectHider.hideSuperboomTNT")
        event.move(3, "dungeon.hideBlessing", "dungeon.objectHider.hideBlessing")
        event.move(3, "dungeon.hideReviveStone", "dungeon.objectHider.hideReviveStone")
        event.move(3, "dungeon.hidePremiumFlesh", "dungeon.objectHider.hidePremiumFlesh")
        event.move(3, "dungeon.hideJournalEntry", "dungeon.objectHider.hideJournalEntry")
        event.move(3, "dungeon.hideSkeletonSkull", "dungeon.objectHider.hideSkeletonSkull")
        event.move(3, "dungeon.hideHealerOrbs", "dungeon.objectHider.hideHealerOrbs")
        event.move(3, "dungeon.hideHealerFairy", "dungeon.objectHider.hideHealerFairy")
    }
}
