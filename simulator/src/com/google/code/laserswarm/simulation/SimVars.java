package com.google.code.laserswarm.simulation;

import java.util.HashMap;

import javax.vecmath.Point3d;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;

public class SimVars {

	public double						t0;			// added
	public double						tR;			// added
	public HashMap<Satellite, Double>	tE;			// added

	public Point3d						p0;			// added
	public Point3d						pR;			// added
	public HashMap<Satellite, Point3d>	pE;			// added

	public double						power0;		// added
	/**
	 * Power reflected
	 */
	public double						powerR;		// added
	/**
	 * Power reflected after scatter
	 */
	public HashMap<Satellite, Double>	powerR_SC;
	public HashMap<Satellite, Double>	powerE;		// added

	public HashMap<Satellite, Double>	photonDensity;	// added
	public HashMap<Satellite, Integer>	photonsE;		// added

	public ScatteringCharacteristics	scatter;

}
