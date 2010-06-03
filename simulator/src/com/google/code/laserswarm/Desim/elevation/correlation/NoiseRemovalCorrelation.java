package com.google.code.laserswarm.Desim.elevation.correlation;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.NoisePercentage;
import com.google.code.laserswarm.Desim.elevation.AltitudeCalculation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class NoiseRemovalCorrelation implements AltitudeCorrelation {
	private static final Logger	logger	= Logger.get(NoiseRemovalCorrelation.class);

	@Override
	public double findAltitude(Map<Satellite, TimeLine> recTimes,
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
}
