package com.google.code.laserswarm;

import java.io.File;
import java.util.Map;

import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.process.EmitterHistory;
import com.google.code.laserswarm.process.TimeLine;

public class RandData {
	@ElementMap
	Map<Satellite, TimeLine>	rec;
	@ElementMap
	Map<Satellite, TimeLine>	em;
	@ElementMap
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


	
	
	public static RandData load(String name) {
		Serializer serializer = new Persister();
		File source = new File(name);

		try {
			return serializer.read(RandData.class, source);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void save(String name, RandData data) {
		Serializer serializer = new Persister();
		File result = new File(name);

		try {
			serializer.write(data, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
