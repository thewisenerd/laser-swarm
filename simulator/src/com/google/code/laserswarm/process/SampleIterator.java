package com.google.code.laserswarm.process;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class SampleIterator implements Iterator<MeasermentSample> {

	private double						binTime;

	private TreeMap<Double, Integer>	laserPhotons;
	UnivariateRealFunction				noise;
	private double						time;

	@Deprecated
	public int							c					= 0;
	@Deprecated
	public boolean						found				= false;

	private static final Logger			logger				= Logger.get(SampleIterator.class);

	private SimpsonIntegrator			integrator			= new SimpsonIntegrator();
	private double						startT;
	private Double						nextSunPhotonBin	= null;

	public SampleIterator(double binFreqency, TreeMap<Double, Integer> laser,
			UnivariateRealFunction noise) {
		super();
		this.binTime = 1 / binFreqency;
		this.laserPhotons = laser;
		this.noise = noise;

		startT = timeBlock(laserPhotons.firstKey() + binTime);
		time = startT;
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

			double t = noise.value(getTime());
			// double t = integrator.integrate(noise, startT, endT);
			t *= binTime;
			int noisePhotons = (int) t;
			if (Math.random() <= t - noisePhotons)
				noisePhotons++;

			return new MeasermentSample(timeBlock(endT), photons + noisePhotons);
		} catch (Exception e) {
			throw new MathException(e);
		}
	}

	private double getTime() {
		return time;
	}

	@Override
	public boolean hasNext() {
		return hasNext(3);
	}

	public boolean hasNext(int n) {
		boolean r = ((laserPhotons.ceilingKey(timeBlock(getTime() + n * binTime))) != null);
		return r;
	}

	public boolean hasNextNonZero() {
		return getTime() < endTime();
	}

	@Override
	public MeasermentSample next() {
		setTime(getTime() + binTime);
		try {
			return getMeasermentSample(getTime(), getTime() + binTime);
		} catch (MathException e) {
			return null;
		}
	}

	public MeasermentSample nextNonZero() {
		double newTime = getTime() + binTime;
		try {
			double nextPulsePhotonTime = laserPhotons.ceilingKey(newTime);
			double nextPulsePhotonBin = timeBlock(nextPulsePhotonTime);
			if (nextSunPhotonBin == null) {
				double r = Math.random();
				nextSunPhotonBin = timeBlock(getTime() + (2 * r) / noise.value(newTime));
			}

			setTime(Math.min(nextPulsePhotonBin, nextSunPhotonBin));

			if (nextPulsePhotonBin == nextSunPhotonBin) {
				// We re so darn unlucky, two at the exact same time (bin) -_-
				nextSunPhotonBin = null;
				return new MeasermentSample(getTime(), laserPhotons.get(nextPulsePhotonTime) + 1);
			} else if (nextPulsePhotonBin < nextSunPhotonBin) {
				// The first pulse sample
				return new MeasermentSample(getTime(), laserPhotons.get(nextPulsePhotonTime));
			} else {
				// The first noise sample
				nextSunPhotonBin = null;
				return new MeasermentSample(getTime(), 1);
			}

		} catch (MathException e) {
			logger.inf(e, "Math");
			return new MeasermentSample(newTime, 1);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void setTime(double time) {
		if (time == 0)
			logger.wrn("aarrgg");
		this.time = time;
	}

	public double startTime() {
		return laserPhotons.firstKey();
	}

	private double timeBlock(double time) {
		return Math.round(time / binTime) * binTime;
	}

}