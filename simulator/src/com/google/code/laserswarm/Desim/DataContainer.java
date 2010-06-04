/**
 * 
 */
package com.google.code.laserswarm.Desim;

import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Nikita Oborin
 * 
 */
public class DataContainer {
	LinkedList<NoiseData>	Data		= Lists.newLinkedList();
	int						queueLength	= 9;

	public LinkedList<NoiseData> getData() {
		return Data;
	}

	public void add(NoiseData nd) {
		if(nd.hasData())
		    Data.add(nd);
		while (Data.size() > queueLength) {
			Data.removeFirst();
		}
	}

	public void setQueueLength(int queueLength) {
		this.queueLength = queueLength;
	}

	public int getQueueLength() {
		return queueLength;
	}

	/**
	 * @param tFrame
	 *            TimePair representing initial time and final time to be found in the list of NoiseData
	 * @return Sublist with all found data
	 */
	public LinkedList<NoiseData> getRange(TimePair tFrame) {
		int pos0 = 0;
		int posF = Integer.MAX_VALUE;

		for (NoiseData ndIterator : Data) {
			if (ndIterator.contains(tFrame.t0))
				pos0 = Data.indexOf(ndIterator);
			if (ndIterator.contains(tFrame.tF))
				posF = Data.indexOf(ndIterator);
		}
		return new LinkedList<NoiseData>(Data.subList(pos0, posF));
	}

	public Map<Double, Integer> getNoise(TimePair tFrame) {
		LinkedList<NoiseData> sampList = getRange(tFrame);
		Map<Double, Integer> sumMaps = Maps.newHashMap();
		for (NoiseData ndIterator : sampList)
			if(ndIterator.hasNoise())				
			sumMaps.putAll(ndIterator.getNoise());
		return sumMaps;
	}

	public Map<Double, Integer> getData(TimePair tFrame) {
		LinkedList<NoiseData> sampList = getRange(tFrame);
		Map<Double, Integer> sumMaps = Maps.newHashMap();
		for (NoiseData ndIterator : sampList)
			if(ndIterator.hasData())	//REDUNDANT
			sumMaps.putAll(ndIterator.getData());
		return sumMaps;
	}
}
