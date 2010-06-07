/**
 * 
 */
package com.google.code.laserswarm.Desim;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.vecmath.*;

/**
 * @author Administrator
 *
 */
public class Sphere  extends TreeMap<Vector3d, Double>{
	
	double total;
/*	class Comp implements Comparator<Coord>{

		@Override
		public int compare(Coord o1, Coord o2) {
			if(o1.thI == o2.thI){
				if(o1.thR > o2.thR)
					return 1;
			}
				
			
		}
		
	}*/
	/**
	 * Rotate the points around the center  in the horizontal plane 
	 */
	@Override
	public Double put(Vector3d key, Double value) {
		// TODO Auto-generated method stub
		total++;
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
		 Double tmp = super.get(key);
		 return tmp/total;
		
	}
	public Sphere() {
		// TODO Auto-generated constructor stub
	super();
	
	}
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1571965505162878630L;
	

	/**
	 * @param args
	 */
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		
	}

}
