package com.google.code.laserswarm.core;

import static com.google.code.laserswarm.math.Convert.toDirectPosition;
import static com.google.code.laserswarm.math.Convert.toSphere;
import static com.google.code.laserswarm.math.VectorMath.enuToLocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.geotools.geometry.DirectPosition2D;

import com.google.code.laserswarm.Desim.brdf.BRDFinput;
import com.google.code.laserswarm.Desim.brdf.Brdf;
import com.google.code.laserswarm.Desim.brdf.BrdfFactory;
import com.google.code.laserswarm.Desim.brdf.DiscreteBrdf;
import com.google.code.laserswarm.Desim.elevation.slope.ElevationSlope;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.code.laserswarm.simulation.Prospector;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.util.GuiFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class BrdfComparison extends LaserSwarm {

	private static final Logger	logger	= Logger.get(BrdfComparison.class);

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet(
				Actions.PROSPECT, Actions.PROCESS));

		Prospector.roughTimeStep = 3;

		new BrdfComparison().run();
	}

	private int	QUE_SIZE	= 2;

	private void compareBrdf(LinkedList<BRDFinput> que, SummaryStatistics stat) {
		BRDFinput compiledBrdfInput;
		if (false) {
			compiledBrdfInput = que.getFirst().clone();
			Iterator<BRDFinput> it = que.iterator();
			it.next();// Skip the first one as the compiledBrdfInput is already based on it
			while (it.hasNext()) {
				BRDFinput brdFinput = it.next();
				compiledBrdfInput.merge(brdFinput);
			}
		} else
			compiledBrdfInput = que.getFirst();
		DiscreteBrdf discreteBrdf = BrdfFactory.construct(compiledBrdfInput);

		Vector3d norm = compiledBrdfInput.getTerrainNormal();
		System.out.println(norm);

		System.out.println("using: " + norm);

		int nrP = 0;
		for (Vector3d origin : compiledBrdfInput.getReceiverPositions().keySet()) {
			System.out.println(origin);
			nrP += compiledBrdfInput.getReceiverPositions().get(origin);
		}
		System.out.println("photons: " + nrP);

		DirectPosition2D pos = toDirectPosition(toSphere(compiledBrdfInput.getScatterPoint()));
		ScatteringParam param = EarthModel.getDefaultModel().getScatteringParam(pos);
		Vector3d in = enuToLocal(new Vector3d(0, 0, 1),
					EarthModel.getDefaultModel().getSurfaceNormal(pos));
		ScatteringCharacteristics brdf2 = new ScatteringCharacteristics(in, param);

		discreteBrdf.scaleTo(brdf2);
		discreteBrdf.compareTo(brdf2, stat);

		Brdf brdf = null;
		try {
			brdf = new Brdf(discreteBrdf);
		} catch (MathException e) {
			e.printStackTrace();
			System.exit(1);
		}

		GuiFactory.dualImageGui(brdf.toImage(), brdf2.toImage());
	}

	@Override
	protected void end() {
		for (ElevationSlope dataset : processed) {
			LinkedList<BRDFinput> brdfInputs = dataset.getBRDFIn();
			// BRDFinput brdfIn = brdfInputs.getFirst();
			Vector3d norm = new Vector3d(0, 0, 0);

			AggregateSummaryStatistics stats = new AggregateSummaryStatistics();

			LinkedList<BRDFinput> que = Lists.newLinkedList();
			for (BRDFinput brdFinput : brdfInputs) {
				logger.inf("Iterating over t=%f", brdFinput.getCurrentTime());
				que.add(brdFinput);
				norm = que.getFirst().getTerrainNormal();
				System.out.println("Testing " + norm);

				if (que.size() >= QUE_SIZE) {
					System.out.println("Computing for " + norm);
					que.removeFirst();
					compareBrdf(que, stats.createContributingStatistics());
					try {
						System.out.println("press enter");
						new BufferedReader(new InputStreamReader(System.in)).readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("continuing ...");
				}
			}
			System.out.println(toString(stats.getSummary()));
		}
	}

	@Override
	protected List<Constellation> mkConstellations() {
		LinkedList<Constellation> l = Lists.newLinkedList();
		l.add(Constellation.swarm(4, 0.007, 500));
		return l;
	}

	@Override
	protected List<SimTemplate> mkTemplates(Constellation constellation) {
		LinkedList<SimTemplate> tmpls = Lists.newLinkedList();
		SimTemplate template = new SimTemplate(constellation, 200);
		tmpls.add(template);
		return tmpls;
	}

	public String toString(StatisticalSummary stats) {
		StringBuffer outBuffer = new StringBuffer();
		String endl = "\n";
		outBuffer.append("DescriptiveStatistics:").append(endl);
		outBuffer.append("n: ").append(stats.getN()).append(endl);
		outBuffer.append("min: ").append(stats.getMin()).append(endl);
		outBuffer.append("max: ").append(stats.getMax()).append(endl);
		outBuffer.append("mean: ").append(stats.getMean()).append(endl);
		outBuffer.append("std dev: ").append(stats.getStandardDeviation()).append(endl);
		return outBuffer.toString();
	}
}
