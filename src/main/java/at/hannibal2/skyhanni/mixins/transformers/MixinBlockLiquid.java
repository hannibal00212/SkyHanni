package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.features.fishing.LavaReplacement;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BlockFluidRenderer.class)
public abstract class MixinBlockLiquid {

    @Shadow
    private TextureAtlasSprite[] atlasSpritesWater;

    @ModifyVariable(
        method = "renderFluid",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockLiquid;getMaterial()Lnet/minecraft/block/material/Material;"),
        ordinal = 0
    )
    TextureAtlasSprite[] replaceRenderedFluid(TextureAtlasSprite[] value) {
        if (LavaReplacement.isActive()) {
            return this.atlasSpritesWater;
        }
        return value;
    }
}
