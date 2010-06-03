package com.google.code.laserswarm.Desim;

import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.TimeLine;

public interface AltitudeCorrelation {
	public double findAltitude(Map<Satellite, TimeLine> recTimes,
			Map<Satellite, DataContainer> satsDatasets, double tPulse, Point3d pEmit)
			throws MathException;
}
