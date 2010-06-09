package com.google.code.laserswarm.Desim.elevation.slope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.NoiseData;
import com.google.code.laserswarm.Desim.BRDFcalc.BRDFinput;
import com.google.code.laserswarm.Desim.elevation.AltitudeCalculation;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.math.LookupTable;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SubSampleCorrelation implements SampleCorrelation {
	private static final Logger							logger	= Logger.get(SubSampleCorrelation.class);

	private Map<Satellite, TimeLine>					receiverTimelines;
	private Map<Satellite, DataContainer>				interpulseData;

	private LinkedList<ElevationRelatedEntriesPoint>	rawElevationSlopes;
	private double										equalitySpacing;
	private double										interval;
	private double										fractionD;
	private int											qLength;
	private int											middle;
	private Constellation								cons;

	public SubSampleCorrelation(Constellation swarm, Map<Satellite, TimeLine> receiverTimes,
			double correlationInterval, int comparisonQueueLength, double whenEqual, double fractionD) {
		this.interpulseData = Maps.newHashMap();
		this.receiverTimelines = receiverTimes;
		this.interval = correlationInterval;
		this.qLength = comparisonQueueLength;
		this.middle = (int) Math.floor((double) qLength / 2.0);
		this.equalitySpacing = whenEqual;
		this.fractionD = fractionD;
		this.cons = swarm;
		this.rawElevationSlopes = Lists.newLinkedList();
	}

	@Override
	public ElevationBRDF next(Map<Satellite, NoiseData> nextInterpulse,
			double nextPulseT,
			Point3d nextEmitPt) throws MathException {
		for (Satellite tempSat : nextInterpulse.keySet()) {
			if (interpulseData.get(tempSat) == null) {
				interpulseData.put(tempSat, new DataContainer());
				interpulseData.get(tempSat).setQueueLength(1);
			}
			interpulseData.get(tempSat).add(nextInterpulse.get(tempSat));
		}
		ElevationBRDF out = null;
		// Do all calculations that can be done for a single data point (or: interpulse window).
		// Start with the altitudes.
		TreeMap<Double, Vector3d> altitudes = Maps.newTreeMap();
		double altTot = 0;
		double satCount = 0;
		int photonCount = 0;
		for (Satellite curSat : interpulseData.keySet()) {
			satCount++;
			DataContainer tempContainer = interpulseData.get(curSat);
			TreeMap<Double, Integer> tempData = tempContainer.getData().getLast().getData();
			for (Double time : tempData.keySet()) {
				double thisAlt = AltitudeCalculation.calcAlt(nextEmitPt,
						new Point3d(receiverTimelines.get(curSat)
						.getLookupPosition()
						.find(time)), time - nextPulseT);
				int numPhotons = tempData.get(time);
				photonCount += numPhotons;
				LookupTable satPositions = receiverTimelines.get(curSat).getLookupPosition();
				for (int i = 0; i < numPhotons; i++) {
					altitudes.put(thisAlt, new Vector3d(satPositions.find(time)));
				}
				altTot += numPhotons * thisAlt;
				logger.dbg("Found an altitude: %s, with photon no.: %s", thisAlt - Configuration.R0,
						numPhotons);
			}
		}
		// Find and average altitudes that show a strong correlation.
		ElevationRelatedEntriesPoint result = findAndAverageRelatedAltitudes(altitudes, nextPulseT,
				nextEmitPt);
		rawElevationSlopes.add(result);
		while (rawElevationSlopes.size() > qLength) {
			rawElevationSlopes.removeFirst();
		}
		// Filter averages based on spike removal
		if (rawElevationSlopes.size() == qLength) {
			Iterator<ElevationRelatedEntriesPoint> rawIt = rawElevationSlopes.iterator();
			double last = 0;
			double current = 0;
			int count = 0;
			boolean stillEqual = true;
			while (rawIt.hasNext()) {
				last = current;
				current = rawIt.next().getElevation();
				if (count == 0) {
					last = current;
					current = rawIt.next().getElevation();
					count++;
				} else if (count == middle) {
					current = rawIt.next().getElevation();
					count++;
				}
				if (!areEqual(last, current)) {
					stillEqual = false;
				}
				count++;
			}
			if (stillEqual) {
				if (areEqual(rawElevationSlopes.getFirst().getElevation(), rawElevationSlopes
						.get(middle).getElevation())) {
				} else {
					rawIt = rawElevationSlopes.iterator();
					double totalAlt = 0;
					double altNo = 0;
					while (rawIt.hasNext()) {
						double localAlt = rawIt.next().getElevation();
						if (!(altNo == middle)) {
							totalAlt += localAlt;
						}
						altNo++;
					}
					ElevationRelatedEntriesPoint middlePoint = rawElevationSlopes.get(middle);
					ArrayList<TreeMap<Double, Vector3d>> closeEntryMaps = middlePoint
							.getRelatedEntries();
					Iterator<TreeMap<Double, Vector3d>> closeIt = closeEntryMaps.iterator();
					TreeMap<Double, Vector3d> bestFitMap = Maps.newTreeMap();
					double minDist = Double.MAX_VALUE;
					double elevation = totalAlt / altNo;
					while (closeIt.hasNext()) {
						TreeMap<Double, Vector3d> thisMap = closeIt.next();
						double mapAvg = treeMapAvg(thisMap);
						double dist = Math.abs(mapAvg - elevation);
						if (dist < minDist) {
							bestFitMap = thisMap;
							minDist = dist;
						}
					}
					rawElevationSlopes.set(middle, new ElevationRelatedEntriesPoint(elevation,
							middlePoint.getTEmit(), middlePoint.getPosEmit(),
							closeEntryMaps, bestFitMap));
				}
				ElevationRelatedEntriesPoint rawElBRDF = rawElevationSlopes.get(middle);
				out = new ElevationBRDF(rawElBRDF.getElevation(), genBRDFInput(rawElevationSlopes
						.get(middle - 1), rawElBRDF, rawElevationSlopes.get(middle + 1)));
			}
		} else {
		}
		return out;
	}

	private BRDFinput genBRDFInput(ElevationRelatedEntriesPoint lastElBRDF,
			ElevationRelatedEntriesPoint elBRDF,
			ElevationRelatedEntriesPoint nextElBRDF) {
		// Calculate the emitter groundtrack point.
		Point3d posEmit = elBRDF.getPosEmit();
		Point3d posEmitSph = Convert.toSphere(posEmit);
		posEmitSph.x -= elBRDF.getElevation();
		Vector3d emPos = new Vector3d(Convert.toXYZ(posEmitSph));
		// Calculate the direction in which the emitter is moving.
		Vector3d nextEmPos = new Vector3d(nextElBRDF.getPosEmit());
		Vector3d thisEmPos = new Vector3d(elBRDF.getPosEmit());
		Vector3d emDir = new Vector3d();
		emDir.sub(nextEmPos, thisEmPos);
		emDir.normalize();
		// Calculate the along-track slope.
		Point3d lastEmitSph = Convert.toSphere(lastElBRDF.getPosEmit());
		Point3d thisEmitSph = Convert.toSphere(elBRDF.getPosEmit());
		double avgRad = (lastEmitSph.x + thisEmitSph.x) / 2.0;
		double difRad = Math.abs(lastEmitSph.x - thisEmitSph.x);
		lastEmitSph.x = avgRad;
		thisEmitSph.x = avgRad;
		Point3d lastEmitCart = Convert.toXYZ(lastEmitSph);
		Point3d thisEmitCart = Convert.toXYZ(thisEmitSph);
		Vector3d difTan = new Vector3d();
		difTan.sub(thisEmitCart, lastEmitCart);
		double alongTrackSlope = difRad / difTan.length();
		// Calculate the cross-track slope.
		Vector3d emitVect = new Vector3d(posEmit);
		double footprintD = fractionD * 2.0 * cons.getEmitter().getBeamDivergence()
				* (emitVect.length() - Configuration.R0);
		TreeMap<Double, Vector3d> altitudes = elBRDF.getBestMap();
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (Double alt : altitudes.keySet()) {
			if (alt > max) {
				max = alt;
			}
			if (alt < min) {
				min = alt;
			}
		}
		double crossTrackSlope = Math.sqrt(Math.pow(alongTrackSlope, 2)
				- Math.pow((max - min) / footprintD, 2));
		// The satellite vectors go in here.
		Map<Vector3d, Integer> photonDirs = Maps.newLinkedHashMap();

		// Get the current emitter time.
		double curTime = elBRDF.getTEmit();
		return new BRDFinput(emPos, emDir, alongTrackSlope, crossTrackSlope, photonDirs, curTime);
	}

	private boolean areEqual(double a, double b) {
		if (Math.abs(a - b) < equalitySpacing) {
			return true;
		} else {
			return false;
		}
	}

	private double treeMapAvg(TreeMap<Double, Vector3d> map) {
		// Average the altitude within the given TreeMap.
		double altTotal = 0;
		for (Double alt : map.keySet()) {
			altTotal += alt;
		}
		return altTotal / map.size();
	}

	private ElevationRelatedEntriesPoint findAndAverageRelatedAltitudes(TreeMap<Double, Vector3d> alts,
			double tEmit, Point3d posEmit) {
		ArrayList<TreeMap<Double, Vector3d>> entriesClose = Lists.newArrayList();
		// Try to find lists of related altitudes, then put them in TreeMaps
		for (Double currentAlt : alts.keySet()) {
			boolean entryIsAlreadyListed = false;
			boolean entryHasTreeMap = false;
			for (TreeMap<Double, Vector3d> relatedEntryMap : entriesClose) {
				if (relatedEntryMap.get(currentAlt) != null) {
					entryIsAlreadyListed = true;
				}
			}
			if (!entryIsAlreadyListed) {
				for (Double thisAlt : alts.keySet()) {
					Vector3d thisVect = alts.get(thisAlt);
					if (Math.abs(currentAlt - thisAlt) < interval) {
						if (!entryHasTreeMap) {
							entryHasTreeMap = true;
							entriesClose.add(new TreeMap<Double, Vector3d>());
							entriesClose.get(entriesClose.size() - 1).put(currentAlt, thisVect);
						}
						entriesClose.get(entriesClose.size() - 1).put(thisAlt, thisVect);
					}
				}
			}
		}
		// Try to find the TreeMap with the largest amount of altitudes.
		int maxSizeTreeMap = 0;
		TreeMap<Double, Vector3d> maxRelatedEntryMap = Maps.newTreeMap();
		for (TreeMap<Double, Vector3d> relatedEntryMap : entriesClose) {
			if (relatedEntryMap.size() > maxSizeTreeMap) {
				maxSizeTreeMap = relatedEntryMap.size();
				maxRelatedEntryMap = relatedEntryMap;
			}
		}
		return new ElevationRelatedEntriesPoint(treeMapAvg(maxRelatedEntryMap), tEmit, posEmit,
				entriesClose, maxRelatedEntryMap);
	}
}
