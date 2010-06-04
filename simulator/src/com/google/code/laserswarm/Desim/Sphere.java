/**
 * 
 */
package com.google.code.laserswarm.Desim;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 *
 */
public class Sphere  extends TreeMap<Coord, Double>{
	
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
	public void rotate(double rad){		//rotate around x-axis
		for (Map.Entry<Coord, Double> tmpIt : entrySet()){
			tmpIt.getKey().vecI.y += rad;
			tmpIt.getKey().vecR.y += rad;
		}
		
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
