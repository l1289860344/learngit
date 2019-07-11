package zy.core;

import lombok.Data;

import java.util.Arrays;

@Data
public class Point {
    double lnglat[];
    double x;
    double y;

    Point() {
        this.lnglat = null;
        this.x = 0;
        this.y = 0;
    }

    public Point(double x, double y) {
        double[] d = new double[2];
        d[0] = x;
        d[1] = y;
        this.lnglat = d;
        this.x = x;
        this.y = y;
    }

    public double[] getLnglat() {
        return this.lnglat;
    }

    public void setLnglat(double[] lnglat) {
        this.lnglat = lnglat;
        this.x = lnglat[0];
        this.y = lnglat[1];
    }

    public double getX() {
//        this.x = lnglat[0];
        return this.x;
    }

    public void setX(double x) {
        this.x = lnglat[0];
    }

    public double getY() {
//        this.y = lnglat[1];
        return this.y;
    }

    public void setY(double y) {
        this.y = lnglat[1];
    }

    @Override
    public String toString() {
        return "Point{" +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
