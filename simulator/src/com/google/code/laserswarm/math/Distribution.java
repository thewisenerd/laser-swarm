package com.google.code.laserswarm.math;

import org.apache.commons.math.MathException;
import org.apache.commons.math.geometry.Vector3D;

public interface Distribution {

	double probability(Vector3D x) throws MathException;

}
