/**
 * 
 */
package com.google.code.laserswarm.Desim;

import java.util.TreeMap;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * @author Administrator Class that holds the data for the iterpulse window per receiver
 */

public class NoiseData {

	public TimePair getNoiseFrameL() {
		return noiseFrameL;
	}

	public TimePair getNoiseFrameR() {
		return noiseFrameR;
	}

	public TimePair getDataFrame() {
		return dataFrame;
	}

	public TimePair getWindowFrame() {
		return windowFrame;
	}

	public TreeMap<Double, Integer> getNoise() {
		return noise;
	}

	public TreeMap<Double, Integer> getData() {
		return data;
	}

	private static final Logger			logger	= Logger.get(NoiseData.class);

	private TimePair					noiseFrameL;							// points to the first
	// and last element of
	// the left noise region
	// on interpulse window
	private TimePair					noiseFrameR;							// points to the first
	// and last element of
	// the right noise region
	// on interpulse window
	private TimePair					dataFrame;								// points to the data
	// part of the interpulse
	// window
	private TimePair					windowFrame;							// points ot the
	// beginning and end of
	// the interpulse window
	// window
	private TreeMap<Double, Integer>	noise;
	private TreeMap<Double, Integer>	data;

	// private NoiseData next; //points to the next NoiseData element
	// private NoiseData prev; //point to the previous element;

	/**
	 * 
	 */
	public NoiseData(TreeMap<Double, Integer> interpulseData, TimePair dataWindow) {
try{
	windowFrame = new TimePair(interpulseData.firstKey(), interpulseData.lastKey());
}catch(Exception E){
	logger.dbg("interpulseData is empty!");
		windowFrame = new TimePair(0.0,0.0);
		dataFrame = new TimePair(0.0,0.0);
		noiseFrameL = new TimePair(0.0,0.0);
		noiseFrameR = new TimePair(0.0,0.0);
		noise = Maps.newTreeMap();
		noise.put(0.0, 0);
		data = Maps.newTreeMap();
		data.put(0.0, 0);
		return;
		
};
		dataFrame = new TimePair(dataWindow.t0Ref, dataWindow.tFRef);
		noiseFrameL = new TimePair(windowFrame.t0Ref, dataFrame.t0Ref);
		noiseFrameR = new TimePair(dataFrame.tFRef, windowFrame.tFRef);
		logger.dbg("dataFrame: %s", dataFrame);
		logger.dbg("windowFrame: %s", windowFrame);
		logger.dbg("noiseFrameL: %s", noiseFrameL);
		logger.dbg("noiseFrameR: %s", noiseFrameR);
		TreeMap<Double, Integer> noiseTMP = Maps.newTreeMap();
		TreeMap<Double, Integer> dataTMP = Maps.newTreeMap();
		if(dataFrame.diff() != 0){
		dataTMP = Maps.newTreeMap(interpulseData.subMap(dataFrame.t0, true, dataFrame.tF, true));// create
		}
		else{
			dataTMP = Maps.newTreeMap();
		}
		// a
		// treem
		if(noiseFrameL.diff() != 0){
			
		noiseTMP.putAll(Maps.newTreeMap(interpulseData.subMap(windowFrame.t0Ref, true, dataFrame.t0Ref,
				false)));
		}
		else{
			noiseTMP = Maps.newTreeMap();
		}

		try {
			TreeMap<Double,Integer> tmp = Maps.newTreeMap(interpulseData.subMap(dataFrame.tFRef, false,
					windowFrame.tFRef, false));
			noiseTMP.putAll(tmp);
		} catch (Exception e) {
			logger.inf(e, "Reached end of array");

		}

		Predicate<Integer> filt = Predicates.not(Predicates.equalTo(new Integer(0))); // create a filter
		// that checks
		// for 0
		noise = new TreeMap<Double, Integer>(Maps.filterValues(noiseTMP, filt)); // apply filter
		data = new TreeMap<Double, Integer>(Maps.filterValues(dataTMP, filt));

	}

	/**
	 * 
	 * @param time
	 *            Checks whether the time is within the current NoiseData instance
	 */

	public boolean contains(double time) {
		return (windowFrame.t0 <= time && windowFrame.tF > time);

	}

	@Override
	public String toString() {
		return "\n tFrac:" + dataFrame.diff() / windowFrame.diff() + "\n" + "beg: " + noiseFrameL.diff()
				/ windowFrame.diff() + "\n end:" + (1 - noiseFrameR.diff() / windowFrame.diff());
		// ""nL, nR frac: " + noiseFrameL.diff() + " \t" + noiseFrameR.diff();
	}
}
