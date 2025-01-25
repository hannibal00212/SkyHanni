package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.dungeon.DungeonBossRoomEnterEvent
import at.hannibal2.skyhanni.events.dungeon.DungeonCompleteEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzColor.Companion.toLorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.MobUtils.mob
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.RenderUtils.exactBoundingBox
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.compat.EffectsCompat
import at.hannibal2.skyhanni.utils.compat.EffectsCompat.Companion.activePotionEffect
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks

@SkyHanniModule
object DungeonLividFinder {
    private val config get() = SkyHanniMod.feature.dungeon.lividFinder
    private val blockLocation = LorenzVec(6, 109, 43)

    private val isBlind by RecalculatingValue(2.ticks, ::isCurrentlyBlind)

    var livid: Mob? = null
        private set
    private var lividArmorStandId: Int? = null

    val lividEntityOrArmorstand: Entity?
        get() = livid?.baseEntity ?: lividArmorStandId?.let { EntityUtils.getEntityByID(it) }

    private var fakeLivids = mutableSetOf<Mob>()

    private var color: LorenzColor? = null

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (!inLividBossRoom()) return
        val mob = event.mob
        if (mob.name != "Livid" && mob.name != "Real Livid") return
        if (mob.baseEntity !is EntityOtherPlayerMP) return

        val lividColor = color
        val isCorrectLivid = if (lividColor == null) false else mob.isLividColor(lividColor)

        if (lividColor == null) {
            fakeLivids += mob
            return
        }

        if (isCorrectLivid) {
            livid = mob
            lividArmorStandId = mob.armorStand?.entityId
            // When the real livid dies at the same time as a fake livid, Hypixel despawns the player entity,
            // and makes it impossible to get the mob of the real livid again.

            ChatUtils.debug("Livid found: $lividColor§7 | $lividArmorStandId")
            if (config.enabled.get()) mob.highlight(lividColor.toColor())
        } else fakeLivids += mob
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.enabled.onToggle {
            reloadHighlight()
        }
    }

    private fun reloadHighlight() {
        val enabled = config.enabled.get()

        if (enabled) {
            livid?.highlight(color?.toColor())
        } else {
            livid?.highlight(null)
        }
    }

    @HandleEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!inLividBossRoom()) return
        if (event.location != blockLocation) return
        if (event.location.getBlockAt() != Blocks.wool) return

        val newColor = event.newState.getValue(BlockStainedGlass.COLOR).toLorenzColor()
        color = newColor
        ChatUtils.debug("newColor! $newColor")

        val lividSet = fakeLivids + livid

        for (mob in lividSet) {
            if (mob == null) continue
            if (mob.isLividColor(LorenzColor.RED) && newColor != LorenzColor.RED) {
                if (mob == livid) {
                    livid = null
                    lividArmorStandId = null
                }
                mob.highlight(null)
                fakeLivids += mob
                continue
            }

            if (mob.isLividColor(newColor)) {
                livid = mob
                lividArmorStandId = mob.armorStand?.entityId
                ChatUtils.debug("Livid found: $newColor§7 | $lividArmorStandId")
                if (config.enabled.get()) mob.highlight(newColor.toColor())
                fakeLivids -= mob
                continue
            }
        }
    }

    @HandleEvent
    fun onBossStart(event: DungeonBossRoomEnterEvent) {
        if (DungeonApi.getCurrentBoss() != DungeonFloor.F5) return
        color = LorenzColor.RED
    }

    @HandleEvent
    fun onBossEnd(event: DungeonCompleteEvent) {
        color = null
        livid = null
        lividArmorStandId = null
        fakeLivids.clear()
    }

    @HandleEvent
    fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        when (event.mob) {
            livid -> livid = null
            in fakeLivids -> fakeLivids -= event.mob
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        color = null
        lividArmorStandId = null
    }

    @HandleEvent(onlyOnIsland = IslandType.CATACOMBS)
    fun onCheckRender(event: CheckRenderEntityEvent<Entity>) {
        if (!inLividBossRoom() || !config.hideWrong) return
        if (livid == null && lividArmorStandId == null) return // in case livid detection fails, don't hide anything
        if (event.entity.mob in fakeLivids) event.cancel()
    }

    private fun isCurrentlyBlind() = (Minecraft.getMinecraft().thePlayer?.activePotionEffect(EffectsCompat.BLINDNESS)?.duration ?: 0) > 10

    private fun Mob.isLividColor(color: LorenzColor): Boolean {
        val chatColor = color.getChatColor()
        return armorStand?.name?.startsWith("$chatColor﴾ $chatColor§lLivid") == true
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!inLividBossRoom() || !config.enabled.get()) return
        if (isBlind) return

        val entity = lividEntityOrArmorstand ?: return
        val lorenzColor = color ?: return

        val location = event.exactLocation(entity)
        val boundingBox = event.exactBoundingBox(entity)

        event.drawDynamicText(location, lorenzColor.getChatColor() + "Livid", 1.5)

        val color = lorenzColor.toColor()
        event.drawFilledBoundingBoxNea(boundingBox, color, 0.5f)

        if (location.distanceSqToPlayer() > 50) {
            event.drawLineToEye(location.add(x = 0.5, z = 0.5), color, 3, true)
        }
    }

    private fun inLividBossRoom() = DungeonApi.inBossRoom && DungeonApi.getCurrentBoss() == DungeonFloor.F5

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Livid Finder")

        if (!inLividBossRoom()) {
            event.addIrrelevant {
                add("Not in Livid Boss")
                add("currentBoss: ${DungeonApi.getCurrentBoss()}")
                add("inBossRoom: ${DungeonApi.inBossRoom}")
            }
            return
        }

        // TODO either hide if setting is disabled, or include the info if setting is enabled
        event.addData {
            add("inBoss: ${inLividBossRoom()}")
            add("isBlind: $isBlind")
            add("blockColor: ${blockLocation.getBlockStateAt()}")
            add("livid: '${livid?.armorStand?.name}'")
            add("lividArmorStandID: $lividArmorStandId")
            add("color: ${color?.name}")
        }
    }
}
