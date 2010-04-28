package com.google.code.laserswarm;

import java.io.File;
import java.util.HashMap;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class LaserSwarm {
	private static final Logger	logger	= Logger.get(LaserSwarm.class);

	public static void main(String[] args) {
		new LaserSwarm();
	}

	private HashMap<SimTemplate, Simulator>	simulations;

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
	}

	private void process() {

		throw new UnsupportedOperationException();
	}

	private void simulate() {
		Configuration config = Configuration.getInstance();

		ElevationModel dem = null;
		try {
			dem = DemReader.parseDem(new File("DEM/srtm_37_02-red.asc"));
		} catch (DemCreationException e1) {
			logger.err("Cannot load the DEM");
			System.exit(1);
		}
		EarthModel earth = new EarthModel(dem);
		SimulatorMaster simMaster = new SimulatorMaster(earth);

		for (Constellation constellation : config.getConstellations()) {
			simMaster.addSimTemplate(new SimTemplate(null, constellation));
		}

		simulations = simMaster.runSim();
	}
}
