package com.google.code.laserswarm;

import com.google.code.laserswarm.out.plot1D.plotHeightDistribution2;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.util.demReader.DemCreationException;

public class TestHeightDistributionPlot {
	public static void main(String[] args) throws DemCreationException {
		plotHeightDistribution2 plot = new plotHeightDistribution2();
		for (Simulator sim : SimulationTester.sim().values()) {
			plot.plot(sim.getDataPoints(), 3, "testHeightPlot");
		}
	}
}
