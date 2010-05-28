/**
 * 
 */
package com.google.code.laserswarm.Desim;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Maps;

/**
 * @author Administrator
 * 
 */
public class FindElevation {

	/**
	 * @param emit
	 *            Point3d that represents the position of the emitter
	 * @param rec
	 *            Point3d that represents the position of the receiver
	 * @param travTime
	 *            The time in seconds it takes for the pulse to travel between emitter and receiver
	 */
	public double findNoisePercentage(final DataContainer data, TimePair timeRng) {
		// data.getNoise(tFrame);
		LinkedList<NoiseData> nsData = data.getRange(timeRng);
		double noise = 0; // noise photons
		double tNoise = 0; // time of the signal
		double tSignal = 0; // time of the signal
		double signal = 0; // signal Photons

		for (NoiseData noiseIt : nsData) { // iterate over the interpulse windows
			tNoise += noiseIt.getNoiseFrameL().diff() + // calculate time of the noise in one interpulse
					// window
					noiseIt.getNoiseFrameR().diff();
			tSignal += noiseIt.getWindowFrame().diff(); // calculate the signal time in one interpulse
			// window

			for (Integer intIt : noiseIt.getNoise().values()) {
				noise += intIt; // add up noise photons
			}
			for (Integer intIt : noiseIt.getData().values()) {
				signal += intIt; // add up signal photons
			}
		}
		return (noise / tNoise * (tNoise + tSignal)) / (signal + noise);
	}

	public static double calcAlt(Point3d emit, Point3d rec, double travTime) throws MathException {
		// if(trav1 < 0) throw new MathException("time difference can't be negative");
		// Assumed: Location of the satellite is known to high precision
		// Earth is a perfect sphere
		// Emitter points perp. to the earth center
		// Recievers points to the same point as the emitter

		// create an ellipse

		double focalDist = emit.distance(rec); // distance between the focal points formed by receiver
		// and emitter
		double c = Configuration.c; // speed of light
		double dist = Math.abs(travTime) * c;
		if (dist < focalDist)
			throw new MathException("Distance Traveled is shorter than Focal length");

		double a = dist / 2; // semimajor axis

		double b_2 = Math.pow(dist / 2, 2) - Math.pow(focalDist / 2.0, 2.0); // b^2 (semiminor axis
		// squared)
		double ecc_2 = Math.sqrt(1 - b_2 / (a * a)); // eccentricity^2
		double ecc = Math.sqrt(ecc_2); // eccentricity^2
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

	public static/* List<SimVars> */void run(Map<Satellite, TimeLine> rec, EmitterHistory hist,
			Constellation con) {
		Iterator<Double> timeIt = hist.time.iterator();
		Map<Satellite, DataContainer> InterpulseWindows = Maps.newHashMap();
		FindWindow emitRecPair = new FindWindow(hist, rec, con, (int) 1e6);
		while (timeIt.hasNext()) {
			Map<Satellite, NoiseData> tempInterpulseWindow = emitRecPair.next();
			for (Satellite tempSat : tempInterpulseWindow.keySet()) {
				InterpulseWindows.get(tempSat).add(tempInterpulseWindow.get(tempSat));
			}
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

}
