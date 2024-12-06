package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.entity.projectile.EntityFishHook

class FishingBobberInLiquidEvent(val bobber: EntityFishHook, val onWater: Boolean) : SkyHanniEvent()
