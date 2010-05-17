package com.google.code.laserswarm.core;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.out.Report;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.google.common.collect.ImmutableSet;
import com.lyndir.lhunath.lib.system.logging.Logger;

public abstract class LaserSwarm {
	private static final Logger					logger			= Logger.get(LaserSwarm.class);

	protected HashMap<SimTemplate, Simulator>	simulations;
	protected List<Constellation>				constellations	= mkConstellations();

	protected double[][]						timeSteps		= { { 0, 500000 } };

	public LaserSwarm() {
		Configuration config = Configuration.getInstance();

		if (config.hasAction(Actions.SLEEP)) {
			logger.inf("Sleeping ...");
			System.exit(0);
		}

		if (config.hasAction(Actions.SIMULATE) || //
				config.hasAction(Actions.PROCESS) || //
				config.hasAction(Actions.PLOT_DISK) || //
				config.hasAction(Actions.PLOT_SCREEN) || //
				config.hasAction(Actions.TABULATE)) {
			simulate();
		}

		if (config.hasAction(Actions.PROCESS) || //
				config.hasAction(Actions.PLOT_DISK) || //
				config.hasAction(Actions.PLOT_SCREEN) || //
				config.hasAction(Actions.TABULATE)) {
			process();
		}

		if (config.hasAction(Actions.PLOT_DISK) || //
				config.hasAction(Actions.PLOT_SCREEN) || //
				config.hasAction(Actions.TABULATE)) {
			Report.write(simulations);
		}

	}

	protected abstract List<Constellation> mkConstellations();

	protected void process() {
		// throw new UnsupportedOperationException();
	}

	protected void simulate() {
		File demFolder = new File("DEM");
		File[] dems = demFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.getName().startsWith("ASTGTM"));
			}

		});
		EarthModel earth = new EarthModel();
		ImmutableSet<ElevationModel> parsed = DemReader.parseDem(Arrays.asList(dems));
		for (ElevationModel file : parsed)
			earth.add(file);
		earth.loadCoef();
		SimulatorMaster simMaster = new SimulatorMaster(earth);

		for (Constellation constellation : constellations) {
			for (double[] timeRange : timeSteps) {
				SimTemplate template = new SimTemplate(constellation);
				template.setTime(timeRange[0], timeRange[1]);
				simMaster.addSimTemplate(template);
			}
		}

		simulations = simMaster.runSim();
	}
}
