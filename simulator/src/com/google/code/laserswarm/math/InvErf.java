package com.google.code.laserswarm.math;

import org.apache.commons.math.MathException;

public class InvErf {

	private static long[]	teller	= new long[] { 1, 1, 7, 127, 4369, 243649, 20036983, 2280356863L,
									343141433761L, 65967241200001L, 15773461423793767L,
									4591227123230945407L };
	private static long[]	noemer	= new long[] { 1, 3, 30, 630, 22680, 1247400, 97297200,
									10216206000L, 1389404016000L, 237588086736000L, 49893498214560000L };
	public static int		ORDER	= 7;

	/**
	 * Default constructor. Prohibit instantiation.
	 */
	private InvErf() {
		super();
	}

	/**
	 * Returns the inverse of the error function erf(x).
	 * 
	 * 
	 * <p>
	 * Algorithm according to http://mathworld.wolfram.com/InverseErf.html equation 16
	 * </p>
	 * 
	 * @param x
	 *            the value.
	 * @return the inverse error function invErf(x)
	 * @throws MathException
	 *             if the algorithm fails to converge.
	 */
	public static double invErf(double x) throws MathException {
		double x2 = x * Math.sqrt(Math.PI) / 2;
		double invErf = 0;
		for (int i = 1; i < ORDER; i += 2) {
			int idx = (i + 1) / 2;
			invErf += (teller[idx] / noemer[idx]) * Math.pow(x2, i);
		}
		return invErf;
	}
}
