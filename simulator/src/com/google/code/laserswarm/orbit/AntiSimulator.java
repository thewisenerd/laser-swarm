/**
 * 
 */
package com.google.code.laserswarm.orbit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import jat.cm.Constants;
import jat.constants.IERS_1996;
import jat.demo.vr.SolarSystem.Constellation;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.stat.*;

import com.google.code.laserswarm.ProcessorTester;
import com.google.code.laserswarm.RandData;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.out.plot1D.plotHeightDistribution2;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.MeasermentSample;
import com.google.code.laserswarm.process.SampleIterator;
import com.google.code.laserswarm.process.TimeLine;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.code.laserswarm.out.*;
import com.sun.corba.se.impl.orbutil.closure.Constant;

/**
 * @author Administrator
 * 
 */
public class AntiSimulator {
	static int	callcnt	= 0;
	static int	modi	= 20;

	public void calcposition() {
		

	}
	
	public static double extractpath(Map<Satellite, Vector<Double>> altData) {
		// TODO optimize the search algorithm
		// preallocate capacity of vector
		// use more sets instead of vectors
		// program to return Satellite/time relation

		// pathlength should be equal to num of satellites,
		// number of pathes = maximum number of elements in Vecotr<Double> array
		int pathNum = 0, pathLength = 0;
		Satellite maxSat = new Satellite();
		Vector<Double> initVec;// = new Vector<Double>();
		Vector<Vector<Double>> vertVec = new Vector<Vector<Double>>();// verticle vectors vector
		// containing the path
		if (callcnt % modi == 0)
			System.out.print("extracting path... \n");

		int loc = 0;

		for (Satellite a : altData.keySet()) { // find max Vector length
			// calculate pathLength, pathNum
			pathLength++;
			if (pathNum < altData.get(a).size()) {
				maxSat = a; // remember the satellite;
				pathNum = altData.get(a).size();
			}
		}
		System.out.println("path num:" + pathNum);
		System.out.println("path len:" + pathLength);

		TreeSet<Double> distSet = Sets.newTreeSet();
		initVec = altData.get(maxSat);
		// Double[] arr = (Double[]) initVec.toArray();
		if(pathNum == 1) { };
		//ITERATE MAKING PATH
		for (int i = 0; i < pathNum; i++) {		
			Double tmp = initVec.get(i); // initial vector
			vertVec.add(new Vector<Double>());
			vertVec.get(i).add(tmp);

			for (Satellite eh : altData.keySet()) { // iterate over all the satellites and find closest
													// value to the tmp for each satellite
				if (eh == maxSat)
					continue; // ignore the root element
				distSet.clear();
				distSet.addAll(altData.get(eh)); // set of distances to iterate for a specific satellite
				System.out.println("dist set: " + distSet);
				Double ceilD = distSet.ceiling(tmp);
				Double floorD = distSet.floor(tmp);

				if (ceilD == null) {
					tmp = floorD;
				} else if (floorD == null) {
					tmp = ceilD;
				} else if (tmp == null) {
					tmp = new Double(0);
				} else {
					if ((ceilD.doubleValue() - tmp.doubleValue()) > (tmp.doubleValue() - floorD
							.doubleValue())) // find closest value in the array
					{
						tmp = floorD;
					} else {
						tmp = ceilD;
					}
				}
				System.out.println("path num:" + i);
				vertVec.get(i).add(tmp); // add the value;
			}
		} 
		//END iterate, making path

		System.out.println("verVec = " + vertVec);
		double var = 0; // variance
		double[] resul = null;
		// resul[0] = 0; // result array
		if(pathLength == 1) ;
		Vector<Double> selPath = new Vector<Double>(); // selected path
		
		//ITERATE to find 
		for (int i = 0; i < pathNum; i++) {		//iterate over evry path
			Object[] ar1 = vertVec.get(i).toArray();	//spikes for a specific path; 
			double[] ar2 = new double[ar1.length];

			for (int j = 0; j < ar1.length; j++) {
				ar2[j] = ((Double) ar1[j]).doubleValue();		//convert object array to doulbe array
			}
			double hm = StatUtils.variance(ar2);				//find variance of the path
			if (i == 0) {										
				var = hm;								//set initial values for a first run
				selPath = vertVec.get(0);				//select initial path
			}
			
			// Select one with the minimal variance
			if (var >= hm) { // old bigger than new then
				resul = ar2;
				var = hm;
				selPath = vertVec.get(i);
			}
		}
		//ITERATE 
		
		if (!(resul == null)) {
			return StatUtils.mean(resul);

		} else {
			return 0;
		}

	}

	public static Map<Double, Integer> findspikes(Map<Double, Integer> data) {
		// TODO Auto-generated method stub
		// Integer sum = new Integer(0);
		int size = data.size();
		int sum = 0;
		int average;
		Map<Double, Integer> hi = Maps.newHashMap();
		if (callcnt % modi == 0)
			System.out.print("searching for spikes \n");
		for (Map.Entry<Double, Integer> h : data.entrySet()) {
			sum = sum + h.getValue().intValue();
		}
		average = sum / size;
		for (Map.Entry<Double, Integer> h : data.entrySet()) {
			if (h.getValue().intValue() > average)
				;
			{
				hi.put(h.getKey(), h.getValue());
			}
		}
		return hi;
	}

	public static Map<Double, Integer> findspikes(final SampleIterator data, double time) {
		// TODO Auto-generated method stub
		// Integer sum = new Integer(0);
		int sum = 0;
		double average;
		int size = 0;
		Map<Double, Integer> hi = Maps.newHashMap();

		if (callcnt % modi == 0)
			System.out.println("finding spikes");
		Vector<MeasermentSample> sampls = new Vector<MeasermentSample>();

		while (data.hasNext()) { // Compute size, sum
			MeasermentSample hii = data.next();
			size++;

			sum = sum + hii.getPhotons();
			sampls.add(hii);
			if (hii.getTime() > time)
				break;
		}
		if (size == 0) {
			average = 0;
		} else {
			average = sum / size; // average
		}
		System.out.println("average " + average);
		Iterator<MeasermentSample> iter = sampls.iterator();
		while (iter.hasNext()) {
			MeasermentSample tempms = iter.next();

			if (tempms.getPhotons() > 1.3 * average) { // comparator

				hi.put(new Double(tempms.getTime()), new Integer(tempms.getPhotons()));

			}

		}
		return hi;
	}

	/**
	 * @param emit
	 *            , rec1 in METERS
	 * @param trav1
	 *            in sec.
	 */

	public static double calcalt( Point3d emit, Point3d rec1, double trav1) throws MathException {
		//if(trav1 < 0) throw new MathException("time difference can't be negative");
		// Assumed: Location of the satellite is known to high precission
		// Earth is a perfect sphere
		// Emitter points perp. to the earth center
		// Recievers points to the same point as the emitter

		// create an ellipse

		double f = emit.distance(rec1); // focal distance
		double c = Configuration.c; // speed of light
		double dist = Math.abs(trav1) * c;
		if (dist < f)
			throw new MathException("Distance Traveled is shorter than Focal length");
		double a = dist / 2; // semimajor axis

		double b_2 = Math.pow(dist / 2, 2) - Math.pow(f / 2.0, 2.0); // b^2
		double eps_2 = Math.sqrt(1 - b_2 / (a * a)); // eccentricity^2
		double eps = Math.sqrt(eps_2); // eccentricity^2
		Vector3d em = new Vector3d(emit);
		Vector3d re = new Vector3d(rec1);
		Vector3d dif = new Vector3d();
		dif.sub(em, re);
		double theta = dif.angle(em);	
		if(dif.length()==0) theta = Math.PI/2; //iff the receiver and emitter are the same
			
		// if(theta == Double.NaN) theta = 0;
		double H = a * (1 - eps_2) / (1 - eps * Math.cos(theta)); // distance to the ground from the
		// emitter
/*		if (callcnt % modi == 0) {
			System.out.println("Caclulating Altitude---- ");
			System.out.print("dist: " + dif.length() + "\n");
			System.out.print("a: " + a + "\n");
			System.out.print("b^2: " + b_2 + "\n");
			System.out.print("focal distance " + f + "\n");
			System.out.print("eps: " + eps + "\n");
			System.out.print("Gdist: " + H + "\n");
			System.out.println("Distance of Emitter: " + em.length());
		}*/
		
		return em.length() - Configuration.R0- H; // altitude above the earth sphere in meters

	}

	/*
	 * public Map<Double,Integer> Vector<MeaserementSample> getRegion(SampleIterator it, double t1){
	 * 
	 * }
	 */
	public static List<SimVars> desim(Map<Satellite, TimeLine> rec, EmitterHistory hist) {
		// TODO Auto-generated method stub
		// double talt = alt/c; //time radius for search

		// Satellite emit = hist.getEm(); // satellite emitter
		// double del = 1E-5;
		double[] ret = new double[hist.time.size()]; // return array

		int bin_freq = (int) 50000000; // bin frequency
		double timeoffset = 2 * 45E4 / Configuration.c; // alt over speed of light in sec //1.0/bin_freq;
		double timeframe = 70E-6;//1 / 5000; // window size in in seconds 35 ms 
		
		//Map<Satellite, EmiRecPair> ipair = Maps.newHashMap();
		Map<Satellite, SampleIterator> satsamMap = Maps.newHashMap();

		for (Satellite isat : rec.keySet()) { // map each satellite to sample iterator
			try {
				satsamMap.put(isat, rec.get(isat).getIterator(bin_freq)); // the map
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} // END building a sample iterator map

		EmiRecPair gr = null;
		gr = new EmiRecPair(timeoffset, timeframe, hist, satsamMap);	//emitted pulse, received pulses pair object; 
		int i = 0;		//counter variable for printing debug info
		double tcur = 0;
		// System.out.println("first emitted : " + t0 + "s \n last emitted : " + tf);
		// double timeit = hist.time.iterator().; //CHANGE
		List<SimVars> retval = Lists.newLinkedList();
		Vector3d pos = new Vector3d(0, 0, 0);
		Vector<Double> altPoints = new Vector<Double>();
	//	Iterator<Double> timeIt = hist.time.iterator(); // time iterator over the samples

/*		Map<Satellite, SampleIterator> satSampIterators = Maps.newHashMap();
		try {
			for (Map.Entry<Satellite, TimeLine> iter : rec.entrySet())
				// Put satellite iterators into the map
				satSampIterators.put(iter.getKey(), iter.getValue().getIterator(bin_freq));

		} catch (MathException e) {
			System.out.println(e + "Math Exception has occured");
		}*/


		if (i % modi == 0)
			System.out.print("desimulating \n");
		// timeIt.next();
		// for (int i = 0; i < hist.time.size(); i++) { // iterate over all the time instances.
		while (gr.timeIt.hasNext()) { // iterate over all the Sent pulses
			// offset one

			//tcur = timeIt.next().doubleValue(); // current itme of the emitter
			if (i % 100 == 0)		System.out.print("Current Time: " + tcur + "\n");
			
			System.out.print("desimulating pulse " + i + " out of " + hist.time.size() + "\n");

			SimVars res = new SimVars();
			// em = emData.get(Emit).getLookupPosition().find(tr) ; //position of the emitter at a time t
			Map<Satellite, Vector<Double>> altData = Maps.newHashMap();
			Map<Satellite, Map<Double,Integer>> itset = gr.next();		//Satellite - Spikes relation.
			
			altData.clear();
			for (Map.Entry<Satellite, TimeLine> iter : rec.entrySet()) { // Iterate over all the receivers

				if (i % modi == 0)
					System.out.print("desimulating satellite \n");

				Map<Double, Integer> tphoton = Maps.newHashMap(); // photon peaks

				/*
				 * try{
				 * 
				 * while(iterat.getValue().getIterator(frequency).hasNext()){ MeasermentSample tmp =
				 * iterat.getValue().getIterator(frequency).next(); System.out.print(tmp.getPhotons() +
				 * "photons to Map<Double, Integer>\n"); tmpMap.put(new Double(tmp.getTime()), new
				 * Integer(tmp.getPhotons())); } } catch(Exception e){
				 * System.out.print("math exception has occured :("); }
				 */

				/*
				 * for(int j = 0; j< 34; j++){ double tim = emittorHistory.getPulseClosesTo(tinc*j);
				 * Double key = emData.get(Emit).getPhotons().ceilingKey(new Double(tim-tinc)); Double
				 * key2 = emData.get(Emit).getPhotons().floorKey(new Double(tim+tinc)); }
				 */
				// REG data
				// Double[] timeregs = (Double[])
				// satData.get(iterat.getKey()).getPhotons().keySet().toArray(); //time when the pulses
				// were registered

				// tphoton = AntiSimulator.findspikes(satSampIterators.get(iter.getKey()),
				// tcur+timeoffset - timeframe/2); // list of spike photons <-- made obsolete
				// tphoton = gr;
				// that are not noise
				// for this satellite

				for (Double d :itset.get(iter.getKey()).keySet() ) { // iterate over the spikes
					altPoints = new Vector<Double>();  
					if (i % modi == 0)
						System.out.print(tphoton.get(d) + " photons at time " + d.toString() + " s \n");
					double t1 = gr.tPulse;	//current emitted pulse time
					
					// double t1 = hist.getPulseBeforePulse(d.doubleValue());
					// em.get(hist).getLookupPosition(); // the time of the
					// sent pulse
					Point3d emitp = new Point3d(hist.getPosition().find(t1));
					Point3d recp = new Point3d(iter.getValue().getLookupPosition().find(d.doubleValue()));
					
					try {
						altPoints.add(new Double(AntiSimulator
								.calcalt(emitp, recp, d.doubleValue() - t1)));			//WARNING SAME emitter, receiver position
					} catch (MathException e) {
						System.out.println("Error occured at " + tcur
								+ "s pulse. \n --> On the spike at time " + d);
						break;
					}

					if (i % modi == 0) {
						System.out.println("time of sent pulse (gr): " + t1);
						System.out.println("time of received pulse (d): " + d);

						System.out.println("dT for distance: " + (d.doubleValue() - t1));
						System.out.println("distance between points: " + emitp.distance(recp));
						System.out.println("Points: " + emitp + recp);
						emitp.sub(recp);
						System.out.println("dR vector: " + emitp);
					}
					pos = new Vector3d(emitp); // WARNING!!

				}	//END iterate over the spikes
				
				if (i % modi == 0) {
					System.out.println("Position of em: " + pos);
					try {
						System.out.println("Altitue point: " + altPoints.get(0) + " \n 1 of "
								+ altPoints.size());
					} catch (Exception e) {
						System.out.println("Altitue point fubar");
					}
					;
				};

				altData.put(iter.getKey(), altPoints); // Store the satellite, altitude points relation
				System.out.println("altitudes map (altData): " + altData);
				// Iterator<Double> hi = emittorHistory.time.iterator();

				// re = satData.get(iterat.getKey()).getLookupPosition().find(trd); //position of the
				// satellites at a time t

				// satData.get(iterat.getKey()).getPhotons().keySet();
				// filter();

				// calcalt(em,re,time);
			} //END receiver iteration
			ret[i] = extractpath(altData);
			System.out.println("extracted ALT: " + ret[i]);
			pos.normalize();
			pos.scale(ret[i] + Configuration.R0);
			res.pR = new Point3d(pos);
			if (i % modi == 0)
				System.out.println("res = " + res.pR);
			retval.add(res);
			i++;
			callcnt++;
			if (i > 4500)
				break;
		}
		//for(int k =0; k<ret.length; k++)	System.out.println(ret[k]);
		return retval;

	}

	public static void myplot(Vector<Integer> sims, double lineThickness, String plotFile) {
		int width = 160;
		int height = 150;
		int hOffset = 50;
		int wOffset = 30; // 60;
		BufferedImage bimg = new BufferedImage(width + wOffset, height + hOffset,
				BufferedImage.TYPE_INT_RGB); // New BufferedImage used for writing
		Graphics2D g = bimg.createGraphics(); // create Graphics context and map Graphics context to new
		// instance of Graphics2D
		g.setBackground(Color.white); // set the background to white
		g.clearRect(0, 0, width + wOffset, height + hOffset);
		g.setPaintMode(); // set mode to overwrite pixels
		g.setColor(new Color(0, 0, 0)); // set color to black
		for (int l = 0; l < lineThickness; l++) {
			g.drawLine(wOffset, height - l, width + wOffset, height - l);
			g.drawLine(wOffset + l, height, wOffset + l, 0);
		}
		ArrayList<Double> h = Lists.newArrayList();
		ArrayList<Double> theta = Lists.newArrayList();
		ArrayList<Double> phi = Lists.newArrayList();
		ArrayList<Double> dist = Lists.newArrayList();
		dist.add(0.0);
		double hMax = Double.MIN_VALUE;
		double hMin = Double.MAX_VALUE;
		for (Integer aSim : sims) {

			h.add(new Double(aSim.doubleValue()));

		}
		double hDiff = hMax - hMin;
		int size = h.size();
		double scaleFactor = 1;
		double plotOffset = 3;
		int fontSize = 20;
		Font font = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int n = 0; n < size - 1; n++) {
			for (int l = 0; l < lineThickness; l++) {
				int x1 = wOffset + (int) (((double) n) / ((double) size) * width);
				int y1 = height - (int) ((h.get(n) - hMin) / hDiff * height * scaleFactor) - l;
				int x2 = wOffset + (int) (((double) n + 1) / (size) * width);
				int y2 = height - (int) ((h.get(n + 1) - hMin) / hDiff * height * scaleFactor) - l;
				g.drawLine(x1, y1, x2, y2);
			}
		}
		final float dash1[] = { 10.0f };
		final BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				10.0f, dash1, 0.0f);
		g.setStroke(dashed);
		for (int i = 1; i < 5; i++) {
			g.drawLine(wOffset, height - (int) (i * 0.25 * height * scaleFactor), wOffset + width,
					height - (int) (i * 0.25 * height * scaleFactor));
		}
		for (int i = 1; i < 9; i++) {
			g.drawLine(wOffset + (int) (i * 0.125 * width), 0, wOffset + (int) (i * 0.125 * width),
					height);
		}
		g.drawString("h [m]", 5, (int) (0.13 * height * scaleFactor + fontSize));
		for (int i = 0; i < 5; i++) {
			g.drawString(Integer.toString((int) ((1 - i * 0.25) * hMax)), 5, (int) (i * 0.25 * height
					* scaleFactor + plotOffset + fontSize));
		}
		/*
		 * for (int i = 0; i < 8; i++) { g.drawString("(" + Math.floor(1000 * 180 / Math.PI *
		 * theta.get((int) (i * 0.125 * size))) / 1000 + "," + Math.floor(1000 * 180 / Math.PI *
		 * phi.get((int) (i * 0.125 * size))) / 1000 + ")", wOffset + (int) (i * 0.125 * width), height +
		 * hOffset - 5); }
		 */

		try {
			ImageIO.write(bimg, "png", new File(plotFile + ".png"));
		} catch (Exception e) {
			System.out.println(e + "Plot file writing failed.");
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 

		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT));

		double[] ter;
		Map<Satellite, TimeLine> em;
		Map<Satellite, TimeLine> rec;
		EmitterHistory hist;
		String flname = "sim_3sat";
		ProcessorTester tester = new ProcessorTester();
		RandData ret;
		try {
			ret = RandData.read(flname);
		} catch (FileNotFoundException e) {
			ret = tester.testProcessing();
			ret.write(flname);
		}

		Satellite emit;

		int i = 0;
		/*
		 * Satellite sat1 = new Satellite("eh",4.5,4.5f,4.0f,0.5f,4.5f,4.5f,3.2f); Satellite sat2 = new
		 * Satellite("eh2",4.5,4.5f,4.0f,0.5f,4.5f,4.5f,3.2f); Satellite sat3 = new
		 * Satellite("eh3",4.5,4.5f,4.0f,0.5f,4.5f,4.5f,3.2f); Vector<Double> vec1 = new
		 * Vector<Double>(); Vector<Double> vec2 = new Vector<Double>(); Vector<Double> vec3 = new
		 * Vector<Double>(); Vector<Integer> hi = new Vector<Integer>(); hi.add(new Integer(3));
		 * hi.add(new Integer(8)); hi.add(new Integer(7)); hi.add(new Integer(5));
		 * 
		 * //vec1.add(new Double(12)); vec1.add(new Double(5)); vec1.add(new Double(10)); vec1.add(new
		 * Double(7)); vec1.add(new Double(3));
		 * 
		 * vec2.add(new Double(30)); vec2.add(new Double(40)); vec2.add(new Double(70)); vec2.add(new
		 * Double(4)); vec2.add(new Double(90));
		 * 
		 * vec3.add(new Double(1)); vec3.add(new Double(6)); vec3.add(new Double(9)); vec3.add(new
		 * Double(12)); vec3.add(new Double(20));
		 * 
		 * Map<Satellite, Vector<Double>> alda = Maps.newHashMap();
		 * System.out.println(extractpath(alda)); alda.put(sat1, vec1) ;
		 * //System.out.println(extractpath(alda));
		 * 
		 * alda.put(sat2,vec2); alda.put(sat3,vec3);
		 * 
		 * System.out.println(extractpath(alda));
		 */
		// List<SimVars> hi = Lists.newLinkedList();
		// for(i = 0; i<ter.length-1; i++){
		// SimVars tmpSim = new SimVars();
		// em.get(emit).getLookupPosition().
		// tmpSim.pR = ter[i];
		// hi.add(tmpSim);
		// //tmpSim.
		// }
		  plotHeightDistribution2 plotter = new plotHeightDistribution2(); 
		  //List<SimVars> abcd =
			  /*
		 * Lists.newLinkedList(); abcd = desim(ret.getRec(),ret.getEm(),ret.getEmHist());
		 * 
		 * for (int j = 0; j < abcd.size()-1; j++) { if(abcd.get(j).pR.x < 0 ) abcd.remove(j);
		 * //System.out.println(abcd.get(j).pR); } for (int j = 0; j < abcd.size()-1; j++) {
		 * //if(abcd.get(j).pR.x < 0 ) abcd.remove(j); if(Math.abs(abcd.get(j).pR.distance(new
		 * Point3d(0,0,0)))-6400000+21863 == Double.NaN) abcd.remove(j);
		 * System.out.println(1E10*(Math.abs(abcd.get(j).pR.distance(new
		 * Point3d(0,0,0)))-6400000+21863+0.51) ); }
		 * 
		 * plotter.plot(abcd,3,"vafli");
		 */
		/*
		 * for (Map.Entry<Satellite, TimeLine> tmp: ret.getRec().entrySet()) { Satellite sat
		 * =tmp.getKey(); TimeLine tim = tmp.getValue(); SampleIterator sampiter= null; try { sampiter =
		 * tim.getIterator(5000000); //60 meters resolution //window 200 microsecs long mm thick } catch
		 * (MathException e) { // TODO Auto-generated catch block e.printStackTrace(); } for(int k =0; k<
		 * 3000; k++){ System.out.println(findspikes(sampiter,191675+1.0/3000.0*(i+1)) + "\t | \t" + k);
		 * 
		 * } while(sampiter.hasNext()){ MeasermentSample hm= sampiter.next();
		 * System.out.println(hm.getTime() + "\t | \t" + hm.getPhotons() ); } }
		 */
		// ret.getRec().get(emit).getIterator(2).
		TreeMap<Double, Integer> laser = Maps.newTreeMap();
		laser.put(2.4d, 15);
		laser.put(3.4d, 17);
		PolynomialFunction polyfunc[] = new PolynomialFunction[3];
		polyfunc[0] = new PolynomialFunction(new double[] { 2d, 3d, 4d });
		polyfunc[1] = new PolynomialFunction(new double[] { 2d, 3d, 4d });
		polyfunc[2] = new PolynomialFunction(new double[] { 2d, 3d, 4d });

		PolynomialSplineFunction noisepoly = new PolynomialSplineFunction(
				new double[] { 1d, 2d, 3d, 4d }, polyfunc);
/*		EmiRecPair gr = null;
		try {
			gr = new EmiRecPair(2d * 500d / 300000d, 1.0 / 5000.0, ret.getEmHist(), ret.getRec()
					.values().iterator().next().getIterator(5000000));
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int j = 0; j < 200; j++) {

			System.out.println(gr.next());
		}*/

		/*
		 * for (int j = 0; j < 200; j++) { try { System.out.println(noisepoly.value(j/45.0) +
		 * " noise for " + j/45.0); } catch (ArgumentOutsideDomainException e) { // TODO Auto-generated
		 * catch block //e.printStackTrace(); System.out.println("Outside domain for " + j/45.0); } }
		 */
		/*
		 * SampleIterator samTest = new SampleIterator(20, laser, noisepoly); for (int j = 0; j < 4; j++)
		 * {
		 * 
		 * System.out.println(findspikes(samTest,2.0*j)); }
		 */

		/*
		 * for (int j = 0; j < 40; j++) { MeasermentSample tmp; tmp = samTest.next();
		 * System.out.println(tmp.getPhotons() + " \t | \t " + tmp.getTime() + " " +j);
		 * 
		 * }
		 */

		// for ( Map<Double,Integer> ter:
		// ret.getEm().get(ret.getEm().keySet().iterator().next()).getIterator(3).next().) {
		// Vector<Integer> hi = new Vector<Integer>();*/

		/*
		 * try {
		 * 
		 * System.out.print(findspikes(ret.getEm().values().iterator().next().getIterator((int) 10),
		 * 4542935.369999992));
		 * System.out.print(findspikes(ret.getEm().values().iterator().next().getIterator((int) 10),
		 * 4542935.969999992)); SampleIterator iter =
		 * ret.getRec().values().iterator().next().getIterator((int) 10); iter.c = 0; while
		 * (iter.hasNext()) {
		 * 
		 * hi.add(new Integer(iter.next().getPhotons()));
		 * 
		 * } myplot(hi, 3, "bbf"); } catch (Exception e) { System.out.println(":((((("); }
		 */
		//plotter.plot(ret.getRec(),ret.getEmHist(),1.0,"original");
		plotter.plot(desim(ret.getRec(),ret.getEmHist()),1.0,"recover2");
	/*	for (TimeLine eh : ret.getRec().values()) {
			double lastT;
			System.out.println("Satellite" + eh.getSatellite());
			SampleIterator mh = null;
			try {
				mh = eh.getIterator(500000);
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MeasermentSample ms = mh.next();
			lastT =ms.getTime();
			while(mh.hasNext()){
				
			//	ret.getEmHist().time;
				if(ms.getPhotons() > 0 ) System.out.println("Photons:" + ms.getPhotons() + " @" + ms.getTime() + "s" );
			ms = mh.next();
			}*/
				
			
	//	}
				// System.out.println(desim(ret.getRec(),ret.getEm(),ret.getEmHist()));//,3.0,"recover");

		System.out.print(Integer.MAX_VALUE);

		/*
		 * Point3d em = new Point3d(); Point3d rem = new Point3d(); double xdist = 7000000; double r =
		 * IERS_1996.R_Earth; double km = 1 / IERS_1996.c * 1000; double ns = 1E-9; double t = 1200 * km;
		 * em.set(xdist, 0, 0); rem.set(xdist - 410, 410, 0); double alt = calcalt(em, rem, t);
		 * System.out.print((xdist - r) / 1000 + "\n" + alt / 1000);
		 */

	}

}
