package com.google.code.laserswarm.earthModel;

import java.util.SortedMap;

import javax.vecmath.Vector3d;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.google.code.laserswarm.math.Distribution;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ScatteringCharacteristics extends Distribution {
	private static final Logger	logger	= Logger.get(ScatteringCharacteristics.class);

	private static double		refrAir	= 1.0002926;

	public static void main(String[] args) {
		ScatteringCharacteristics.mkLookupTable(0, 5, 10, (long) 1E4);
	}

	public static void mkLookupTable(double min, double max, int maxIter, long maxTime) {
		long t0 = System.currentTimeMillis();

		double step = (max - min) / 2;
		double current = min;

		SortedMap<Double, Double> computed = Maps.newTreeMap();
		final UnivariateRealIntegrator integrator = new SimpsonIntegrator();

		logger.inf("Making lookup table");
		while (System.currentTimeMillis() - t0 <= maxTime && computed.size() <= maxIter) {
			final double refrSurf = current;

			logger.dbg("Iteration=%d, Time=%d", computed.size(), System.currentTimeMillis() - t0);
			logger.dbg("RefrSurf=%f", refrSurf);
			UnivariateRealFunction lambertianF = new UnivariateRealFunction() {
				@Override
				public double value(double x) throws FunctionEvaluationException {
					double theta0 = x;
					double theta_t = Math.asin(refrAir / refrSurf * Math.sin(theta0));
					double fresnel_s = Math.pow(
									((refrAir * Math.cos(theta0) - refrSurf * Math.cos(theta_t)) /
									(refrAir * Math.cos(theta0) + refrSurf * Math.cos(theta_t)))
									, 2);
					double fresnel_p = Math.pow( //
							((refrAir * Math.cos(theta_t) - refrSurf * Math.cos(theta0)) /
									(refrAir * Math.cos(theta_t) + refrSurf * Math.cos(theta0)))
									, 2);
					double R_Lambertian = 2 * Math.abs(fresnel_s * fresnel_p) * Math.cos(theta0)
									* Math.sin(theta0);
					return R_Lambertian;
				}
			};

			try {
				double value = integrator.integrate(lambertianF, 0, Math.PI / 2);
				computed.put(current, value);
				logger.dbg("R_Lambertian=%f", value);
			} catch (MathException e) {
				e.printStackTrace();
				System.exit(1);
			}

			/* Propagate to the next point */
			/* Check if the next step will overflow the range */
			if (current + 2 * step <= min || current + 2 * step >= max) {
				step = step / 2;
				current += step;
				step = step * -1;
			} else {
				current += 2 * step;
			}
		}

	}

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
		// // find the transmittance angle from Snell's law
		// double theta_t = Math.asin(refrAir / refrSurf * Math.sin(theta0));
		//
		// // find the Lambertian BRDF
		// double fresnel_s = Math.pow( //
		// ((refrAir * Math.cos(theta0) - refrSurf * Math.cos(theta_t)) / //
		// (refrAir * Math.cos(theta0) + refrSurf * Math.cos(theta_t))) //
		// , 2);
		// double fresnel_p = Math.pow( //
		// ((refrAir * Math.cos(theta_t) - refrSurf * Math.cos(theta0)) / //
		// (refrAir * Math.cos(theta_t) + refrSurf * Math.cos(theta0))) //
		// , 2);
		// R_Lambertian = 2 / Math.PI * Math.abs(fresnel_s * fresnel_p) * Math.cos(theta0)
		// * Math.sin(theta0);

		PolynomialFunction poly = new PolynomialFunction(
				new double[] { -0.0002, -0.0008, 0.0169, -0.0287, 0.0176 });
		R_Lambertian = poly.value(indexOfRefraction);
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
		if (theta1 > Math.PI / 2 && theta1 < (3 / 2) * Math.PI)
			theta1 -= Math.PI;
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

	@Override
	public String toString() {
		return "Index of Refraction:" + refrSurf + "\nkappaMinnaert:" + kappa
				+ "\nthetaHenyeyGreenstein:" + Theta;
	}
}
