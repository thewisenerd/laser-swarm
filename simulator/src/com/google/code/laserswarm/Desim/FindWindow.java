package com.google.code.laserswarm.Desim;

import com.google.code.laserswarm.conf.Constellation;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.MeasermentSample;
import com.google.code.laserswarm.process.SampleIterator;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Maps;

public class FindWindow {

	EmitterHistory					hist;

	double							ipwindow;
	Iterator<Double>				timeIt;
	Map<Satellite, TimeLine>		satData;		// satellite - timeline map
	Map<Satellite, SampleIterator>	satIter;		// satellite - sampleiterator map
	public double					tPulse;
	int								binFreqency;
	double							bigWindow;

	public FindWindow(EmitterHistory hist, Map<Satellite, TimeLine> sit, Constellation con,
			int binFreqency) {
		// TODO Auto-generated constructor stub
		this.binFreqency = binFreqency;
		this.hist = hist;
		this.bigWindow = con.getPulseFrequency();
		satData = sit;
		timeIt = hist.time.iterator();
		for (Satellite satIt : sit.keySet()) {
			try {
				satIter.put(satIt, satData.get(satIt).getIterator(binFreqency));
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public Map<Satellite, NoiseData> next() { // return window of values
		// TODO Auto-generated method stub
		// 
		tPulse = timeIt.next();

		Map<Satellite, NoiseData> dataMap = Maps.newHashMap();

		for (Satellite satCur : satData.keySet()) { // iterate over all satellites
			TimePair tmp = getWindow(new Vector3d(satData.get(satCur).getLookupPosition().find(tPulse)),
					new Vector3d(hist.getPosition().find(tPulse))); // calculate the window times for the
			// current sat, current time
			double tDataHigh = tmp.tF;
			double tDataLow = tmp.t0;
			boolean exec = false;
			TreeMap<Double, Integer> result = Maps.newTreeMap();
			result.clear();
			SampleIterator satIt = satIter.get(satCur);

			MeasermentSample ms = satIt.next();
			// check whether the window is out of bounds

			if (ms.getTime() > tDataLow)
				tDataLow = ms.getTime();
			if ((ms.getTime() + bigWindow) < tDataHigh)
				tDataHigh = ms.getTime() + bigWindow;

			double tIPWoffset = (bigWindow - (tDataHigh - tDataLow)) / 2; // offset from the highest time
			double tUpper = tIPWoffset + tDataHigh; // the highest time

			while (satIt.hasNext() & (ms.getTime() < tUpper)) { // Construct unfiltered result vector
				if (exec) {
					ms = satIt.next(); // ensure the incrmintation is repeated once

				}
				exec = true;
				result.put(ms.getTime(), ms.getPhotons()); // put the results in the map

			} // END construction of the result vector;

			dataMap.put(satCur, new NoiseData(result, new TimePair(tDataLow, tDataHigh)));

		} // Stop ITerating over satellites;

		return dataMap;

	}

	/**
	 * @param REC
	 *            Vector3d position of the receiver
	 * @param EMIT
	 *            Vector3d position of the emitter
	 */

	public TimePair getWindow(Vector3d REC, Vector3d EMIT) {
		double maxDist;
		double minDist;

		Vector3d maxGrndHght = new Vector3d(EMIT);
		Vector3d minGrndHght = new Vector3d(EMIT);

		Vector3d maxEmitHght = new Vector3d(EMIT);
		Vector3d minEmitHght = new Vector3d(EMIT);

		Vector3d maxRecHght = new Vector3d(REC);
		Vector3d minRecHght = new Vector3d(REC);

		minGrndHght.normalize();
		maxGrndHght.normalize();
		maxGrndHght.scale(Configuration.R0 + 9E3);
		minGrndHght.scale(Configuration.R0 - 5E2);

		maxRecHght.sub(maxGrndHght);
		minRecHght.sub(minGrndHght);

		maxEmitHght.sub(maxGrndHght);
		minEmitHght.sub(minGrndHght);

		maxDist = maxRecHght.length() + maxEmitHght.length();
		minDist = minRecHght.length() + minEmitHght.length();

		return new TimePair(maxDist / Configuration.c, minDist / Configuration.c);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

}
