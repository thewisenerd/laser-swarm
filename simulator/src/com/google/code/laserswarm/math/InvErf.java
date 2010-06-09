package com.google.code.laserswarm.math;

import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Erf;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class InvErf {

	private static double[]		a		= new double[] { 0.886226899, -1.645349621, 0.914624893,
										-0.140543331 };
	private static double[]		b		= new double[] { -2.118377725, 1.442710462, -0.329097515,
										0.012229801 };
	private static double[]		c		= new double[] { -1.970840454, -1.624906493, 3.429567803,
										1.641345311 };
	private static double[]		d		= new double[] { 3.543889200, 1.637067800 };

	private static final Logger	logger	= Logger.get(InvErf.class);

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
	public static double invErf(double y) throws MathException {
		double x = Double.NaN;
		// Central range.
		double y0 = .7;
		double z;

		// Exceptional cases.
		if (y == -1)
			return Double.NEGATIVE_INFINITY;
		if (y == 1)
			return Double.POSITIVE_INFINITY;
		if (Math.abs(y) > 1)
			return Double.NaN;

		if (Math.abs(y) <= y0) {
			z = y * y;
			x = y * (((a[3] * z + a[2]) * z + a[1]) * z + a[0]) /
					((((b[3] * z + b[2]) * z + b[1]) * z + b[0]) * z + 1);
		}
		// Near end points of range.
		else if ((y0 < y) && (y < 1)) {
			z = Math.sqrt(-Math.log((1 - y) / 2));
			x = (((c[3] * z + c[2]) * z + c[1]) * z + c[0]) / ((d[1] * z + d[0]) * z + 1);
		} else if ((-y0 > y) & (y > -1)) {
			z = Math.sqrt(-Math.log((1 + y) / 2));
			x = -(((c[3] * z + c[2]) * z + c[1]) * z + c[0]) / ((d[1] * z + d[0]) * z + 1);
		} else
			throw new MathException("Outside range for %f", y);

		// Two steps of Newton-Raphson correction to full accuracy.
		// Without these steps, erfinv(y) would be about 3 times
		// faster to compute, but accurate to only about 6 digits.
		x = x - (Erf.erf(x) - y) / (2 / Math.sqrt(Math.PI) * Math.exp(-x * x));
		x = x - (Erf.erf(x) - y) / (2 / Math.sqrt(Math.PI) * Math.exp(-x * x));

		return x;
	}

	/**
	 * Default constructor. Prohibit instantiation.
	 */
	private InvErf() {
		super();
	}
}
