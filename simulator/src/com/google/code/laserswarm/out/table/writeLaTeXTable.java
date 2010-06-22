package com.google.code.laserswarm.out.table;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.out.plot1D.PlotHeightDistribution;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class writeLaTeXTable {
	private static final Logger	logger	= Logger.get(PlotHeightDistribution.class);

	private String format(double val) {
		return Double.toString(Math.floor(100000.0 * val) / 100000.0);
	}

	/**
	 * Writes all simulation data to a table.
	 * 
	 * @param sims
	 *            The data to be tabulated.
	 * @param tablefile
	 *            The file to write the table to.
	 */
	public void write(HashMap<SimTemplate, Simulator> sims, String constTableFile) {
		try {
			BufferedWriter tableOut = new BufferedWriter(new FileWriter(constTableFile));
			tableOut.write("\\begin{center}\n\t\\begin{longtable}{| c | c | c | c | c | c | c | c |}"
					+ "\\hline\n\t\t\\label{t:performance}\n\t\tSwarm & "
					+ "P [W] & $\\lambda$ [nm] & f [Hz] & $p_{length}$ [s] & $\\#_{Recv}$ & "
					+ "$A_{aperture}$ [$m^{2}$] & $\\phi_{received}$ \\\\\\hline");
			int num = 0;
			for (SimTemplate conf : sims.keySet()) {
				num++;
				Simulator sim = sims.get(conf);
				List<SimVars> dataSet = sim.getDataPoints();
				double avPhotonsRecv = 0;
				double avPowerRecvWSqM = 0;
				double avPhotonDensSqM = 0;
				for (SimVars dataPoint : dataSet) {
					avPowerRecvWSqM += dataPoint.powerR;
					for (double photons : dataPoint.photonsE.values()) {
						avPhotonsRecv += photons;
					}
					for (double photonDens : dataPoint.photonDensity.values()) {
						avPhotonDensSqM += photonDens;
					}
				}
				double apertureArea = 0;
				for (Satellite sat : conf.getConstellation().getReceivers()) {
					apertureArea += sat.getAperatureArea();
				}
				double size = dataSet.size();
				avPhotonDensSqM /= size;
				avPowerRecvWSqM /= size;
				avPhotonsRecv /= size;
				Constellation constel = conf.getConstellation();
				tableOut.write("\n\t\t" + num + " & " + constel.getPower() + " & " + 1E9
						* constel.getLaserWaveLength() + " & " + constel.getPulseFrequency() + " & "
						+ constel.getPulselength() + " & " + num + " & " + apertureArea + " & "
						+ avPhotonsRecv + "\\\\\\hline");
			}
			tableOut.write("\n\t\t\\caption{The constellation information.}"
					+ "\n\t\\end{longtable}\n\\end{center}\n\n");
			num = 0;
			for (SimTemplate conf : sims.keySet()) {
				num++;
				int n = 1;
				tableOut.write("\\begin{center}\n\t\\begin{longtable}"
						+ "{| c | c | c | c | c | c | c | c | c |}" + "\\hline\n\t\t\\label{t:swarm"
						+ num + "}\n\t\tSat no. & Name &"
						+ "$A_{aperture}$ [$m^{2}$] & a [km] & e [-] & i [rad] & $\\Omega$ [rad] & "
						+ "$\\theta$ [rad]& $\\omega$ [rad] \\\\\\hline");
				Satellite emit = conf.getConstellation().getEmitter();
				tableOut.write("\n\t\t" + n + " & " + emit.toString() + " & - & "
						+ format(emit.getSemimajorAxis()) + " & " + format(emit.getEccentricity())
						+ " & " + format(emit.getInclination()) + " & "
						+ format(emit.getRightAngleOfAscendingNode()) + " & "
						+ format(emit.getTrueAnomaly()) + " & " + format(emit.getArgumentOfPerigee())
						+ "\\\\\\hline");
				for (Satellite recv : conf.getConstellation().getReceivers()) {
					n++;
					tableOut.write("\n\t\t" + n + " & " + recv.toString() + " & "
							+ format(recv.getAperatureArea()) + " & " + format(recv.getSemimajorAxis())
							+ " & " + format(recv.getEccentricity()) + " & "
							+ format(recv.getInclination()) + " & "
							+ format(recv.getRightAngleOfAscendingNode()) + " & "
							+ format(recv.getTrueAnomaly()) + " & "
							+ format(recv.getArgumentOfPerigee()) + "\\\\\\hline");
				}
				tableOut.write("\n\t\t\\caption{Satellites in Swarm " + num
						+ ". The first Satellite is the emitter.}"
						+ "\n\t\\end{longtable}\n\\end{center}\n\n");
			}
			tableOut.close();
		} catch (IOException e) {
			logger.inf(e, "Table file writing failed.");
		}
	}
}
