package com.google.code.laserswarm;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import junit.framework.TestCase;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVarUtil;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.util.RetrievalExecption;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SimulationTester extends TestCase {

	public static final String	CfgName	= "unitTestConfig.xml";

	private static final Logger	logger	= Logger.get(SimulationTester.class);

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT, Actions.CONSTANT_SCATTER, Actions.DEM_CACHE));
		new SimulationTester().testSim();
	}

	static Constellation mkTestConstilation() {
		Satellite emittor = new Satellite("SAT01", (0.2 * 0.2),
				(float) ((Configuration.R0 / 1E3) + 450), // 
				0f, (float) Math.PI / 2, (float) (8.5 * Math.PI / 180), 0f, 0f);
		LinkedList<Satellite> r = Lists.newLinkedList();
		r.add(emittor);
		return new Constellation(5, 5000, emittor, r);
	}

	public static HashMap<SimTemplate, Simulator> sim() {
		Configuration cfg = new Configuration();
		Configuration.write(CfgName, cfg);
		try {
			Configuration.read(CfgName);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Constellation testConstallation = mkTestConstilation();
		EarthModel earth = new EarthModel();

		ImmutableSet<ElevationModel> dems = DemReader.getDefaultDems();
		for (ElevationModel elevationModel : dems)
			earth.add(elevationModel);

		earth.loadCoef(); // Stretch refl coef

		SimulatorMaster mgr = new SimulatorMaster(earth);
		SimTemplate tmpl = new SimTemplate(testConstallation);
		tmpl.setTime(0, 191675.2);
		mgr.addSimTemplate(tmpl);

		HashMap<SimTemplate, Simulator> points = mgr.runSim();
		return points;
	}

	public void testSim() {
		HashMap<SimTemplate, Simulator> points = sim();

		for (Simulator sim : points.values()) {
			int nP = 0;
			List<SimVars> pnts = sim.getDataPoints();
			for (SimVars simVar : pnts) {
				// System.out.println(simVar.photonsE);
				for (Satellite sat : simVar.photonsE.keySet()) {
					nP += simVar.photonsE.get(sat);
				}
			}
			List<HashMap<Satellite, Point3d>> groundPoints = null;
			try {
				System.out.println(SimVarUtil.getField("photonsE", pnts));
				groundPoints = SimVarUtil.getField("photonsE", pnts);
			} catch (RetrievalExecption e) {
				e.printStackTrace();
			}

			logger.inf("Received %s photons in total (%s pulses)", nP, pnts.size());
		}

	}
}