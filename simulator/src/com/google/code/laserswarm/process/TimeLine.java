package com.google.code.laserswarm.process;

import static com.google.code.laserswarm.math.VectorMath.relative;
import jat.cm.Constants;

import java.util.List;
import java.util.TreeMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Atmosphere;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.code.laserswarm.math.LookupTable;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class TimeLine {

	public static int					noiseFreq		= (int) 1E4;

	private double						ePhoton;

	// time, nrPhotons
	private TreeMap<Double, Integer>	laserPhotons	= Maps.newTreeMap();
	// time, nrPhotons/s
	private TreeMap<Double, Double>		noisePhotons	= Maps.newTreeMap();
	private LookupTable					lookupPosition	= new LookupTable();
	private LookupTable					lookupDirection	= new LookupTable();

	private List<SimVars>				dataSet;
	private Satellite					sat;
	private Constellation				constellation;

	private static final Logger			logger			= Logger.get(TimeLine.class);

	public TimeLine(Satellite sat, Constellation constellation, List<SimVars> dataSet) {
		this.constellation = constellation;
		this.dataSet = dataSet;
		this.sat = sat;

		ePhoton = (Constants.c * 6.62606896E-34) / constellation.getLaserWaveLength();

		makePhotons();
		makeLookupTables();
	}

	/**
	 * The radiance integration approximation (see Rees, p 26).
	 * 
	 * @param x
	 *            The independent wavelength coordinate
	 * @return Returns the integration approximation.
	 */
	private double f(double x) {
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
	 * <pre>
	 * 	    * alpha
	 * 	    | \
	 * 	  c	 \	\  b
	 *        |   \
	 * 	beta   \    \ gamma
	 *          -----
	 * 			  a
	 * </pre>
	 * 
	 * 
	 * @param current
	 * @return
	 */
	private double findA(SimVars current) {
		// sin(gamma)/c = sin(alpha)/a => a
		double alpha = getSatellite().getBeamDivergence();
		Vector3d dR = relative(current.pR, current.pE.get(getSatellite()));
		double beta = Math.PI - new Vector3d(current.pE.get(getSatellite())).angle(dR);
		double gamma = Math.PI - beta - alpha;
		if (gamma <= 0) // circle
			return Math.PI * Math.pow(dR.length() * Math.tan(alpha), 2);
		double c = dR.length();
		double a = c * (Math.sin(alpha) / Math.sin(gamma)); // Semi-major axis

		double minorAxis = c * Math.tan(alpha); // Semi-minor axis

		double area = Math.PI * a * minorAxis; // Area ellipse
		return area;
	}

	/**
	 * Iterator to loop over the measerments
	 * 
	 * @param binFreqency
	 *            Sample frequency of the receiver
	 * @return An iterator of the data
	 * @throws MathException
	 */
	public SampleIterator getIterator(int binFreqency) throws MathException {
		final double binTime = 1. / binFreqency;
		logger.inf("Bin f=%s\tBin t=%s", binFreqency, binTime);

		final TreeMap<Double, Integer> laserPhotons = Maps.newTreeMap(this.laserPhotons);

		double[] x = new double[noisePhotons.size()];
		double[] y = new double[noisePhotons.size()];
		int i = 0;
		for (Double t : noisePhotons.keySet()) {
			x[i] = t;
			y[i] = noisePhotons.get(t) * binTime;
			y[i] += (Math.random() <= y[i] - Math.round(y[i]) ? 1 : 0);

			if (Double.isNaN(y[i]))
				y[i] = 0;

			i++;
		}
		final PolynomialSplineFunction noise = new LoessInterpolator().interpolate(x, y);
		return new SampleIterator(binFreqency, laserPhotons, noise);
	}

	public LookupTable getLookupDirection() {
		return lookupDirection;
	}

	public LookupTable getLookupPosition() {
		return lookupPosition;
	}

	public Satellite getSatellite() {
		return sat;
	}

	/**
	 * Gets the 'x' variable for the radiance approximation (see Rees, p 24-25).
	 * 
	 * @param lambda
	 *            The wavelength that gives x.
	 * @return Returns x.
	 */
	private double getX(double lambda, double temp, double eps) {
		double x = Configuration.h * Configuration.c / (lambda * Configuration.k * temp * eps);
		return x;
	}

	private void makeLookupTables() {
		for (SimVars simVars : dataSet) {
			double t = simVars.t0;
			Point3d satPos = simVars.pE.get(getSatellite());
			lookupPosition.put(t, satPos);

			Vector3d dir = relative(satPos, simVars.pR);
			dir.normalize();
			lookupDirection.put(t, dir);
		}
	}

	private void makePhotons() {
		for (SimVars current : dataSet) {
			double t = current.t0 + current.tR + current.tE.get(sat);
			/* Add measured photons */
			Integer nrPhotons = current.photonsE.get(sat);
			laserPhotons.put(t, nrPhotons);
			/* Introduce noise */
			double area = findA(current);

			/* Find the average scattering characteristics */
			ScatteringParam param = current.scatter.getParam();

			/* Find the average satellite position */
			Vector3d position = new Vector3d(current.pE.get(getSatellite()));
			/* Find the average reflection position */
			Vector3d reflection = new Vector3d(current.pR);

			/* Make scatterer */
			double angle = Math.acos((current.sunVector.dot(position))
					/ (current.sunVector.length() * position.length()));
			double z = Math.cos(angle);
			double x = Math.sin(angle);
			Vector3d incidence = new Vector3d(-x, 0, -z);
			ScatteringCharacteristics scatter = new ScatteringCharacteristics(incidence, param);

			/* Find the power of the sun in the given frequency */
			double powerIn = 0;
			if (current.illuminated) {
				double solAngle = area * Math.cos(current.sunVector.angle(new Vector3d(current.pR)))
						/ (4 * Math.PI * Configuration.R0);
				double exoatmosphericRadiance = radiatedPower(constellation.getLaserWaveLength(),
						constellation.getReceiverBandWidth(), solAngle, // 
						Configuration.TSun, Configuration.epsSun);
				powerIn = Atmosphere.getInstance().computeIntensity(exoatmosphericRadiance * solAngle,
						current.sunVector.angle(new Vector3d(current.pR)));
			} else {
				double solAngle = area / (4 * Math.PI * Configuration.R0);
				powerIn = radiatedPower(constellation.getLaserWaveLength(), constellation
						.getReceiverBandWidth(), solAngle, // 
						Configuration.TEarth, Configuration.epsEarth);
			}

			angle = Math.acos(((position.dot(reflection)) / (position.length() * reflection.length())));
			z = position.length() * Math.cos(angle);
			x = position.length() * Math.sin(angle);
			Vector3d exittanceVector = new Vector3d(x, 0, z);
			// exittanceVector.negate();
			double scatteredPower = scatter.probability(exittanceVector) * powerIn;

			double totalReceivedPower = Atmosphere.getInstance().computeIntensity( //
					scatteredPower, position.angle(reflection)) * getSatellite().getAperatureArea();

			/* Find the number of photons */
			double nrP = (totalReceivedPower / ePhoton); // # per second
			if (nrP == 0)
				System.out.println("NOO (nr photons == 0)");
			noisePhotons.put(t, nrP);
		}
		logger.dbg("Done making noise");
	}

	private double radiatedPower(double centerWaveLength, double waveLengthBandwidth, double solAngle,
			double temp, double eps) {
		double lambda1 = centerWaveLength - 0.5 * waveLengthBandwidth;
		double lambda2 = centerWaveLength + 0.5 * waveLengthBandwidth;

		double power = Configuration.sigma * Math.pow(temp, 4)
				* (f(getX(lambda1, temp, eps)) - f(getX(lambda2, temp, eps)));
		return power;
	}
}
