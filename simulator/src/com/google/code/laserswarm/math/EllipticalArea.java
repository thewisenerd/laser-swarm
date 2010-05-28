package com.google.code.laserswarm.math;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.ComposableFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;
import org.apache.commons.math.distribution.AbstractDistribution;

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
	/**
	 * solve the CDF for x,
	 * so find the location where the area % is x
	 */
	public double cumulativeProbability(final double x) throws MathException {
		UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
		UnivariateRealSolver solver = factory.newDefaultSolver();
		double val = solver.solve(ComposableFunction.ZERO.of(this).subtract(
				new UnivariateRealFunction() {
					@Override
					public double value(double x2) throws FunctionEvaluationException {
						return x;
					}
				}), -a, a, 0);

		return val;
	}

	@Override
	/**
	 * Integration of -a => x for y 
	 */
	public double value(double x) throws FunctionEvaluationException {
		SimpsonIntegrator i = new SimpsonIntegrator();
		try {
			return i.integrate(new UnivariateRealFunction() {

				@Override
				public double value(double x) throws FunctionEvaluationException {
					return EllipticalArea.this.y(x);
				}
			}, -a, x);
		} catch (MathException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * y value of the ellipse, divided with the ellipse area
	 * 
	 * @param x
	 * @return
	 * @throws FunctionEvaluationException
	 */
	public double y(double x) throws FunctionEvaluationException {
		double y = 2 * Math.sqrt((1 - (x * x) / (a * a)) * (b * b)); // evaluate y length
		y = y / (a * b * Math.PI); // Divide out the ellispe area
		return y;
	}

}
