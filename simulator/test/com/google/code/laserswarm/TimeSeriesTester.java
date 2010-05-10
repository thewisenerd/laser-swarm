package com.google.code.laserswarm;

import java.io.File;
import java.io.FileFilter;
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

public class TimeSeriesTester {

	public static final String	CfgName	= "unitTestConfig.xml";

	private static final Logger	logger	= Logger.get(TimeSeriesTester.class);

	public static void main(String[] args) {
		new TimeSeriesTester().testProcessing();
	}

	private void displayData(Map<Satellite, TimeLine> satData) {
		for (TimeLine timeLine : satData.values()) {
			logger.dbg("sat: %s ", timeLine.getSatellite());
			SampleIterator iterator;
			try {
				iterator = timeLine.getIterator((int) 1E5);
				while (iterator.hasNext()) {
					MeasermentSample sample = iterator.next();
					if (iterator.found)
						logger.inf("- t: %s\tn: %s", sample.getTime(), sample.getPhotons());
					else
						logger.dbg("t: %s\tn: %s", sample.getTime(), sample.getPhotons());
				}
				logger.dbg("found: %s", iterator.c);
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void testProcessing() {
		Configuration cfg = new Configuration();
		Configuration.write(CfgName, cfg);
		Configuration.read(CfgName);
		Constellation testConstallation = SimulationTester.mkTestConstilation();
		try {
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
		}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EarthModel earth = new EarthModel(dem);
		// Plot2D.make(dem.getCoverage());

		SimulatorMaster mgr = new SimulatorMaster(earth);
		SimTemplate template = new SimTemplate(testConstallation);
		// template.setTime(4542935, 4542935.1);
		template.setTime(4542935, 4542935.0065);
		mgr.addSimTemplate(template);
		// template = new SimTemplate(testConstallation);
		// template.setTime(4250000, 4500000);
		// mgr.addSimTemplate(template);
		// template = new SimTemplate(testConstallation);
		// template.setTime(4500000, 4750000);
		// mgr.addSimTemplate(template);
		// template = new SimTemplate(testConstallation);
		// template.setTime(4750000, 5000000);
		// mgr.addSimTemplate(template);

		HashMap<SimTemplate, Simulator> points = mgr.runSim();
		for (SimTemplate templ : points.keySet()) {
			List<SimVars> dataPoints = points.get(templ).getDataPoints();

			EmitterHistory emittorHistory = new EmitterHistory( //
					templ.getConstellation(), dataPoints);
			Map<Satellite, TimeLine> satData = Maps.newHashMap();
			for (Satellite sat : templ.getConstellation().getReceivers()) {
				satData.put(sat, new TimeLine(sat, templ.getConstellation(), dataPoints));
			}
			displayData(satData);

		}

	}
}
