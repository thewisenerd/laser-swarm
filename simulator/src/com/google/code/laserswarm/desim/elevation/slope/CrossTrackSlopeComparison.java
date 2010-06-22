package com.google.code.laserswarm.desim.elevation.slope;

import java.util.Iterator;

import javax.vecmath.Point3d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.desim.brdf.BRDFinput;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.math.Convert;
import com.lyndir.lhunath.lib.system.logging.Logger;

@SuppressWarnings("serial")
public class CrossTrackSlopeComparison extends DescriptiveStatistics {
	private static final Logger	logger	= Logger.get(CrossTrackSlopeComparison.class);

	private static double toDeg(double rad) {
		return rad * 180 / Math.PI;
	}

	public CrossTrackSlopeComparison(EarthModel earth, ElevationSlope elSlope) {
		super();
		Iterator<BRDFinput> slopeIt = elSlope.getBRDFIn().iterator();

		while (slopeIt.hasNext()) {
			BRDFinput slope = slopeIt.next();
			Point3d pSphere = Convert.toSphere(new Point3d(slope.getScatterPoint()));
			Point3d dir3d = Convert.toSphere(slope.getEmitterDirection());
			double dAngle = 0.00001;
			DirectPosition2D left = new DirectPosition2D(toDeg(pSphere.y) + dAngle * toDeg(dir3d.z),
					toDeg(pSphere.z) - dAngle * toDeg(dir3d.y));
			DirectPosition2D right = new DirectPosition2D(toDeg(pSphere.y) - dAngle * toDeg(dir3d.z),
					toDeg(pSphere.z) + dAngle * toDeg(dir3d.y));
			double hLeft = 0;
			double hRight = 0;
			try {
				hLeft = earth.getElevation(left) + Configuration.R0;
				hRight = earth.getElevation(right) + Configuration.R0;
			} catch (PointOutsideEnvelopeException e) {
				logger.err(e, "Error");
			}
			Point3d pLeft = Convert.toXYZ(new Point3d(Configuration.R0, left.x, left.y));
			Point3d pRight = Convert.toXYZ(new Point3d(Configuration.R0, right.x, right.y));
			double crossTrackSlope = (hRight - hLeft) / pLeft.distance(pRight);
			addValue(Math.abs(crossTrackSlope - slope.getCrossTrackSlope()));
		}
	}
}
