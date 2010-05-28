package com.google.code.laserswarm.Desim;

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

	EmitterHistory				hist;
	// double tWindow; // window time
	// double tOffset; // offset time
	double						ipwindow;
	Iterator<Double>			timeIt;
	Map<Satellite, TimeLine>	satData;		// satellite - timeline map
	Map<Satellite,SampleIterator> satIter;		//satellite - sampleiterator map
	public double				tPulse;
	int							binFreqency;
	double

	public FindWindow(EmitterHistory hist, Map<Satellite, TimeLine> sit, double bigwindow,
			int binFreqency) {
		// TODO Auto-generated constructor stub
		this.binFreqency = binFreqency;
		this.hist = hist;
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
		// timeIt.
		// tPulse = timeIt.
	}


	public Map<Satellite, TreeMap<Double, Integer>> next() { // return window of values
		// TODO Auto-generated method stub
		// 
		tPulse = timeIt.next();
		Map<Satellite, TreeMap<Double, Integer>> satmap = Maps.newHashMap();
		// double tPulse = hist.getPulseClosesTo(tCur); //pulse time of the closest pulse;
		// find spikes untill time, and before time

		for (Satellite satCur : satData.keySet()) { // iterate over all satellites
			double[] tmp = getWindow(new Vector3d(satData.get(satCur).getLookupPosition().find(tPulse)),
					new Vector3d(hist.getPosition().find(tPulse)));		//calculate the window times
			double tHigh = tmp[0];
			double tLow = tmp[1];
			boolean exec = false;
			
			
			TreeMap<Double, Integer> result = Maps.newTreeMap();

			
			result.clear();

			SampleIterator satIt = satIter.get(satCur);
		/*	try {
				satIt = satData.get(isat).getIterator(binFreqency);
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			MeasermentSample ms = satIt.next();
			int size = 0;
			double sum = 0;
			int count = 0;
			System.out.println("High Time " + tHigh);
			System.out.println("MsTime: " + ms.getTime());

			while (satIt.hasNext() & (ms.getTime() < tHigh)) { // Construct unfiltered result vector
				// System.out.println("Loop cycle " + count++);
				if (exec) {
					ms = satIt.next(); // ensure nothing is repeated

				}
				exec = true;
				result.put(ms.getTime(), ms.getPhotons()); // put the results in the map

			} // END consturcting resutl vector;

			satmap.put(satCur, result);
		} // Stop ITerating over satellites;
		System.out.println("filtered data: " + satmap);
		return satmap;
		// (tPulse+tOffset+1/2*tWindow);

	}

	/**
	 * @param REC
	 *            Vector3d position of the receiver
	 * @param EMIT
	 *            Vector3d position of the emitter
	 */

	public double[] getWindow(Vector3d REC, Vector3d EMIT) {
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

		return new double[] { maxDist / Configuration.c, minDist / Configuration.c };

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

}
