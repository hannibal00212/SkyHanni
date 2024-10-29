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

/**
 * Note: This mixin is changed in 1.12 so any changes to this mixin should also be applied to the mixin in the versions/1.12 folder
 */
@Mixin(DataWatcher.class)
public class MixinDataWatcher {
    @Shadow
    @Final
    private Entity owner;

    @Inject(method = "updateWatchedObjectsFromList", at = @At("TAIL"))
    public void onWhatever(List<DataWatcher.WatchableObject> list, CallbackInfo ci) {
        new DataWatcherUpdatedEvent(owner, list).postAndCatch();
    }
}
