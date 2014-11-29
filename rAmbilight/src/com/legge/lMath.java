package com.legge;

public class lMath {

    public static final float PI = 3.14159265358979323846264338327950288419716939f;

    private static       int    TRIGONOMETRYPRESICION = 8;
    private static final long[] FACTORIAL             = new long[]{1l, 2l, 6l, 24l, 120l, 720l, 5040l, 40320l, 362880l, 3628800l, 39916800l, 479001600l, 6227020800l, 87178291200l, 1307674368000l, 20922789890000l, 355687428100000l};

    public static void setTrigonometryPrecision(int precision) {
        TRIGONOMETRYPRESICION = precision;
    }

    public static int getTrigonometryPrecision() {
        return TRIGONOMETRYPRESICION;
    }


    public static float toRad(float deg) {
        return deg / 180 * PI;
    }

    public static float toDeg(float rad) {
        return rad / PI * 180;
    }


    public static float tan(float rad) {
        return sin(rad) / cos(rad);
    }

    public static float cos(float rad) {
        return sin((rad > 0 ? -(rad - PI / 2) : rad + PI / 2));
    }

    public static float sin(float rad) {
        rad = rad % (PI * 2) - PI;
        float res = 0;
        for (int i = 1; i < TRIGONOMETRYPRESICION * 2; i += 2)
            res += (Math.pow(rad, i) / factorial(i)) * ((i / 2) % 2 > 0 ? -1 : 1);
        return res * -1;
    }


    /**
     * A recursive method for calculating the sin of the input angle.
     *
     * @param rad The angle in radians.
     * @return The ratio between two sides in a right angle triangle.
     */
    public static float rsin(float rad) {
        return rad == 0 ? rad : rsin((rad) % PI, 1);
    }

    private static float rsin(float rad, int fac) {
        float res = 0;
        res += (Math.pow(rad, fac) / factorial(fac)) * ((fac / 2) % 2 > 0 ? -1 : 1);
        if (fac <= TRIGONOMETRYPRESICION * 2) res += rsin(rad, fac + 2);
        return res;
    }


    public static long factorial(int n) {
        if (n < FACTORIAL.length)
            return FACTORIAL[n - 1];
        long fact = 1;
        for (long i = 1; i <= n; i++) fact *= i;
        return fact;
    }
}