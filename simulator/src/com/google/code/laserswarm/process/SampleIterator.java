package com.google.code.laserswarm.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.SimulationTester;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SampleIterator implements Iterator<MeasermentSample> {

	private double						binTime;
	private double						time;
	private Double						nextSunPhotonBin	= null;

	private TreeMap<Double, Integer>	laserPhotons;
	private TreeMap<Double, Double>		noise;

	private static final Logger			logger				= Logger.get(SampleIterator.class);

	public static void main(String[] args) throws MathException {
		Configuration.getInstance().getMode().remove(Actions.DEM_CACHE);
		HashMap<SimTemplate, Simulator> results = new SimulationTester().sim();
		SimTemplate tmpl = results.keySet().iterator().next();
		Simulator sim = results.get(tmpl);
		List<SimVars> data = sim.getDataPoints();
		Satellite sat = data.iterator().next().pE.keySet().iterator().next();
		TimeLine timeLine = new TimeLine(sat, tmpl.getConstellation(), data);
		SampleIterator it = timeLine.getIterator((int) 1E8);
		while (it.hasNextNonZero()) {
			MeasermentSample measermentSample = it.nextNonZero();
			logger.inf(measermentSample.toString());
		}
	}

	public SampleIterator(double binFreqency, TreeMap<Double, Integer> laser,
			TreeMap<Double, Double> noise) {
		this.binTime = 1 / binFreqency;
		this.laserPhotons = laser;
		this.noise = noise;

		time = timeBlock(laserPhotons.firstKey() + binTime);
	}

	public double endTime() {
		return laserPhotons.lastKey();
	}

	private MeasermentSample getMeasermentSample(double startT, double endT) {
		Integer photons = 0;
		SortedMap<Double, Integer> values = laserPhotons.subMap(startT, endT);
		for (Integer photonsSample : values.values())
			photons += photonsSample;

		double t = noise.floorEntry(getTime()).getValue();
		// double t = integrator.integrate(noise, startT, endT);
		t *= binTime;
		int noisePhotons = (int) t;
		if (Math.random() <= t - noisePhotons)
			noisePhotons++;

		return new MeasermentSample(timeBlock(endT), photons + noisePhotons);
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
		return getMeasermentSample(getTime(), getTime() + binTime);
	}

	public MeasermentSample nextNonZero() {
		double newTime = getTime() + binTime;
		double nextPulsePhotonTime = laserPhotons.ceilingKey(newTime);
		double nextPulsePhotonBin = timeBlock(nextPulsePhotonTime);
		if (nextSunPhotonBin == null) {
			double r = Math.random();
			nextSunPhotonBin = timeBlock(getTime() + (2 * r)
						/ noise.floorEntry(getTime()).getValue());
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
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void setTime(double time) {
		if (time < laserPhotons.firstKey())
			throw new RuntimeException("Cannot set Iterator Time under the start time !!");

		this.time = time;
	}

	public double startTime() {
		return laserPhotons.firstKey();
	}

	private double timeBlock(double time) {
		return Math.round(time / binTime) * binTime;
	}

}