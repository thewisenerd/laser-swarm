package com.google.code.laserswarm.math;

import javax.vecmath.Vector3d;


/**
 * A distribution implemention.
 * <p>
 * Evaluate the probability in a given direction (vector, normalized). <br />
 * The up vector is considered to be <0,1,0>
 * </p>
 * 
 * @author Simon Billemont, TUDelft, Faculty Aerospace Engineering (aodtorusan@gmail.com or
 *         s.billemont@student.tudelft.nl)
 * 
 */
public interface Distribution {

	/**
	 * Evaluate the distribution for a given direction <0,1,0> is considered to be up
	 * 
	 * @param x
	 *            Directional vector (normalized)
	 * @return The probability in the given direction
	 */
	double probability(Vector3d x);

}
