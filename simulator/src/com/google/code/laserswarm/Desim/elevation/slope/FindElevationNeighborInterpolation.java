package com.google.code.laserswarm.Desim.elevation.slope;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.FindWindow;
import com.google.code.laserswarm.Desim.NoiseData;
import com.google.code.laserswarm.Desim.elevation.ElevationFinder;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class FindElevationNeighborInterpolation implements ElevationFinder {
	private int					qLength		= 5;
	private int					frequency	= (int) 1e9;
	private SampleCorrelation	interpolator;
	private static final Logger	logger		= Logger.get(FindElevationNeighborInterpolation.class);

	public FindElevationNeighborInterpolation(int queueLength, int resolution_Hz) {
		qLength = queueLength;
		frequency = resolution_Hz;
	}

	@Override
	public ElevationSlope run(Map<Satellite, TimeLine> recTimes, EmitterHistory hist,
			Constellation con, int dataPoints) throws MathException {
		interpolator = new SubSampleCorrelation(con, recTimes, 5, 3, 1.0, 0.707);
		Iterator<Double> timeIt = hist.time.iterator();
		Map<Satellite, DataContainer> interpulseWindows = Maps.newHashMap();
		for (DataContainer tempData : interpulseWindows.values()) {
			if (tempData == null) {
				tempData = new DataContainer();
			}
			tempData.setQueueLength(qLength);
		}
		FindWindow emitRecPair = new FindWindow(hist, timeIt, recTimes, con, frequency);
		int count = 0;
		ElevationSlope elSlope = new ElevationSlope();
		LinkedList<Point3d> altitudes = Lists.newLinkedList();
		LinkedList<Double> slopes = Lists.newLinkedList();
		while (count < dataPoints - 1 && timeIt.hasNext()) {
			count++;
			logger.inf("Iteration number %s", count);
			// Copy over the values from FindWindow.
			Map<Satellite, NoiseData> tempInterpulseWindow = emitRecPair.next();
			Point3d thisEmit = new Point3d(hist.getPosition().find(emitRecPair.tPulse));
			Point3d sphericalEmit = Convert.toSphere(thisEmit);
			ElevationBRDF elPt = interpolator.next(tempInterpulseWindow, emitRecPair.tPulse,
					thisEmit);
			if (elPt != null) {
				double alt = elPt.getElevation();
				logger.dbg("Altitude found: %s", alt);
				if (!(new Double(alt).isNaN())) {
					altitudes.add(new Point3d(alt, sphericalEmit.y, sphericalEmit.z));
				}
			}
		}
		logger.inf("Altitudes LinkedList size: %s", altitudes.size());
		while (Double.isNaN(altitudes.getLast().x)
				|| Math.abs(altitudes.getLast().x - altitudes.get(altitudes.size() - 2).x) > 1000) {
			altitudes.removeLast();
		}
		elSlope.setAltitudes(altitudes);
		elSlope.setSlopes(slopes);
		return elSlope;
	}
}
