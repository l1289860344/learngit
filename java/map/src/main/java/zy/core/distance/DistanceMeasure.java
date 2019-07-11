package zy.core.distance;

import java.io.Serializable;

public interface DistanceMeasure extends Serializable {

	/**
	 * Compute the distance between two n-dimensional vectors.
	 * <p>
	 * The two vectors are required to have the same dimension.
	 *
	 * @param a
	 *            the first vector
	 * @param b
	 *            the second vector
	 * @return the distance between the two vectors
	 */
	double compute(double[] a, double[] b);
}
