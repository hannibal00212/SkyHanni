package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FontRenderer.class)
public abstract class ExtendedColorPatch {

    @Shadow(remap = false)
    protected abstract void setColor(float r, float g, float b, float a2);

    @Shadow
    private int textColor;
    @Shadow
    private float alpha;
    @Unique
    private int skyhanni$colorSR;
    @Unique
    private int skyhanni$colorState;

    @Inject(method = "renderStringAtPos", at = @At("HEAD"))
    private void resetStateWhenRendering(String text, boolean shadow, CallbackInfo ci) {
        skyhanni$colorSR = 0;
        skyhanni$colorState = -1;
    }

    @Unique
    private static boolean skyhanni$isSpecial = false;

    @Inject(
        method = "isFormatSpecial",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private static void protectFormatCodesSpecial(char formatChar, CallbackInfoReturnable<Boolean> cir) {
        if (formatChar == '/') {
            skyhanni$isSpecial = false;
            cir.setReturnValue(true);
        } else if (skyhanni$isSpecial) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "isFormatColor",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private static void protectFormatCodesSimple(char formatChar, CallbackInfoReturnable<Boolean> cir) {
        if (formatChar == '#') {
            skyhanni$isSpecial = true;
            cir.setReturnValue(true);
        } else if (skyhanni$isSpecial) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "getFormatFromString",
        at = @At(value = "TAIL")
    )
    private static void resetState(String text, CallbackInfoReturnable<String> cir) {
        skyhanni$isSpecial = false;
    }

    @Inject(
        method = "getFormatFromString",
        at = @At(value = "HEAD")
    )
    private static void resetStateAtHead(String text, CallbackInfoReturnable<String> cir) {
        skyhanni$isSpecial = false;
    }

    @Inject(
        method = "renderStringAtPos",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;setColor(FFFF)V",
            ordinal = 0,
            shift = At.Shift.AFTER,
            remap = false
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onChooseColor(
        String text,
        boolean shadow,
        CallbackInfo ci,
        int i,
        char c0,
        int i1
    ) {

        char c = text.charAt(i + 1);
        int hexCode = "0123456789abcdef".indexOf(c);
        if (c == '#') {
            if (skyhanni$colorState != -1) {
                throw new IllegalStateException("Encountered §# while inside push sequence");
            }
            skyhanni$colorState = 0;
            skyhanni$colorSR = 0;
        } else if (c == '/') {
            if (skyhanni$colorState != 8 && skyhanni$colorState != 6) {
                throw new IllegalStateException("Encountered §/ without encountering enough pushes: " + skyhanni$colorState);
            }
            textColor = skyhanni$colorSR;
            int shadowDivisor = shadow ? 4 : 1;
            setColor(
                (skyhanni$colorSR >> 16 & 0xFF) / 255.0f / shadowDivisor,
                (skyhanni$colorSR >> 8 & 0xFF) / 255.0f / shadowDivisor,
                (skyhanni$colorSR & 0xFF) / 255.0f / shadowDivisor,
                (skyhanni$colorState == 8 ? (skyhanni$colorSR >> 24 & 0xFF) / 255.0f : this.alpha)
            );
            skyhanni$colorState = -1;
        } else if (0 <= hexCode && skyhanni$colorState != -1) {
            skyhanni$colorState++;
            if (skyhanni$colorState > 8)
                throw new IllegalStateException("Encountered too many pushes inside of §#§/ sequence");
            skyhanni$colorSR = (skyhanni$colorSR << 4) | hexCode;
        }
    }

}
