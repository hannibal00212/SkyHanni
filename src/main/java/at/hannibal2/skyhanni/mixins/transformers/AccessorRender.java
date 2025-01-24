package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Render.class)
public interface AccessorRender<T extends Entity> {

    @Invoker("bindEntityTexture")
    boolean bindEntityTexture_skyhanni(T entity);


}
