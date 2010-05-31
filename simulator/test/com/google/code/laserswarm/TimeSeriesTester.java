package com.google.code.laserswarm;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
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
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class TimeSeriesTester {

	public static final String	CfgName	= "unitTestConfig.xml";

	private static final Logger	logger	= Logger.get(TimeSeriesTester.class);

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT, Actions.CONSTANT_SCATTER, Actions.FORCE_FLAT));
		new TimeSeriesTester().testProcessing();
	}

	private void displayData(Map<Satellite, TimeLine> satData) {
		for (TimeLine timeLine : satData.values()) {
			logger.dbg("sat: %s ", timeLine.getSatellite());
			SampleIterator iterator;
			try {
				iterator = timeLine.getIterator((int) 1E5);
				int rec = 0;
				while (iterator.hasNext()) {
					MeasermentSample sample = iterator.next();
					int p = sample.getPhotons();
					rec += p;
					// if (iterator.found)
					// logger.inf("- t: %s\tn: %s", sample.getTime(), p);
					// else
					// logger.dbg("t: %s\tn: %s", sample.getTime(), p);
				}
				logger.dbg("found: %s over %s s (%s - %s)", rec, iterator.endTime()
						- iterator.startTime(), iterator.startTime(), iterator.endTime());
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void testProcessing() {
		Constellation testConstallation = SimulationTester.mkTestConstellation();

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
		template.setTime(0, 191680);
		mgr.addSimTemplate(template);

		HashMap<SimTemplate, Simulator> points = mgr.runSim();

		/* Total nr pulse photons */
		for (SimTemplate tmpl : points.keySet()) {
			double nrP = 0;
			long samples = 0;
			for (Satellite sat : tmpl.getConstellation().getReceivers()) {
				samples += points.get(tmpl).getDataPoints().size();
				for (SimVars var : points.get(tmpl).getDataPoints())
					nrP += var.photonsE.get(sat);

			}
			logger.inf(tmpl + " nr photons = " + nrP + " of " + samples + " samples\t=> avg: " + nrP
					/ samples);
		}

		/* Total nr photons ()pulse+noise */
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
