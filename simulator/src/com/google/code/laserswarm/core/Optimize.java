package com.google.code.laserswarm.core;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Optimize extends LaserSwarm implements MultivariateRealFunction {

	private static final Logger	logger	= Logger.get(Optimize.class);

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT));

		Prospector.roughTimeStep = 3;

		Optimize sim = new Optimize();
		sim.optimize();
	}

	@Override
	protected List<SimTemplate> mkTemplates(Constellation constellation) {
		LinkedList<SimTemplate> tmpls = Lists.newLinkedList();
		tmpls.add(new SimTemplate(constellation, 1000L));
		return tmpls;
	}

	private int	photons	= 0;

	private void optimize() {
		NelderMead optimizer = new NelderMead();
		try {
			RealPointValuePair values = optimizer.optimize(this, GoalType.MAXIMIZE, new double[] { 5,
					0.02 * 0.02 });

			for (Double val : values.getPoint())
				logger.inf("Value %s", val);

			logger.inf("Result %s", values.getValue());
		} catch (MathException e) {
			e.printStackTrace();
		}
	}

	private static Constellation mkConstellation(double power, double aperature) {
		Satellite emittor = new Satellite("Emittor", aperature, (float) Configuration.R0 / 1000 + 500,
				0f, (float) Math.PI / 2, (float) (8.5 * Math.PI / 180), 0f, 0f);

		LinkedList<Satellite> r = Lists.newLinkedList();
		r.add(new Satellite("Receiver #1", emittor));

		Constellation c = new Constellation(power, 5000, emittor, r);
		c.setName(String.format("Constellation"));
		return c;
	}

	@Override
	protected List<Constellation> mkConstellations() {
		List<Constellation> constellations = Lists.newLinkedList();
		return constellations;
	}

	@Override
	protected void simulate() {
		super.simulate();
		photons = 0;
		for (SimTemplate tmpl : simulations.keySet()) {
			long samples = 0;
			for (Satellite sat : tmpl.getConstellation().getReceivers()) {
				samples += simulations.get(tmpl).getDataPoints().size();
				for (SimVars var : simulations.get(tmpl).getDataPoints())
					photons += var.photonsE.get(sat);
			}
		}
	}

	@Override
	public double value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
		double power = point[0];
		double aperature = point[1];

		logger.inf("Running iteration for %s W and %s m²", power, aperature);
		constellations.clear();
		constellations.add(mkConstellation(power, aperature));

		run();

		double performace = (photons) / (power * aperature * aperature);
		logger.inf("Performence: %s", performace);

		return performace;
	}
}
