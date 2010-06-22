package com.google.code.laserswarm.desim;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.ProcessorTester;
import com.google.code.laserswarm.RandData;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.MeasermentSample;
import com.google.code.laserswarm.process.SampleIterator;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class FindWindow {
	private static final Logger					logger	= Logger.get(FindWindow.class);

	EmitterHistory								hist;

	double										ipwindow;
	Iterator<Double>							timeIt;
	Map<Satellite, TimeLine>					satData;								// satellite -
																						// timeline map
	Map<Satellite, SampleIterator>				satIter;								// satellite -
																						// sampleiterator
																						// map
	/**
	 * Time of the current emitted pulse
	 */
	public double								tPulse;
	int											binFrequency;
	double										bigWindow;
	private Map<Satellite, MeasermentSample>	satMsMap;								// map that
																						// contains
																						// initial data

	/**
	 * 
	 * @param hist
	 *            EmitterHistory
	 * @param sit
	 *            Map of satellites and their TimeLines
	 * @param con
	 *            Constellation to determine the pulseFrequency;
	 * @param binFrequency
	 *            determines the amount of samples, 5E8 corresponds to 2ns
	 */
	public FindWindow(EmitterHistory hist, Iterator<Double> tIt, Map<Satellite, TimeLine> sit,
			Constellation con,
			int binFrequency) {
		// TODO Auto-generated constructor stub
		satMsMap = Maps.newHashMap();
		this.binFrequency = binFrequency;
		this.hist = hist;
		this.bigWindow = 1 / con.getPulseFrequency();
		satData = sit;
		satIter = Maps.newHashMap();
		timeIt = tIt; // hist.time.iterator();
		// tPulse=timeIt.next();
		for (Satellite satIt : sit.keySet()) {
			try {
				satIter.put(satIt, satData.get(satIt).getIterator(binFrequency));

				MeasermentSample tms = satIter.get(satIt).nextNonZero();
				// tms = satIter.get(satIt).nextNonZero();
				satMsMap.put(satIt, tms); // could be set to nextNonZero

			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * 
	 * @return returns the next Satellite, NoiseData window
	 */
	@Deprecated
	public Map<Satellite, NoiseData> next2() { // return window of values
		// TODO Auto-generated method stub
		// 
		tPulse = timeIt.next();

		Map<Satellite, NoiseData> dataMap = Maps.newHashMap();

		for (Satellite satCur : satData.keySet()) { // iterate over all satellites
			TimePair tmp = getWindow(new Vector3d(satData.get(satCur).getLookupPosition().find(tPulse)),
					new Vector3d(hist.getPosition().find(tPulse))); // calculate the window times for the
			// current sat, current time
			double tDataHigh = tmp.tF + tPulse;
			double tDataLow = tmp.t0 + tPulse;
			boolean exec = false;
			TreeMap<Double, Integer> result = Maps.newTreeMap();
			result.clear();
			SampleIterator satIt = satIter.get(satCur);

			MeasermentSample ms = satIt.next();
			// check whether the window is out of bounds

			if (ms.getTime() > tDataLow) {
				// System.out.println("tDataLow out exceeds left margin"); //opt
				// System.out.println("tDataLow = " + tDataLow); //opt
				tDataLow = ms.getTime();
				// System.out.println("ms.getTime = " + tDataLow); //opt
			}
			if ((ms.getTime() + bigWindow) < tDataHigh) {
				logger.dbg("tDataHigh out exceeds right margin"); // opt
				logger.dbg("tDataHigh = %s ", tDataHigh); // opt
				tDataHigh = ms.getTime() + bigWindow;
				logger.dbg("ms.getTime() + bigwindow= %s", tDataHigh); // opt
			}

			double tIPWoffset = (bigWindow - (tDataHigh - tDataLow)) / 2; // offset from the highest time
			logger.dbg("tIPWoffset = %s", tIPWoffset);
			double tUpper = tIPWoffset + tDataHigh; // the highest time
			logger.dbg("tUppser = %s", tUpper);
			while (satIt.hasNext() && (ms.getTime() < tUpper)) { // Construct unfiltered result vector
				if (exec) {
					ms = satIt.next(); // ensure the increment is repeated once

				}
				exec = true;
				result.put(ms.getTime(), ms.getPhotons()); // put the results in the map

			} // END construction of the result vector;

			dataMap.put(satCur, new NoiseData(result, new TimePair(tDataLow, tDataHigh)));

		} // Stop ITerating over satellites;

		return dataMap;

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
			double tDataHigh = tmp.tF + tPulse;
			double tDataLow = tmp.t0 + tPulse;

			TreeMap<Double, Integer> result = Maps.newTreeMap();
			result.clear();
			SampleIterator satIt = satIter.get(satCur);

			MeasermentSample ms = satMsMap.get(satCur);
			// check whether the window is out of bounds
			logger.dbg("tDataLow = %s", tDataLow); // opt
			logger.dbg("tDataHigh = %s ", tDataHigh); // opt

			if (ms.getTime() > tDataLow) { // fit tDataLow into the window
				logger.dbg("tDataLow out exceeds left margin"); // opt
				tDataLow = ms.getTime();
				logger.dbg("ms.getTime = %s", tDataLow); // opt
			}
			if ((ms.getTime() + bigWindow) < tDataHigh) { // fit the tDataHigh into the bigWindow
				logger.dbg("tDataHigh out exceeds right margin"); // opt
				tDataHigh = ms.getTime() + bigWindow;
				logger.dbg("ms.getTime() + bigwindow= %s", tDataHigh); // opt
			}
			if (tDataLow > tDataHigh)
				tDataLow = tDataHigh;

			double tIPWoffset = (bigWindow - (tDataHigh - tDataLow)) / 2; // offset from the highest time
			logger.dbg("tIPWoffset = %s", tIPWoffset);
			double tUpper = tIPWoffset + tDataHigh; // the highest time
			logger.dbg("tUpper = %s", tUpper);

			while (satIt.hasNextNonZero() && (ms.getTime() < tUpper)) { // Construct unfiltered result
																		// vector
				result.put(ms.getTime(), ms.getPhotons()); // put the results in the map
				ms = satIt.nextNonZero(); //

			}
			if (tDataHigh > tUpper)
				tDataHigh = tUpper;

			// END construction of the result vector;
			satMsMap.put(satCur, ms); // store the last value
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

	private TimePair getWindow(Vector3d REC, Vector3d EMIT) {
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
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT));

		String flname = "sim_3sat";
		ProcessorTester tester = new ProcessorTester();
		RandData ret;
		try {
			ret = RandData.read(flname);
		} catch (FileNotFoundException e) {
			ret = tester.testProcessing();
			ret.write(flname);
		}

		Constellation testcon = new Constellation(23, 5000, ret.getEmHist().getEm(), Lists
				.newArrayList((ret.getRec().keySet())));
		// FindWindow testWindow = new FindWindow(ret.getEmHist(), ret.getRec(), testcon, (int) 5E8); //
		// 2ns
		// resolution
		// for (int i = 0; i < 5000; i++)
		// System.out.println(testWindow.next() + "\n ______________");

	}

}
