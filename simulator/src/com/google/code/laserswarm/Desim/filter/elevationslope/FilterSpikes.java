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

	public FilterSpikes(int queueLength, double whenEqual) {
		qLength = queueLength;
		middle = (int) Math.floor(((double) qLength) / 2.0);
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
				Iterator<BRDFinput> slopeQIt = slopeQueue.iterator();
				int count = 0;
				BRDFinput last = null;
				BRDFinput current = null;
				boolean stillEqual = true;
				while (slopeQIt.hasNext()) {
					last = current;
					current = slopeQIt.next();
					if (count == 0) {
						last = current;
						current = slopeQIt.next();
						count++;
					} else if (count == middle) {
						current = slopeQIt.next();
						count++;
					}
					if (!areEqual(last, current)) {
						stillEqual = false;
					}
					count++;
				}
				if (stillEqual) {
					if (areEqual(slopeQueue.getFirst(), slopeQueue.get(middle))) {
						results.add(slopeQueue.get(middle));
					} else {
						slopeQIt = slopeQueue.iterator();
						double totalAlongSlope = 0;
						double totalCrossSlope = 0;
						double slopeNo = 0;
						while (slopeQIt.hasNext()) {
							BRDFinput localBRDFIn = slopeQIt.next();
							double localAlongSlope = localBRDFIn.getAlongTrackSlope();
							double localCrossSlope = localBRDFIn.getCrossTrackSlope();
							if (!(slopeNo == middle)) {
								totalAlongSlope += localAlongSlope;
								totalCrossSlope += localCrossSlope;
							}
							slopeNo++;
						}
						BRDFinput midPt = slopeQueue.get(middle);
						results.add(new BRDFinput(midPt.getEmitterPosition(), midPt
								.getEmitterDirection(), midPt.getScatterPoint(), totalAlongSlope
								/ (slopeNo - 1), totalCrossSlope / (slopeNo - 1), midPt
								.getReceiverPositions(), midPt.getCurrentTime()));
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
