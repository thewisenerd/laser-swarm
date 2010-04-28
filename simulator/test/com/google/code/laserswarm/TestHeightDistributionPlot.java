package com.google.code.laserswarm;

import com.google.code.laserswarm.out.plot1D.plotHeightDistribution;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.util.demReader.DemCreationException;

public class TestHeightDistributionPlot {
	public static void main(String[] args) throws DemCreationException {
		plotHeightDistribution plot = new plotHeightDistribution();
		for (Simulator sim : SimulationTester.sim().values()) {
			plot.plot(sim.getDataPoints(), 3, "../report/simulator/img/testHeightPlot");
		}
	}
}
