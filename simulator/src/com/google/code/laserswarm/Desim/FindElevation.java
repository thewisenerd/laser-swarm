/**
 * 
 */
package com.google.code.laserswarm.Desim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

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
	 * @param emit
	 *            Point3d that represents the position of the emitter
	 * @param rec
	 *            Point3d that represents the position of the receiver
	 * @param travTime
	 *            The time in seconds it takes for the pulse to travel between emitter and receiver
	 */
	public static double findNoisePercentage(final DataContainer data) {
		LinkedList<NoiseData> nsData = data.getData();
		double noise = 0; // noise photons
		double tNoise = 0; // time of the signal
		double tSignal = 0; // time of the signal
		double signal = 0; // signal Photons

		for (NoiseData noiseIt : nsData) { // iterate over the interpulse windows
			tNoise += noiseIt.getNoiseFrameL().diff() + // calculate time of the noise in one interpulse
					// window
					noiseIt.getNoiseFrameR().diff();
			tSignal += noiseIt.getDataFrame().diff(); // calculate the signal time in one interpulse
			// window

			for (Integer intIt : noiseIt.getNoise().values()) {
				noise += intIt; // add up noise photons
			}
			for (Integer intIt : noiseIt.getData().values()) {
				signal += intIt; // add up signal photons
			}
		}
		logger.dbg("Noise, signal photons: %s, %s, Noise, signal times: %s, %s", noise, signal, tNoise,
				tSignal);
		return (noise / tNoise * (tNoise + tSignal)) / (signal + noise);
	}

	public static double calcAlt(Point3d emit, Point3d rec, double travTime) throws MathException {
		// if(trav1 < 0) throw new MathException("time difference can't be negative");
		// Assumed: Location of the satellite is known to high precision
		// Earth is a perfect sphere
		// Emitter points perp. to the earth center
		// Recievers points to the same point as the emitter

		// create an ellipse

		logger.dbg("emit: %s, %s, %s\n rec: %s, %s, %s\n travTime: %s", emit.x, emit.y, emit.z, rec.x,
				rec.y, rec.z, travTime);
		double focalDist = emit.distance(rec); // distance between the focal points formed by receiver
		// and emitter
		double dist = Math.abs(travTime) * Configuration.c;
		if (dist < focalDist)
			throw new MathException("Distance Traveled is shorter than Focal length");

		double a = dist / 2; // semimajor axis
		double a_2 = Math.pow(a, 2); // semimajor axis, squared
		double c = focalDist / 2; // half of the centerline length
		double b_2 = Math.pow(a, 2) - Math.pow(c, 2); // b^2 (semiminor axis squared)
		double ecc_2 = (a_2 - b_2) / a_2; // eccentricity^2
		double ecc = Math.sqrt(ecc_2); // eccentricity
		Vector3d em = new Vector3d(emit);
		Vector3d re = new Vector3d(rec);
		Vector3d dif = new Vector3d();
		dif.sub(em, re);
		double theta = dif.angle(em); // angle between focal length vector and the emitter vector

		if (dif.length() == 0)
			theta = Math.PI / 2; // if the receiver and emitter are the same
		double distGrndEmit = a * (1 - ecc_2) / (1 - ecc * Math.cos(theta)); // distance to the ground
		// from the emitter
		return em.length() - Configuration.R0 - distGrndEmit; // altitude above the earth sphere in
		// meters
	}

	public static double findAltitude(Map<Satellite, TimeLine> recTimes,
			Map<Satellite, DataContainer> satsDatasets, double tPulse, Point3d pEmit)
			throws MathException {
		double prctNoise = 0;
		LinkedList<Double> altitudes = Lists.newLinkedList();
		int altCount = 0;
		int noiseCount = 0;
		double altTot = 0;
		for (Satellite tempSat : satsDatasets.keySet()) {
			DataContainer data = satsDatasets.get(tempSat);
			// Count the noise photons.
			double nsPrct = findNoisePercentage(data);
			prctNoise += nsPrct;
			noiseCount++;
			logger.dbg("Percentage noise: %s", nsPrct);
			TreeMap<Double, Integer> middleDataWindow = data.getData().get(
					(int) Math.ceil(0.5 * data.getQueueLength())).getData();
			// Count the photons in the pulse data window; find the altitude for every photon.
			for (Double time : middleDataWindow.keySet()) {
				Double lastKey = recTimes.get(tempSat).getLookupPosition().lastKey();
				Integer nPhotons = middleDataWindow.get(time);
				logger.dbg("Time: %s : photon number: %s", time, nPhotons);
				if (time < lastKey) {
					Double alt = calcAlt(pEmit, new Point3d(recTimes.get(tempSat).getLookupPosition()
							.find(time)), time - tPulse);
					logger.dbg("Altitude: %s", alt);
					altCount += (int) nPhotons;
					altTot += (double) nPhotons * alt;
					for (int i = 0; i < (int) nPhotons; i++) {
						altitudes.add(alt);
					}
				}
			}
		}
		// Find the percentage of noise, the average altitude found, the number of noise photons.
		prctNoise /= (double) noiseCount;
		double altAv = altTot / altCount;
		double altNoise = prctNoise * altCount;
		// Remove the outlying altitudes.
		Collections.sort(altitudes);
		logger.dbg("Length of the altitudes list: %s, altCount: %s, prctNoise: %s, altNoise: %s",
				altitudes.size(), altCount, prctNoise, altNoise);
		while (altNoise > 0) {
			double altFirst = altitudes.getFirst();
			double altLast = altitudes.getLast();
			if (altAv - altFirst > altLast - altAv) {
				altitudes.removeFirst();
			} else {
				altitudes.removeLast();
			}
			altNoise--;
		}
		// Find the average altitude for the cleansed altitude set.
		Iterator<Double> altIt = altitudes.iterator();
		altTot = altCount = 0;
		while (altIt.hasNext()) {
			altCount++;
			altTot += altIt.next();
		}
		return altTot / altCount;
	}

	public static LinkedList<Point3d> outlierRemoval(LinkedList<Point3d> altitudes, int sigmaWindow,
			int meanWindow) {
		LinkedList<Point3d> result = Lists.newLinkedList();
		ArrayList<Point3d> temp = Lists.newArrayList();
		int count = 0;
		for (Point3d point : altitudes) {
			count++;
			temp.add(point);
			if (count == sigmaWindow) {

			}
		}
		return result;
	}

	public static LinkedList<Point3d> run(Map<Satellite, TimeLine> recTimes, EmitterHistory hist,
			Constellation con, int dataPoints) throws MathException {
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
							+ findAltitude(recTimes, interpulseWindows, timePulses.getFirst(),
							thisEmit),
							sphericalEmit.y, sphericalEmit.z));
				}
			}
		}
		while (Double.isNaN(altitudes.getLast().x)
				|| Math.abs(altitudes.getLast().x - altitudes.get(altitudes.size() - 2).x) > 1000) {
			altitudes.removeLast();
		}
		return altitudes;
	}
}
