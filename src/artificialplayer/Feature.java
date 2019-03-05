package artificialplayer;

import java.io.Serializable;

public class Feature implements Serializable {
    static final long serialVersionUID = 42L;

    double a;
    double b;
    double c;

    public Feature(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public double getOutput(double input, double phase) {
        return a * input + phase * b * input + (1 - phase) * c * input;
    }
}
