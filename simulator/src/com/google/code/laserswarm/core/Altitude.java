package com.google.code.laserswarm.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.TestFindElevation;
import com.google.code.laserswarm.Desim.elevation.ElevationComparison;
import com.google.code.laserswarm.Desim.elevation.ElevationFinder;
import com.google.code.laserswarm.Desim.elevation.slope.ElevationSlope;
import com.google.code.laserswarm.Desim.elevation.slope.FindElevationNeighborInterpolation;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.out.plot1D.plotHeightDistribution;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.simulation.postSimulation.SlopeSpread;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Altitude {
	private static final int	dataPoints	= 1000;
	private static final Logger	logger		= Logger.get(TestFindElevation.class);

	public static void main(String[] args) throws DemCreationException, MathException,
			IOException {
		run(dataPoints, new FindElevationNeighborInterpolation(1, (int) 97e12, 1, 7, 100, 0.707));
	}

	public static void run(int dataPoint, ElevationFinder findEl) throws DemCreationException,
			MathException,
			IOException {
		System.out.println("Do you want to re-run the simulator? (y/n default: no)");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String runSim = null;
		String runCalc = null;
		boolean dontSimulate = true;
		boolean dontCalculate = true;
		try {
			runSim = br.readLine();
		} catch (Exception ioe) {
			logger.err(ioe, "Caught input exception.");
		}
		if (runSim.equals("y")) {
			dontSimulate = false;
			dontCalculate = false;
			System.out.println("Re-running simulator, data analysis and post-processing.");
		} else {
			System.out.println("Do you want to re-run the data analysis? (y/n, default: no)");
			try {
				runCalc = br.readLine();
			} catch (Exception ioe) {
				logger.err(ioe, "Caught input exception.");
			}
			if (runCalc.equals("y")) {
				dontCalculate = false;
				System.out.println("If possible, re-running only data analysis and post-processing.");
			} else {
				System.out.println("If possible, re-running only post-processing.");
			}
		}

		EmitterHistory emitterHistory = null;
		Constellation constellation = null;
		Map<Satellite, TimeLine> satData = Maps.newHashMap();
		plotHeightDistribution plotter = new plotHeightDistribution();
		ElevationSlope elSlope = new ElevationSlope();
		LinkedList<Point3d> alts = Lists.newLinkedList();
		if (new File("satData.xml").exists() & new File("emitterHistory.xml").exists()
				& new File("constellation.xml").exists() & dontSimulate) {
			satData = Configuration.read("satData.xml", Configuration
					.getDefaultSerializer("satData.xml"));
			emitterHistory = Configuration.read("emitterHistory.xml", Configuration
					.getDefaultSerializer("emitterHistory.xml"));
			constellation = Configuration.read("constellation.xml", Configuration
					.getDefaultSerializer("constellation.xml"));
		} else {
			Constellation cons = Constellation.swarm(5, 0.005625, 500);
			cons.setReceiverBandWidth(2E-9);
			cons.setReceiverEfficiency(0.36 * 0.9);
			cons.setLaserWaveLength(473E-9);

			SimTemplate template = new SimTemplate(cons, dataPoints);
			SimulatorMaster mgr = new SimulatorMaster(EarthModel.getDefaultModel());
			mgr.addSimTemplate(template);

			HashMap<SimTemplate, Simulator> points = mgr.runSim();
			for (SimTemplate templ : points.keySet()) { // assuming only one template
				SlopeSpread slope = new SlopeSpread();
				slope.modify(points.get(templ), templ.getConstellation());
				List<SimVars> dataPoints = points.get(templ).getDataPoints();
				emitterHistory = new EmitterHistory(templ.getConstellation(), dataPoints);
				constellation = templ.getConstellation();
				ImmutableList<SimVars> dataPointsImm = ImmutableList.copyOf(dataPoints);
				for (Satellite sat : templ.getConstellation().getReceivers()) {
					satData.put(sat, new TimeLine(sat, templ.getConstellation(), dataPointsImm));
				}
			}

			plotter.plot(points.get(template).getDataPoints(), 3, "heightSimulated");
			Configuration.write("satData.xml", satData);
			Configuration.write("emitterHistory.xml", emitterHistory);
			Configuration.write("constellation.xml", constellation);
		}
		if (new File("altData.xml").exists() & dontCalculate) {
			elSlope = Configuration.read("altData.xml", Configuration
					.getDefaultSerializer("altData.xml"));
		} else {
			elSlope = findEl.run(satData, emitterHistory, constellation, dataPoints);
			Configuration.write("altData.xml", elSlope);
		}
		alts = elSlope.getAltitudes();
		plotter.plot(alts, 3, "heightAnalysed");

		logger.inf("heightAnalysed Stats:\n%s",
				new ElevationComparison(EarthModel.getDefaultModel(), alts));

		// logger.inf("slope Stats:\n%s",
		// new SlopeComparison(EarthModel.getDefaultModel(), elSlope));

		// Filter filtAvg = new FilterAverage(21);
		// LinkedList<Point3d> averagedAlts = filtAvg.filter(alts);
		// plotter.plot(averagedAlts, 3, "heightAnalysed&Averaged");
		// Filter filtOutliers = new FilterOutlierRemoval(200, 50, 3);
		// LinkedList<Point3d> outlierAlts = filtOutliers.filter(alts);
		// plotter.plot(outlierAlts, 3, "heightAnalysed&OutlierFiltered");
		// ElevationSlopeFilter filtSpikes = new FilterSpikes(3, 0.3);
		// ElevationSlope despiked = filtSpikes.filter(elSlope);
		// plotter.plot(despiked.getAltitudes(), 3, "heightAnalysed&SpikeFiltered");
	}
}
