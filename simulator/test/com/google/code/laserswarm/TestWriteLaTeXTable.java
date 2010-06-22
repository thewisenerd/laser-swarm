package com.google.code.laserswarm;

import com.google.code.laserswarm.out.table.WriteLaTeXTable;

public class TestWriteLaTeXTable {
	public static void main(String[] args) {
		WriteLaTeXTable write = new WriteLaTeXTable();
		write.write(SimulationTester.sim(), "../report/simulator/tables/testTable.tex");
	}
}
