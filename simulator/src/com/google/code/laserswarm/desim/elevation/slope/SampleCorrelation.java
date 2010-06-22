package com.google.code.laserswarm.desim.elevation.slope;

import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.desim.NoiseData;

public interface SampleCorrelation {
	public ElevationRelatedEntriesPoint next(Map<Satellite, NoiseData> nextInterpulse,
			double nextPulseT,
			Point3d nextEmitPt) throws MathException;
}
