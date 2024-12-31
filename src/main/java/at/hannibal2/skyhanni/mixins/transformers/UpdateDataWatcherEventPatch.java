package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.DataWatcherUpdatedEvent;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DataWatcher.class)
public class UpdateDataWatcherEventPatch {
    @Shadow
    @Final
    private Entity owner;

    @Inject(method = "updateWatchedObjectsFromList", at = @At("TAIL"))
    public void onWhatever(List<DataWatcher.WatchableObject> list, CallbackInfo ci) {
        new DataWatcherUpdatedEvent(owner, list).post();
    }
}
