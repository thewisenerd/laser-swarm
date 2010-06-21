package com.google.code.laserswarm.Desim.brdf;

import static com.google.code.laserswarm.math.Convert.toVector;
import static com.google.code.laserswarm.math.Convert.toXYZ;

import java.util.Set;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.opengis.coverage.PointOutsideCoverageException;

import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.code.laserswarm.math.Distribution;
import com.google.code.laserswarm.util.GuiFactory;
import com.google.common.collect.Sets;

public class DiscreteBrdf {

	public static void main(String[] args) {
		DiscreteBrdf discreteBrdf = DiscreteBrdf.random(50);
		try {
			Brdf brdf = new Brdf(discreteBrdf);
			GuiFactory.getImageFrame("test", brdf.toImage());
		} catch (MathException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static DiscreteBrdf random(int rays) {
		ScatteringParam param = ScatteringParam.random();
		ScatteringCharacteristics brdf = new ScatteringCharacteristics(
				new Vector3d(Math.random(), Math.random(), Math.random()), param);

		DiscreteBrdf discreteBrdf = new DiscreteBrdf();
		for (int i = 0; i < rays; i++) {
			Vector3d ray = new Vector3d(1, Math.random() * 2 * Math.PI, Math.random() * Math.PI / 2);
			Point3d rayXYZ = toXYZ(ray);
			ray.x = brdf.probability(toVector(rayXYZ));
			discreteBrdf.addVector(ray);
		}
		return discreteBrdf;
	}

	private Set<BrdfSection2>	sections		= Sets.newHashSet();
	private double				internalScale	= 1;

	public void addVector(Tuple3d vector) {
		if (vector.y < 0)
			vector.y += Math.PI * 2;
		if (vector.z < 0)
			vector.z += Math.PI * 2;

		boolean added = false;
		for (BrdfSection2 section : sections) {
			try {
				section.add(vector);
				added = true;
				break;
			} catch (PointOutsideCoverageException e) {
				continue;
			}
		}

		if (!added) {
			BrdfSection2 section = BrdfSection2.findSection(vector);
			section.add(vector);
			sections.add(section);
		}

	}

	/**
	 * Get the coordinates of the points in Array( {lon, lat} )
	 * 
	 * @return
	 */
	public double[][] asArrayPoints() {
		double[][] points = new double[sections.size()][2];
		int i = 0;
		for (BrdfSection2 section : sections) {
			points[i][0] = section.getBounds().getCenterX();
			points[i][1] = section.getBounds().getCenterY();
			i++;
		}
		return points;
	}

	/**
	 * Get the brdf value off all the coordinates in asArrayPoints (correct order)
	 * 
	 * @return
	 */
	public double[] asArrayValues() {
		double[] vals = new double[sections.size()];
		int i = 0;
		for (BrdfSection2 section : sections) {
			vals[i] = section.getPhotonCountAvg() * internalScale;
			i++;
		}
		return vals;
	}

	public double[] asArrayValues(double scale) {
		double[] vals = new double[sections.size()];
		int i = 0;
		for (BrdfSection2 section : sections) {
			vals[i] = section.getPhotonCountAvg() * scale * internalScale;
			i++;
		}
		return vals;
	}

	public StatisticalSummary compareTo(Distribution distribution) {
		SummaryStatistics stats = new SummaryStatistics();
		return compareTo(distribution, stats);
	}

	public StatisticalSummary compareTo(Distribution distribution, SummaryStatistics stats) {

		for (BrdfSection2 section : sections) {
			Point3d resulatant = section.getResulatant();
			double ourValue = resulatant.x * internalScale;
			Point3d direction = toXYZ(resulatant);
			double thereValue = distribution.probability(toVector(direction));
			stats.addValue(thereValue - ourValue);
		}

		return stats;
	}

	public double getInternalScale() {
		return internalScale;
	}

	public Set<BrdfSection2> getSections() {
		return sections;
	}

	/**
	 * Set the internal scale to match the given distribution
	 * 
	 * @param distribution
	 * @return returns the internal scale
	 */
	public double scaleTo(Distribution distribution) {
		internalScale = 1;

		double ownSum = 0;
		double equivalentSum = 0;
		for (BrdfSection2 section : sections) {
			Point3d resulatant = section.getResulatant();
			ownSum += resulatant.x;
			Point3d direction = toXYZ(resulatant);
			equivalentSum += distribution.probability(toVector(direction));
		}
		internalScale = equivalentSum / ownSum;
		return internalScale;
	}
}
