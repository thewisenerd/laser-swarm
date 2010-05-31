package com.google.code.laserswarm.math;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.distribution.AbstractDistribution;
import org.apache.commons.math.special.Erf;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class EllipticalArea extends AbstractDistribution implements UnivariateRealFunction {

	private static final long	serialVersionUID	= 1L;
	private static final Logger	logger				= Logger.get(EllipticalArea.class);

	private double				a;
	private double				b;

	public EllipticalArea() {
		this(1, 1);
	}

	public EllipticalArea(double a, double b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public double cumulativeProbability(final double x) throws MathException {
		return 0.707107 * a * InvErf.invErf(-1. + 2 * x);
	}

	public double value(double x) throws FunctionEvaluationException {
		try {
			return 0.5 * (1 + Erf.erf((1.41421 * x) / a));
		} catch (MathException e) {
			throw new FunctionEvaluationException(e, x);
		}
	}

}
