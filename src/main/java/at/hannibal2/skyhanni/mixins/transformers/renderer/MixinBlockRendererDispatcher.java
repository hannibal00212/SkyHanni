package at.hannibal2.skyhanni.mixins.transformers.renderer;

import at.hannibal2.skyhanni.mixins.hooks.BlockRendererDispatcherHookKt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Taken from Skytils
@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher implements IResourceManagerReloadListener {
    @Inject(method = "getModelFromBlockState", at = @At("RETURN"), cancellable = true)
    private void modifyGetModelFromBlockState(IBlockState state, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<IBakedModel> cir) {
        BlockRendererDispatcherHookKt.modifyGetModelFromBlockState((BlockRendererDispatcher) (Object) this, state, worldIn, pos, cir);
    }
}
