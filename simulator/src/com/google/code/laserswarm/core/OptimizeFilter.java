package com.google.code.laserswarm.core;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.desim.elevation.ElevationComparison;
import com.google.code.laserswarm.desim.elevation.slope.FindElevationNeighborInterpolation;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.simulation.Prospector;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.code.laserswarm.util.CSVwriter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class OptimizeFilter extends LaserSwarm implements MultivariateRealFunction {

	private static final Logger	logger		= Logger.get(OptimizeFilter.class);
	private static final long	SEQ_LENGTH	= 2000;

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet(
				Actions.SAVED, Actions.PROSPECT, Actions.PROCESS, Actions.DISTRIBUTE_SLOPE));

		Prospector.roughTimeStep = 3;

		final OptimizeFilter sim = new OptimizeFilter();
		sim.optimize();
	}

	private static Constellation mkConstellation(double power, double aperature) {
		return Constellation.swarm(power, aperature, 500);
	}

	private CSVwriter			prefLog;
	private ElevationComparison	stats;
	private double				correlationInterval;
	private int					comparisonQueueLength;
	private double				whenEqual;

	@Override
	protected void end() {
		stats = new ElevationComparison(EarthModel.getDefaultModel(),
				processed.iterator().next().getAltitudes());
		logger.inf("heightAnalysed&SpikeFiltered Stats:\n%s", stats);
	}

	@Override
	protected List<Constellation> mkConstellations() {
		List<Constellation> constellations = Lists.newLinkedList();
		constellations.add(mkConstellation(3, 0.04 * 0.04));
		return constellations;
	}

	@Override
	protected FindElevationNeighborInterpolation mkDataProcessor() {
		return new FindElevationNeighborInterpolation(1, (int) 97e12,
				correlationInterval, comparisonQueueLength, whenEqual, 0.707);
	}

	@Override
	protected List<SimTemplate> mkTemplates(Constellation constellation) {
		LinkedList<SimTemplate> tmpls = Lists.newLinkedList();
		tmpls.add(new SimTemplate(constellation, SEQ_LENGTH));
		return tmpls;
	}

	private void optimize() {
		NelderMead optimizer = new NelderMead();
		optimizer.setMaxIterations(1000);
		optimizer.setMaxEvaluations(1000);

		prefLog = new CSVwriter(new File("optimize-filter.csv"), "\t");
		try {
			RealPointValuePair values = optimizer.optimize(this, GoalType.MAXIMIZE,
							new double[] { 5, 1.2, 0.3 });

			for (Double val : values.getPoint())
				logger.inf("Value %s", val);

			logger.inf("Result %s", values.getValue());
		} catch (MathException e) {
			e.printStackTrace();
		}

	}

	private int toSpecialInt(double value) {
		int y = (int) (value * 5);
		y *= 2;
		y++;
		return Math.max(y, 3);
	}

	@Override
	public double value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
		// new FindElevationNeighborInterpolation(1, (int) 97e12, , 0.707)

		correlationInterval = point[0];
		comparisonQueueLength = toSpecialInt(point[1]);
		whenEqual = point[2];

		logger.inf("Running iteration for \ncorrelationInterval=%f\ncomparisonQueueLength=%d\n"
				+ "whenEqual=%f", correlationInterval, comparisonQueueLength, whenEqual);
		if (correlationInterval < 0 || whenEqual < 0) {
			logger.inf("Negative correlationInterval or whenEqual");
			logger.inf("Performance: 0");
			return 0;
		}

		run();

		for (Simulator sim : simulations.values())
			sim.getDataPointsDB().close();

		// NormalDistribution gausian = new NormalDistributionImpl(stats.getMean(),
		// stats.getStandardDeviation());

		// TTestImpl ttest = new TTestImpl();
		double performace = -1;
		// try {
		// performace = ttest.tTest(new StatisticalSummaryValues(0, 1, 1000, 3, -3, 0), stats);
		// } catch (MathException e) {
		// e.printStackTrace();
		// System.exit(1);
		// }
		performace = 1 / (stats.getMean() * stats.getStandardDeviation());

		logger.inf("Performance: %f", performace);
		prefLog.write(correlationInterval, comparisonQueueLength, whenEqual, performace, stats.getN(),
						stats.getMin(), stats.getMax(), stats.getMean(), stats.getStandardDeviation(),
						stats.getPercentile(50), stats.getSkewness(), stats.getKurtosis());
		return performace;
	}
}
