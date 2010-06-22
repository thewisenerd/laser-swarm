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
public class AlongTrackSlopeComparison extends DescriptiveStatistics {
	private static final Logger	logger	= Logger.get(AlongTrackSlopeComparison.class);

	private static double toDeg(double rad) {
		return rad * 180 / Math.PI;
	}

	public AlongTrackSlopeComparison(EarthModel earth, ElevationSlope elSlope) {
		super();
		Iterator<BRDFinput> slopeIt = elSlope.getBRDFIn().iterator();

		while (slopeIt.hasNext()) {
			BRDFinput slope = slopeIt.next();
			Point3d pSphere = Convert.toSphere(new Point3d(slope.getScatterPoint()));
			Point3d dir3d = Convert.toSphere(slope.getEmitterDirection());
			double dAngle = 0.00001;
			DirectPosition2D forward = new DirectPosition2D(toDeg(pSphere.y) + dAngle * toDeg(dir3d.y),
					toDeg(pSphere.z) + dAngle * toDeg(dir3d.z));
			DirectPosition2D backward = new DirectPosition2D(toDeg(pSphere.y) - dAngle * toDeg(dir3d.y),
					toDeg(pSphere.z) - dAngle * toDeg(dir3d.z));
			double hForward = 0;
			double hBackward = 0;
			try {
				hForward = earth.getElevation(forward) + Configuration.R0;
				hBackward = earth.getElevation(backward) + Configuration.R0;
			} catch (PointOutsideEnvelopeException e) {
				logger.err(e, "Error");
			}
			Point3d pForward = Convert.toXYZ(new Point3d(Configuration.R0, forward.x, forward.y));
			Point3d pBackward = Convert.toXYZ(new Point3d(Configuration.R0, backward.x, backward.y));
			double alongTrackSlope = (hForward - hBackward) / pForward.distance(pBackward);
			addValue(Math.abs(alongTrackSlope - slope.getAlongTrackSlope()));
		}
	}
}
