package at.hannibal2.skyhanni.utils;

import java.awt.Color;

/**
 * Taken from NotEnoughUpdates
 */
public class SpecialColor {

    private static final int RADIX = 10;

    private static int[] decompose(String csv) {
        String[] split = csv.split(":");

        int[] arr = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            arr[i] = Integer.parseInt(split[split.length - 1 - i], RADIX);
        }
        return arr;
    }

    public static int getSpeed(String special) {
        return decompose(special)[4];
    }

    public static float getSecondsForSpeed(int speed) {
        return (255 - speed) / 254f * (MAX_CHROMA_SECS - MIN_CHROMA_SECS) + MIN_CHROMA_SECS;
    }

    private static final int MIN_CHROMA_SECS = 1;
    private static final int MAX_CHROMA_SECS = 60;

    public static long startTime = -1;

    @Deprecated // use String.toChromaColor()
    public static int specialToChromaRGB(String special) {
        if (startTime < 0) startTime = System.currentTimeMillis();

        int[] d = decompose(special);
        int chr = d[4];
        int a = d[3];
        int r = d[2];
        int g = d[1];
        int b = d[0];

        float[] hsv = Color.RGBtoHSB(r, g, b, null);

        if (chr > 0) {
            float seconds = getSecondsForSpeed(chr);
            hsv[0] += (System.currentTimeMillis() - startTime) / 1000f / seconds;
            hsv[0] %= 1;
            if (hsv[0] < 0) hsv[0] += 1;
        }

        return (a & 0xFF) << 24 | (Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0x00FFFFFF);
    }

    public static int rotateHue(int argb, int degrees) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb) & 0xFF;

        float[] hsv = Color.RGBtoHSB(r, g, b, null);

        hsv[0] += degrees / 360f;
        hsv[0] %= 1;

        return (a & 0xFF) << 24 | (Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0x00FFFFFF);
    }
}
