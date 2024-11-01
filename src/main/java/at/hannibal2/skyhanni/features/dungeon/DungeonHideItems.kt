package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.EntityMoveEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DungeonHideItems {

    private val config get() = SkyHanniMod.feature.dungeon.objectHider

    private val hideParticles = mutableMapOf<EntityArmorStand, Long>()
    private val movingSkeletonSkulls = mutableMapOf<EntityArmorStand, Long>()

    // TODO put in skull data repo part
    @Suppress("MaxLineLength")
    private const val SOUL_WEAVER_HIDER =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0="
    @Suppress("MaxLineLength")
    private const val BLESSING_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkzZTIwNjg2MTc4NzJjNTQyZWNkYTFkMjdkZjRlY2U5MWM2OTk5MDdiZjMyN2M0ZGRiODUzMDk0MTJkMzkzOSJ9fX0="

    @Suppress("MaxLineLength")
    private const val REVIVE_STONE_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNzZjYzIyZTdjMmFiOWM1NDBkMTI0NGVhZGJhNTgxZjVkZDllMThmOWFkYWNmMDUyODBhNWI0OGI4ZjYxOCJ9fX0K"

    @Suppress("MaxLineLength")
    private const val PREMIUM_FLESH_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE3NWU4YjA0NGM3MjAxYTRiMmU4NTZiZTRmYzMxNmE1YWFlYzY2NTc2MTY5YmFiNTg3MmE4ODUzNGI4MDI1NiJ9fX0K"

    @Suppress("MaxLineLength")
    private const val ABILITY_ORB_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTAxZTA0MGNiMDFjZjJjY2U0NDI4MzU4YWUzMWQyZTI2NjIwN2M0N2NiM2FkMTM5NzA5YzYyMDEzMGRjOGFkNCJ9fX0="
    @Suppress("MaxLineLength")
    private const val SUPPORT_ORB_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTMxYTRmYWIyZjg3ZGI1NDMzMDEzNjUxN2I0NTNhYWNiOWQ3YzBmZTc4NDMwMDcwOWU5YjEwOWNiYzUxNGYwMCJ9fX0="
    @Suppress("MaxLineLength")
    private const val DAMAGE_ORB_TEXTURE =
        "eyJ0aW1lc3RhbXAiOjE1NzQ5NTEzMTkwNDQsInByb2ZpbGVJZCI6IjE5MjUyMWI0ZWZkYjQyNWM4OTMxZjAyYTg0OTZlMTFiIiwicHJvZmlsZU5hbWUiOiJTZXJpYWxpemFibGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FiODZkYTJlMjQzYzA1ZGMwODk4YjBjYzVkM2U2NDg3NzE3MzE3N2UwYTIzOTQ0MjVjZWMxMDAyNTljYjQ1MjYifX19"

    @Suppress("MaxLineLength")
    private const val HEALER_FAIRY_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19"

    private fun isSkeletonSkull(entity: EntityArmorStand): Boolean {
        val itemStack = entity.inventory[4]
        return itemStack != null && itemStack.cleanName() == "Skeleton Skull"
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!DungeonAPI.inDungeon()) return

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

        val head = entity.inventory[4]
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
            // Healer Fairy texture is stored in id 0, not id 4 for some reasons.
            if (entity.inventory[0]?.getSkullTexture() == HEALER_FAIRY_TEXTURE) {
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

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!DungeonAPI.inDungeon()) return
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

    @SubscribeEvent
    fun onEntityMove(event: EntityMoveEvent) {
        if (!DungeonAPI.inDungeon()) return

        val entity = event.entity
        if (entity !is EntityArmorStand) return

        if (isSkeletonSkull(entity)) {
            movingSkeletonSkulls[entity] = System.currentTimeMillis()
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                entity,
                LorenzColor.GOLD.toColor().withAlpha(60)
            ) { shouldColorMovingSkull(entity) }
        }
    }

    private fun shouldColorMovingSkull(entity: Entity) =
        SkyHanniMod.feature.dungeon.highlightSkeletonSkull && movingSkeletonSkulls[entity]?.let {
            it + 200 > System.currentTimeMillis()
        } ?: false

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        hideParticles.clear()
        movingSkeletonSkulls.clear()
    }

    @SubscribeEvent
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
