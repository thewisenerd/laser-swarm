package com.google.code.laserswarm.process;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class SampleIterator implements Iterator<MeasermentSample> {

	private double						binTime;

	private TreeMap<Double, Integer>	laserPhotons;
	PolynomialSplineFunction			noise;
	private double						time;

	@Deprecated
	public int							c		= 0;
	@Deprecated
	public boolean						found	= false;

	private static final Logger			logger	= Logger.get(SampleIterator.class);

	public SampleIterator(double binFreqency, TreeMap<Double, Integer> laser,
			PolynomialSplineFunction noise) {
		super();
		this.binTime = 1 / binFreqency;
		this.laserPhotons = laser;
		this.noise = noise;

		time = timeBlock(laserPhotons.firstKey() + binTime);
	}

	public double endTime() {
		return laserPhotons.lastKey();
	}

	@Override
	public boolean hasNext() {
		return hasNext(2);
	}

	public boolean hasNext(int n) {
		return ((laserPhotons.ceilingKey(timeBlock(time + n * binTime))) != null);
	}

	@Override
	public MeasermentSample next() {
		time += binTime;
		try {
			found = false;
			Integer photons = 0;
			SortedMap<Double, Integer> values = laserPhotons.subMap(time, time + binTime);
			for (Integer photonsSample : values.values()) {
				c++;
				photons += photonsSample;
				found = true;
			}

			double t = noise.value(time);
			int noisePhotons = (int) t;
			if (Math.random() <= t - noisePhotons)
				noisePhotons++;
			return new MeasermentSample(time, photons + noisePhotons);
		} catch (ArgumentOutsideDomainException e) {
			return null;
		}
	}

	public MeasermentSample nextNonZero() {
		MeasermentSample nxt = null;
		while (hasNext()) {
			nxt = next();
			if (nxt.getPhotons() > 0)
				return nxt;
		}
		return new MeasermentSample(nxt.getTime(), 1);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public double startTime() {
		return laserPhotons.firstKey();
	}

	private double timeBlock(double time) {
		return Math.round(time / binTime) * binTime;
	}

}