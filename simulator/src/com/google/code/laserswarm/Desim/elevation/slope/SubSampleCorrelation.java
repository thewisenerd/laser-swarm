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
import com.google.code.laserswarm.conf.Satellite;
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
	private int											qLength;
	private int											middle;

	public SubSampleCorrelation(Map<Satellite, TimeLine> receiverTimes, double correlationInterval,
			int comparisonQueueLength, double whenEqual) {
		this.interpulseData = Maps.newHashMap();
		this.receiverTimelines = receiverTimes;
		this.interval = correlationInterval;
		this.qLength = comparisonQueueLength;
		this.middle = (int) Math.floor((double) qLength / 2.0);
		this.equalitySpacing = whenEqual;
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
		ArrayList<Double> altitudes = Lists.newArrayList();
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
				for (int i = 0; i < numPhotons; i++) {
					altitudes.add(thisAlt);
				}
				altTot += numPhotons * thisAlt;
				logger.dbg("Found an altitude: %s, with photon no.: %s", thisAlt, numPhotons);
			}
		}
		// Find and average altitudes that show a strong correlation.
		ElevationRelatedEntriesPoint result = findAndAverageRelatedAltitudesAndSlopes(altitudes,
				nextPulseT, nextEmitPt);
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
					ArrayList<TreeMap<Double, Boolean>> closeEntryMaps = middlePoint.getRelatedEntries();
					Iterator<TreeMap<Double, Boolean>> closeIt = closeEntryMaps.iterator();
					TreeMap<Double, Boolean> bestFitMap = Maps.newTreeMap();
					double minDist = Double.MAX_VALUE;
					double elevation = totalAlt / altNo;
					while (closeIt.hasNext()) {
						TreeMap<Double, Boolean> thisMap = closeIt.next();
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
				out = new ElevationBRDF(rawElBRDF.getElevation(), genBRDFInput(rawElBRDF));
			}
		} else {
		}
		return out;
	}

	private BRDFinput genBRDFInput(ElevationRelatedEntriesPoint elBRDF) {
		Point3d posEmit = elBRDF.getPosEmit();
		Vector3d emPos = new Vector3d(posEmit.x - elBRDF.getElevation(), posEmit.y, posEmit.z);
		return new BRDFinput(emPos,
				null, equalitySpacing, equalitySpacing, null, equalitySpacing);
	}

	private boolean areEqual(double a, double b) {
		if (Math.abs(a - b) < equalitySpacing) {
			return true;
		} else {
			return false;
		}
	}

	private double treeMapAvg(TreeMap<Double, Boolean> map) {
		// Average the altitude within the given TreeMap.
		double altTotal = 0;
		for (Double alt : map.keySet()) {
			altTotal += alt;
		}
		return altTotal / map.size();
	}

	private ElevationRelatedEntriesPoint findAndAverageRelatedAltitudesAndSlopes(ArrayList<Double> alts,
			double tEmit, Point3d posEmit) {
		ArrayList<TreeMap<Double, Boolean>> entriesClose = Lists.newArrayList();
		int index = 0;
		// Try to find lists of related altitudes, then put them in TreeMaps
		while (index < alts.size()) {
			Double currentAlt = alts.get(index);
			boolean entryIsAlreadyListed = false;
			boolean entryHasTreeMap = false;
			for (TreeMap<Double, Boolean> relatedEntryMap : entriesClose) {
				if (relatedEntryMap.get(currentAlt) != null) {
					entryIsAlreadyListed = true;
				}
			}
			if (!entryIsAlreadyListed) {
				for (int i = index + 1; i < alts.size(); i++) {
					Double thisAlt = alts.get(i);
					if (Math.abs(currentAlt - thisAlt) < interval) {
						if (!entryHasTreeMap) {
							entryHasTreeMap = true;
							entriesClose.add(new TreeMap<Double, Boolean>());
							entriesClose.get(entriesClose.size() - 1).put(currentAlt, true);
						}
						entriesClose.get(entriesClose.size() - 1).put(thisAlt, true);
					}
				}
			}
			index++;
		}
		// Try to find the TreeMap with the largest amount of altitudes.
		int maxSizeTreeMap = 0;
		TreeMap<Double, Boolean> maxRelatedEntryMap = Maps.newTreeMap();
		for (TreeMap<Double, Boolean> relatedEntryMap : entriesClose) {
			if (relatedEntryMap.size() > maxSizeTreeMap) {
				maxSizeTreeMap = relatedEntryMap.size();
				maxRelatedEntryMap = relatedEntryMap;
			}
		}
		return new ElevationRelatedEntriesPoint(treeMapAvg(maxRelatedEntryMap), tEmit, posEmit,
				entriesClose,
				maxRelatedEntryMap);
	}
}
