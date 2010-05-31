package com.google.code.laserswarm.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.out.Report;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.simulation.postSimulation.SlopeSpread;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public abstract class LaserSwarm {
	private static final Logger					logger			= Logger.get(LaserSwarm.class);

	protected HashMap<SimTemplate, Simulator>	simulations;
	protected List<Constellation>				constellations	= mkConstellations();

	protected double[][]						timeSteps		= { { 0, 500000 } };

	protected EarthModel						earth;

	public void run() {
		if (Configuration.hasAction(Actions.SLEEP)) {
			logger.inf("Sleeping ...");
			System.exit(0);
		}

		/* Simulation */
		if (Configuration.hasAction(Actions.SIMULATE) || //
				Configuration.hasAction(Actions.PROCESS) || //
				Configuration.hasAction(Actions.PLOT_DISK) || //
				Configuration.hasAction(Actions.PLOT_SCREEN) || //
				Configuration.hasAction(Actions.TABULATE)) {
			simulate();
		}

		/* Post simulation modifiers */
		for (SimTemplate tmpl : simulations.keySet()) {
			if (Configuration.hasAction(Actions.DISTRIBUTE_SLOPE)) {
				Simulator sim = simulations.get(tmpl);
				SlopeSpread spread = new SlopeSpread();
				simulations.put(tmpl, spread.modify(sim, tmpl.getConstellation()));
			}

		}

		/* Processing */
		if (Configuration.hasAction(Actions.PROCESS) || //
				Configuration.hasAction(Actions.PLOT_DISK) || //
				Configuration.hasAction(Actions.PLOT_SCREEN) || //
				Configuration.hasAction(Actions.TABULATE)) {
			process();
		}

		/* Make some reports */
		if (Configuration.hasAction(Actions.PLOT_DISK) || //
				Configuration.hasAction(Actions.PLOT_SCREEN) || //
				Configuration.hasAction(Actions.TABULATE)) {
			Report.write(simulations);
		}

	}

	protected abstract List<Constellation> mkConstellations();

	protected void process() {
		// throw new UnsupportedOperationException();
	}

	protected void simulate() {
		if (earth == null)
			mkEarth();
		SimulatorMaster simMaster = new SimulatorMaster(earth);

		for (Constellation constellation : constellations)
			simMaster.addSimTemplates(mkTemplates(constellation));

		simulations = simMaster.runSim();
	}

	protected List<SimTemplate> mkTemplates(Constellation constellation) {
		LinkedList<SimTemplate> tmpls = Lists.newLinkedList();

		for (double[] timeRange : timeSteps) {
			SimTemplate template = new SimTemplate(constellation);
			template.setTime(timeRange[0], timeRange[1]);
			tmpls.add(template);
		}

		return tmpls;
	}

	protected void mkEarth() {
		earth = EarthModel.getDefaultModel();
	}
}
