package com.google.code.laserswarm.Desim.elevation.slope;

import static com.google.code.laserswarm.math.TreeMapTools.treeMapAvg;
import static com.google.code.laserswarm.math.TreeMapTools.treeMapsOverlap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.Desim.DataContainer;
import com.google.code.laserswarm.Desim.NoiseData;
import com.google.code.laserswarm.Desim.brdf.BRDFinput;
import com.google.code.laserswarm.Desim.elevation.AltitudeCalculation;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.math.LookupTable;
import com.google.code.laserswarm.process.TimeLine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SubSampleCorrelation implements SampleCorrelation {
	private static final Logger							logger	= Logger.get(SubSampleCorrelation.class);

	private Map<Satellite, TimeLine>					receiverTimelines;
	private Map<Satellite, DataContainer>				interpulseData;

	private LinkedList<ElevationRelatedEntriesPoint>	rawElevationSlopes;
	private double										equalitySpacing;
	private double										interval;
	private double										fractionD;
	private double										minEl	= Configuration.R0 - 500.0;
	private double										maxEl	= Configuration.R0 + 9000.0;
	private int											qLength;
	private int											middle;
	private Constellation								cons;

	/**
	 * 
	 * @param swarm
	 *            The constellation used in the simulation is given here.
	 * @param receiverTimes
	 *            The Timelines generated by the simulation are give here.
	 * @param correlationInterval
	 *            The interval used in the correlation algorithm; two elevations found are considered
	 *            close enough to be connected to the same datapoint if they are less than an interval
	 *            apart.
	 * @param comparisonQueueLength
	 *            The length of the correlation queue. It is a good idea to make it uneven, because the
	 *            middle entry is looked for, and it must be three or larger. Three seems to be doing
	 *            well, though.
	 * @param whenEqual
	 *            For the spike filter, two altitudes are assumed to be equal if they are apart less than
	 *            this distance.
	 * @param fractionD
	 *            The fraction of the diameter used for the slope calculation.
	 */
	public SubSampleCorrelation(Constellation swarm, Map<Satellite, TimeLine> receiverTimes,
			double correlationInterval, int comparisonQueueLength, double whenEqual, double fractionD) {
		this.interpulseData = Maps.newHashMap();
		this.receiverTimelines = receiverTimes;
		this.interval = correlationInterval;
		this.qLength = comparisonQueueLength;
		this.middle = (int) Math.floor((double) qLength / 2.0);
		this.equalitySpacing = whenEqual;
		this.fractionD = fractionD;
		this.cons = swarm;
		this.rawElevationSlopes = Lists.newLinkedList();
	}

	@Override
	public ElevationBRDF next(Map<Satellite, NoiseData> nextInterpulse, double nextPulseT,
			Point3d nextEmitPt) throws MathException {
		for (Satellite tempSat : nextInterpulse.keySet()) {
			if (interpulseData.get(tempSat) == null) {
				interpulseData.put(tempSat, new DataContainer());
				interpulseData.get(tempSat).setQueueLength(1);
			}
			interpulseData.get(tempSat).add(nextInterpulse.get(tempSat));
		}
		ElevationBRDF out = null;
		// Do all calculations that can be done for a single data point (or: interpulse window).
		TreeMap<Double, Vector3d> altitudes = findInterpulseData(nextEmitPt, nextPulseT);
		// Find and average altitudes that show a strong correlation.
		ElevationRelatedEntriesPoint result = findAndAverageRelatedAltitudes(altitudes, nextPulseT,
				nextEmitPt);
		// Some queue maintenance.
		rawElevationSlopes.add(result);
		while (rawElevationSlopes.size() > qLength) {
			rawElevationSlopes.removeFirst();
		}
		if (rawElevationSlopes.size() == qLength) {
			// Filter averages based on spike removal.
			spikeFilter(25 * equalitySpacing);
			spikeFilter(5 * equalitySpacing);
			spikeFilter(equalitySpacing);
			averageExclusionFilter(5);
			averageExclusionFilter(1);
			// Final elevation and BRDFinput generation.
			ElevationRelatedEntriesPoint rawElBRDF = rawElevationSlopes.get(middle);
			double height = rawElBRDF.getElevation();
			out = new ElevationBRDF(height, genBRDFInput(rawElevationSlopes));
		}
		return out;
	}

	private ElevationRelatedEntriesPoint adjustElevationRelatedEntriesPoint(
			ElevationRelatedEntriesPoint old, double newHeight) {
		ArrayList<TreeMap<Double, Vector3d>> closeEntryMaps = old.getRelatedEntries();
		Iterator<TreeMap<Double, Vector3d>> closeIt = closeEntryMaps.iterator();
		TreeMap<Double, Vector3d> bestFitMap = Maps.newTreeMap();
		double minDist = Double.MAX_VALUE;
		while (closeIt.hasNext()) {
			TreeMap<Double, Vector3d> thisMap = closeIt.next();
			double mapAvg = treeMapAvg(thisMap);
			double dist = Math.abs(mapAvg - newHeight);
			if (dist < minDist) {
				bestFitMap = thisMap;
				minDist = dist;
			}
		}
		return new ElevationRelatedEntriesPoint(newHeight, old.getTEmit(), old.getPosEmit(),
				closeEntryMaps, bestFitMap);
	}

	private TreeMap<Double, Vector3d> findInterpulseData(Point3d nextEmitPt, Double nextPulseT)
			throws MathException {
		TreeMap<Double, Vector3d> altitudes = Maps.newTreeMap();
		double satCount = 0;
		for (Satellite curSat : interpulseData.keySet()) {
			satCount++;
			DataContainer tempContainer = interpulseData.get(curSat);
			TreeMap<Double, Integer> tempData = tempContainer.getData().getLast().getData();
			for (Double time : tempData.keySet()) {
				double thisAlt = AltitudeCalculation.calcAlt(nextEmitPt,
						new Point3d(receiverTimelines.get(curSat)
								.getLookupPosition().find(time)), time - nextPulseT);
				int numPhotons = tempData.get(time);
				LookupTable satPositions = receiverTimelines.get(curSat).getLookupPosition();
				if (thisAlt < minEl || thisAlt > maxEl) {
					logger.dbg("Filtering impossible elevation: %s", maxEl - Configuration.R0);
				} else {
					for (int i = 0; i < numPhotons; i++) {
						altitudes.put(thisAlt, new Vector3d(satPositions.find(time)));
					}
					logger.dbg("Found an altitude: %s, with photon no.: %s", thisAlt - Configuration.R0,
							numPhotons);
				}
			}
		}
		return altitudes;
	}

	private BRDFinput genBRDFInput(LinkedList<ElevationRelatedEntriesPoint> elBRDFs) {
		ElevationRelatedEntriesPoint thisElBRDF = elBRDFs.get(middle);
		ElevationRelatedEntriesPoint nextElBRDF = elBRDFs.get(middle + 1);
		// Calculate the emitter groundtrack point.
		Point3d posEmit = thisElBRDF.getPosEmit();
		Point3d posEmitSph = Convert.toSphere(posEmit);
		posEmitSph.x -= thisElBRDF.getElevation();
		Vector3d emPos = new Vector3d(Convert.toXYZ(posEmitSph));
		// Calculate the direction in which the emitter is moving.
		Vector3d nextEmPos = new Vector3d(nextElBRDF.getPosEmit());
		Vector3d thisEmPos = new Vector3d(thisElBRDF.getPosEmit());
		Vector3d emDir = new Vector3d();
		emDir.sub(nextEmPos, thisEmPos);
		emDir.normalize();
		// Calculate the along-track slope.
		Iterator<ElevationRelatedEntriesPoint> elBRDFIt = elBRDFs.iterator();
		int count = 0;
		Point3d firstSlopeTot = new Point3d(0, 0, 0);
		Point3d secondSlopeTot = new Point3d(0, 0, 0);
		int firstCount = 0;
		int secondCount = 0;
		while (elBRDFIt.hasNext()) {
			ElevationRelatedEntriesPoint thisEB = elBRDFIt.next();
			Point3d thisEmitPos = Convert.toSphere(thisEB.getPosEmit());
			double height = thisEB.getElevation();
			if (count < middle) {
				firstSlopeTot.x += height;
				firstSlopeTot.y += thisEmitPos.y;
				firstSlopeTot.z += thisEmitPos.z;
				firstCount++;
			} else if (count >= middle) {
				secondSlopeTot.x += height;
				secondSlopeTot.y += thisEmitPos.y;
				secondSlopeTot.z += thisEmitPos.z;
				secondCount++;
			}
			count++;
		}
		Point3d hFirst = new Point3d(firstSlopeTot.x / firstCount, firstSlopeTot.y / firstCount,
				firstSlopeTot.z / firstCount);
		Point3d hSecond = new Point3d(secondSlopeTot.x / secondCount, secondSlopeTot.y / secondCount,
				secondSlopeTot.z / secondCount);
		double avgRad = (hFirst.x + hSecond.x) / 2.0;
		double difRad = hSecond.x - hFirst.x;
		hFirst.x = avgRad;
		hSecond.x = avgRad;
		Point3d hFirstCart = Convert.toXYZ(hFirst);
		Point3d hSecondCart = Convert.toXYZ(hSecond);
		Vector3d difTan = new Vector3d();
		difTan.sub(hFirstCart, hSecondCart);
		double alongTrackSlope = difRad / difTan.length();
		logger.dbg("avgRad, difRad, difTan: %s, %s, %s", avgRad, difRad, difTan.length());
		// Calculate the cross-track slope.
		Vector3d emitVect = new Vector3d(posEmit);
		double footprintD = fractionD * 2.0 * cons.getEmitter().getBeamDivergence()
				* (emitVect.length() - Configuration.R0);
		TreeMap<Double, Vector3d> altitudes = thisElBRDF.getBestMap();
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (Double alt : altitudes.keySet()) {
			if (alt > max) {
				max = alt;
			}
			if (alt < min) {
				min = alt;
			}
		}
		double a = Math.pow(alongTrackSlope, 2);
		double b = Math.pow((max - min) / footprintD, 2);
		double crossTrackSlope = Math.sqrt(b - a);
		if (new Double(crossTrackSlope).isNaN()) {
			crossTrackSlope = 0;
			logger
					.wrn("Intercepted a NaN crossTrackSlope. Is your footprint diameter fraction a bit too optimistic?");
		}
		logger.inf("min, max, footprintD: %s, %s, %s", min, max, footprintD);
		logger.inf("a, b, along & crossTrackSlope: %s, %s, %s, %s", a, b, alongTrackSlope,
				crossTrackSlope);
		// The satellite vectors go in here.
		Map<Vector3d, Integer> photonDirs = Maps.newLinkedHashMap();
		for (Double time : altitudes.keySet()) {
			Vector3d satVect = altitudes.get(time);
			if (photonDirs.get(satVect) == null) {
				photonDirs.put(satVect, 1);
			} else {
				photonDirs.put(satVect, photonDirs.get(satVect) + 1);
			}
		}
		// Get the current emitter time.
		double curTime = thisElBRDF.getTEmit();
		logger
				.dbg(
						"Generated the following BRDFinput: \nemPos: %s\nemDir: %s\ngroundPoint: %s\nslopes: %s, %s\nphotonDirs: %s\ntime: %s",
						emPos.toString(), emDir.toString(), scatterPoint.toString(), alongTrackSlope,
						crossTrackSlope, photonDirs
								.toString(), curTime);
		return new BRDFinput(emPos, emDir, scatterPoint, alongTrackSlope, crossTrackSlope, photonDirs,
				curTime);
	}

	private boolean areEqual(double a, double b, double spacing) {
		if (Math.abs(a - b) < spacing) {
			return true;
		} else {
			return false;
		}
	}

	private ElevationRelatedEntriesPoint findAndAverageRelatedAltitudes(TreeMap<Double, Vector3d> alts,
			double tEmit, Point3d posEmit) {
		ArrayList<TreeMap<Double, Vector3d>> entriesClose = Lists.newArrayList();
		// Try to find lists of related altitudes, then put them in TreeMaps
		for (Double currentAlt : alts.keySet()) {
			boolean isInRelatedEntryMap = false;
			for (TreeMap<Double, Vector3d> relatedEntryMap : entriesClose) {
				for (Double thisAlt : relatedEntryMap.keySet()) {
					if (Math.abs(currentAlt - thisAlt) < interval) {
						isInRelatedEntryMap = true;
						relatedEntryMap.put(currentAlt, alts.get(currentAlt));
						break;
					}
				}
				if (isInRelatedEntryMap) {
					break;
				}
			}
			if (!(isInRelatedEntryMap)) {
				TreeMap<Double, Vector3d> treeMap = Maps.newTreeMap();
				treeMap.put(currentAlt, alts.get(currentAlt));
				entriesClose.add(treeMap);
			}
		}
		// Find and join overlapping treeMaps.
		for (int i = 0; i < entriesClose.size(); i++) {
			TreeMap<Double, Vector3d> entryMapFirst = entriesClose.get(i);
			int j = i + 1;
			while (j < entriesClose.size()) {
				TreeMap<Double, Vector3d> entryMapSecond = entriesClose.get(j);
				if (treeMapsOverlap(entryMapFirst, entryMapSecond)) {
					entryMapFirst.putAll(entryMapSecond);
					entriesClose.remove(j);
				}
				j++;
			}
		}
		// Try to find the TreeMap with the largest amount of altitudes.
		int maxSizeTreeMap = 0;
		TreeMap<Double, Vector3d> maxRelatedEntryMap = Maps.newTreeMap();
		for (TreeMap<Double, Vector3d> relatedEntryMap : entriesClose) {
			if (relatedEntryMap.size() > maxSizeTreeMap) {
				maxSizeTreeMap = relatedEntryMap.size();
				maxRelatedEntryMap = relatedEntryMap;
			}
		}
		return new ElevationRelatedEntriesPoint(treeMapAvg(maxRelatedEntryMap), tEmit, posEmit,
				entriesClose, maxRelatedEntryMap);
	}

	private boolean averageExclusionFilter(int nSpacings) {
		boolean didFilter = false;
		Iterator<ElevationRelatedEntriesPoint> rawIt = rawElevationSlopes.iterator();
		double totalAlt = 0;
		double altNo = 0;
		while (rawIt.hasNext()) {
			totalAlt += rawIt.next().getElevation();
			altNo++;
		}
		double altAvg = totalAlt / altNo;
		while (rawIt.hasNext()) {
			ElevationRelatedEntriesPoint thisElSlope = rawIt.next();
			if (!(areEqual(altAvg, thisElSlope.getElevation(), nSpacings * equalitySpacing))) {
				thisElSlope = adjustElevationRelatedEntriesPoint(thisElSlope, altAvg);
			}
		}
		return didFilter;
	}

	private boolean spikeFilter(double spacing) {
		boolean didFilter = false;
		double first = rawElevationSlopes.get(middle - 1).getElevation();
		double mid = rawElevationSlopes.get(middle).getElevation();
		double last = rawElevationSlopes.get(middle + 1).getElevation();
		if (areEqual(first, last, spacing)) {
			if (!(areEqual(first, mid, spacing))) {
				didFilter = true;
				ElevationRelatedEntriesPoint middlePoint = rawElevationSlopes.get(middle);
				double elevation = (first + last) / 2;
				rawElevationSlopes.set(middle,
						adjustElevationRelatedEntriesPoint(middlePoint, elevation));
			}
		}
		return didFilter;
	}
}
