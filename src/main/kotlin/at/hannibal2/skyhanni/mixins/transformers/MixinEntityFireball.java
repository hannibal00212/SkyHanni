package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.SkyHanniMod;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFireball.class)
public class MixinEntityFireball {

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void onLivingUpdate(World world, EnumParticleTypes particleType, double x, double y, double z, double xOffset, double yOffset, double zOffset, int[] parameters) {
        if (!SkyHanniMod.feature.misc.particleHiders.hideFireballParticles) {
            world.spawnParticle(particleType, x, y, z, xOffset, yOffset, zOffset, parameters);
        }
    }
}
