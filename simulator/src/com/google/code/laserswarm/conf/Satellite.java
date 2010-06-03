package com.google.code.laserswarm.conf;

import jat.cm.KeplerElements;

public class Satellite implements Comparable<Satellite> {

	private double	semimajorAxis				= 0f;
	private double	eccentricity				= 0f;
	private double	inclination					= 0f;
	private double	rightAngleOfAscendingNode	= 0f;
	private double	trueAnomaly					= 0f;
	private double	argumentOfPerigee			= 0f;
	private String	name						= "SAT";
	private double	aperatureArea				= 1;				// (0.08 * 0.08); // m�
	private double	beamDivergence				= 2.3333E-4 / 2;	// rad for the half beam /|

	public Satellite() {
		trueAnomaly = semimajorAxis = rightAngleOfAscendingNode = inclination = eccentricity = argumentOfPerigee = 0f;
	}

	public Satellite(String name, double aperatureArea, double a, double e, double i, double raan,
			double ta, double w) {
		this.name = name;
		trueAnomaly = ta;
		semimajorAxis = a;
		rightAngleOfAscendingNode = raan;
		inclination = i;
		eccentricity = e;
		argumentOfPerigee = w;
		this.aperatureArea = aperatureArea;
	}

	public Satellite(String name, Satellite s) {
		this(name, s.aperatureArea, s.semimajorAxis, s.eccentricity, s.inclination,
				s.rightAngleOfAscendingNode, s.trueAnomaly, s.argumentOfPerigee);
	}

	@Override
	public int compareTo(Satellite o) {
		return name.compareTo(o.name);
	}

	public double getAperatureArea() {
		return aperatureArea;
	}

	public double getArgumentOfPerigee() {
		return argumentOfPerigee;
	}

	public double getBeamDivergence() {
		return beamDivergence;
	}

	public double getEccentricity() {
		return eccentricity;
	}

	public double getInclination() {
		return inclination;
	}

	public KeplerElements getKeplerElements() {
		return new KeplerElements(semimajorAxis, eccentricity, inclination, rightAngleOfAscendingNode,
				argumentOfPerigee, trueAnomaly);
	}

	public double getRightAngleOfAscendingNode() {
		return rightAngleOfAscendingNode;
	}

	public double getSemimajorAxis() {
		return semimajorAxis;
	}

	public double getTrueAnomaly() {
		return trueAnomaly;
	}

	public void setAperatureArea(double aperatureArea) {
		this.aperatureArea = aperatureArea;
	}

	public void setArgumentOfPerigee(double argumentOfPerigee) {
		this.argumentOfPerigee = argumentOfPerigee;
	}

	public void setEccentricity(double eccentricity) {
		this.eccentricity = eccentricity;
	}

	public void setInclination(double inclination) {
		this.inclination = inclination;
	}

	public void setRightAngleOfAscendingNode(double config) {
		this.rightAngleOfAscendingNode = config;
	}

	public void setSemimajorAxis(double semimajorAxis) {
		this.semimajorAxis = semimajorAxis;
	}

	public void setTrueAnomaly(double config) {
		this.trueAnomaly = config;
	}

	@Override
	public String toString() {
		return name;
	}
}
