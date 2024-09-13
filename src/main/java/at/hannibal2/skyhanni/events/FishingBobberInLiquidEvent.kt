package at.hannibal2.skyhanni.events

import net.minecraft.entity.projectile.EntityFishHook

class FishingBobberInLiquidEvent(val bobber: EntityFishHook, val onWater: Boolean) : LorenzEvent()
