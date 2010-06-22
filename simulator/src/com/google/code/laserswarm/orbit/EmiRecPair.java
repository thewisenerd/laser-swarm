package com.google.code.laserswarm.orbit;

import java.util.Iterator;
import java.util.Map;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.MeasermentSample;
import com.google.code.laserswarm.process.SampleIterator;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Maps;

public class EmiRecPair {

	/**
	 * @param args
	 */
	EmitterHistory					hist;
	double							tWindow;	// window time
	double							tOffset;	// offset time
	Iterator<Double>				timeIt;
	Map<Satellite, SampleIterator>	photIt;
	public double					tPulse;

	public EmiRecPair(double offset, double window, EmitterHistory hist,
			Map<Satellite, SampleIterator> sit) {
		// TODO Auto-generated constructor stub
		tOffset = offset;
		tWindow = window;
		this.hist = hist;
		photIt = sit;
		timeIt = hist.time.iterator();
		//timeIt.
		//tPulse = timeIt.
	}

	public void nexdt() {
		// TODO Auto-generated method stub
		// 
	}

	public Map<Satellite, Map<Double, Integer>> next() { // return window of values
		// TODO Auto-generated method stub
		// 
		tPulse = timeIt.next();
		Map<Satellite, Map<Double, Integer>> satmap = Maps.newHashMap();
		// double tPulse = hist.getPulseClosesTo(tCur); //pulse time of the closest pulse;
		// find spikes untill time, and before time
		double tHigh = tPulse + tOffset + 1.0 / 2.0 * tWindow;
		double tLow = tPulse + tOffset - 1.0 / 2.0 * tWindow;

		for (Satellite isat : photIt.keySet()) {	//iterate over  all satellites
			boolean exec = false;
			Map<Double, Integer> fresult = Maps.newHashMap();
			Map<Double, Integer> result = Maps.newHashMap();
						
			fresult.clear();
			result.clear();
			
			SampleIterator satIt = photIt.get(isat);
			MeasermentSample ms = satIt.next();
			
			// photIt.next();
			int size = 0;
			double sum = 0;
			int count = 0;
			System.out.println("High Time " + tHigh);
			System.out.println("MsTime: " + ms.getTime());
			
			while (satIt.hasNext() & (ms.getTime() < tHigh)) { //Construct unfiltered result vector
				// System.out.println("Loop cycle " + count++);
				if(exec){
					ms = satIt.next();		//ensure nothing is repeated
					
				}
				exec = true;
				result.put(ms.getTime(), ms.getPhotons()); // put the results in the map
				size++;
				sum =+ ms.getPhotons();
			}	//END consturcting resutl vector;
			
			// System.out.println(result + "result");
			double average = sum / (double) size;
			if(average == 0) average = 1;
			System.out.println("average photons: " + average);
			for (Map.Entry<Double, Integer> temp : result.entrySet()) {
				if (temp.getValue() > 2)
					fresult.put(temp.getKey(), temp.getValue());
			}
			satmap.put(isat, fresult);
		} // Stop ITerating over satellites;
		System.out.println("filtered data: " + satmap);
		return satmap;
		// (tPulse+tOffset+1/2*tWindow);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		// EmiRecPair a = new EmiRecPair(3.0, 0.5, hist, sit)

	}

}
