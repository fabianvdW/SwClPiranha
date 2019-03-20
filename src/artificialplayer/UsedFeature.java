package artificialplayer;

public class UsedFeature {

    Feature f;
    public double inputA;
    public double inputB;
    public double inputC;

    double value;
    public UsedFeature(double input, Feature f, double phase) {
        this.f = f;
        this.inputA = input;
        this.inputB = input * phase;
        this.inputC = input * (1 - phase);
        this.value=f.a*inputA+f.b*inputB+f.c*inputC;
    }
}
