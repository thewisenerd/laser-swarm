package com.google.code.laserswarm.process;

import jat.cm.Constants;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math.distribution.Distribution;

import com.google.common.collect.Maps;

public class TimeLine {

	private double						t0;
	private double						tE;

	private TreeMap<Double, Integer>	photons	= Maps.newTreeMap();

	public TimeLine(double t0, double tE) {
		this.t0 = t0;
		this.tE = tE;
	}

	public void addMeaserment(double t, int count) {
		if (photons.containsKey(t))
			photons.put(t, photons.get(t) + count);
		else
			photons.put(t, count);
	}

	public void addMeaserments(Map<Double, Integer> counts) {
		for (Double t : counts.keySet()) {
			addMeaserment(t, counts.get(t));
		}
	}

	public void addNoise(double tStart, double tEnd, Distribution probability, double Pr, double lambda) {
		double ePhoton = (Constants.c * 6.62606896E-34) / lambda;
		double dT = 1E-3;

		double t = tStart;
		while (t < tEnd) {
			double energy = Pr * dT;
			int nrP = (int) Math.floor(energy / ePhoton);
			if (Math.random() < (energy / ePhoton) - nrP)
				nrP++;
			photons.put(t, nrP);
			t += dT;
		}

	}

}
