package com.google.code.laserswarm.core;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

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
import com.google.code.laserswarm.util.CSVwriter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Optimize extends LaserSwarm implements MultivariateRealFunction {

	private static final Logger	logger	= Logger.get(Optimize.class);

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT));

		Prospector.roughTimeStep = 3;

		final Optimize sim = new Optimize();
		sim.POWER_POWER = 1;
		sim.APERTURE_POWER = 1.5;
		sim.optimize();
	}

	private static Constellation mkConstellation(double power, double aperature) {
		return Constellation.swarm(power, aperature, 500);
	}

	private double						POWER_POWER		= 1;

	private double						APERTURE_POWER	= 1;

	private int							photons			= 0;
	private CSVwriter					prefLog;
	private TreeMap<Satellite, Integer>	satPhotons;

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
		optimizer.setMaxIterations(1000);
		optimizer.setMaxEvaluations(1000);

		prefLog = new CSVwriter(new File(String
				.format("optimize-%f-%f.csv", POWER_POWER, APERTURE_POWER)), "\t");
		try {
			RealPointValuePair values = optimizer.optimize(this, GoalType.MAXIMIZE,
							new double[] { 5,
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
		satPhotons = Maps.newTreeMap();
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

		logger.inf("Running iteration for %s W and %s m²", power, aperature);
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

		NormalDistributionImpl gausian = new NormalDistributionImpl(800 * satPhotons.size(), 100);
		double performace = 0;
		try {
			performace = 1 / (Math.pow(power, POWER_POWER) * Math.pow(aperature, APERTURE_POWER));
			performace *= gausian.cumulativeProbability(photons);
			for (Satellite sat : satPhotons.keySet()) {
				NormalDistribution gausian2 = new NormalDistributionImpl(800, 500);
				double modifier = gausian2.cumulativeProbability(satPhotons.get(sat));
				performace *= modifier;
			}
		} catch (MathException e) {
			e.printStackTrace();
			System.exit(1);
		}
		logger.inf("Performance: %s (%s photons)", performace, photons);

		List<Double> values = Lists.newLinkedList();
		values.add(power);
		values.add(aperature);
		values.addAll(Collections2.transform(satPhotons.values(), new Function<Integer, Double>() {
			@Override
			public Double apply(Integer from) {
				return new Double(from);
			}
		}));
		values.add(performace);
		prefLog.write(values.toArray(new Double[] {}));

		return performace;
	}
}
