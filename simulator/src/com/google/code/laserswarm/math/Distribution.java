package com.google.code.laserswarm.math;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.code.laserswarm.util.CSVwriter;
import com.google.code.laserswarm.util.GuiFactory;
import com.google.common.collect.Lists;
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

	private static final Logger	logger	= Logger.get(Distribution.class);

	public static void main(String[] args) {
		Configuration.getInstance(args);

		ScatteringParam param = new ScatteringParam(1.5, 1.3, -0.5);
		ScatteringCharacteristics sc = new ScatteringCharacteristics(new Vector3d(0, 1, 1), param);

		JFrame fr = GuiFactory.getDefaultJFrame("Scatter");
		fr.add(new JLabel(new ImageIcon(sc.toImage())));
		fr.setVisible(true);
	}

	public PointCloudComparison compareTo(Distribution distribution2) {
		return new PointCloudComparison(this, distribution2, 0, false);
	}

	public List<Point3d> pointCloud(int steps) {
		return pointCloud(steps, 0, Math.PI * 2, 0, Math.PI / 2);
	}

	public List<Point3d> pointCloud(int steps, double azMin, double azMax, double elMin, double elMax) {
		double step = (2 * Math.PI) / steps;
		List<Point3d> points = Lists.newLinkedList();
		for (double az = azMin; az < azMax; az += step) {
			for (double el = elMin; el < elMax; el += step) {
				Vector3d exittanceVector = new Vector3d(
						Convert.toXYZ(new Point3d(1, az, Math.PI / 2 - el)));
				double out = probability(exittanceVector);
				points.add(new Point3d(az, el, out));
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

	public void toCSV(String filename) {
		CSVwriter csv = new CSVwriter(new File(filename), "\t");

		List<Point3d> points = pointCloud(100);
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
		File kernelFile = Configuration.getInstance().getMathematicaKernel();

		if (kernelFile == null || !kernelFile.exists())
			throw new LinkageError("Cannot find the Mathematica Kernel");

		KernelLink ml = null;
		try {
			String[] mlArgs = { "-linkmode", "launch", "-linkname", kernelFile.getAbsolutePath() };
			ml = MathLinkFactory.createKernelLink(mlArgs);
			ml.discardAnswer();
		} catch (MathLinkException e) {
			logger.wrn(e, "Kernel connection failed");
			throw new LinkageError("An error occurred connecting to the kernel)");
		}

		File csv = new File(Configuration.volatileCache, "scatter.csv");
		toCSV(csv.getAbsolutePath());

		byte[] gifData = ml
				.evaluateToImage(
						"Clear[\"Global`*\"];"
								+ "data = Import[\"" + csv.getAbsolutePath() + "\", \"Table\"];"
								+ "scale = data[[All, 6]];"
								+ "scale = scale /Mean[scale];"
								+ "coordinates = data[[All, 3 ;; 5]];"
								+ "ListPointPlot3D[coordinates*scale]",
						800, 600, 0, false);
		Image im = Toolkit.getDefaultToolkit().createImage(gifData);
		return im;
	}

}
