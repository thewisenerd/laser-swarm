/**
 * 
 */
package com.google.code.laserswarm.Desim.elevation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.FindWindow;
import com.google.code.laserswarm.Desim.NoiseData;
import com.google.code.laserswarm.Desim.elevation.correlation.AltitudeCorrelation;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * @author Administrator
 * 
 */
public class FindElevation {
	private static int			qLength	= 9;
	private static final Logger	logger	= Logger.get(FindElevation.class);

	/**
	 * @param recTimes
	 *            The receiver history to be analysed.
	 * @param hist
	 *            The emitter history.
	 * @param con
	 *            The constellation used.
	 * @param dataPoints
	 *            The number of data points in this run. One data point corresponds to one emitted pulse.
	 * @param altCorr
	 *            The AltitudeCorrelation class to be used.
	 * @param nLastRemove
	 *            The number of last results that need to be removed to provide good end-of-list data.
	 * @return Returns a list of the Point3d's found on the Earth's surface (in r-lat-lon coordinates).
	 * @throws MathException
	 */
	public static LinkedList<Point3d> run(Map<Satellite, TimeLine> recTimes, EmitterHistory hist,
			Constellation con, int dataPoints, AltitudeCorrelation altCorr, int nLastRemove)
			throws MathException {
		Iterator<Double> timeIt = hist.time.iterator();
		Map<Satellite, DataContainer> interpulseWindows = Maps.newHashMap();
		for (DataContainer tempData : interpulseWindows.values()) {
			tempData.setQueueLength(qLength);
		}
		FindWindow emitRecPair = new FindWindow(hist, timeIt, recTimes, con, (int) 1e8);
		int count = 0;
		LinkedList<Double> timePulses = Lists.newLinkedList();
		LinkedList<Point3d> posEmits = Lists.newLinkedList();
		LinkedList<Point3d> altitudes = Lists.newLinkedList();
		while (count < dataPoints - 1 && timeIt.hasNext()) {
			count++;
			logger.inf("Iteration number %s", count);
			// Copy over the values from FindWindow.
			Map<Satellite, NoiseData> tempInterpulseWindow = emitRecPair.next();
			if (timeIt.hasNext()) {
				for (Satellite tempSat : tempInterpulseWindow.keySet()) {
					if (interpulseWindows.get(tempSat) == null) {
						interpulseWindows.put(tempSat, new DataContainer());
					}
					interpulseWindows.get(tempSat).add(tempInterpulseWindow.get(tempSat));
				}
				// Store the emitter time and position.
				timePulses.addLast(emitRecPair.tPulse);
				logger.dbg("Pulse time: %s", emitRecPair.tPulse);
				posEmits.addLast(new Point3d(hist.getPosition().find(emitRecPair.tPulse)));
				logger.dbg("Point: [%s, %s, %s]", hist.getPosition().find(emitRecPair.tPulse).x,
						hist
						.getPosition().find(emitRecPair.tPulse).y, hist.getPosition().find(
						emitRecPair.tPulse).z);
				// Remove pulse data we do not care about any more.
				if (timePulses.size() > (int) Math.ceil(0.5 * qLength)) {
					timePulses.removeFirst();
					posEmits.removeFirst();
				}
				// Do the actual data processing.
				if (count > qLength) {
					Point3d thisEmit = posEmits.getFirst();
					Point3d sphericalEmit = Convert.toSphere(thisEmit);
					altitudes.add(new Point3d(
							Configuration.R0
							+ altCorr.findAltitude(recTimes, interpulseWindows, timePulses.getFirst(),
							thisEmit),
							sphericalEmit.y, sphericalEmit.z));
				}
			}
		}
		while (Double.isNaN(altitudes.getLast().x)
				|| Math.abs(altitudes.getLast().x - altitudes.get(altitudes.size() - 2).x) > 1000) {
			altitudes.removeLast();
		}
		while (nLastRemove > 0) {
			nLastRemove--;
			altitudes.removeLast();
		}
		return altitudes;
	}
}
