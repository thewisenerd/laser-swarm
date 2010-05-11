package com.google.code.laserswarm.simulation.postSimulation;

import com.google.code.laserswarm.conf.Configuration;

/**
 * Compute the radiated power of a gray body over a given wavelength interval
 * 
 * @author simon
 * 
 */
public class RadiatedNoise {

	/**
	 * The radiance integration approximation (see Rees, p 26).
	 * 
	 * @param x
	 *            The independent wavelength coordinate
	 * @return Returns the integration approximation.
	 */
	private static double f(double x) {
		double f = -1;
		if (x <= 0.5) {
			f = 15
					/ Math.pow(Math.PI, 4)
					* (Math.pow(x, 3) / 3 - Math.pow(x, 4) / 8 + Math.pow(x, 5) / 60 - Math.pow(x, 7) / 5040);
		} else {
			double sum = 0;
			for (int m = 1; m < 3; m++) {
				sum += Math.exp(-m * x) * // 
						(+(Math.pow(x, 3) / m)//
								+ (3 * x * x) / (m * m)//
								+ (6 * x) / (m * m * m) //
						+ 6 / (m * m * m * m));
			}
			f = 1 - (15 / Math.pow(Math.PI, 4) * sum);
		}

		return f;
	}

	/**
	 * Gets the 'x' variable for the radiance approximation (see Rees, p 24-25).
	 * 
	 * @param lambda
	 *            The wavelength that gives x.
	 * @return Returns x.
	 */
	private static double getX(double lambda, double temp, double eps) {
		double x = Configuration.h * Configuration.c / (lambda * Configuration.k * temp * eps);
		return x;
	}

	/**
	 * Compute the radiated power of a grey body in a given waveband
	 * 
	 * @param centerWaveLength
	 *            Center wavelength considered
	 * @param waveLengthBandwidth
	 *            Complete wavelength bandwith (half above and half below the center wavelength)
	 * @param solAngle
	 *            Solid angle that the power is radiated over
	 * @param temp
	 *            Grey body temperature
	 * @param eps
	 *            Grey body epison
	 * @return Power emitted in the given waveband
	 * 
	 */
	public static double radiatedPower(double centerWaveLength, double waveLengthBandwidth,
			double solAngle, double temp, double eps) {
		double lambda1 = centerWaveLength - 0.5 * waveLengthBandwidth;
		double lambda2 = centerWaveLength + 0.5 * waveLengthBandwidth;

		double power = Configuration.sigma * Math.pow(temp, 4)
				* (f(getX(lambda1, temp, eps)) - f(getX(lambda2, temp, eps)));
		return power * solAngle;
	}

}
