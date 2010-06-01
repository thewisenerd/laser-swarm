package com.google.code.laserswarm.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.simulation.Prospector;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Optimize extends LaserSwarm implements MultivariateRealFunction {

	private class PrefLog {
		private File	log	= new File("optimize.csv");

		public PrefLog() {

			try {
				log.delete();
				log.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void write(Double[] values) {
			try {
				StringBuffer str = new StringBuffer();
				for (int i = 0; i < values.length; i++) {
					str.append(values[i]);
					if (i + 1 < values.length)
						str.append(",\t");
				}
				Files.append(str + "\n", log, Charset.defaultCharset());
			} catch (IOException e) {
				logger.wrn(e, "Could not write to preflog");
			}
		}
	}

	private static final Logger	logger	= Logger.get(Optimize.class);

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT));

		Prospector.roughTimeStep = 3;

		Optimize sim = new Optimize();
		sim.optimize();
	}

	private static Constellation mkConstellation(double power, double aperature) {
		return Constellation.swarm(power, aperature, 500);
	}

	private int							photons	= 0;
	private PrefLog						prefLog	= new PrefLog();
	private HashMap<Satellite, Integer>	satPhotons;

	@Override
	protected List<Constellation> mkConstellations() {
		List<Constellation> constellations = Lists.newLinkedList();
		return constellations;
	}

	@Override
	protected List<SimTemplate> mkTemplates(Constellation constellation) {
		LinkedList<SimTemplate> tmpls = Lists.newLinkedList();
		tmpls.add(new SimTemplate(constellation, 1000L));
		return tmpls;
	}

	private void optimize() {
		NelderMead optimizer = new NelderMead();
		try {
			RealPointValuePair values = optimizer.optimize(this, GoalType.MAXIMIZE, new double[] { 5,
					0.075 * 0.075 });

			for (Double val : values.getPoint())
				logger.inf("Value %s", val);

			logger.inf("Result %s", values.getValue());
		} catch (MathException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void simulate() {
		super.simulate();
		photons = 0;
		satPhotons = Maps.newHashMap();
		StringBuffer str = new StringBuffer();
		for (SimTemplate tmpl : simulations.keySet()) {
			for (Satellite sat : tmpl.getConstellation().getReceivers()) {
				int satP = 0;
				for (SimVars var : simulations.get(tmpl).getDataPoints())
					satP += var.photonsE.get(sat);
				photons += satP;
				satPhotons.put(sat, satP);
				str.append(String.format("%s=>%d\t", sat, satP));
			}
		}
		logger.inf(str.toString());
	}

	@Override
	public double value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
		double power = point[0];
		double aperature = point[1];

		logger.inf("Running iteration for %s W and %s m�", power, aperature);
		if (power < 0 || aperature < 0) {
			logger.inf("Negative power or aperture");
			logger.inf("Performance: 0");
			return 0;
		}

		constellations.clear();
		constellations.add(mkConstellation(power, aperature));

		run();

		for (Simulator sim : simulations.values())
			sim.getDataPointsDB().close();

		NormalDistributionImpl gausian = new NormalDistributionImpl(1000 * satPhotons.size(), 100);
		double performace = 0;
		try {
			performace = (gausian.cumulativeProbability(photons) * 100)
					/ (power * aperature * aperature);
			for (Satellite sat : satPhotons.keySet()) {
				NormalDistribution gausian2 = new NormalDistributionImpl(1000, 300);
				double modifier = 500 * gausian2.cumulativeProbability(satPhotons.get(sat));
				performace *= modifier;
			}
		} catch (MathException e) {
			e.printStackTrace();
			System.exit(1);
		}
		logger.inf("Performance: %s (%s photons)", performace, photons);

		prefLog.write(new Double[] { power, aperature, performace });

		return performace;
	}
}
