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
	private double				equalitySpacing;
	private double				correlationInterval;
	private double				fractionD;
	private int					comparisonQueueLength;
	private int					qLength		= 5;
	private int					frequency	= (int) 1e9;
	private SampleCorrelation	interpolator;
	private static final Logger	logger		= Logger.get(FindElevationNeighborInterpolation.class);

	/**
	 * 
	 * @param queueLength
	 *            The length of the noiseData queue. Can be one in this algorithm.
	 * @param resolution_Hz
	 *            The bin size, or sampling frequency.
	 * @param correlationInterval
	 *            The interval used in the correlation algorithm; two elevations found are considered
	 *            close enough to be connected to the same datapoint if they are less than an interval
	 *            apart.
	 * @param comparisonQueueLength
	 *            The length of the correlation queue. It is a good idea to make it uneven, because the
	 *            middle entry is looked for, and it must be three or larger. Three seems to be doing
	 *            well, though.
	 * @param whenEqual
	 *            For the spike filter, two altitudes are assumed to be equal if they are apart less than
	 *            this distance.
	 * @param fractionD
	 *            The fraction of the diameter used for the slope calculation.
	 */
	public FindElevationNeighborInterpolation(int queueLength, int resolution_Hz,
			double correlationInterval, int comparisonQueueLength, double whenEqual, double fractionD) {
		this.qLength = queueLength;
		this.frequency = resolution_Hz;
		this.correlationInterval = correlationInterval;
		this.comparisonQueueLength = comparisonQueueLength;
		this.equalitySpacing = whenEqual;
		this.fractionD = fractionD;
	}

	/**
	 * @param cons
	 *            The constellation used in the simulation is given here.
	 * @param recTimes
	 *            The Timelines generated by the simulation are give here.
	 * @param hist
	 *            The emitter history used.
	 * @param dataPoints
	 *            The number of data points.
	 */
	@Override
	public ElevationSlope run(Map<Satellite, TimeLine> recTimes, EmitterHistory hist,
			Constellation swarm, int dataPoints) throws MathException {
		interpolator = new SubSampleCorrelation(recTimes, correlationInterval,
				comparisonQueueLength, equalitySpacing);
		Iterator<Double> timeIt = hist.time.iterator();
		Map<Satellite, DataContainer> interpulseWindows = Maps.newHashMap();
		LinkedList<ElevationRelatedEntriesPoint> firstResult = Lists.newLinkedList();
		for (DataContainer tempData : interpulseWindows.values()) {
			if (tempData == null) {
				tempData = new DataContainer();
			}
			tempData.setQueueLength(qLength);
		}
		FindWindow emitRecPair = new FindWindow(hist, timeIt, recTimes, swarm, frequency);
		int count = 0;

		LinkedList<Point3d> altitudes = Lists.newLinkedList();
		while (count < dataPoints - 1 && timeIt.hasNext()) {
			count++;
			logger.inf("Iteration number %s", count);
			// Copy over the values from FindWindow.
			Map<Satellite, NoiseData> tempInterpulseWindow = emitRecPair.next();
			Point3d thisEmit = new Point3d(hist.getPosition().find(emitRecPair.tPulse));
			Point3d sphericalEmit = Convert.toSphere(thisEmit);
			ElevationRelatedEntriesPoint elPt = interpolator.next(tempInterpulseWindow,
					emitRecPair.tPulse,
					thisEmit);
			if (elPt != null) {
				double alt = elPt.getElevation();
				logger.dbg("Altitude found: %s", alt);
				altitudes.add(new Point3d(alt, sphericalEmit.y, sphericalEmit.z));
				firstResult.add(elPt);
			}
		}
		logger.inf("Altitudes LinkedList size: %s", altitudes.size());
		while (Double.isNaN(altitudes.getLast().x)
				|| Math.abs(altitudes.getLast().x - altitudes.get(altitudes.size() - 2).x) > 1000) {
			altitudes.removeLast();
		}
		BRDFInputGenerator generator = new BRDFInputGenerator(swarm, fractionD, 16);
		ElevationSlope elSlope = generator.generate(firstResult);
		return elSlope;
	}
}
