/**
 * 
 */
package com.google.code.laserswarm.Desim;

/**
 * @author Administrator
 *
 */
public class TimePair {

	double t0;
	double tF;
	Double  t0Ref;
	Double  tFRef;
	/**
	 * 
	 * @param t0Ref
	 * initial time to be stored in the class
	 * @param tFRef
	 * final time to be stored in the class
	 */
	public TimePair(Double t0Ref, Double tFRef) {
		if(t0Ref!=null && tFRef != null){
			this.t0 = t0Ref.doubleValue();
			this.tF = tFRef.doubleValue();
			
		}else
		{
			this.t0 = Double.NaN;
			this.tF = Double.NaN;
			
		}
		
		this.tFRef = tFRef;
		this.t0Ref = t0Ref;
		
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 * @return Difference between times
	 */
	public double diff() {
		return (tF-t0);
	}
	@Override
	public String toString() {
		return "[ " + t0 + ", " + tF + " ] \n";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

}
