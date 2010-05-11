package com.google.code.laserswarm.Orbit;

import jat.cm.Constants;
import jat.cm.KeplerElements;
import jat.cm.TwoBody;
import jat.cm.cm;
import jat.matvec.data.Matrix;
import jat.matvec.data.VectorN;
import jat.spacetime.EarthRef;
import jat.spacetime.Time;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class OrbitClass {

	public static void main(String[] args) {
		Time now = new Time(2007, 3, 21, 0, 7, 0.);
		double pi = Math.PI;
		KeplerElements kep = new KeplerElements(6725, 1 / 13.0, 30 * pi / 180, 0.0, 0, 0);
		OrbitClass or1 = new OrbitClass(now, kep);
		// OrbitClass or2 = new OrbitClass(now.plus(TimeUtils.days2sec * 365), kep);
		for (int i = 0; i < 360; i++) {
			// or1.sunvec_ECI();
			or1.propogate(84500);
			System.out.print(or1.sunvec_ECI() + "\n");
		}
		/*
		 * for (int i = 0; i < 365; i++) { or1.t_cur.step_seconds(3600); double lat =
		 * Math.asin((or1.ECEF().get(2, 0)) / (or1.ECEF().getColumnVector(0).mag())) Constants.rad2deg;
		 * double lon = Math.atan2(or1.ECEF().get(1, 0), or1.ECEF().get(0, 0)) * Constants.rad2deg;
		 * 
		 * if (lon > 180.0) lon = lon - 360.0; if (lon < -180.0) lon = lon + 360.0;
		 * 
		 * System.out.print("@" + or1.t_cur.get_sim_time() / 3600 + "\n" + or1.ECEF() + " " + "\n " +
		 * or1.ECI() + "\n"); or1.propogate(3);
		 */

		// }
		// GroundTrack.main(kep);

	}

	public Time				epoch0, t_cur;	// in MJD to determine earth position
	private KeplerElements	ke;			// w.r.t ECI

	private EarthRef		defref;

	public double[]			x, y, z, t;	// PQW reference;

	/**
	 * @param epoch
	 * @param elm
	 */
	public OrbitClass(Time epoch, KeplerElements elm) {
		defref = new EarthRef(epoch);
		ke = elm;
		epoch0 = t_cur = epoch;
		// defref.initializeSunEphem(t_cur.mjd_tt());

		// draw_orbit(5000);

	}

	private double calc_E(KeplerElements a) {
		// determine initial E and M
		double sqrome2 = Math.sqrt(1.0 - a.e * a.e);
		double cta = Math.cos(a.ta);
		double sta = Math.sin(a.ta);
		double sine0 = (sqrome2 * sta) / (1.0 + a.e * cta);
		double cose0 = (a.e + cta) / (1.0 + a.e * cta);
		double e0 = Math.atan2(sine0, cose0);
		return e0;

	}

	private double calc_M(double e0) {
		return (e0 - ke.e * Math.sin(e0));

	}

	public void draw_orbit(int steps) {
		x = new double[steps];
		y = new double[steps];
		z = new double[steps];
		t = new double[steps];
		double acubed = ke.a * ke.a * ke.a;
		double n = Math.sqrt(EarthRef.GM_Earth / acubed);
		double period = 2.0 * Constants.pi / n;
		double tloc = 0.0;
		double dt = period / steps;

		double e0 = calc_E(ke);
		double ma = calc_M(e0);
		// double q = Math.sqrt((1.0 + ke.e) / (1.0 - ke.e));
		double sqrome2 = Math.sqrt(1.0 - ke.e * ke.e);
		double[] temp = new double[3];

		for (int i = 0; i < steps; i++) {
			// Matrix.
			temp = this.ECI().getColumnArrayCopy(0);
			x[i] = temp[0];
			y[i] = temp[1];
			z[i] = temp[2];
			t[i] = tloc;

			ma = ma + n * dt;
			double ea = TwoBody.solveKepler(EarthRef.GM_Earth, ke.e);

			double sinE = Math.sin(ea);
			double cosE = Math.cos(ea);
			double den = 1.0 - ke.e * cosE;

			double sinv = (sqrome2 * sinE) / den;
			double cosv = (cosE - ke.e) / den;

			ke.ta = Math.atan2(sinv, cosv);
			if (ke.ta < 0.0) {
				ke.ta = ke.ta + 2.0 * Constants.pi;
			}

			tloc = tloc + dt;
		}
	}

	/**
	 * Return the vector
	 * 
	 * @return Matrix (3x1 vector) representing the location of the satellite in ECEF frame
	 */
	public Matrix ECEF() {
		return defref.eci2ecef(t_cur).times(this.ECI());

	}

	public Point3d ECEF_point() {
		Matrix m = ECEF();
		return new Point3d(m.get(0, 0), m.get(1, 0), m.get(2, 0));
	}

	public Matrix ECI() {
		Matrix x = new Matrix(3, 1);
		x = cm.r_from_el(ke.a, ke.e, ke.i, ke.raan, ke.w, ke.ta); // determine r
		// in ECI
		return x;
	}

	public Point3d ECI_point() {
		Matrix m = ECI();
		return new Point3d(m.get(0, 0), m.get(1, 0), m.get(2, 0));
	}

	public Matrix PQW() {
		Matrix r_pqw = new Matrix(3, 1);
		double p = cm.p(ke.a, ke.e);
		r_pqw.set(0, 0, p * Math.cos(ke.ta) / (1 + ke.e * Math.cos(ke.ta)));
		r_pqw.set(1, 0, p * Math.sin(ke.ta) / (1 + ke.e * Math.cos(ke.ta)));
		r_pqw.set(2, 0, 0.);
		return r_pqw;
	}

	public void propogate(double dt) {

		// Determine step size
		double acubed = ke.a * ke.a * ke.a;
		double n = Math.sqrt(Constants.mu / acubed);
		// double period = 2 * Constants.pi / n;

		double e0 = calc_E(ke);
		double ma = calc_M(e0);
		double sqrome2 = Math.sqrt(1.0 - ke.e * ke.e);

		// double q = Math.sqrt((1.0 + ke.e) / (1.0 - ke.e));

		ma = ma + n * dt;
		double ea = TwoBody.solveKepler(ma, ke.e);

		double sinE = Math.sin(ea);
		double cosE = Math.cos(ea);
		double den = 1.0 - ke.e * cosE;

		double sinv = (sqrome2 * sinE) / den;
		double cosv = (cosE - ke.e) / den;

		ke.ta = Math.atan2(sinv, cosv);
		if (ke.ta < 0.0) {
			ke.ta = ke.ta + 2.0 * Constants.pi;
		}
		t_cur.update(t_cur.get_sim_time() + dt);

	}

	private Matrix sunvec() {
		VectorN vec = EarthRef.sunVector(t_cur.mjd_tt()).unitVector();
		Matrix res = new Matrix(vec);
		return res.diag();
	}

	public Vector3d sunvec_ECEF() {
		// System.out.print("**"+sunvec()+"**\n");
		Matrix hi = defref.eci2ecef(t_cur).times(this.sunvec());
		return (new Vector3d(hi.get(0, 0), hi.get(1, 0), hi.get(2, 0)));
	}

	/*
	 * public void sunvec_ECI(){ double year2day = 365.25; double dayspassed = t_cur.mjd_tt() -
	 * t_cur.vafli(); double radsturned = 2* Math.PI / year2day * dayspassed; double inc = 23.4 *
	 * Math.PI/180; Matrix natnox = new Matrix(new VectorN(-1,0,0)); Matrix rotx = cm.Rot1(inc); Matrix
	 * rotz = cm.Rot3(-1*radsturned); Matrix sunvec = rotz.times(rotx.times(natnox));
	 * 
	 * 
	 * }
	 */
	/*
	 * public void sunvec_ECEF(){ return defref.eci2ecef(t_cur).times(this.ECI()); }
	 */

	public Vector3d sunvec_ECI() {
		Matrix hi = this.sunvec();
		// System.out.print("**"+hi+"**");
		return (new Vector3d(hi.get(0, 0), hi.get(1, 0), hi.get(2, 0)));
	}
}
