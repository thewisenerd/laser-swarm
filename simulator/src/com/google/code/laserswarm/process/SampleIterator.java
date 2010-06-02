package com.google.code.laserswarm.process;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class SampleIterator implements Iterator<MeasermentSample> {

	private double						binTime;

	private TreeMap<Double, Integer>	laserPhotons;
	PolynomialSplineFunction			noise;
	private double						time;

	@Deprecated
	public int							c				= 0;
	@Deprecated
	public boolean						found			= false;

	private static final Logger			logger			= Logger.get(SampleIterator.class);

	private SimpsonIntegrator			integrator		= new SimpsonIntegrator();
	private boolean						endNextNonZero	= false;

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

	private MeasermentSample getMeasermentSample(double startT, double endT) throws MathException {
		try {
			Integer photons = 0;
			SortedMap<Double, Integer> values = laserPhotons.subMap(startT, endT);
			for (Integer photonsSample : values.values()) {
				c++;
				photons += photonsSample;
				found = true;
			}

			double t = noise.value(time);
			// double t = integrator.integrate(noise, startT, endT);
			t *= binTime;
			int noisePhotons = (int) t;
			if (Math.random() <= t - noisePhotons)
				noisePhotons++;

			return new MeasermentSample(binTime * timeBlock(endT), photons + noisePhotons);
		} catch (Exception e) {
			throw new MathException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return hasNext(3);
	}

	public boolean hasNext(int n) {
		return ((laserPhotons.ceilingKey(timeBlock(time + n * binTime))) != null);
	}

	public boolean hasNextNonZero() {
		return time < endTime();

	}

	@Override
	public MeasermentSample next() {
		time += binTime;
		try {
			return getMeasermentSample(time, time + binTime);
		} catch (MathException e) {
			return null;
		}
	}

	public MeasermentSample nextNonZero() {
		double newTime = time + binTime;
		try {
			double nextPulsePhotonTime = laserPhotons.ceilingKey(newTime);
			double nextPulsePhotonBin = timeBlock(nextPulsePhotonTime);
			double nextSunPhotonBin = timeBlock((2 * Math.random()) / noise.value(newTime));

			time = Math.min(nextPulsePhotonBin, nextSunPhotonBin);

			if (nextPulsePhotonBin == nextSunPhotonBin)
				// We re so darn unlucky, two at the exact same time (bin) -_-
				return new MeasermentSample(time, laserPhotons.get(nextPulsePhotonTime) + 1);
			else if (nextPulsePhotonBin < nextSunPhotonBin)
				// The first pulse sample
				return new MeasermentSample(time, laserPhotons.get(nextPulsePhotonTime));
			else
				// The first noise sample
				return new MeasermentSample(time, 1);

		} catch (MathException e) {
			return new MeasermentSample(newTime, 1);
		}
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