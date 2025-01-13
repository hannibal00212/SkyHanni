package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.entity.EntityRemovedEvent;
import at.hannibal2.skyhanni.data.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "unloadEntities", at = @At("HEAD"))
    private void unloadEntities(Collection<Entity> entityCollection, CallbackInfo ci) {
        for (Entity entity : entityCollection) EntityData.despawnEntity(entity);
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    private void onEntityRemoved(Entity entityIn, CallbackInfo ci) {
        EntityData.despawnEntity(entityIn);
        new EntityRemovedEvent(entityIn).post();
    }
}
