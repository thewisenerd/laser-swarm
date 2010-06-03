package com.google.code.laserswarm.Desim;

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
			tNoise += noiseIt.getNoiseFrameL().diff() + // calculate time of the noise in one interpulse
					// window
					noiseIt.getNoiseFrameR().diff();
			tSignal += noiseIt.getDataFrame().diff(); // calculate the signal time in one interpulse
			// window

			for (Integer intIt : noiseIt.getNoise().values()) {
				noise += intIt; // add up noise photons
			}
			for (Integer intIt : noiseIt.getData().values()) {
				signal += intIt; // add up signal photons
			}
		}
		logger.dbg("Noise, signal photons: %s, %s, Noise, signal times: %s, %s", noise, signal, tNoise,
				tSignal);
		return (noise / tNoise * (tNoise + tSignal)) / (signal + noise);
	}

}
