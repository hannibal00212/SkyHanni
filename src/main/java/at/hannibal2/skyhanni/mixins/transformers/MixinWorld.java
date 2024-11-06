package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.entity.EntityRemovedEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld {

    // Source: Aton addons https://github.com/FloppaCoding/AtonAddons/blob/main/src/main/java/atonaddons/mixins/MixinWorld.java
    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(Entity entityIn, CallbackInfo ci) {
        new EntityRemovedEvent(entityIn).post();
    }
}
