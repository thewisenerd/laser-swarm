package com.google.code.laserswarm;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;

import junit.framework.TestCase;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimulatorMaster;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.google.common.collect.Lists;

public class SimulationTester extends TestCase {

	public static final String	CfgName	= "unitTestConfig.xml";

	private Constellation mkTestConstilation() {
		Satellite emittor = new Satellite(6700f, 0f, (float) Math.PI / 2, (float) (3.2 * Math.PI / 180),
				0f, 0f);
		LinkedList<Satellite> r = Lists.newLinkedList();
		return new Constellation(1E6, 50, emittor, r);
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
		SimulatorMaster mgr = new SimulatorMaster(dem);
		mgr.addSimTemplate(new SimTemplate(Configuration.getInstance(), testConstallation));

		mgr.runSim();
	}

}