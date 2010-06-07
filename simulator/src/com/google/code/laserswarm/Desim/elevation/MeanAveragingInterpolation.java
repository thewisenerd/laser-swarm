package com.google.code.laserswarm.Desim.elevation;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

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
	private LinkedList<LinkedList<Double>>	altitudes;
	private LinkedList<Double>				noisePercentages;
	private LinkedList<Double>				altitudeTotals;
	private LinkedList<Integer>				photonCounts;
	private int								qLength	= 7;

	public MeanAveragingInterpolation(int queueLength, Map<Satellite, TimeLine> receiverTimes) {
		qLength = queueLength;
		receiverTimelines = receiverTimes;
		interpulseData = Maps.newHashMap();
		altitudes = Lists.newLinkedList();
		noisePercentages = Lists.newLinkedList();
		altitudeTotals = Lists.newLinkedList();
		photonCounts = Lists.newLinkedList();
		for (Satellite sat : interpulseData.keySet()) {
			interpulseData.put(sat, new DataContainer());
			interpulseData.get(sat).setQueueLength(qLength);
		}
	}

	@Override
	public double next(Map<Satellite, NoiseData> nextInterpulse, double nextPulseT,
			Point3d nextEmitPt) throws MathException {
		for (Satellite tempSat : nextInterpulse.keySet()) {
			if (interpulseData.get(tempSat) == null) {
				interpulseData.put(tempSat, new DataContainer());
			}
			interpulseData.get(tempSat).add(nextInterpulse.get(tempSat));
		}
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
		// Now remove all the old data.
		while (altitudes.size() > qLength) {
			altitudes.removeFirst();
			noisePercentages.removeFirst();
			altitudeTotals.removeFirst();
			photonCounts.removeFirst();
		}
		// Now use the altitude totals to find the overall mean and raw sigma data.
		double overallTot = 0;
		for (Double altTot : altitudeTotals) {
			overallTot += altTot;
		}
		double overallMean = overallTot / altitudeTotals.size();
		int photonTot = 0;
		for (Integer photCnt : photonCounts) {
			photonTot += photCnt;
		}
		double sigmaTot = 0;
		for (Double thisAlt : lastAlts) {
			sigmaTot += overallMean - thisAlt;
		}
		double sigma = sigmaTot / (double) photonTot;
		// Find the overall noise percentage
		double noisePrctTot = 0;
		for (Double thisNoise : noisePercentages) {
			noisePrctTot += thisNoise;
		}
		double overallNoisePrct = noisePrctTot / noisePercentages.size();
		// Find lower and upper noise boundaries.
		NormalDistributionImpl normal = new NormalDistributionImpl(overallMean, sigma);
		double upper = normal.inverseCumulativeProbability(1.0 - 0.5 * overallNoisePrct);
		double lower = overallMean - (overallMean - upper);
		// Filter altitudes and compute average.
		double filteredAltTotal = 0;
		int filteredAltCount = 0;
		double result = Double.NaN;
		if (altitudes.size() >= qLength) {
			for (Double thisAlt : altitudes.get((int) Math.ceil((double) qLength / 2))) {
				if (thisAlt < upper & thisAlt > lower) {
					filteredAltTotal += thisAlt;
					filteredAltCount++;
				}
			}
			result = filteredAltTotal / filteredAltCount;
		}
		return result;
	}
}
