package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object HideMobNames {

    private val lastMobName = TimeLimitedCache<Int, String>(2.minutes)
    private val mobNamesHidden = mutableListOf<Int>()
    private val patterns = mutableListOf<Pattern>()

    init {
        // TODO USE SH-REPO
        addMobToHide("Zombie")
        addMobToHide("Zombie Villager")
        addMobToHide("Crypt Ghoul")

        addMobToHide("Dasher Spider")
        addMobToHide("Weaver Spider")
        addMobToHide("Splitter Spider")
        addMobToHide("Voracious Spider")
        addMobToHide("Silverfish")

        addMobToHide("Wolf")
        addMobToHide("§bHowling Spirit")
        addMobToHide("§bPack Spirit")

        addMobToHide("Enderman")
        addMobToHide("Voidling Fanatic")

        addMobToHide("Blaze") // 1.2m
        addMobToHide("Mutated Blaze") // 1.5m
        addMobToHide("Bezal") // 2m
        addMobToHide("Smoldering Blaze") // 5.5m
    }

    private fun addMobToHide(bossName: String) {
        patterns.add("§8\\[§7Lv\\d+§8] §c$bossName§r §[ae](?<min>.+)§f/§a(?<max>.+)§c❤".toPattern())
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.slayer.hideMobNames) return

        val entity = event.entity
        if (entity !is EntityArmorStand) return
        if (!entity.hasCustomName()) return

        val name = entity.name
        val id = entity.entityId
        if (lastMobName[id] == name) {
            if (id in mobNamesHidden) {
                event.cancel()
            }
            return
        }

        lastMobName[id] = name
        mobNamesHidden.remove(id)

        if (shouldNameBeHidden(name)) {
            event.cancel()
            mobNamesHidden.add(id)
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastMobName.clear()
        mobNamesHidden.clear()
    }

    private fun shouldNameBeHidden(name: String): Boolean {
        for (pattern in patterns) {
            pattern.matchMatcher(name) {
                val min = group("min")
                val max = group("max")
                if (min == max || min == "0") {
                    return true
                }
            }
        }

        return false
    }
}
