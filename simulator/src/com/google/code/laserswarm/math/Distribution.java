package com.google.code.laserswarm.math;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.vecmath.Point3d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.code.laserswarm.util.CSVwriter;
import com.google.code.laserswarm.util.GuiFactory;
import com.google.code.laserswarm.util.ThreadRunner;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

/**
 * A distribution implemention.
 * <p>
 * Evaluate the probability in a given direction (vector, normalized). <br />
 * The up vector is considered to be <0,1,0>
 * </p>
 * 
 * @author Simon Billemont, TUDelft, Faculty Aerospace Engineering (aodtorusan@gmail.com or
 *         s.billemont@student.tudelft.nl)
 * 
 */
public abstract class Distribution {

	public static final Logger	logger	= Logger.get(Distribution.class);
	public static KernelLink	ml;

	public static void main(String[] args) {
		Configuration.getInstance(args);

		ScatteringParam param = new ScatteringParam(1.5, 1.3, -0.5);
		ScatteringCharacteristics sc = new ScatteringCharacteristics(new Vector3d(0, 1, 1), param);

		JFrame fr = GuiFactory.getDefaultJFrame("Scatter");
		fr.add(new JLabel(new ImageIcon(sc.toImage())));
		fr.setVisible(true);
	}

	private boolean	MULTI_THREAD	= false;

	public DistributionComparison compareTo(Distribution distribution2) {
		return new DistributionComparison(this, distribution2, false);
	}

	public DistributionComparison compareTo(Distribution distribution2, Point4d range) {
		return new DistributionComparison(this, distribution2, range, false);
	}

	private void loadKernel() {
		if (ml == null) {

			File kernelFile = Configuration.getInstance().getMathematicaKernel();
			if (kernelFile == null || !kernelFile.exists())
				throw new LinkageError("Cannot find the Mathematica Kernel");

			try {
				String[] mlArgs = { "-linkmode", "launch", "-linkname", kernelFile.getAbsolutePath() };
				ml = MathLinkFactory.createKernelLink(mlArgs);
				ml.discardAnswer();
			} catch (MathLinkException e) {
				logger.wrn(e, "Kernel connection failed");
				throw new LinkageError("An error occurred connecting to the kernel)");
			}
		}
	}

	public Set<Point3d> pointCloud(int steps) {
		return pointCloud(steps, new Point4d(0, Math.PI * 2, 0, Math.PI / 2));
	}

	public Set<Point3d> pointCloud(int steps, Point4d range) {
		double step = (2 * Math.PI) / steps;
		final Set<Point3d> points = Sets.newHashSet();

		if (MULTI_THREAD) {
			int size = ((int) Math.ceil((range.y - range.x) / step) + 1)
					* ((int) Math.ceil((range.w - range.z) / step));
			Double[][] coordinates = new Double[size][2];

			int i = 0;
			for (double az = range.x; az < range.y; az += step) {
				for (double el = range.z; el < range.w; el += step) {
					coordinates[i] = new Double[] { az, el };
					i++;
				}
			}

			List<Double[][]> splitCoordinates = splitQue(coordinates, 4);
			HashSet<Thread> threads = Sets.newHashSet();
			for (final Double[][] coordinateSubset : splitCoordinates) {
				threads.add(new Thread() {
					@Override
					public void run() {
						for (Double[] doubles : coordinateSubset) {
							double az = doubles[0];
							double el = doubles[1];
							Vector3d exittanceVector = new Vector3d(
									Convert.toXYZ(new Point3d(1, az, Math.PI / 2 - el)));
							double out = probability(exittanceVector);
							synchronized (points) {
								points.add(new Point3d(az, el, out));
							}
						}
					}
				});
			}

			ThreadRunner<Thread> runner = new ThreadRunner<Thread>(threads);
			runner.setMaxThreads(4);
			runner.start();
			runner.waitForMerge();

		} else {
			for (double az = range.x; az < range.y; az += step) {
				for (double el = range.z; el < range.w; el += step) {
					Vector3d exittanceVector = new Vector3d(
							Convert.toXYZ(new Point3d(1, az, Math.PI / 2 - el)));
					double out = probability(exittanceVector);
					points.add(new Point3d(az, el, out));
				}
			}
		}
		return points;
	}

	/**
	 * Evaluate the distribution for a given direction. <0,0,1> is considered to be up
	 * 
	 * @param x
	 *            Directional vector (normalized)
	 * @return The probability in the given direction
	 */
	public abstract double probability(Vector3d x);

	private List<Double[][]> splitQue(Double[][] points, int parts) {
		LinkedList<Double[][]> list = Lists.newLinkedList();
		int roughSize = Math.round(points.length / parts);
		int add = 0;
		for (int i = 0; i < parts; i++) {
			Double[][] subSet = new Double[roughSize][2];
			for (int j = 0; j < roughSize; j++) {
				subSet[j] = new Double[] { points[add][0], points[add][1] };
				add++;
			}
			list.add(subSet);
		}
		while (add < points.length) {
			ObjectArrays.concat((Object[]) list.getLast(),
					(Object) new Double[] { points[add][0], points[add][1] });
			add++;
		}
		return list;
	}

	public void toCSV(String filename) {
		CSVwriter csv = new CSVwriter(new File(filename), "\t");
		Set<Point3d> points = pointCloud(72);
		for (Point3d point3d : points) {
			double az = point3d.x;
			double el = point3d.y;
			double out = point3d.z;

			Vector3d exittanceVector = new Vector3d(
						Convert.toXYZ(new Point3d(1, az, Math.PI / 2 - el)));
			csv.write(az, el, exittanceVector.x, exittanceVector.y, exittanceVector.z, out);
		}
	}

	/**
	 * Visualize the scattering characteristics to an image.
	 * <p>
	 * This code uses J/Link with mathematica. To use this function you need to have Mathematica
	 * installed, and the appropriate kernel set in the config.
	 * </p>
	 * 
	 * @return
	 * @throws LinkageError
	 *             If there was an error in creating the MathLink to Mathematica
	 */
	public Image toImage() throws LinkageError {
		return toImage(800, 600);
	}

	public Image toImage(int width, int hight) throws LinkageError {

		loadKernel();

		File csv = new File(Configuration.volatileCache, "scatter-" + Math.random() + ".csv");
		toCSV(csv.getAbsolutePath());

		byte[] gifData = ml
				.evaluateToImage(
						"Clear[\"Global`*\"];"
								+ "data = Import[\"" + csv.getAbsolutePath() + "\", \"Table\"];"
								+ "scale = data[[All, 6]];"
								+ "scale = scale /Mean[scale];"
								+ "coordinates = data[[All, 3 ;; 5]];"
								+ "ListPointPlot3D[coordinates*scale]",
						width, hight, 0, false);
		Image im = Toolkit.getDefaultToolkit().createImage(gifData);
		return im;
	}

}
