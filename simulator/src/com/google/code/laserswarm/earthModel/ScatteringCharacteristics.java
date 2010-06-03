package com.google.code.laserswarm.earthModel;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.math.Distribution;
import com.google.code.laserswarm.util.CSVwriter;
import com.google.code.laserswarm.util.GuiFactory;
import com.lyndir.lhunath.lib.system.logging.Logger;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

public class ScatteringCharacteristics implements Distribution {
	private static final Logger	logger	= Logger.get(ScatteringCharacteristics.class);

	public static void main(String[] args) {
		Configuration.getInstance(args);

		ScatteringParam param = new ScatteringParam(1.5, 1.3, -0.5);
		ScatteringCharacteristics sc = new ScatteringCharacteristics(new Vector3d(0, 1, 0), param);

		JFrame fr = GuiFactory.getDefaultJFrame("Scatter");
		fr.add(new JLabel(new ImageIcon(sc.toImage())));
		fr.setVisible(true);
	}

	private double		refrAir	= 1.0002926;

	private double		refrSurf;
	private double		kappa;
	private double		Theta;
	private double		theta0;
	private double		R_Lambertian;
	private Vector3d	incidenceVector;

	/**
	 * Set up the ScatteringCharacteristics
	 * 
	 * @param incidenceVector
	 *            Direction of the incoming radiation, where the z-axis of the coordinate system is the
	 *            local surface normal
	 * @param indexOfRefraction
	 *            The index of reflection of the local terrain
	 * @param kappaMinnaert
	 *            The local Minnaert constant (kappa)
	 * @param thetaHenyeyGreenstein
	 *            The local Henyey-Greenstein constant (theta)
	 */
	public ScatteringCharacteristics(Vector3d incidenceVector, double indexOfRefraction,
			double kappaMinnaert, double thetaHenyeyGreenstein) {
		// find angle between incidence and surface normal
		theta0 = incidenceVector.angle(new Vector3d(0, 0, 1));
		if (theta0 > Math.PI / 2)
			theta0 -= Math.PI / 2;
		refrSurf = indexOfRefraction;
		kappa = kappaMinnaert;
		Theta = thetaHenyeyGreenstein;
		this.incidenceVector = incidenceVector;
		// find the transmittance angle from Snell's law
		double theta_t = Math.asin(refrAir / refrSurf * Math.sin(theta0));

		// find the Lambertian BRDF
		double fresnel_s = Math.pow( //
				((refrAir * Math.cos(theta0) - refrSurf * Math.cos(theta_t)) / //
				(refrAir * Math.cos(theta0) + refrSurf * Math.cos(theta_t))) //
				, 2);
		double fresnel_p = Math.pow( //
				((refrAir * Math.cos(theta_t) - refrSurf * Math.cos(theta0)) / //
				(refrAir * Math.cos(theta_t) + refrSurf * Math.cos(theta0))) //
				, 2);
		R_Lambertian = 2 / Math.PI * Math.abs(fresnel_s * fresnel_p) * Math.cos(theta0)
				* Math.sin(theta0);
	}

	/**
	 * Set up the ScatteringCharacteristics
	 * 
	 * @param incidenceVector
	 *            Direction of the incoming radiation, where the z-axis of the coordinate system is the
	 *            local surface normal
	 * @param param
	 *            The key scattering parameters
	 */
	public ScatteringCharacteristics(Vector3d incidenceVector, ScatteringParam param) {
		this(incidenceVector, param.getIndexOfRefraction(), param.getKappaMinnaert(), param
				.getThetaHenyeyGreenstein());
	}

	public ScatteringParam getParam() {
		return new ScatteringParam(refrSurf, kappa, Theta);
	}

	@Override
	/**
	 * Find the percentage of incoming radiation (irradiation) that is emitted in the specified direction
	 * 
	 * @param exittanceVector
	 *            Direction of the outgoing radiation, where the z-axis of the coordinate system is the
	 *            local surface normal
	 * @return the percentage of incoming radiation (irradiation) that is emitted in the specified direction
	 */
	public double probability(Vector3d exittanceVector) {
		// find angle between exittance and surface normal
		double theta1 = exittanceVector.angle(new Vector3d(0, 0, 1));
		if (theta1 > Math.PI / 2)
			theta1 -= Math.PI / 2;
		// incidence and exittance projected to surface
		Vector3d projIncidence = new Vector3d(incidenceVector.x, incidenceVector.y, 0);
		Vector3d projExittance = new Vector3d(exittanceVector.x, exittanceVector.y, 0);
		// phi_1 - phi_0 = dPhi. Used in the Henyey-Greenstein correction
		double dPhi = projIncidence.angle(projExittance);
		if (projExittance.lengthSquared() == 0)
			dPhi = 0;
		// Apply Minnaert correction. Rees, p50
		double R_Minnaert = R_Lambertian * (Math.pow(Math.cos(theta0) * Math.cos(theta1), kappa - 1));
		// Apply Henyey-Greenstein correction. Rees, p51
		double R_HenyeyGreenstein = R_Minnaert
				* (1 - Theta * Theta)
				/ Math.pow((1
						+ 2
						* Theta
						* (Math.cos(theta0) * Math.cos(theta1) + Math.sin(theta0) * Math.sin(theta1)
								* Math.cos(dPhi)) + Theta * Theta), 1.5);
		return R_HenyeyGreenstein;
	}

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
	private Image toImage() throws LinkageError {
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

		File csv = new File(Configuration.volitileCache, "scatter.csv");
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

	@Override
	public String toString() {
		return "Index of Refraction:" + refrSurf + "\nkappaMinnaert:" + kappa
				+ "\nthetaHenyeyGreenstein:" + Theta;
	}
}
