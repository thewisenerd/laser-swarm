package com.google.code.laserswarm.Desim.elevation;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.NoiseData;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SubSampleCorrelation implements SampleCorrelation {
	private static final Logger				logger	= Logger.get(SubSampleCorrelation.class);

	private Map<Satellite, TimeLine>		receiverTimelines;
	private Map<Satellite, DataContainer>	interpulseData;
	private static double					interval;

	public SubSampleCorrelation(Map<Satellite, TimeLine> receiverTimes, double correlationInterval) {
		interpulseData = Maps.newHashMap();
		receiverTimelines = receiverTimes;
		interval = correlationInterval;
	}

	@Override
	public double next(Map<Satellite, NoiseData> nextInterpulse, double nextPulseT,
			Point3d nextEmitPt) throws MathException {
		for (Satellite tempSat : nextInterpulse.keySet()) {
			if (interpulseData.get(tempSat) == null) {
				interpulseData.put(tempSat, new DataContainer());
				interpulseData.get(tempSat).setQueueLength(1);
			}
			interpulseData.get(tempSat).add(nextInterpulse.get(tempSat));
		}
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
		double result = findAndAverageRelatedAltitudes(altitudes);
		logger.inf("Returning altitude: %s", result);
		return result;
	}

	private double findAndAverageRelatedAltitudes(ArrayList<Double> alts) {
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
		// Average the altitude within that TreeMap.
		double altTotal = 0;
		for (Double alt : maxRelatedEntryMap.keySet()) {
			altTotal += alt;
		}
		double result = altTotal / maxRelatedEntryMap.size();
		return result;
	}
}
