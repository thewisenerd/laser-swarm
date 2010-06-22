package com.google.code.laserswarm.out;

import java.util.HashMap;

import com.google.code.laserswarm.SimulationTester;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.out.plot1D.plotHeightDistribution2;
import com.google.code.laserswarm.out.table.WriteLaTeXTable;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.Simulator;

public class Report {
	private static Configuration	conf	= Configuration.getInstance();

	public static void write(HashMap<SimTemplate, Simulator> sims) {
		if (Configuration.getInstance().hasAction(Actions.PLOT_DISK)) {
			plotHeightDistribution2 plot = new plotHeightDistribution2();
			int num = 0;
			for (Simulator sim : SimulationTester.sim().values()) {
				num++;
				plot.plot(sim.getDataPoints(), 3, conf.getFilePathReport() + "simulator/img"
						+ conf.getFilePrefixReport() + "HeightDistributionPlot" + num);
			}
		}
		if (Configuration.getInstance().hasAction(Actions.TABULATE)) {
			WriteLaTeXTable write = new WriteLaTeXTable();
			write.write(sims, conf.getFilePathReport() + "simulator/table" + conf.getFilePrefixReport()
					+ "Performance.tex");
		}
	}
}
