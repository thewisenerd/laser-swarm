package com.google.code.laserswarm.desim;

import java.util.LinkedList;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class NoisePercentage {
	private static final Logger	logger	= Logger.get(NoisePercentage.class);

	public static double findNoisePercentage(final DataContainer data) {
		LinkedList<NoiseData> nsData = data.getData();
		double noise = 0; // noise photons
		double tNoise = 0; // time of the signal
		double tSignal = 0; // time of the signal
		double signal = 0; // signal Photons

		for (NoiseData noiseIt : nsData) { // iterate over the interpulse windows
			if (noiseIt.hasNoise()) {
				for (Integer intIt : noiseIt.getNoise().values()) {
					noise += intIt; // add up noise photons
				}
				tNoise += noiseIt.getNoiseFrameL().diff() + noiseIt.getNoiseFrameR().diff();
				// calculate time of the noise in one interpulse window
			}
			if (noiseIt.hasData()) {
				for (Integer intIt : noiseIt.getData().values()) {
					signal += intIt; // add up signal photons
				}
				tSignal += noiseIt.getDataFrame().diff(); // calculate the signal time in one interpulse
															// window
			}
		}
		logger.dbg("Noise, signal photons: %s, %s, Noise, signal times: %s, %s", noise, signal, tNoise,
				tSignal);
		Double result = (noise / tNoise * (tNoise + tSignal)) / (signal + noise);
		if (result.isNaN()) {
			result = 0.0;
		}
		return (double) result;
	}

}
