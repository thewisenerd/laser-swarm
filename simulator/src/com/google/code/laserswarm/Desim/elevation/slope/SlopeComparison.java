package com.google.code.laserswarm.Desim.elevation.slope;

import java.util.Iterator;

import javax.vecmath.Point3d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.Desim.BRDFcalc_old.BRDFinput;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.math.Convert;
import com.lyndir.lhunath.lib.system.logging.Logger;

@SuppressWarnings("serial")
public class SlopeComparison extends DescriptiveStatistics {
	private static final Logger	logger	= Logger.get(SlopeComparison.class);

	private static double toDeg(double rad) {
		return rad * 180 / Math.PI;
	}

	public SlopeComparison(EarthModel earth, ElevationSlope elSlope) {
		super();
		Iterator<Point3d> altIt = elSlope.getAltitudes().iterator();
		Iterator<BRDFinput> slopeIt = elSlope.getBRDFIn().iterator();

		while (altIt.hasNext()) {
			Point3d pSphere = altIt.next();
			BRDFinput slope = slopeIt.next();
			Point3d dir3d = Convert.toSphere(slope.getEmitterDirection());
			double dAngle = 0.001;
			DirectPosition2D forward = new DirectPosition2D(toDeg(pSphere.y + dAngle * dir3d.y), toDeg(pSphere.z
						+ dAngle * dir3d.z));
			DirectPosition2D backward = new DirectPosition2D(toDeg(pSphere.y - dAngle * dir3d.y), toDeg(pSphere.z
						- dAngle * dir3d.z));
			DirectPosition2D left = new DirectPosition2D(toDeg(pSphere.y + dAngle * dir3d.z), toDeg(pSphere.z
						- dAngle * dir3d.y));
			DirectPosition2D right = new DirectPosition2D(toDeg(pSphere.y - dAngle * dir3d.z), toDeg(pSphere.z
						+ dAngle * dir3d.y));
			double hForward = 0;
			double hBackward = 0;
			double hLeft = 0;
			double hRight = 0;
			try {
				hForward = earth.getElevation(forward) + Configuration.R0;
				hBackward = earth.getElevation(backward) + Configuration.R0;
				hLeft = earth.getElevation(left) + Configuration.R0;
				hRight = earth.getElevation(right) + Configuration.R0;
			} catch (PointOutsideEnvelopeException e) {
				logger.err(e, "Error");
			}
			Point3d pForward = Convert.toXYZ(new Point3d(Configuration.R0, forward.x, forward.y));
			Point3d pBackward = Convert.toXYZ(new Point3d(Configuration.R0, backward.x, backward.y));
			Point3d pLeft = Convert.toXYZ(new Point3d(Configuration.R0, left.x, left.y));
			Point3d pRight = Convert.toXYZ(new Point3d(Configuration.R0, right.x, right.y));
			double crossTrackSlope = (hRight - hLeft) / pLeft.distance(pRight);
			double alongTrackSlope = (hForward - hBackward) / pForward.distance(pBackward);
			addValue(Math.abs(crossTrackSlope - slope.getCrossTrackSlope())
					+ Math.abs(alongTrackSlope - slope.getAlongTrackSlope()));
		}
	}
}
