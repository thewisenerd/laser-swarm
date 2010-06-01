package com.google.code.laserswarm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.FindElevation;
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
import com.google.common.collect.Maps;

public class TestFindElevation {
	private static final int	dataPoints	= 250;

	public static void main(String[] args) throws DemCreationException, MathException,
			IOException {
		EmitterHistory emitterHistory = null;
		Constellation constellation = null;
		Map<Satellite, TimeLine> satData = Maps.newHashMap();
		plotHeightDistribution plotter = new plotHeightDistribution();
		if (new File("satData.xml").exists() & new File("emitterHistory.xml").exists()
				& new File("constellation.xml").exists()) {
			satData = Configuration.read("satData.xml", Configuration
					.getDefaultSerializer("satData.xml"));
			emitterHistory = Configuration.read("emitterHistory.xml", Configuration
					.getDefaultSerializer("emitterHistory.xml"));
			constellation = Configuration.read("constellation.xml", Configuration
					.getDefaultSerializer("constellation.xml"));
		} else {
			SimulatorMaster mgr = new SimulatorMaster(EarthModel.getDefaultModel());
			Constellation cons = SimulationTester.mkTestConstellation();
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
		LinkedList<Point3d> alts = FindElevation.run(satData, emitterHistory, constellation, dataPoints);
		plotter.plot(alts, 3, "heightAnalysed");
		LinkedList<Point3d> averagedAlts = FindElevation.average(alts, 21);
		plotter.plot(averagedAlts, 3, "heightAnalysed&Averaged");
	}
}
