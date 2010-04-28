package com.google.code.laserswarm.util.demReader;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.ujmp.core.doublematrix.impl.ImageMatrix;

import com.google.code.laserswarm.earthModel.IElevationModel;
import com.google.code.laserswarm.out.plot2D.Plot2D;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ArcInfoASCII_2 extends DemReader {

	private static final Logger	logger	= Logger.get(ArcInfoASCII_2.class);

	public static void main(String[] args) {
		try {
			IElevationModel dem = new ArcInfoASCII_2(new File("DEM/srtm_37_02-red.asc")).parse();
		} catch (DemCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Image toImage(BufferedImage bufferedImage) {
		return Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
	}

	public ArcInfoASCII_2(File demFile) {
		super(demFile);
	}

	@Override
	public IElevationModel parse() throws DemCreationException {
		URI arcURL = getDemFile().toURI();
		GridCoverage2D gc2 = null;
		try {
			System.out.println(arcURL.toURL());
			ArcGridReader agr = new ArcGridReader(arcURL.toURL());
			gc2 = agr.read(null);
		} catch (IOException e) {
			throw new DemCreationException(e);
		}

		logger.dbg("Envelope: \n%s", gc2.getEnvelope2D());

		BufferedImage bufferedIm = Plot2D.mkImage(gc2);
		ImageMatrix m;
		try {
			m = new ImageMatrix(bufferedIm);
			m.showGUI();
			Plot2D.make(m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}