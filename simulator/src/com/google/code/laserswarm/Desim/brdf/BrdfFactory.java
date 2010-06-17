package com.google.code.laserswarm.Desim.brdf;

import static com.google.code.laserswarm.math.Convert.toSphere;
import static com.google.code.laserswarm.math.VectorMath.ecefToEnu;
import static com.google.code.laserswarm.math.VectorMath.enuToLocal;
import static com.google.code.laserswarm.math.VectorMath.relative;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public abstract class BrdfFactory {

	public static DiscreteBrdf construct(BRDFinput input) {
		DiscreteBrdf brdf = new DiscreteBrdf();

		Vector3d normal = input.getTerrainNormal();
		Map<Vector3d, Integer> receivers = input.getReceiverPositions();
		Vector3d scatterPoint = input.getScatterPoint();
		Point3d scatterSphere = toSphere(scatterPoint);

		for (Vector3d position : receivers.keySet()) {
			int photons = receivers.get(position);
			Vector3d directionECEF = relative(scatterPoint, position);
			Vector3d directionENU = ecefToEnu(directionECEF, scatterSphere.y, scatterSphere.z);
			Vector3d directionLocal = enuToLocal(directionENU, normal);
			Point3d directionSphere = toSphere(directionLocal);
			brdf.addPoint(new Point3d(photons, directionSphere.y, directionSphere.z));
		}

		return brdf;
	}

	public static DiscreteBrdf construct(LinkedList<BRDFinput> input) {
		BRDFinput compiledBrdfInput = input.getFirst().clone();
		Iterator<BRDFinput> it = input.iterator();
		it.next();// Skip the first one as the compiledBrdfInput is already based on it
		while (it.hasNext()) {
			BRDFinput brdFinput = it.next();
			compiledBrdfInput.merge(brdFinput);
		}
		return construct(compiledBrdfInput);
	}

}
