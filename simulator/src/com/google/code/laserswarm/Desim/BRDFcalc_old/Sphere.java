/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc_old;

import java.util.Comparator;
import java.util.TreeMap;

import javax.vecmath.Vector3d;

import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 *   
 *
 */
public class Sphere extends TreeMap<Vector3d, Integer> {

	private static final Logger			logger	= Logger.get(Sphere.class);

	private static Comparator<Vector3d>	vecComparator;
	static {
		vecComparator = new Comparator<Vector3d>() {
			@Override
			public int compare(Vector3d o1, Vector3d o2) {
				Vector3d zerovec = new Vector3d(0.0, 0.0, 1.0);
				if (o1.angle(zerovec) > o2.angle(zerovec)) {
					return 1;
				} else if (o1.angle(zerovec) == o2.angle(zerovec))
					return 0;
				else {
					return -1;
				}
			}
		};
	}

	public static void main(String[] args) {
		Sphere round = new Sphere();
		Vector3d nvec = new Vector3d(1.0, 2.0, 3.0);
		Vector3d nvec2 = new Vector3d(1.0, 3.0, 2.0);
		Vector3d nvec3 = new Vector3d(1.0, 2.5, 2.0);
		// round.put(nvec, 3.0);
		// round.put(nvec2, 2.3);
		System.out.println(round.get(nvec3));

	}

	private int	total;

	public Sphere() {
		super(vecComparator);

	}

	/*
	 * public void rotate(double rad){ //rotate around x-axis
	 * 
	 * for (Map.Entry<Vector2d, ResVec> tmpIt : entrySet()){ tmpIt.getKey().vecI.y += rad;
	 * tmpIt.getKey().vecR.y += rad; }
	 * 
	 * }
	 */
	@Override
	public Integer get(Object key) {
		try {
			return super.get(key);
		} catch (ClassCastException e) {
			logger.dbg("Vector needs to be made compareable: %s", key);
			return null;
		} catch (NullPointerException e) {
			logger.dbg("Element does not exist", e);
			return 0;
		}

	}

	public double getFrac(Object key) {
		return super.get(key) / total;
	}

	/**
	 * Rotate the points around the center in the horizontal plane
	 */
	@Override
	public Integer put(Vector3d key, Integer value) {
		// TODO Auto-generated method stub
		total += value;
		if (!this.containsKey(key)) {
			return super.put(key, value);
		} else {
			return super.put(key, get(key) + value); // add value
		}

	}

}