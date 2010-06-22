package com.google.code.laserswarm.out;

import java.util.HashMap;

import com.google.code.laserswarm.SimulationTester;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.out.plot1D.PlotHeightDistribution;
import com.google.code.laserswarm.out.table.writeLaTeXTable;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.Simulator;

public class Report {

	private static Configuration	conf	= Configuration.getInstance();

	public static void write(HashMap<SimTemplate, Simulator> sims) {
		if (Configuration.hasAction(Actions.PLOT_DISK)) {
			PlotHeightDistribution plot = new PlotHeightDistribution();
			int num = 0;
			for (Simulator sim : SimulationTester.sim().values()) {
				num++;
				plot.plot(sim.getDataPoints(), 3, conf.getFilePathReport() + "simulator/img"
						+ conf.getFilePrefixReport() + "HeightDistributionPlot" + num);
			}
		}
		if (Configuration.hasAction(Actions.TABULATE)) {
			writeLaTeXTable write = new writeLaTeXTable();
			write.write(sims, conf.getFilePathReport() + "simulator/table" + conf.getFilePrefixReport()
					+ "Performance.tex");
		}
	}
}
