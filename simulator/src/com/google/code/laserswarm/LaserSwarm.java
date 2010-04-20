package com.google.code.laserswarm;

import java.io.File;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.code.laserswarm.conf.Configuration;

public class LaserSwarm {
	public static void main(String[] args) throws Exception {
		Serializer serializer = new Persister();
		Configuration example = new Configuration();
		File result = new File("example.xml");
		serializer.write(example, result);
	}
}
