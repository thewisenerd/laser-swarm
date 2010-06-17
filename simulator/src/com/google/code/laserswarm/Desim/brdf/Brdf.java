package com.google.code.laserswarm.Desim.brdf;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.MicrosphereInterpolator;

import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.math.Distribution;

public class Brdf extends Distribution {

	private MultivariateRealFunction	interpolatedDiscreteBRDF;

	public Brdf(DiscreteBrdf discreteBRDF) throws MathException {
		this(discreteBRDF, 1);
	}

	public Brdf(DiscreteBrdf discreteBRDF, double scale) throws MathException {
		MicrosphereInterpolator interpolator = new MicrosphereInterpolator();
		double[][] xVals = discreteBRDF.asArrayPoints();
		double[] yVals = discreteBRDF.asArrayValues(scale);

		interpolatedDiscreteBRDF = interpolator.interpolate(xVals, yVals);
	}

	@Override
	public double probability(Vector3d x) {
		Point3d sphere = Convert.toSphere(x);
		double lon = sphere.y;
		double lat = sphere.z;

		try {
			return interpolatedDiscreteBRDF.value(new double[] { lon, lat });
		} catch (MathException e) {
			throw new RuntimeException(e);
		}
	}
}
