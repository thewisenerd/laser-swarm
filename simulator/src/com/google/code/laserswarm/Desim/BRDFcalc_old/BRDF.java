/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc_old;

import java.util.Map;

import javax.vecmath.Vector3d;

import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;

import com.google.code.laserswarm.math.Distribution;
import com.google.code.laserswarm.math.VectorMath;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * @author Administrator
 * 
 */
public class BRDF extends Distribution {

	Sphere						curBRDF;							// sphere that represents the brdf
	Map<Vector3d, Sphere>		brdf;
	// Vector3d direction; //direction of the origin
	// Vector3d inDir; //direction of incidence
	final boolean				symetric;							// the brdf is symetric around the
																	// zenith
	/**
	 * If true then the rotation step will be omitted and no slope will be taken into acount
	 */
	final boolean				normalized;						// the convert all incomming vectors
																	// to zenith
	Rotation					curRot;
	private static final Logger	logger	= Logger.get(BRDF.class);

	public static void main(String[] args) {
		Vector3d convec = new Vector3d(0, 0, 1);
		Vector3d emdir = new Vector3d(1, 0.0, 0.0);
		Map<Vector3d, Double> recs = Maps.newHashMap();
		recs.put(convec, 3.0);
		BRDF rdf = new BRDF(false, false);

		// rdf.add(recs,convec,0.5,0.5,emdir);

		System.out.println(rdf);
		// System.out.println(reltoLoc(0,.5, emdir,convec));
		System.out.println(VectorMath.enuToLocal(convec, new Vector3d(0.5, .5, .5)));

	}

	public BRDF(boolean sym, boolean norm) {
		symetric = sym;
		normalized = norm;
		if (normalized) {
			this.curBRDF = new Sphere();
		} else {
			this.brdf = Maps.newHashMap();
		}
	}

	/**
	 * Add to points to BRDF map
	 * 
	 * @param dirs
	 *            in ENU relative to the ground
	 * @param emVec
	 *            in ENU relative to the ground
	 */
	public void add(Map<Vector3d, Integer> dirs, Vector3d emVec, double atSl, double otSl, Vector3d trDir) {
		if (!normalized) {

			Sphere tmpSph = new Sphere();
			makeRot(atSl, otSl, trDir);
			curBRDF = tmpSph;
			for (Map.Entry<Vector3d, Integer> vecDblIt : dirs.entrySet()) {
				tmpSph.put(reltoLoc(vecDblIt.getKey()), vecDblIt.getValue());

			}
			brdf.put(reltoLoc(emVec), tmpSph);
		} else { // don't create the rot matrix
			for (Map.Entry<Vector3d, Integer> vecDblIt : dirs.entrySet()) {
				curBRDF.put(vecDblIt.getKey(), vecDblIt.getValue());

			}
		}

	}

	/**
	 * convert from the inclined plane to normal plane
	 * 
	 * @param atSl
	 * @param otSl
	 * @param trDir
	 *            in ENU
	 * @param vector
	 *            the actual vector in ENU to convert
	 * @return Vector3D wrt sloped plane
	 */
	private void makeRot(double atSl, double otSl, Vector3d trDir) {

		// rotate so direction aligned with ENU
		// shift so that aligned with respect to ENU
		// project to the normal plane

		Vector3D atProj = new Vector3D(trDir.x, trDir.y, 0.0); // projected trDir vector onto the ENU
																// plane
		atProj = atProj.normalize();
		logger.inf("atProj: %s", atProj);
		Rotation rot90 = new Rotation(Vector3D.PLUS_K, Math.PI / 2); // 90 degree rotation

		Vector3D otProj = rot90.applyTo(atProj); // off track projected axis
		otProj = otProj.normalize();
		logger.inf("otProj: %s", otProj);

		// Rotation alignDir = new Rotation(atProj, Vector3D.PLUS_I); //rotate the proj vector to x-axis;

		// Rotation alignX = new Rotation(Vector3D.PLUS_J, Math.atan(atSl)); //rotate around the yaxis by
		// the slope angle
		// Rotation alignY = new Rotation();

		Vector3D sX = new Vector3D(atProj.getX(), atProj.getY(), atSl); // vector that represents the
																		// slanted plane in the ENU
		sX = sX.normalize();
		logger.inf("sX: %s", sX);
		Vector3D sY = new Vector3D(otProj.getX(), otProj.getY(), otSl); // vector that represents the y
																		// -axis of the lsanted plane in
																		// ENU coords
		sY = sY.normalize();
		logger.inf("sY: %s", sY);
		// Vector3D proj = new Vector3D(trDir.x, trDir.y, 0.0);
		// Rotation rot1 = new Rotation(sX, Vector3D.PLUS_I); //rotation that transfers the sX-axis to
		// the ENU x-axis
		// Rotation rot2 = new Rotation(); //rotation that rotates one axis around
		// Vector3D proj2 = rot1.applyTo(proj);

		// up = proj2.getNorm()*otSl;

		Vector3D snorm = Vector3D.crossProduct(sX, sY); // normal of the sloped plane

		logger.inf("sNorm: %s", snorm);

		if (snorm.getZ() < 0)
			logger.dbg("normal of the plane is pointing down");

		Rotation alignNorm = new Rotation(Vector3D.PLUS_K, snorm); // rotation that transforms the
																	// normals from one plane to another
		// Rotation align

		Vector3D xaxisS = alignNorm.applyTo(Vector3D.PLUS_I); // transform the x- axis vector to the
																// coordinates of the slanted plane

		Vector3D projS = alignNorm.applyTo(atProj); // pojection on the slanted plane

		Rotation alignDirection = new Rotation(projS, xaxisS);

		curRot = alignNorm;
		// return alignDirection.applyInverseTo(
		// return alignNorm.applyInverseTo(vec).normalize();

	}

	@Override
	public double probability(Vector3d x) {
		Double tmp;
		try {
			tmp = curBRDF.getFrac(x);
		} catch (NullPointerException e) {
			logger.dbg(e, "");
			tmp = (double) 0;
		}

		return tmp;

	}

	private Vector3d reltoLoc(Vector3d vector) {
		Vector3D vec = new Vector3D(vector.x, vector.y, vector.z); // vector to convert
		vec = vec.normalize();
		logger.inf("vec: %s", vec);
		vec = curRot.applyInverseTo(vec);

		return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
	}

	@Override
	public String toString() {
		return curBRDF.toString();
	}

}
