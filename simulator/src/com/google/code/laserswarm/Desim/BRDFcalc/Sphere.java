/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.vecmath.*;

import com.google.code.laserswarm.TestFindElevation;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * @author 
 *
 */
public class Sphere  extends TreeMap<Vector3d, Double>{
	
	double total;
	private static final Logger	logger		= Logger.get(Sphere.class);
	private static final long	serialVersionUID	= -1571965505162878630L;
	
	static Comparator<Vector3d> vecComparator = new Comparator<Vector3d>() {

		@Override
		public int compare(Vector3d o1, Vector3d o2) {
			Vector3d zerovec = new Vector3d(0.0,0.0,1.0);
			if ( o1.angle(zerovec) > o2.angle(zerovec) ){
				return 1;
			}else if(o1.angle(zerovec) ==  o2.angle(zerovec) )
				return 0;
			else{
				return -1;
			}

		}


	};

	/**
	 * Rotate the points around the center  in the horizontal plane 
	 */
	@Override
	public Double put(Vector3d key, Double value) {
		// TODO Auto-generated method stub
		total += value;
		if(!this.containsKey(key)){
			return super.put(key, value);
		}
		else{
			return super.put(key, get(key)+value); //add value
		}

		
	}
/*	public void rotate(double rad){		//rotate around x-axis
		
		for (Map.Entry<Vector2d, ResVec> tmpIt : entrySet()){
			tmpIt.getKey().vecI.y += rad;
			tmpIt.getKey().vecR.y += rad;
		}
		
		}*/
	@Override
	public Double get(Object key) {
		try{
		Double tmp = super.get(key);
		 return tmp/total;
		}catch(ClassCastException e ){
			logger.dbg("Vector needs to be made compareable: %s", key);
			return null;
		}catch(NullPointerException e){
			logger.dbg("Element does not exist", e);
			return 0.0;
		}
		
		
		
	}
	
	public Sphere() {
	super(vecComparator);
	
	}
	
	
	
	
	public static void main(String[] args) {
		Sphere round = new Sphere();
		Vector3d nvec = new Vector3d(1.0,2.0,3.0);
		Vector3d nvec2 = new Vector3d(1.0,3.0,2.0);
		Vector3d nvec3 = new Vector3d(1.0,2.5,2.0);
		round.put(nvec, 3.0);
		round.put(nvec2, 2.3);
		System.out.println(round.get(nvec3));
		
	}

}
