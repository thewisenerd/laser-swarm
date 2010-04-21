package com.google.code.laserswarm.math;

import java.util.List;

import org.apache.commons.math.geometry.Vector3D;

import com.google.common.collect.Lists;

/**
 * A distribution that can be modified using multipliers and additives.
 * 
 * <p>
 * Evaluates a distribution in the form of:
 * 
 * <pre>
 * 		f(x) = &Pi;( f_factor(x) ) * f_base(x) + &Sigma; ( f_term(x) )
 * </pre>
 * 
 * </p>
 * 
 * @author Simon Billemont, TUDelft, Faculty Aerospace Engineering (aodtorusan@gmail.com or
 *         s.billemont@student.tudelft.nl)
 * 
 */
public class CumulativeDistribution implements Distribution {

	private Distribution		mainDistribution;
	private List<Distribution>	terms	= Lists.newLinkedList();
	private List<Distribution>	factors	= Lists.newLinkedList();

	@Override
	public double probability(Vector3D x) {
		double probability = mainDistribution.probability(x);

		for (Distribution factor : factors)
			probability *= factor.probability(x);

		for (Distribution term : terms)
			probability *= term.probability(x);

		return probability;
	}

}
