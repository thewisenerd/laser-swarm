package com.google.code.laserswarm.Desim.elevation.correlation;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.NoisePercentage;
import com.google.code.laserswarm.Desim.elevation.AltitudeCalculation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class OutlierRemovalCorrelation implements AltitudeCorrelation {
	private static final Logger	logger			= Logger.get(OutlierRemovalCorrelation.class);
	private double				noiseFraction	= 1.0;

	/**
	 * @param noiseFractionRemoved
	 *            The fraction of the noise altitudes to be removed.
	 */
	public OutlierRemovalCorrelation(double noiseFractionRemoved) {
		noiseFraction = noiseFractionRemoved;
	}

	@Override
	public double findAltitude(Map<Satellite, TimeLine> recTimes,
			Map<Satellite, DataContainer> satsDatasets, double tPulse, Point3d pEmit)
			throws MathException {
		LinkedList<Double> altitudes = Lists.newLinkedList();
		double prctNoise = 0;
		int altCount = 0;
		int noiseCount = 0;
		double altTotal = 0;
		for (Satellite tempSat : satsDatasets.keySet()) {
			DataContainer data = satsDatasets.get(tempSat);
			// Count the noise photons.
			double nsPrct = NoisePercentage.findNoisePercentage(data);
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
					Double alt = AltitudeCalculation.calcAlt(pEmit, new Point3d(recTimes.get(tempSat)
							.getLookupPosition()
							.find(time)), time - tPulse);
					logger.dbg("Altitude: %s", alt);
					altCount += (int) nPhotons;
					altTotal += (double) nPhotons * alt;
					for (int i = 0; i < (int) nPhotons; i++) {
						altitudes.add(alt);
					}
				}
			}
		}
		// Find the percentage of noise and the mean altitude found.
		prctNoise /= (double) noiseCount;
		double meanAlt = altTotal / altCount;
		// Find the standard deviation for the altitudes.
		double sigmAlt = 0;
		for (Double alt : altitudes) {
			sigmAlt += Math.pow(alt - meanAlt, 2);
		}
		sigmAlt = Math.sqrt(sigmAlt / (altCount - 1));
		// Calculate the validity thresholds dictated by the noise percentage and noise fraction to be
		// removed
		double lower = -Double.MAX_VALUE;
		double upper = Double.MAX_VALUE;
		if (sigmAlt > 0) {
			NormalDistributionImpl normalDist = new NormalDistributionImpl(meanAlt, sigmAlt);
			lower = normalDist.inverseCumulativeProbability(0.5 * noiseFraction * prctNoise);
			upper = meanAlt + (meanAlt - lower);
		}
		// Remove the outlying altitudes.
		logger.dbg("Length of the altitudes list: %s, altCount: %s, prctNoise: %s",
				altitudes.size(), altCount, prctNoise);
		double altTot = 0;
		double altCnt = 0;
		for (Double alt : altitudes) {
			if (alt < upper & alt > lower) {
				altCnt++;
				altTot += alt;
			}
		}
		return altTot / altCnt;
	}
}
