package com.google.code.laserswarm.Desim.filter.elevationslope;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.code.laserswarm.Desim.brdf.BRDFinput;
import com.google.code.laserswarm.Desim.elevation.slope.ElevationSlope;
import com.google.common.collect.Lists;

public class FilterSpikes implements ElevationSlopeFilter {
	private int		qLength			= 5;
	private int		middle			= 2;
	private double	equalitySpacing	= 0.1;

	private boolean areEqual(BRDFinput a, BRDFinput b) {
		if ((Math.abs(a.getAlongTrackSlope() - b.getAlongTrackSlope()) < equalitySpacing)
				& (Math.abs(a.getCrossTrackSlope() - b.getCrossTrackSlope()) < equalitySpacing)) {
			return true;
		} else {
			return false;
		}
	}

	public FilterSpikes(double whenEqual) {
		qLength = 3;
		middle = 1;
		equalitySpacing = whenEqual;
	}

	@Override
	public ElevationSlope filter(ElevationSlope elSlopeData) {
		LinkedList<BRDFinput> results = Lists.newLinkedList();
		LinkedList<BRDFinput> slopeData = elSlopeData.getBRDFIn();
		Iterator<BRDFinput> slopeIt = slopeData.iterator();
		LinkedList<BRDFinput> slopeQueue = Lists.newLinkedList();
		while (slopeIt.hasNext()) {
			slopeQueue.add(slopeIt.next());
			while (slopeQueue.size() > qLength) {
				slopeQueue.remove();
			}
			if (slopeQueue.size() == qLength) {
				BRDFinput one = slopeQueue.getFirst();
				BRDFinput two = slopeQueue.get(middle);
				BRDFinput three = slopeQueue.getLast();
				if (areEqual(one, three)) {
					if (areEqual(one, two)) {
						results.add(two);
					} else {
						double newAlongSlope = (one.getAlongTrackSlope() + three.getAlongTrackSlope()) / 2.0;
						double newCrossSlope = (one.getCrossTrackSlope() + three.getCrossTrackSlope()) / 2.0;
						results.add(new BRDFinput(two.getEmitterPosition(), two.getEmitterDirection(),
								two.getScatterPoint(), newAlongSlope, newCrossSlope,
								two.getReceiverPositions(), two.getCurrentTime()));
					}
				} else {
					results.add(slopeQueue.get(middle));
				}
			} else {
				results.add(slopeQueue.getFirst());
			}
		}
		results.add(slopeQueue.getLast());
		return new ElevationSlope(elSlopeData.getAltitudes(), results);
	}
}
