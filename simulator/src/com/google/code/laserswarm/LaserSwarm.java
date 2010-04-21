package com.google.code.laserswarm;

import com.google.code.laserswarm.conf.Configuration;

public class LaserSwarm {
	public static void main(String[] args) {
		Configuration example = new Configuration();
		example.write("configuration.xml");
	}
}
