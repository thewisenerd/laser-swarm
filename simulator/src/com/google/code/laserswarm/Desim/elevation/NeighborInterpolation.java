package com.google.code.laserswarm.Desim.elevation;

import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.TimeLine;

public interface NeighborInterpolation {
	public double findAltitude(Map<Satellite, TimeLine> recTimes,
			Map<Satellite, DataContainer> satsDatasets, LinkedList<Double> tPulses,
			LinkedList<Point3d> pEmits)
			throws MathException;
}
