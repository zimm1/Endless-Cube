package com.simonecavazzoni.firstandroidgame.gameEngine.utils;

import android.graphics.Color;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ColorUtils {
    public static int getColorFromString(String colorString) {
        return Color.parseColor("#"+colorString);
    }

    public static String getColorStringFromAlpha(String colorString, int alpha) {
        return alphaToHex(alpha)+colorString;
    }

    private static String alphaToHex(int alpha) {
        String hex = Integer.toHexString(alpha);
        if (alpha < 16) {
            hex = "0"+hex;
        }

        return hex;
    }

    public enum BACKGROUND_COLORS {
        RED("F44336"),
        PURPLE("9C27B0"),
        INDIGO("3F51B5"),
        LIGHT_BLUE("039BE5"),
        GREEN("43A047"),
        DEEP_ORANGE("FF5722");

        private String colorString;

        BACKGROUND_COLORS(String colorString) {
            this.colorString = colorString;
        }

        public String getColorString() {
            return colorString;
        }

        private static final List<BACKGROUND_COLORS> VALUES =
                Collections.unmodifiableList(Arrays.asList(values()));
        private static final int SIZE = VALUES.size();
        private static final Random RANDOM = new Random();

        public static String random()  {
            return (VALUES.get(RANDOM.nextInt(SIZE))).getColorString();
        }
    }
}
