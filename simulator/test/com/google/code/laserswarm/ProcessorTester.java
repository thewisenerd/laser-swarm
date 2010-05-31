package com.google.code.laserswarm;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.out.plot1D.plotHeightDistribution;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.MeasermentSample;
import com.google.code.laserswarm.process.SampleIterator;
import com.google.code.laserswarm.process.TimeLine;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ProcessorTester {

	public static final String	CfgName	= "unitTestConfig.xml";

	private static final Logger	logger	= Logger.get(ProcessorTester.class);

	public static void main(String[] args) {
		new ProcessorTester().testProcessing();
	}

	private void displayData(Map<Satellite, TimeLine> satData) {
		for (TimeLine timeLine : satData.values()) {
			logger.dbg("sat: %s ", timeLine.getSatellite());
			SampleIterator iterator = null;
			try {
				iterator = timeLine.getIterator((int) 1E5);
			} catch (MathException e) {
				e.printStackTrace();
			}
			while (iterator.hasNext()) {
				MeasermentSample sample = iterator.next();
				logger.dbg("t: %s\tn: %s", sample.getTime(), sample.getPhotons());
			}
		}
	}

	public RandData testProcessing() {
		Configuration cfg = new Configuration();
		Configuration.write(CfgName, cfg);
		try {
			Configuration.read(CfgName);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Constellation testConstallation = SimulationTester.mkTestConstellation();
/*		try {
			Field f;
			f = Configuration.class.getDeclaredField("constellations");
			f.setAccessible(true);
			f.set(Configuration.getInstance(), null);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}*/

		File demFolder = new File("DEM");
		File[] dems = demFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.getName().startsWith("ASTGTM"));
			}

		});
		ElevationModel dem = null;
		try {
			dem = DemReader.parseDem(dems[0]);
		} catch (DemCreationException e) {
			e.printStackTrace();
		}
		EarthModel earth = new EarthModel(dem);
		// Plot2D.make(dem.getCoverage());

		SimulatorMaster mgr = new SimulatorMaster(earth);
		SimTemplate template = new SimTemplate(testConstallation);
		// template.setTime(4542935, 4542935.1);
		template.setTime(191675, 191676);
		mgr.addSimTemplate(template);

		/*
		 * SimulatorMaster mgr = new SimulatorMaster(earth); SimTemplate template = new
		 * SimTemplate(testConstallation); template.setTime(template.getT0(), 700000);
		 * mgr.addSimTemplate(template); // only one template
		 */EmitterHistory emittorHistory = null;
		Map<Satellite, TimeLine> satData = Maps.newHashMap();
		Map<Satellite, TimeLine> emData = Maps.newHashMap();

		Satellite Emit = new Satellite();
		HashMap<SimTemplate, Simulator> points = mgr.runSim();
		plotHeightDistribution plotter = new plotHeightDistribution();
		plotter.plot(points.get(template).getDataPoints(), 3, "out.png");
		for (SimTemplate templ : points.keySet()) { // assuming only one template
			List<SimVars> dataPoints = points.get(templ).getDataPoints();

			emittorHistory = new EmitterHistory( //
					templ.getConstellation(), dataPoints);

			Emit = templ.getConstellation().getEmitter();
			emData.put(Emit, new TimeLine(Emit, templ.getConstellation(), dataPoints));

			for (Satellite sat : templ.getConstellation().getReceivers()) {
				satData.put(sat, new TimeLine(sat, templ.getConstellation(), dataPoints));

			}
			displayData(satData);

		}

		return new RandData(satData, emData, emittorHistory);

	}
}
