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
import com.google.code.laserswarm.Desim.elevation.ElevationFinder;
import com.google.code.laserswarm.Desim.elevation.slope.ElevationSlope;
import com.google.code.laserswarm.Desim.elevation.slope.FindElevationNeighborInterpolation;
import com.google.code.laserswarm.Desim.filter.Filter;
import com.google.code.laserswarm.Desim.filter.FilterAverage;
import com.google.code.laserswarm.Desim.filter.FilterOutlierRemoval;
import com.google.code.laserswarm.Desim.filter.elevationslope.ElevationSlopeFilter;
import com.google.code.laserswarm.Desim.filter.elevationslope.FilterSpikes;
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
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Altitude {
	private static final int	dataPoints	= 1000;
	private static final Logger	logger		= Logger.get(TestFindElevation.class);

	public static void main(String[] args) throws DemCreationException, MathException,
			IOException {
		run(dataPoints, new FindElevationNeighborInterpolation(7, (int) 97e12));
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
			SimulatorMaster mgr = new SimulatorMaster(EarthModel.getDefaultModel());
			Constellation cons = Constellation.simpleConstellation(5, 0.005625, 500);
			Satellite recOrig = cons.getEmitter();
			Satellite rec1 = new Satellite("Satellite RAAN-1", recOrig);
			rec1.setArgumentOfPerigee((float) (-2.18 / 180 * Math.PI));
			Satellite rec2 = new Satellite("Satellite RAAN+1", recOrig);
			rec1.setArgumentOfPerigee((float) (2.18 / 180 * Math.PI));
			Satellite rec3 = new Satellite("Satellite TA-1", recOrig);
			rec1.setTrueAnomaly((float) (-2.18 / 180 * Math.PI));
			Satellite rec4 = new Satellite("Satellite TA+1", recOrig);
			rec1.setTrueAnomaly((float) (2.18 / 180 * Math.PI));

			List<Satellite> recs = cons.getReceivers();
			recs.add(rec1);
			recs.add(rec2);
			recs.add(rec3);
			recs.add(rec4);
			cons.setReceivers(recs);
			cons.setReceiverBandWidth(2E-9);
			cons.setReceiverEfficiency(0.36 * 0.9);
			cons.setLaserWaveLength(473E-9);

			SimTemplate template = new SimTemplate(cons, dataPoints);
			mgr.addSimTemplate(template);

			HashMap<SimTemplate, Simulator> points = mgr.runSim();
			plotter.plot(points.get(template).getDataPoints(), 3, "heightSimulated");
			for (SimTemplate templ : points.keySet()) { // assuming only one template
				List<SimVars> dataPoints = points.get(templ).getDataPoints();
				emitterHistory = new EmitterHistory(templ.getConstellation(), dataPoints);
				constellation = templ.getConstellation();
				for (Satellite sat : templ.getConstellation().getReceivers()) {
					satData.put(sat, new TimeLine(sat, templ.getConstellation(), dataPoints));
				}
			}
			Configuration.write("satData.xml", satData);
			Configuration.write("emitterHistory.xml", emitterHistory);
			Configuration.write("constellation.xml", constellation);
		}
		if (new File("altData.xml").exists() & dontCalculate) {
			alts = Configuration.read("altData.xml", Configuration
					.getDefaultSerializer("altData.xml"));
		} else {
			elSlope = findEl.run(satData, emitterHistory, constellation, dataPoints);
			alts = elSlope.getAltitudes();
			Configuration.write("altData.xml", alts);
		}
		plotter.plot(alts, 3, "heightAnalysed");
		Filter filtAvg = new FilterAverage(21);
		LinkedList<Point3d> averagedAlts = filtAvg.filter(alts);
		plotter.plot(averagedAlts, 3, "heightAnalysed&Averaged");
		Filter filtOutliers = new FilterOutlierRemoval(200, 50, 3);
		LinkedList<Point3d> outlierAlts = filtOutliers.filter(alts);
		plotter.plot(outlierAlts, 3, "heightAnalysed&OutlierFiltered");
		ElevationSlopeFilter filtSpikes = new FilterSpikes(3, 0.3);
		ElevationSlope despiked = filtSpikes.filter(elSlope);
		plotter.plot(despiked.getAltitudes(), 3, "heightAnalysed&SpikeFiltered");
	}
}
