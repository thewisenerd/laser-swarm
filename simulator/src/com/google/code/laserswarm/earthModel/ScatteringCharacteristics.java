package com.google.code.laserswarm.earthModel;

import javax.vecmath.Vector3d;

import com.google.code.laserswarm.math.Distribution;


public class ScatteringCharacteristics implements Distribution {
	private double refrAir = 1.0002926;
	private double refrSurf;
	private double kappa;
	private double Theta;
	private double theta0;
	private double R_Lambertian;
	private Vector3d incidenceVector;
	@Override
	public double probability(Vector3d exittanceVector) {
		// TODO Auto-generated method stub
		double theta1 = exittanceVector.angle(new Vector3d(0, 1, 0));
		double dPhi = incidenceVector.angle(exittanceVector);
		double R_Minnaert = R_Lambertian*(Math.pow(Math.cos(theta0)*Math.cos(theta1), kappa - 1));
		double R_HenyeyGreenstein = R_Minnaert*(1-Theta*Theta)/Math.pow((1 + 2*Theta*(Math.cos(theta0)*Math.cos(theta1) + Math.sin(theta0)*Math.sin(theta1)*Math.cos(dPhi)) + Theta*Theta), 1.5);
		return R_HenyeyGreenstein;
	}
	public ScatteringCharacteristics(Vector3d incidenceVector, double indexOfRefraction, double kappaMinnaert, double thetaHenyeyGreenstein) {
		theta0 = incidenceVector.angle(new Vector3d(0, 1, 0));
		refrSurf = indexOfRefraction;
		kappa = kappaMinnaert;
		Theta = thetaHenyeyGreenstein;
		this.incidenceVector = incidenceVector;
		double theta_t = Math.asin(refrAir/refrSurf*Math.sin(theta0));
		double fresnel_s = (Math.sin(theta0) - Math.sin(theta_t))/(Math.sin(theta0) + Math.sin(theta_t));
		double fresnel_p = (Math.tan(theta0) - Math.tan(theta_t))/(Math.tan(theta0) + Math.tan(theta_t));
		R_Lambertian = 2/Math.PI*Math.abs(fresnel_s*fresnel_p)*Math.cos(theta0)*Math.sin(theta0);
	}
	
}
