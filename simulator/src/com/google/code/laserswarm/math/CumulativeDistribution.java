package com.google.code.laserswarm.math;

import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.geometry.Vector3D;

import com.google.common.collect.Lists;

public class CumulativeDistribution implements Distribution {

	private Distribution		mainDistribution;
	private List<Distribution>	terms	= Lists.newLinkedList();
	private List<Distribution>	factors	= Lists.newLinkedList();

	@Override
	public double probability(Vector3D x) throws MathException {
		double probability = mainDistribution.probability(x);

		for (Distribution factor : factors)
			probability *= factor.probability(x);

		for (Distribution term : terms)
			probability *= term.probability(x);

		return probability;
	}

}
