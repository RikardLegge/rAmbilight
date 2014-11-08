package com.legge;

public class lMath {

    public static final double PI                    = 3.14159265358979323846264338327950288419716939;
    public static final int    TRIGONOMETRYPRESICION = 8;
    public static final long[] FACTORIAL             = new long[]{1l, 2l, 6l, 24l, 120l, 720l, 5040l, 40320l, 362880l, 3628800l, 39916800l, 479001600l, 6227020800l, 87178291200l, 1307674368000l, 20922789890000l, 355687428100000l};

    public static double toRad(double deg) {
        return deg / 180 * PI;
    }

    public static double toDeg(double rad) {
        return rad / PI * 180;
    }


    public static double tan(double rad) {
        return sin(rad) / cos(rad);
    }

    public static double cos(double rad) {
        return sin((rad > 0 ? -(rad - PI / 2) : rad + PI / 2));
    }

    public static double sin(double rad) {
        rad = rad % (PI * 2) - PI;
        double res = 0;
        for (int i = 1; i < TRIGONOMETRYPRESICION * 2; i += 2)
            res += (Math.pow(rad, i) / factorial(i)) * ((i / 2) % 2 > 0 ? -1 : 1);
        return res * -1;
    }


    public static double rsin(double rad) {
        return rsin((rad) % PI, 1);
    }

    private static double rsin(double rad, int fac) {
        double res = (Math.pow(rad, fac) / factorial(fac)) * ((fac / 2) % 2 > 0 ? -1 : 1);
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