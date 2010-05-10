package com.google.code.laserswarm.process;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

public class SampleIterator implements Iterator<MeasermentSample> {
	private double						binFreqency;
	private double						binTime;

	private TreeMap<Double, Integer>	laserPhotons;
	PolynomialSplineFunction			noise;
	private double						time;

	public int							c		= 0;
	public boolean						found	= false;

	public SampleIterator(double binFreqency, TreeMap<Double, Integer> laser,
			PolynomialSplineFunction noise) {
		super();
		this.binFreqency = binFreqency;
		this.binTime = 1 / binFreqency;
		this.laserPhotons = laser;
		this.noise = noise;

		time = timeBlock(laserPhotons.firstKey() + binTime);
	}

	@Override
	public boolean hasNext() {
		return ((laserPhotons.ceilingKey(timeBlock(time + 2 * binTime))) != null);
	}

	@Override
	public MeasermentSample next() {
		time += binTime;
		try {
			Integer photons = 0;
			Entry<Double, Integer> closest = laserPhotons.floorEntry(time);
			if (timeToBlock(time) == timeToBlock(closest.getKey())) {
				photons = closest.getValue();
				c++;
				found = true;
			} else
				found = false;

			int noisePhotons = (int) noise.value(time);
			return new MeasermentSample(time, photons + noisePhotons);
		} catch (ArgumentOutsideDomainException e) {
			return null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private double timeBlock(double time) {
		return Math.round(time / binTime) * binTime;
	}

	private long timeToBlock(double time) {
		long block = Math.round((time) / binTime);
		return block;
	}

}