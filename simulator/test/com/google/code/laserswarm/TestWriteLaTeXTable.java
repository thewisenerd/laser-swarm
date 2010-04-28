package com.google.code.laserswarm;

import com.google.code.laserswarm.table.writeLaTeXTable;

public class TestWriteLaTeXTable {
	public static void main(String[] args) {
		writeLaTeXTable write = new writeLaTeXTable();
		write.write(SimulationTester.sim(), "../report/simulator/tables/testTable.tex");
	}
}
