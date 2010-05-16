package com.google.code.laserswarm;

import java.io.FileNotFoundException;
import java.util.Map;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class RandData {

	private static final Logger	logger	= Logger.get(RandData.class);

	public static RandData read(String name) throws FileNotFoundException {
		return Configuration.read(name, Configuration.getDefaultSerializer(name));
	}

	Map<Satellite, TimeLine>	rec;

	Map<Satellite, TimeLine>	em;
	EmitterHistory				emHist;

	public RandData(Map<Satellite, TimeLine> rec, Map<Satellite, TimeLine> em, EmitterHistory emHist) {
		super();
		this.rec = rec;
		this.em = em;
		this.emHist = emHist;
	}

	public Map<Satellite, TimeLine> getEm() {
		return em;
	}

	public EmitterHistory getEmHist() {
		return emHist;
	}

	public Map<Satellite, TimeLine> getRec() {
		return rec;
	}

	public void write(String filename) {
		Configuration.write(filename, this);
	}
}