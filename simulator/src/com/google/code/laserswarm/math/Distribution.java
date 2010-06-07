package com.google.code.laserswarm.math;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.util.CSVwriter;
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

		double step = Math.PI / 100;

		for (double az = 0; az < 2 * Math.PI; az += step) {
			for (double el = 0; el < Math.PI / 2; el += step) {
				Vector3d exittanceVector = new Vector3d(
						Convert.toXYZ(new Point3d(1, az, Math.PI / 2 - el)));
				double out = probability(exittanceVector);
				csv.write(az, el, exittanceVector.x, exittanceVector.y, exittanceVector.z, out);
			}
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
