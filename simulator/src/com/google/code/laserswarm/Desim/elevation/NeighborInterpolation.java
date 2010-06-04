package com.google.code.laserswarm.Desim.elevation;

import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.NoiseData;
import com.google.code.laserswarm.conf.Satellite;

public interface NeighborInterpolation {
	public double next(Map<Satellite, NoiseData> nextInterpulse, double nextPulseT,
			Point3d nextEmitPt) throws MathException;
}
