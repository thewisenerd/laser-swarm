package com.google.code.laserswarm;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.out.Report;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class LaserSwarm {
	private static final Logger	logger	= Logger.get(LaserSwarm.class);

	public static void main(String[] args) {
		Configuration config = Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT, Actions.COUNT_ONLY, Actions.FORCE_FLAT));
		new LaserSwarm();
	}

	private HashMap<SimTemplate, Simulator>	simulations;
	private static List<Constellation>		constellations;
	static {
		constellations = Lists.newLinkedList();
		for (float alt = 300; alt <= 450; alt += 25)
			constellations.add(mkConstellation(alt));
	}

	private static Constellation mkConstellation(float alt) {
		Satellite emittor = new Satellite("SAT01", (0.08 * 0.08), 6370f + alt, 0f, (float) Math.PI / 2,
				(float) (8.5 * Math.PI / 180), 0f, 0f);
		LinkedList<Satellite> r = Lists.newLinkedList();
		r.add(emittor);
		Constellation c = new Constellation(30 * (1. / 3), 5000, emittor, r);
		c.setName(String.format("Constellation %s km", alt));
		return c;
	}

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

	private void process() {

		// throw new UnsupportedOperationException();
	}

	private void simulate() {
		Configuration config = Configuration.getInstance();

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

		double[][] timeSteps = { // 
		{ 0, 2000000 }, // 
		// { 500000, 1000000 }, //
		};
		for (Constellation constellation : constellations) {
			for (double[] timeRange : timeSteps) {
				SimTemplate template = new SimTemplate(constellation);
				template.setTime(timeRange[0], timeRange[1]);
				simMaster.addSimTemplate(template);
			}
		}

		simulations = simMaster.runSim();

		for (SimTemplate tmpl : simulations.keySet()) {
			double nrP = 0;
			long samples = 0;
			for (Satellite sat : tmpl.getConstellation().getReceivers()) {
				samples += simulations.get(tmpl).getDataPoints().size();
				for (SimVars var : simulations.get(tmpl).getDataPoints()) {
					nrP += var.photonsE.get(sat);
					// logger.dbg("p=%s", var.photonsE.get(sat));
				}

			}
			logger.inf(tmpl + " nr photons = " + nrP + " of " + samples + " samples\t=> avg: " + nrP
					/ samples);
		}
	}
}
