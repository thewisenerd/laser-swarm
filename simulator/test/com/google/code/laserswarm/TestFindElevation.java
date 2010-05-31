package com.google.code.laserswarm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.FindElevation;
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
	public static void main(String[] args) throws DemCreationException, MathException {
		SimulatorMaster mgr = new SimulatorMaster(EarthModel.getDefaultModel());
		SimTemplate template = new SimTemplate(SimulationTester.mkTestConstellation(), 30);
		mgr.addSimTemplate(template);

		EmitterHistory emitterHistory = null;
		Constellation constellation = null;
		Map<Satellite, TimeLine> satData = Maps.newHashMap();
		Map<Satellite, TimeLine> emData = Maps.newHashMap();

		Satellite Emit = new Satellite();
		HashMap<SimTemplate, Simulator> points = mgr.runSim();
		plotHeightDistribution plotter = new plotHeightDistribution();
		plotter.plot(points.get(template).getDataPoints(), 3, "heightSimulated");
		for (SimTemplate templ : points.keySet()) { // assuming only one template
			List<SimVars> dataPoints = points.get(templ).getDataPoints();

			emitterHistory = new EmitterHistory(templ.getConstellation(), dataPoints);
			constellation = templ.getConstellation();

			Emit = templ.getConstellation().getEmitter();
			emData.put(Emit, new TimeLine(Emit, templ.getConstellation(), dataPoints));

			for (Satellite sat : templ.getConstellation().getReceivers()) {
				satData.put(sat, new TimeLine(sat, templ.getConstellation(), dataPoints));
			}
		}
		LinkedList<Point3d> alts = FindElevation.run(satData, emitterHistory, constellation);
		plotter.plot(alts, 3, "heightAnalysed");
	}
}
