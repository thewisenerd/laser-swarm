package com.google.code.laserswarm.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.desim.elevation.slope.ElevationSlope;
import com.google.code.laserswarm.desim.elevation.slope.FindElevationNeighborInterpolation;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.out.Report;
import com.google.code.laserswarm.out.plot1D.PlotHeightDistribution;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.simulation.postSimulation.SlopeSpread;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public abstract class LaserSwarm {
	private static final Logger					logger			= Logger.get(LaserSwarm.class);

	protected HashMap<SimTemplate, Simulator>	simulations		= Maps.newHashMap();
	protected List<Constellation>				constellations	= mkConstellations();

	protected double[][]						timeSteps		= { { 0, 500000 } };

	protected EarthModel						earth;

	protected Set<ElevationSlope>				processed		= Sets.newHashSet();

	protected abstract void end();

	protected List<Constellation> mkConstellations() {
		LinkedList<Constellation> l = Lists.newLinkedList();
		l.add(Constellation.swarm(5, 0.007, 500));
		return l;
	}

	public void mkData() {
		simulate();

		/* Post simulation modifiers */
		if (Configuration.hasAction(Actions.DISTRIBUTE_SLOPE)) {
			for (SimTemplate tmpl : simulations.keySet()) {
				logger.inf("SlopeStpreading over %s", tmpl);
				Simulator sim = simulations.get(tmpl);
				SlopeSpread spread = new SlopeSpread();
				simulations.put(tmpl, spread.modify(sim, tmpl.getConstellation()));
			}
		}
	}

	protected FindElevationNeighborInterpolation mkDataProcessor() {
		return new FindElevationNeighborInterpolation(1, (int) 97e12, 5, 3, 0.3, 0.707);
	}

	protected void mkEarth() {
		earth = EarthModel.getDefaultModel();
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

	protected void process() {
		logger.inf("Running process section");
		FindElevationNeighborInterpolation dataprocessor = mkDataProcessor();

		Map<Satellite, TimeLine> satData = null;
		EmitterHistory emitterHistory = null;
		Constellation constellation = null;

		boolean fallback = false;
		if (!(simulations != null && simulations.size() > 0))
			if (Configuration.hasAction(Actions.SAVED)) {
				try {
					satData = Configuration.read("satData.xml", Configuration
							.getDefaultSerializer("satData.xml"));
					emitterHistory = Configuration.read("emitterHistory.xml", Configuration
							.getDefaultSerializer("emitterHistory.xml"));
					constellation = Configuration.read("constellation.xml", Configuration
							.getDefaultSerializer("constellation.xml"));
				} catch (FileNotFoundException e) {
					logger.wrn(e, "Cannot load data, using fallback (computing again)");
					fallback = true;
				}
			} else
				fallback = true;
		else
			fallback = true;

		/* Use and possible make the new simulation */
		if (fallback) {
			if (simulations == null || simulations.size() == 0)
				mkData();
			Iterator<SimTemplate> it = simulations.keySet().iterator();
			if (it.hasNext()) {
				SimTemplate templ = it.next();
				List<SimVars> dataPoints = simulations.get(templ).getDataPoints();
				emitterHistory = new EmitterHistory(templ.getConstellation(), dataPoints);

				constellation = templ.getConstellation();
				ImmutableList<SimVars> dataPointsImm = ImmutableList.copyOf(dataPoints);
				satData = Maps.newHashMap();
				for (Satellite sat : templ.getConstellation().getReceivers()) {
					satData.put(sat, new TimeLine(sat, templ.getConstellation(), dataPointsImm));
				}
				if (Configuration.hasAction(Actions.SAVED)) {
					Configuration.write("satData.xml", satData);
					Configuration.write("emitterHistory.xml", emitterHistory);
					Configuration.write("constellation.xml", constellation);
				}
			} else {
				throw new RuntimeException("Cannot load any data");
			}
		}

		try {
			ElevationSlope elSlope = dataprocessor.run(
					satData, emitterHistory, constellation, emitterHistory.getTime().size());
			processed.add(elSlope);

			PlotHeightDistribution plotter = new PlotHeightDistribution();
			try {
				plotter.plot(elSlope.getAltitudes(), 3, "heightAnalysed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MathException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void run() {
		logger.inf("Simulating laserswarm");
		if (Configuration.hasAction(Actions.SLEEP)) {
			logger.inf("Sleeping ...");
			System.exit(0);
		}

		/* Simulation */
		if (Configuration.hasAction(Actions.SIMULATE)) {
			mkData();
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

		end();
	}

	protected void simulate() {
		logger.inf("Running simulation section");
		if (earth == null)
			mkEarth();
		SimulatorMaster simMaster = new SimulatorMaster(earth);

		for (Constellation constellation : constellations)
			simMaster.addSimTemplates(mkTemplates(constellation));

		simulations = simMaster.runSim();
	}
}
