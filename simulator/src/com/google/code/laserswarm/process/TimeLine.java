package com.google.code.laserswarm.process;

import static com.google.code.laserswarm.math.VectorMath.avgVector;
import static com.google.code.laserswarm.math.VectorMath.relative;
import jat.cm.Constants;

import java.util.List;
import java.util.TreeMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Atmosphere;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.code.laserswarm.math.LookupTable;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.common.collect.Maps;

public class TimeLine {

	private double						ePhoton;

	private TreeMap<Double, Integer>	photons			= Maps.newTreeMap();
	private LookupTable					lookupPosition	= new LookupTable();
	private LookupTable					lookupDirection	= new LookupTable();

	private List<SimVars>				dataSet;
	private Satellite					sat;
	private Constellation				constellation;

	public TimeLine(Satellite sat, Constellation constellation, List<SimVars> dataSet) {
		this.constellation = constellation;
		this.dataSet = dataSet;
		this.sat = sat;

		ePhoton = (Constants.c * 6.62606896E-34) / constellation.getLaserWaveLength();

		makePhotons();
		makeLookupTables();
	}

	private void add(Double t, Integer nrPhotons) {
		if (photons.containsKey(t))
			photons.put(t, photons.get(t) + nrPhotons);
		else
			photons.put(t, nrPhotons);
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
		Vector3d dR = new Vector3d(current.pE.get(getSatellite()));
		dR.sub(current.pR);
		double beta = Math.PI - new Vector3d(current.pE.get(getSatellite())).angle(dR);
		double gamma = Math.PI - beta - alpha;
		double c = dR.length();
		double a = c * (Math.sin(alpha) / Math.sin(gamma)); // Semi-major axis

		double minorAxis = c * Math.tan(alpha); // Semi-minor axis

		double area = Math.PI * a * minorAxis; // Area ellipse
		return area;
	}

	public LookupTable getLookupDirection() {
		return lookupDirection;
	}

	public LookupTable getLookupPosition() {
		return lookupPosition;
	}

	public TreeMap<Double, Integer> getPhotons() {
		return photons;
	}

	public Satellite getSatellite() {
		return sat;
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

	/**
	 * Gets the 'x' variable for the radiance approximation (see Rees, p 24-25).
	 * 
	 * @param lambda
	 *            The wavelength that gives x.
	 * @return Returns x.
	 */
	private double getX(double lambda) {
		return Configuration.h * Configuration.c / lambda * Configuration.k * Configuration.TSun
				* Configuration.epsSun;
	}

	/**
	 * The radiance integration approximation (see Rees, p 26).
	 * 
	 * @param x
	 *            The independent wavelength coordinate
	 * @return Returns the integration approximation.
	 */
	private double f(double x) {
		return 15
				/ Math.pow(Math.PI, 4)
				* (Math.pow(x, 3) / 3 - Math.pow(x, 4) / 8 + Math.pow(x, 5) / 60 - Math.pow(x, 7) / 5040);
	}

	private void makePhotons() {
		SimVars last = null;
		double lastA = -1;
		double lastT = -1;
		for (SimVars current : dataSet) {
			double t = current.t0 + current.tR + current.tE.get(sat);
			/* Add measured photons */
			Integer nrPhotons = current.photonsE.get(sat);
			add(t, nrPhotons);
			/* Introduce noise */
			double currentA = findA(current);
			if (last != null) {
				double dT = t - lastT;
				double averageA = (currentA + lastA) / 2;
				Vector3d sunVector = avgVector(last.sunVector, current.sunVector);

				/* Find the average scattering characteristics */
				ScatteringParam param = new ScatteringParam(current.scatter.getParam(), //
						last.scatter.getParam());
				ScatteringCharacteristics scatter = new ScatteringCharacteristics(sunVector, param);

				/* Find the average satellite position */
				Vector3d position = avgVector(last.pE.get(getSatellite()), //
						current.pE.get(getSatellite()));
				/* Find the average reflection position */
				Vector3d reflection = avgVector(last.pR, current.pR);

				/* Find the vector from the average location on the ground to the satellite */
				Vector3d exittanceVector = new Vector3d(position);
				exittanceVector.sub(reflection);

				/* Find the power of the sun in the given frequency */
				double energyIn = 0;
				if (current.illuminated) {
					double lambda1 = constellation.getLaserWaveLength() - 0.5
							* constellation.getReceiverBandWidth();
					double lambda2 = constellation.getLaserWaveLength() + 0.5
							* constellation.getReceiverBandWidth();
					double solAngle = averageA * Math.cos(sunVector.angle(new Vector3d(current.pR)))
							/ (4 * Math.PI * Configuration.R0);
					double exoatmosphericRadiance = Configuration.sigma
							* Math.pow(Configuration.TSun, 4) * (f(getX(lambda1)) - f(getX(lambda2)));
					Atmosphere.getInstance().computeIntensity(exoatmosphericRadiance * solAngle,
							sunVector.angle(new Vector3d(current.pR)));
					energyIn = 0.5 * dT;
				}
				// http://springerlink.com/content/w03843u415122240/?p=b44b970f34e9480ba22f8850692c07c3&pi=1
				// http://springerlink.com/content/w03843u415122240/fulltext.pdf
				double totalReceivedPower = scatter.probability(exittanceVector) * energyIn;

				/* Find the number of photons */
				int nrP = (int) Math.floor(totalReceivedPower / ePhoton);
				if (Math.random() < (totalReceivedPower / ePhoton) - nrP)
					nrP++;

				/* Distribute noise photons evenly over the time interval */
				for (int i = 0; i < nrP; i++) {
					double tRand = Math.random() * dT + lastT;
					photons.put(tRand, 1);
				}
			}
			last = current;
			lastA = currentA;
			lastT = t;
		}
	}
}
