/**
 * 
 */
package com.google.code.laserswarm.Orbit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import jat.cm.Constants;
import jat.constants.IERS_1996;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.*;

import com.google.code.laserswarm.ProcessorTester;
import com.google.code.laserswarm.RandData;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Convert;
import com.google.code.laserswarm.out.plot1D.plotHeightDistribution;
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
	static int callcnt=0;
	static int modi=20;
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
		if(callcnt%modi ==  0) System.out.print("extracting path... \n");
		int loc = 0;
		for (Satellite a : altData.keySet()) { // find max Vector length
			pathLength++;
			if (pathNum < altData.get(a).size()) {
				maxSat = a; // remember the satellite;
				pathNum = altData.get(a).size();
			}
		}

		TreeSet<Double> distSet = Sets.newTreeSet();
		initVec = altData.get(maxSat);
		// Double[] arr = (Double[]) initVec.toArray();
 
		for (int i = 0; i < pathNum-1; i++) {
			Double tmp = initVec.get(i);
			vertVec.add(new Vector<Double>());
			vertVec.get(i).add(tmp);
			for (Satellite eh : altData.keySet()) { // iterate over all the satellites
				if (eh == maxSat)
					continue; // ignore the root element

				distSet.addAll(altData.get(eh)); // set of distances to iterate for a specific satellite
				double ceil = distSet.ceiling(tmp);
				double floor = distSet.floor(tmp);
				if ((ceil - tmp) > (tmp - floor)) // find closest value in the array
				{
					tmp = floor;
				} else {
					tmp = ceil;
				}

				vertVec.get(i).add(tmp); // add the value;
			}
		}

		double var = 0; // variance
		double[] resul = null;
		//resul[0] = 0;		// result array
		Vector<Double> selPath = new Vector<Double>(); // selected path
		for (int i = 0; i < pathNum-1; i++) {
			Object[] ar1 = vertVec.get(i).toArray();
			double[] ar2 = new double[ar1.length];

			for (int j = 0; j < ar1.length-1; j++) {
				ar2[j] = ((Double) ar1[j]).doubleValue();
			}
			double hm = StatUtils.variance(ar2);
			if (i == 0) {
				var = hm;
				selPath = vertVec.get(i);
			}
			// Select one with the minimal variance
			if (var > hm) { // old bigger than new then
				resul = ar2;
				var = hm;
				selPath = vertVec.get(i);
			}

		}
		if(!(resul== null)){
			return StatUtils.mean(resul);
			
		}else{
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
		if(callcnt%modi == 0) System.out.print("searching for spikes \n");
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

	public static Map<Double, Integer> findspikes(SampleIterator data, double time) {
		// TODO Auto-generated method stub
		// Integer sum = new Integer(0);
		int sum = 0;
		double average;
		int size = 0;
		Map<Double, Integer> hi = Maps.newHashMap();

		if(callcnt%modi == 0) System.out.println("finding spikes");
		Vector<MeasermentSample> sampls = new Vector<MeasermentSample>();

		while (data.hasNext()) { // Compute size, sum
			MeasermentSample hii = data.next();
			size++;
			if (hii.getTime() > time)
				break;
			sum = sum + hii.getPhotons();
			sampls.add(hii);
		}
		if(size==0){
		average = 0;	
		}else{
		average = sum / size; // average
		}
		Iterator<MeasermentSample> iter = sampls.iterator();
		while (iter.hasNext()) {
			MeasermentSample tempms = iter.next();

			if (tempms.getPhotons() > average) { // comparator
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

	public static double calcalt(Point3d emit, Point3d rec1, double trav1) {
		
		// Assumed: Location of the satellite is known to high precission
		// Earth is a perfect sphere
		// Emitter points perp. to the earth center
		// Recievers points to the same point as the emitter

		// create an ellipse
	
		double f = emit.distance(rec1); // focal distance
		double c = IERS_1996.c; // speed of light
		double dist = Math.abs(trav1) * c;
		double a = dist / 2; // semimajor axis

		double b_2 = Math.pow(dist / 2, 2) - Math.pow(f / 2.0, 2.0); // b^2
		double eps_2 = Math.sqrt(1 - b_2 / (a * a)); // eccentricity^2
		double eps = Math.sqrt(eps_2); // eccentricity^2
		Vector3d em = new Vector3d(emit);
		Vector3d re = new Vector3d(rec1);
		Vector3d dif = new Vector3d();
		dif.sub(em, re);
		double theta = dif.angle(em);
		double H = a * (1 - eps_2) / (1 - eps * Math.cos(theta)); // distance to the ground from the
		// emitter
		if(callcnt%modi == 0){
			System.out.println("Caclulating Altitude---- " );
			System.out.print("dist: " + dif.length() + "\n");
			System.out.print("a: " + a + "\n");
			System.out.print("b^2: " + b_2 + "\n");
			System.out.print("focal distance " + f + "\n");
			System.out.print("eps: " + eps + "\n");
			System.out.print("Gdist: " + H + "\n");
		}
		return em.length() - IERS_1996.R_Earth - H; // altitude above the earth sphere in meters

	}

	/*
	 * public Map<Double,Integer> Vector<MeaserementSample> getRegion(SampleIterator it, double t1){
	 * 
	 * }
	 */
	public static List<SimVars> desim(Map<Satellite, TimeLine> rec, Map<Satellite, TimeLine> em,
			EmitterHistory hist) {
		// TODO Auto-generated method stub
		// double talt = alt/c; //time radius for search

		Satellite emit = hist.getEm(); // satellite emitter
		double[] ret = new double[hist.time.size()];
		//double t0 =  hist.time.first(); //initial time
	//	double tf =  hist.time.last();	//final time
		//double timelim = t0;
		int i = 0;
		double tcur = 0;
		int bin_freq = (int) 1E5;
		double timeframe = 5E5/Constants.c; //alt over speed of light //1.0/bin_freq;

		//System.out.println("first emitted : " + t0 + "s \n last emitted : " + tf);
		//double timeit = hist.time.iterator().;			//CHANGE
		List<SimVars> retval = Lists.newLinkedList();
		Vector3d pos =  new Vector3d(0,0,0);
		Vector<Double> altPoints = new Vector<Double>();
		Iterator<Double> timeIt = hist.time.iterator(); //time iterator over the samples

		
		Map<Satellite, SampleIterator> satSampIterators = Maps.newHashMap();
		try{
		for (Map.Entry<Satellite, TimeLine> iter : rec.entrySet())  // Put satellite iterators into the map
			satSampIterators.put(iter.getKey(), iter.getValue().getIterator(bin_freq));

		}catch(MathException e){
			System.out.println(e + "Math Exception has occured");
		}
		

		// double tinc = 1 / em.get(Emit).getCons().getPulseFrequency(); // time between pulses
		// double[] t;
	
		if(i % modi == 0)System.out.print("desimulating \n");
		//timeIt.next();
	//	for (int i = 0; i < hist.time.size(); i++) { // iterate over all the time instances.
		while(timeIt.hasNext())	{			//iterate over all the Sent pulses
			 //offset one
			
			tcur = timeIt.next().doubleValue(); //current itme of the emitter 
			if(i%100== 0) System.out.print("Current Time: " + tcur + "\n");
			SimVars res = new SimVars();
			
			System.out.print("desimulating sample " + i + " out of " + hist.time.size() + "\n");
			// em = emData.get(Emit).getLookupPosition().find(tr) ; //position of the emitter at a time t
			Map<Satellite, Vector<Double>> altData = Maps.newHashMap();

			for (Map.Entry<Satellite, TimeLine> iter : rec.entrySet()) { // Iterate over all the receiver 

				
				if(i%modi == 0)System.out.print("desimulating satellites \n");
				
				Map<Double, Integer> tphoton = Maps.newHashMap();

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

				tphoton = AntiSimulator.findspikes(satSampIterators.get(iter.getKey()), tcur+timeframe); // list of spike photons
				// that are not noise
				// for this satellite

				for (Double d : tphoton.keySet()) { // iterate over the spikes
					if(i%modi == 0) System.out.print(tphoton.get(d) + " photons at time " + d.toString() + " s \n");
					double t1 = hist.getPulseBeforePulse(d.doubleValue());
					// em.get(hist).getLookupPosition(); // the time of the
					// sent pulse
					Point3d emitp = new Point3d(hist.getPosition().find(d.doubleValue()));
					Point3d recp = new   Point3d(iter.getValue().getLookupPosition().find(d.doubleValue()));
					
					altPoints.add(new Double(AntiSimulator.calcalt( emitp, recp, d.doubleValue() - t1)));
					if(i%20 == 0)	{
						System.out.println("dT for distance: " + (d.doubleValue() - t1));
						System.out.println("distance between points: " + emitp.distance(recp));
						System.out.println("Points: " + emitp + recp);
						emitp.sub(recp);
						System.out.println("dR vector: " + emitp);
					}
					pos = new Vector3d(emitp); // WARNING!!
				
				}
				if(i%modi == 0){
					System.out.println("Position of em: " + pos);
					try{
						System.out.println("Altitue point: " + altPoints.get(0) + " \n 1 of " + altPoints.size() );
					}catch(Exception e){
						System.out.println("Altitue point fubar");
					};
				};
				
				altData.put(iter.getKey(), altPoints); // Store the satellite, altitude points relation

				// Iterator<Double> hi = emittorHistory.time.iterator();

				// re = satData.get(iterat.getKey()).getLookupPosition().find(trd); //position of the
				// satellites at a time t

				// satData.get(iterat.getKey()).getPhotons().keySet();
				// filter();

				// calcalt(em,re,time);
			}
			ret[i] = extractpath(altData);
			 
		
			
			pos.normalize();
			pos.scale(ret[i] + IERS_1996.R_Earth);
			res.pR = new Point3d(pos);
			if(i%modi == 0) System.out.println("res = " + res.pR );
			retval.add(res);
			i++;
			callcnt++;
			if(i > 200) break;
		}

		return retval;

	}

	public static void myplot(Vector<Integer> sims, double lineThickness, String plotFile) {
		int width = 1600;
		int height = 200;
		int hOffset = 50;
		int wOffset = 60;
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
		for (int i = 0; i < 8; i++) {
			g.drawString("(" + Math.floor(1000 * 180 / Math.PI * theta.get((int) (i * 0.125 * size)))
					/ 1000 + "," + Math.floor(1000 * 180 / Math.PI * phi.get((int) (i * 0.125 * size)))
					/ 1000 + ")", wOffset + (int) (i * 0.125 * width), height + hOffset - 5);
		}

		try {
			ImageIO.write(bimg, "png", new File(plotFile + ".png"));
		} catch (Exception e) {
			System.out.println(e + "Plot file writing failed.");
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
/*		
		double[] ter;
		Map<Satellite, TimeLine> em;
		Map<Satellite, TimeLine> rec;
		EmitterHistory hist;
		String flname = "ser_PT";
		ProcessorTester tester = new ProcessorTester();
		RandData ret;
		ret = RandData.load(flname);
		 if(ret == null ){
			 ret = tester.testProcessing();
			 RandData.save(flname, ret);
			 
		 }
		
		Satellite emit;
		int i = 0;*/
		Satellite sat1 = new Satellite();
		Satellite sat2 = new Satellite();
	Vector<Double> vec1 = new Vector<Double>();
	Vector<Double> vec2 = new Vector<Double>();
	vec1.add(new Double(2));
	vec1.add(new Double(5));
	vec1.add(new Double(10));
	vec1.add(new Double(7));
	vec1.add(new Double(3));
	
	vec2.add(new Double(2));
	vec2.add(new Double(5));
	vec2.add(new Double(10));
	vec2.add(new Double(7));
	vec2.add(new Double(3));
	
	
	Map<Satellite, Vector<Double>> alda = Maps.newHashMap();
	alda.put(sat1, vec1) ;
	System.out.println(extractpath(alda));
	 alda.put(sat2,vec2);
	 System.out.println(extractpath(alda));
		// List<SimVars> hi = Lists.newLinkedList();
		// for(i = 0; i<ter.length-1; i++){
		// SimVars tmpSim = new SimVars();
		// em.get(emit).getLookupPosition().
		// tmpSim.pR = ter[i];
		// hi.add(tmpSim);
		// //tmpSim.
		// }
/*		plotHeightDistribution plotter = new plotHeightDistribution();
		plotter.plot(desim(ret.getRec(),ret.getEm(),ret.getEmHist()),3,"vafli");
		// for ( Map<Double,Integer> ter:
		// ret.getEm().get(ret.getEm().keySet().iterator().next()).getIterator(3).next().) {
		Vector<Integer> hi = new Vector<Integer>();*/
		
/*		try {

			System.out.print(findspikes(ret.getEm().values().iterator().next().getIterator((int) 10),
					4542935.369999992));
			System.out.print(findspikes(ret.getEm().values().iterator().next().getIterator((int) 10),
					4542935.969999992));
			SampleIterator iter = ret.getRec().values().iterator().next().getIterator((int) 10);
			iter.c = 0;
			while (iter.hasNext()) {

				hi.add(new Integer(iter.next().getPhotons()));

			}
			myplot(hi, 3, "bbf");
		} catch (Exception e) {
			System.out.println(":(((((");
		}
*/
		// plotter.plot(desim(ret.getRec(),ret.getEm(),ret.getEmHist()),3.0,"recover");

		System.out.print(Integer.MAX_VALUE);

		/*
		 * Point3d em = new Point3d(); Point3d rem = new Point3d(); double xdist = 7000000; double r =
		 * IERS_1996.R_Earth; double km = 1 / IERS_1996.c * 1000; double ns = 1E-9; double t = 1200 * km;
		 * em.set(xdist, 0, 0); rem.set(xdist - 410, 410, 0); double alt = calcalt(em, rem, t);
		 * System.out.print((xdist - r) / 1000 + "\n" + alt / 1000);
		 */

	}

}
