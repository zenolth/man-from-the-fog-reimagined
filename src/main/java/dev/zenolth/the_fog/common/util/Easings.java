package dev.zenolth.the_fog.common.util;

import java.util.function.Function;

public class Easings {
    public static Function<Double,Double> inSine = Easings::inSine;
    public static Function<Double,Double> outSine = Easings::outSine;
    public static Function<Double,Double> inOutSine = Easings::inOutSine;

    public static Function<Double,Double> inCubic = Easings::inCubic;
    public static Function<Double,Double> outCubic = Easings::outCubic;
    public static Function<Double,Double> inOutCubic = Easings::inOutCubic;

    public static Function<Double,Double> inQuint = Easings::inQuint;
    public static Function<Double,Double> outQuint = Easings::outQuint;
    public static Function<Double,Double> inOutQuint = Easings::inOutQuint;

    public static Function<Double,Double> inCirc = Easings::inCirc;
    public static Function<Double,Double> outCirc = Easings::outCirc;
    public static Function<Double,Double> inOutCirc = Easings::inOutCirc;

    public static Function<Double,Double> inElastic = Easings::inElastic;
    public static Function<Double,Double> outElastic = Easings::outElastic;
    public static Function<Double,Double> inOutElastic = Easings::inOutElastic;

    public static Function<Double,Double> inQuad = Easings::inQuad;
    public static Function<Double,Double> outQuad = Easings::outQuad;
    public static Function<Double,Double> inOutQuad = Easings::inOutQuad;

    public static Function<Double,Double> inQuart = Easings::inQuart;
    public static Function<Double,Double> outQuart = Easings::outQuart;
    public static Function<Double,Double> inOutQuart = Easings::inOutQuart;

    public static Function<Double,Double> inExpo = Easings::inExpo;
    public static Function<Double,Double> outExpo = Easings::outExpo;
    public static Function<Double,Double> inOutExpo = Easings::inOutExpo;

    public static Function<Double,Double> inBack = Easings::inBack;
    public static Function<Double,Double> outBack = Easings::outBack;
    public static Function<Double,Double> inOutBack = Easings::inOutBack;

    public static Function<Double,Double> inBounce = Easings::inBounce;
    public static Function<Double,Double> outBounce = Easings::outBounce;
    public static Function<Double,Double> inOutBounce = Easings::inOutBounce;

    public static double inSine(double x) {
        return 1 - Math.cos((x * Math.PI) / 2);
    }

    public static double outSine(double x) {
        return Math.sin((x * Math.PI) / 2);
    }

    public static double inOutSine(double x) {
        return 0 - (Math.cos(Math.PI * x) - 1) / 2;
    }

    public static double inCubic(double x) {
        return x * x * x;
    }

    public static double outCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    public static double inOutCubic(double x) {
        if (x < 0.5) {
            return 4 * x * x * x;
        } else {
            return 1 - Math.pow(-2 * x + 2, 3) / 2;
        }
    }

    public static double inQuint(double x) {
        return x * x * x * x;
    }

    public static double outQuint(double x) {
        return 1 - Math.pow(1 - x, 5);
    }

    public static double inOutQuint(double x) {
        if (x < 0.5) {
            return 16 * x * x * x * x * x;
        } else {
            return 1 - Math.pow(-2 * x + 2, 5) / 2;
        }
    }

    public static double inCirc(double x) {
        return 1 - Math.sqrt(1 - (x * x));
    }

    public static double outCirc(double x) {
        return Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    public static double inOutCirc(double x) {
        if (x < 0.5) {
            return (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2;
        } else {
            return (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
        }
    }

    public static double inElastic(double x) {
        if (x == 0) {
            return 0;
        } else if (x == 1) {
            return 1;
        } else {
            return 0 - Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * ((2 * Math.PI) / 3));
        }
    }

    public static double outElastic(double x) {
        if (x == 0) {
            return 0;
        } else if (x == 1) {
            return 1;
        } else {
            return Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * ((2 * Math.PI) / 3)) + 1;
        }
    }

    public static double inOutElastic(double x) {
        if (x == 0) {
            return 0;
        } else if (x == 1) {
            return 1;
        } else if (x < 0.5) {
            return 0 - (Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * ((2 * Math.PI) / 4.5))) / 2;
        } else {
            return (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * ((2 * Math.PI) / 4.5))) / 2 + 1;
        }
    }

    public static double inQuad(double x) {
        return x * x;
    }

    public static double outQuad(double x) {
        return 1 - (1 - x) * (1 - x);
    }

    public static double inOutQuad(double x) {
        if (x < 0.5) {
            return 2 * x * x;
        } else {
            return 1 - Math.pow(-2 * x + 2, 2) / 2;
        }
    }

    public static double inQuart(double x) {
        return x * x * x * x;
    }

    public static double outQuart(double x) {
        return 1 - Math.pow(1 - x, 4);
    }

    public static double inOutQuart(double x) {
        if (x < 0.5) {
            return 8 * x * x * x * x;
        } else {
            return 1 - Math.pow(-2 * x + 2, 4) / 2;
        }
    }

    public static double inExpo(double x) {
        if (x == 0) {
            return 0;
        } else {
            return Math.pow(2, 10 * x - 10);
        }
    }

    public static double outExpo(double x) {
        if (x == 1) {
            return 1;
        } else {
            return 1 - Math.pow(2, -10 * x);
        }
    }

    public static double inOutExpo(double x) {
        if (x == 0) {
            return 0;
        } else if (x == 1) {
            return 1;
        } else if (x < 0.5) {
            return Math.pow(2, 20 * x - 10) / 2;
        } else {
            return 2 - Math.pow(2, -20 * x + 10) / 2;
        }
    }

    public static double inBack(double x) {
        return 2.70158 * x * x * x - 1.70158 * x * x;
    }

    public static double outBack(double x) {
        return 1 + 2.70158 * Math.pow(x - 1, 3) + 1.70158 * Math.pow(x - 1, 2);
    }

    public static double inOutBack(double x) {
        if (x < 0.5) {
            return (Math.pow(2 * x, 2) * (7.189819 * x - 2.5949095)) / 2;
        } else {
            return (Math.pow(2 * x - 2, 2) * (3.5949095 * (x * 2 - 2) + 2.5949095) + 2) / 2;
        }
    }

    public static double outBounce(double x) {
        if (x < 1 / 2.75) {
            return 7.5625 * x * x;
        } else if (x < 2 / 2.75) {
            return 7.5625 * (x - 1.5 / 2.75) * (x - 1.5) + 0.75;
        } else if (x < 2.5 / 2.75) {
            return 7.5625 * (x - 2.25 / 2.75) * (x - 2.25) + 0.9375;
        } else {
            return 7.5625 * (x - 2.625 / 2.75) * (x - 2.625) + 0.984375;
        }
    }

    public static double inBounce(double x) {
        return 1 - outBounce(1 - x);
    }

    public static double inOutBounce(double x) {
        if (x < 0.5) {
            return (1 - outBounce(1 - 2 * x)) / 2;
        } else {
            return (1 + outBounce(2 * x - 1)) / 2;
        }
    }
}
