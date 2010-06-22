package com.google.code.laserswarm.desim.elevation.slope;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.desim.brdf.BRDFinput;
import com.google.code.laserswarm.math.Convert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class BRDFInputGenerator {
	private static final Logger	logger	= Logger.get(BRDFInputGenerator.class);
	private double				fractionD;
	private Constellation		cons;
	private int					qLength;
	private int					middle;

	public BRDFInputGenerator(Constellation swarm, double fractionD, int queueLength) {
		this.cons = swarm;
		this.fractionD = fractionD;
		if (queueLength < 3) {
			queueLength = 3;
		}
		this.qLength = queueLength;
		this.middle = (int) Math.floor(((double) qLength) / 2.0);
	}

	// Final elevation and BRDFinput generation.
	public ElevationSlope generate(LinkedList<ElevationRelatedEntriesPoint> points) {
		LinkedList<Point3d> heights = Lists.newLinkedList();
		LinkedList<BRDFinput> input = Lists.newLinkedList();
		LinkedList<ElevationRelatedEntriesPoint> elQueue = Lists.newLinkedList();
		Iterator<ElevationRelatedEntriesPoint> ptIt = points.iterator();

		while (ptIt.hasNext()) {
			elQueue.add(ptIt.next());
			while (elQueue.size() > qLength) {
				elQueue.removeFirst();
			}
			if (elQueue.size() == qLength) {
				ElevationRelatedEntriesPoint midPt = elQueue.get(middle);
				// Find and add the groundtrack point to the height vector
				double thisH = midPt.getElevation();
				Point3d thisEmitPt = midPt.getPosEmit(); // ECEF
				Point3d thisEmitSph = Convert.toSphere(thisEmitPt); // R, lon, lat
				Point3d thisGrndSph = new Point3d(thisH, thisEmitSph.y, thisEmitSph.z);
				heights.add(thisGrndSph);
				// Try to do some BRDFInput generation.
				// Get the current emitter time.
				double curTime = midPt.getTEmit();
				// Find the emitter satellite flying direction.
				Point3d nextEmitSph = Convert.toSphere(elQueue.get(middle + 1).getPosEmit());
				Point3d levelNext = Convert.toXYZ(new Point3d(Configuration.R0, nextEmitSph.y,
						nextEmitSph.z));
				Point3d levelThis = Convert.toXYZ(new Point3d(Configuration.R0, thisEmitSph.y,
						thisEmitSph.z));
				Vector3d emitterDirection = new Vector3d();
				emitterDirection.sub(levelNext, levelThis);
				emitterDirection.normalize();
				// The along track slope.
				Point3d hOne = new Point3d(0, 0, 0); // R, lat, lon
				Point3d hTwo = new Point3d(0, 0, 0); // R, lat, lon
				int count = 0;
				int cntOne = 0;
				int cntTwo = 0;
				for (ElevationRelatedEntriesPoint itPt : elQueue) {
					Point3d itEmitPt = itPt.getPosEmit();
					Point3d itEmitSph = Convert.toSphere(itEmitPt);
					itEmitSph.x = itPt.getElevation();
					Point3d itGrndPt = Convert.toXYZ(itEmitSph);
					if (count < middle) {
						hOne.add(itGrndPt);
						cntOne++;
					} else {
						hTwo.add(itGrndPt);
						cntTwo++;
					}
					count++;
				}
				hOne = new Point3d(hOne.x / cntOne, hOne.y / cntOne, hOne.z / cntOne);
				hTwo = new Point3d(hTwo.x / cntTwo, hTwo.y / cntTwo, hTwo.z / cntTwo);
				Point3d hOneSph = Convert.toSphere(hOne); // R, lon, lat
				Point3d hTwoSph = Convert.toSphere(hTwo); // R, lon, lat
				double alongTrackDistance = horizontalDistance(hOne, hTwo);
				double radialDistance = hTwoSph.x - hOneSph.x;
				double alongTrackSlope = radialDistance / alongTrackDistance;
				// Now for the cross track slope.
				Vector3d emitVect = new Vector3d(midPt.getPosEmit()); // ECEF
				double footprintD = 2.0 * fractionD * cons.getEmitter().getBeamDivergence()
						* (emitVect.length() - midPt.getElevation());
				TreeMap<Double, Vector3d> altitudes = midPt.getBestMap();
				double min = 1E9;
				double max = -1E9;
				for (Double itAlt : altitudes.keySet()) {
					if (itAlt < min) {
						min = itAlt;
					}
					if (itAlt > max) {
						max = itAlt;
					}
				}
				double radialSubDistance = max - min;
				double totalSlope = radialSubDistance / footprintD;
				double crossTrackSlope = Math.sqrt(Math.pow(totalSlope, 2)
						- Math.pow(alongTrackSlope, 2));
				if (new Double(crossTrackSlope).isNaN()) {
					crossTrackSlope = 0;
					logger.wrn("Intercepted a NaN crossTrackSlope.");
				}
				logger.inf("Along-track, cross-track and total slope: %s, %s, %s", alongTrackSlope,
						crossTrackSlope, totalSlope);
				// The satellite vectors go in here.
				Map<Vector3d, Integer> photonDirs = Maps.newLinkedHashMap();
				for (Double time : altitudes.keySet()) {
					Vector3d satVect = altitudes.get(time);
					if (photonDirs.get(satVect) == null) {
						photonDirs.put(satVect, 1);
					} else {
						photonDirs.put(satVect, photonDirs.get(satVect) + 1);
					}
				}
				input.add(new BRDFinput(new Vector3d(thisEmitPt), emitterDirection, new Vector3d(Convert
						.toXYZ(thisGrndSph)), alongTrackSlope, crossTrackSlope, photonDirs, curTime));
			}
		}
		return new ElevationSlope(heights, input);
	}

	/**
	 * 
	 * @param a
	 *            In ECEF.
	 * @param b
	 *            In ECEF.
	 * @return Returns the horizontal distance over the earth sphere between the two points.
	 */
	private double horizontalDistance(Point3d a, Point3d b) {
		Point3d strt = Convert.toSphere(a);
		Point3d stop = Convert.toSphere(b);
		strt.x = Configuration.R0;
		stop.x = Configuration.R0;
		strt = Convert.toXYZ(strt);
		stop = Convert.toXYZ(stop);
		return strt.distance(stop);
	}
}