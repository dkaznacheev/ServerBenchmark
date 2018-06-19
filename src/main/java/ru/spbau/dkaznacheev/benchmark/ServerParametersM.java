package ru.spbau.dkaznacheev.benchmark;

public class ServerParametersM {
    private final String architecture;
    private final String parameter;
    private final int x;
    private final int pMin;
    private final int pMax;
    private final int pStep;
    private final int n;
    private final int m;
    private final int d;

    public ServerParametersM(String architecture, String parameter, String  x, String pMin, String pMax, String pStep, String n, String m, String d) {
        this.architecture = architecture;
        this.parameter = parameter;
        this.x = Integer.parseInt(x);
        this.pMin = Integer.parseInt(pMin);
        this.pMax = Integer.parseInt(pMax);
        this.pStep = Integer.parseInt(pStep);
        if (n != null)
            this.n = Integer.parseInt(n);
        else
            this.n = -1;
        if (m != null)
            this.m = Integer.parseInt(m);
        else
            this.m = -1;
        if (d != null)
            this.d = Integer.parseInt(d);
        else
            this.d = -1;
    }
}
