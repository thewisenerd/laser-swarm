package com.google.code.laserswarm;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;
import org.opengis.metadata.spatial.VectorSpatialRepresentation;

import com.google.code.laserswarm.Orbit.AntiSimulator;
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

public class ProcessorTester {

	public static final String	CfgName	= "unitTestConfig.xml";

	private static final Logger	logger	= Logger.get(ProcessorTester.class);

	private class RandData {
		Map<Satellite, TimeLine>	rec;
		Map<Satellite, TimeLine>	em;
		EmitterHistory				emHist;

		public RandData(Map<Satellite, TimeLine> rec, Map<Satellite, TimeLine> em,EmitterHistory	emHist) {
			super();
			this.rec = rec;
			this.em = em;
			this.emHist =emHist;
		}

		public Map<Satellite, TimeLine> getEm() {
			return em;
		}
		
		public EmitterHistory getEmHist() {
			return emHist;
		}

		public Map<Satellite, TimeLine> getRec() {
			return rec;
		}

	}

	public static void main(String[] args) {
		new ProcessorTester().testProcessing();
	}

	private void displayData(Map<Satellite, TimeLine> satData) {
		for (TimeLine timeLine : satData.values()) {
			logger.dbg("sat: %s ", timeLine.getSatellite());
			SampleIterator iterator = null;
			try {
				iterator = timeLine.getIterator((int) 1E9);
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
		template.setTime(template.getT0(), 700000);
		mgr.addSimTemplate(template); // only one template
		EmitterHistory emittorHistory = null;
		Map<Satellite, TimeLine> satData = Maps.newHashMap();
		Map<Satellite, TimeLine> emData = Maps.newHashMap();

		Vector<Map<Satellite, TimeLine>> res = new Vector<Map<Satellite, TimeLine>>();
		Satellite Emit = new Satellite();
		HashMap<SimTemplate, Simulator> points = mgr.runSim();

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
