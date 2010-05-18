package com.google.code.laserswarm.conf;

import jat.cm.KeplerElements;

public class Satellite {
	private float	semimajorAxis				= 0f;
	private float	eccentricity				= 0f;
	private float	inclination					= 0f;
	private float	rightAngleOfAscendingNode	= 0f;
	private float	trueAnomaly					= 0f;
	private float	argumentOfPerigee			= 0f;
	private String	name						= "SAT";
	private double	aperatureArea				= 1;				// (0.08 * 0.08); // m²
	private double	beamDivergence				= 2.3333E-4 / 2;	// rad for the half beam /|

	public Satellite() {
		trueAnomaly = semimajorAxis = rightAngleOfAscendingNode = inclination = eccentricity = argumentOfPerigee = 0f;
	}

	public Satellite(String name, double aperatureArea, float a, float e, float i, float raan, float ta,
			float w) {
		this.name = name;
		trueAnomaly = ta;
		semimajorAxis = a;
		rightAngleOfAscendingNode = raan;
		inclination = i;
		eccentricity = e;
		argumentOfPerigee = w;
		this.aperatureArea = aperatureArea;
	}

	public double getAperatureArea() {
		return aperatureArea;
	}

	public float getArgumentOfPerigee() {
		return argumentOfPerigee;
	}

	public double getBeamDivergence() {
		return beamDivergence;
	}

	public float getEccentricity() {
		return eccentricity;
	}

	public float getInclination() {
		return inclination;
	}

	public KeplerElements getKeplerElements() {
		return new KeplerElements(semimajorAxis, eccentricity, inclination, rightAngleOfAscendingNode,
				argumentOfPerigee, trueAnomaly);
	}

	public float getRightAngleOfAscendingNode() {
		return rightAngleOfAscendingNode;
	}

	public float getSemimajorAxis() {
		return semimajorAxis;
	}

	public float getTrueAnomaly() {
		return trueAnomaly;
	}

	public void setAperatureArea(double aperatureArea) {
		this.aperatureArea = aperatureArea;
	}

	@Override
	public String toString() {
		return name;
	}
}
