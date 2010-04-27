package com.google.code.laserswarm;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVarUtil;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.util.RetrievalExecption;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SimulationTester extends TestCase {

	public static final String	CfgName	= "unitTestConfig.xml";

	private static final Logger	logger	= Logger.get(SimulationTester.class);

	public static void main(String[] args) {
		new SimulationTester().testSim();
	}

	private Constellation mkTestConstilation() {
		Satellite emittor = new Satellite("SAT01", (0.08 * 0.08), 6700f, 0f, (float) Math.PI / 2,
				(float) (3.2 * Math.PI / 180), 0f, 0f);
		LinkedList<Satellite> r = Lists.newLinkedList();
		r.add(emittor);
		return new Constellation(10, 50, emittor, r);
	}

	public void testSim() {
		Configuration cfg = new Configuration();
		Configuration.write(CfgName, cfg);
		Configuration.read(CfgName);
		Constellation testConstallation = mkTestConstilation();
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

		ElevationModel dem = null;
		try {
			dem = DemReader.parseDem(new File("DEM/srtm_37_02-red.asc"));
		} catch (DemCreationException e1) {
			fail("Cannot load the DEM");
		}
		EarthModel earth = new EarthModel(dem);

		SimulatorMaster mgr = new SimulatorMaster(earth);
		mgr.addSimTemplate(new SimTemplate(Configuration.getInstance(), testConstallation));

		HashMap<SimTemplate, Simulator> points = mgr.runSim();
		for (Simulator sim : points.values()) {
			int nP = 0;
			List<SimVars> pnts = sim.getDataPoints();
			for (SimVars simVar : pnts) {
				System.out.println(simVar.photonsE);
				System.out.println(simVar.scatter);
				for (Satellite sat : simVar.photonsE.keySet()) {
					nP += simVar.photonsE.get(sat);
				}
			}
			try {
				System.out.println(SimVarUtil.getField("photonsE", pnts));
			} catch (RetrievalExecption e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.inf("Received %s photons in total (%s pulses)", nP, pnts.size());
		}

	}

}