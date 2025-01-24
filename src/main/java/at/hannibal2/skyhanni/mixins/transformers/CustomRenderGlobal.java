package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderGlobal.class)
public interface CustomRenderGlobal {
    @Accessor("entityOutlineFramebuffer")
    Framebuffer getEntityOutlineFramebuffer_skyhanni();

    @Accessor("entityOutlineShader")
    ShaderGroup getEntityOutlineShader_skyhanni();

}