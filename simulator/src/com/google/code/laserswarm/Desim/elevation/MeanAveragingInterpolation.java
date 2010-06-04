package com.google.code.laserswarm.Desim.elevation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.NoiseData;
import com.google.code.laserswarm.Desim.NoisePercentage;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MeanAveragingInterpolation implements NeighborInterpolation {
	private Map<Satellite, TimeLine>		receiverTimelines;
	private Map<Satellite, DataContainer>	interpulseData;
	// private LinkedList<Double> pulseTimes;
	// private LinkedList<Point3d> emitterPoints;
	private LinkedList<LinkedList<Double>>	altitudes;
	private LinkedList<Double>				noisePercentages;
	private LinkedList<Double>				altitudeTotals;
	private LinkedList<Integer>				photonCounts;
	private LinkedList<Double>				sigmaRaws;
	private int								qLength	= 7;

	public MeanAveragingInterpolation(int queueLength, Map<Satellite, TimeLine> receiverTimes) {
		qLength = queueLength;
		receiverTimelines = receiverTimes;
		interpulseData = Maps.newHashMap();
		for (Satellite sat : interpulseData.keySet()) {
			interpulseData.put(sat, new DataContainer());
			interpulseData.get(sat).setQueueLength(qLength);
		}
		// pulseTimes = Lists.newLinkedList();
		// emitterPoints = Lists.newLinkedList();
	}

	private double sigma(LinkedList<Point3d> alts, LinkedList<Double> means, int interval) {
		LinkedList<Double> result = Lists.newLinkedList();
		Iterator<Double> meanIt = means.iterator();
		Iterator<Double> meanFirst = means.iterator();
		double sigma = 0;
		int count = 0;
		int untreated = 0;
		for (Point3d pt : alts) {
			count++;
			untreated++;
			sigma += Math.pow(pt.x - meanIt.next(), 2);
			if (count >= interval) {
				while (untreated > 0) {
					double res = Math.sqrt(sigma / (interval - 1));
					result.add(res);
					untreated--;
				}
				sigma -= Math.pow(pt.x - meanFirst.next(), 2);
			}
		}
		Double last = result.getLast();
		while (result.size() != alts.size()) {
			result.add(last);
		}
		return result;
	}

	@Override
	public double next(Map<Satellite, NoiseData> nextInterpulse, double nextPulseT,
			Point3d nextEmitPt) throws MathException {
		// add new values
		// emitterPoints.add(nextEmitPt);
		// pulseTimes.add(nextPulseT);
		for (Satellite tempSat : nextInterpulse.keySet()) {
			interpulseData.get(tempSat).add(nextInterpulse.get(tempSat));
		}
		// remove old values
		// if (pulseTimes.size() > qLength) {
		// emitterPoints.removeFirst();
		// pulseTimes.removeFirst();
		// }
		// Do all calculations that can be done for a single data point (or: interpulse window).
		// Start with the altitudes, the noise percentages and the means.
		LinkedList<Double> lastAlts = Lists.newLinkedList();
		double noiseTot = 0;
		double thisAltTot = 0;
		double satCount = 0;
		int thisPhotonCount = 0;
		for (Satellite curSat : interpulseData.keySet()) {
			satCount++;
			DataContainer tempContainer = interpulseData.get(curSat);
			noiseTot += NoisePercentage.findNoisePercentage(tempContainer);
			TreeMap<Double, Integer> tempData = tempContainer.getData().getLast().getData();
			for (Double time : tempData.keySet()) {
				double thisAlt = AltitudeCalculation.calcAlt(nextEmitPt,
						new Point3d(receiverTimelines.get(curSat)
						.getLookupPosition()
						.find(time)), time - nextPulseT);
				int numPhotons = tempData.get(time);
				thisPhotonCount += numPhotons;
				for (int i = 0; i < numPhotons; i++) {
					lastAlts.add(thisAlt);
				}
				thisAltTot += numPhotons * thisAlt;
			}
		}
		altitudes.add(lastAlts);
		noisePercentages.add(noiseTot / satCount);
		altitudeTotals.add(thisAltTot);
		photonCounts.add(thisPhotonCount);
		// Now use the altitude totals to find the overall mean and raw sigma data.
		double overallTot = 0;
		for (Double altTot : altitudeTotals) {
			overallTot += altTot;
		}
		double overallMean = overallTot / altitudeTotals.size();
		for (Double thisAlt : lastAlts) {

		}
		return 2.0;
	}
}
