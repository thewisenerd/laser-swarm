package com.google.code.laserswarm.Desim.BRDFcalc_old;

import java.util.List;

import javax.vecmath.Vector2d;

/**
 * Coord class stores the pair of vectors vecI - input vector vecR - reflecting vector
 * 
 * @author Administrator
 * 
 */
public class Coord {
	/**
	 * x coord = [0..pi/2] elevation y coord = [-pi..pi] rotation
	 */
	public Vector2d			vecI;	// angle incident wrt normal
	/**
	 * Reflection vector
	 * 
	 */
	public List<Vector2d>	vecR;	// angle reflected wrt normal

	public Coord() { // Vector
		vecI = new Vector2d(); // x azimuth in degrees
		vecR = new List<Vector2d>(); // y offset in degrees

	}

	/**
	 * Assuming that the VecI is from zenith [0,0]
	 * 
	 * @param thR
	 *            vector of reflection ;
	 */
	public Coord(Vector2d thR) { // zenith coordinate
		this.vecR = new Vector2d(thR);
		this.vecI = new Vector2d(new double[] { 0, 0 });
	}

	public Coord(Vector2d thR, Vector2d thI) { // zenith coordinate
		this.vecR = new Vector2d(thR);
		this.vecI = new Vector2d(thI);
	}

	/**
	 * 
	 * @param val
	 */
	private void incI(double val) {

	}

	private void incR() {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}
}
