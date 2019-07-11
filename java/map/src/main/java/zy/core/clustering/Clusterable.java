package zy.core.clustering;

public interface Clusterable {

    /**
     * Gets the n-dimensional point.
     *
     * @return the point array
     */
    double[] getFeatures();

    boolean isPart();

    void setPart(boolean part);
}