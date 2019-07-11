package zy.core.clustering;

public class Lnglat {
    private double[] lnglat;

    public Lnglat(double[] lnglat) {
        this.lnglat = lnglat;
    }

    public Lnglat() {
        this.lnglat = null;
    }

    public double[] getLnglat() {
        return lnglat;
    }

    public void setLnglat(double[] lnglat) {
        this.lnglat = lnglat;
    }
}
