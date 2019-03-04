package artificialplayer;

import java.io.Serializable;

public class Feature implements Serializable {
    double a;
    double b;
    double c;
    double d;
    double e;
    double f;

    public Feature(double a, double b, double c, double d, double e, double f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    public double getOutput(double input, double phase) {
        double xmb = input - b;
        double xmbsquared = Math.pow(xmb, 2);
        return a * xmbsquared + c * xmb + d * phase * (e * xmbsquared + f * xmb);
    }
}
