package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.features.misc.visualwords.ModifyVisualWords;
import at.hannibal2.skyhanni.mixins.hooks.FontRendererHook;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    /**
     * Inject call to {@link FontRendererHook#beginChromaRendering(String, boolean)} as first call
     */
    @Inject(method = "renderStringAtPos", at = @At("HEAD"))
    public void beginRenderString(String text, boolean shadow, CallbackInfo ci) {
        FontRendererHook.beginChromaRendering(text, shadow);
    }

    /**
     * Modify color code constant to add Z color code
     */
    @ModifyConstant(method = "renderStringAtPos", constant = @Constant(stringValue = "0123456789abcdefklmnor"))
    public String insertZColorCode(String constant) {
        return FontRendererHook.insertZColorCode(constant);
    }

    /**
     * Inject call to {@link FontRendererHook#restoreChromaState()} after 1st and 3rd fontrenderer.italicStyle = ___ call
     */
    @Inject(method = "renderStringAtPos", at = {
        @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/FontRenderer;italicStyle:Z", ordinal = 0, shift = At.Shift.AFTER),
        @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/FontRenderer;italicStyle:Z", ordinal = 2, shift = At.Shift.AFTER)})
    public void insertRestoreChromaState(CallbackInfo ci) {
        FontRendererHook.restoreChromaState();
    }

    @Shadow
    protected abstract void resetStyles();

    /**
     * Inject call to {@link FontRendererHook#toggleChromaOn()} to check for Z color code index and if so,
     * reset styles and toggle chroma on
     */
    @Inject(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;indexOf(I)I", ordinal = 0, shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    public void toggleChromaCondition(String text, boolean shadow, CallbackInfo ci, int i, char c0, int i1) {
        if (FontRendererHook.toggleChromaAndResetStyle(i1)) {
            this.resetStyles();
        }
    }

    /**
     * Replace all color codes (when chroma is enabled) to white so chroma renders uniformly and at best brightness
     */
    @ModifyVariable(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;indexOf(I)I", ordinal = 0, shift = At.Shift.BY, by = 2), ordinal = 1)
    public int forceWhiteColorCode(int i1) {
        return FontRendererHook.forceWhiteColorCode(i1);
    }

    /**
     * Inject call to {@link FontRendererHook#endChromaRendering()} to turn off chroma rendering after entire
     * string has been rendered
     */
    @Inject(method = "renderStringAtPos", at = @At("RETURN"))
    public void insertEndOfString(String text, boolean shadow, CallbackInfo ci) {
        FontRendererHook.endChromaRendering();
    }

    @ModifyVariable(method = "renderStringAtPos", at = @At("HEAD"), argsOnly = true)
    private String renderStringAtPos(String text) {
        return ModifyVisualWords.INSTANCE.modifyText(text);
    }

    @ModifyVariable(method = "getStringWidth(Ljava/lang/String;)I", at = @At("HEAD"), argsOnly = true)
    private String getStringWidth(String text) {
        return ModifyVisualWords.INSTANCE.modifyText(text);
    }
}
