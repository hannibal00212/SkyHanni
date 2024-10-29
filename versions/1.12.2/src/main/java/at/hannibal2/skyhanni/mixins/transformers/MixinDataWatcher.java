package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.DataWatcherUpdatedEvent;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.EntityDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EntityDataManager.class)
public class MixinDataWatcher {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "setEntryValues", at = @At("TAIL"))
    public void onSetEntryValues(List<EntityDataManager.DataEntry<?>> list, CallbackInfo ci) {
        new DataWatcherUpdatedEvent(entity, list).postAndCatch();
    }
}
