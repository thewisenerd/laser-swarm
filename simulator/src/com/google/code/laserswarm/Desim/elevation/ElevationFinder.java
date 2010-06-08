package com.google.code.laserswarm.Desim.elevation;

import java.util.Map;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.elevation.slope.ElevationSlope;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;

public interface ElevationFinder {
	/**
	 * @param recTimes
	 *            The receiver history to be analysed.
	 * @param hist
	 *            The emitter history.
	 * @param con
	 *            The constellation used.
	 * @param dataPoints
	 *            The number of data points in this run. One data point corresponds to one emitted pulse.
	 * @return Returns a list of the Point3d's found on the Earth's surface (in r-lat-lon coordinates).
	 * @throws MathException
	 */
	public ElevationSlope run(Map<Satellite, TimeLine> recTimes, EmitterHistory hist,
			Constellation con, int dataPoints)
			throws MathException;
}
