package at.hannibal2.skyhanni.features.combat.mobs

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.getBlockInHand
import at.hannibal2.skyhanni.utils.EntityUtils.hasNameTagWith
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityCaveSpider
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.init.Blocks

@SkyHanniModule
object MobHighlight {

    private val config get() = SkyHanniMod.feature.combat.mobs
    private var arachne: EntityLivingBase? = null

    @HandleEvent(onlyOnSkyblock = true)
    fun onEntityHealthUpdate(event: EntityHealthUpdateEvent) {

        val entity = event.entity
        val baseMaxHealth = entity.baseMaxHealth
        if (config.corruptedMobHighlight && event.health == baseMaxHealth * 3) {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                entity,
                LorenzColor.DARK_PURPLE.toColor().addAlpha(127),
            ) { config.corruptedMobHighlight }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {

        val entity = event.entity
        val maxHealth = event.maxHealth
        if (config.arachneKeeperHighlight && (maxHealth == 3_000 || maxHealth == 12_000) && entity is EntityCaveSpider) {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                entity,
                LorenzColor.DARK_BLUE.toColor().addAlpha(127),
            ) { config.arachneKeeperHighlight }
        }

        if (config.corleoneHighlighter && maxHealth == 1_000_000 && entity is EntityOtherPlayerMP && entity.name == "Team Treasurite") {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                entity,
                LorenzColor.DARK_PURPLE.toColor().addAlpha(127),
            ) { config.corleoneHighlighter }
        }

        if (entity is EntityEnderman) {
            val isZealot = maxHealth == 13_000 || maxHealth == 13_000 * 4 // runic
            val isBruiser = maxHealth == 65_000 || maxHealth == 65_000 * 4 // runic

            if (!(isZealot || isBruiser)) return

            if (config.zealotBruiserHighlighter) {
                RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                    entity,
                    LorenzColor.DARK_AQUA.toColor().addAlpha(127),
                ) { config.zealotBruiserHighlighter }
            }

            val heldItem = entity.getBlockInHand()?.block
            if (config.chestZealotHighlighter && heldItem == Blocks.ender_chest) {
                RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                    entity,
                    LorenzColor.GREEN.toColor().addAlpha(127),
                ) { config.chestZealotHighlighter }
            }

            if (config.specialZealotHighlighter && heldItem == Blocks.end_portal_frame) {
                RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                    entity,
                    LorenzColor.DARK_RED.toColor().addAlpha(50),
                ) { config.specialZealotHighlighter }
            }
        }

        if (entity is EntitySpider) {
            checkArachne(entity)
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.lineToArachne) return

        val arachne = arachne ?: return
        if (arachne.isDead || arachne.health <= 0) {
            this.arachne = null
            return
        }

        if (arachne.distanceToPlayer() > 10) return

        event.drawLineToEye(
            arachne.getLorenzVec().up(),
            LorenzColor.RED.toColor(),
            config.lineToArachneWidth,
            true,
        )
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        arachne = null
    }

    private fun checkArachne(entity: EntitySpider) {
        if (!config.arachneBossHighlighter && !config.lineToArachne) return

        if (!entity.hasNameTagWith(1, "[§7Lv300§8] §cArachne") &&
            !entity.hasNameTagWith(1, "[§7Lv300§8] §lArachne") &&
            !entity.hasNameTagWith(1, "[§7Lv500§8] §cArachne") &&
            !entity.hasNameTagWith(1, "[§7Lv500§8] §lArachne")
        ) return

        if (entity is EntityCaveSpider) {
            markArachneMinis(entity)
        } else if (entity.baseMaxHealth == 20_000 || entity.baseMaxHealth == 100_000) {
            this.arachne = entity
            markArachne(entity)
        }
    }

    private fun markArachneMinis(entity: EntityLivingBase) {
        RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
            entity,
            LorenzColor.GOLD.toColor().addAlpha(50),
        ) { config.arachneBossHighlighter }
    }

    private fun markArachne(entity: EntityLivingBase) {
        RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
            entity,
            LorenzColor.RED.toColor().addAlpha(50),
        ) { config.arachneBossHighlighter }
    }
}
