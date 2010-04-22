package com.google.code.laserswarm.util.demReader;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.media.jai.PlanarImage;
import javax.swing.JFrame;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.map.DefaultMapContext;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.operation.transform.GeocentricTransform;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.swing.JMapFrame;
import org.opengis.referencing.operation.TransformException;
import org.ujmp.core.doublematrix.impl.ImageMatrix;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ArcInfoASCII_2 extends DemReader {

	private static final Logger	logger	= Logger.get(ArcInfoASCII_2.class);

	public ArcInfoASCII_2(File demFile) {
		super(demFile);
	}

	@Override
	public ElevationModel parse() throws DemCreationException {
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

		GridGeometry2D t = gc2.getGridGeometry();
		GeneralDirectPosition p = new GeneralDirectPosition(3.6, 50.7);
		double[] temp = gc2.evaluate(p, new double[1]);
		logger.dbg("p: %s", p);
		logger.dbg("Value for p: %s", temp[0]);
		System.out.println();

		// p = new GeneralDirectPosition(3.6, 50.7, temp[0]);
		GeneralDirectPosition p2 = new GeneralDirectPosition(0, 0, 0);
		GeocentricTransform gt = new GeocentricTransform(DefaultEllipsoid.SPHERE, false);
		try {
			gt.transform(p, p2);
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.dbg("p2: %s", p2);
		logger.dbg("Value for p2: %s", temp[0]);

		PlanarImage img = (PlanarImage) gc2.getRenderedImage();
		RenderedImage img2 = gc2.getRenderedImage();
		ImageMatrix m = null;
		logger.dbg("%s", m);

		m.showGUI();

		StyleBuilder sb = new StyleBuilder();
		Style style = sb.createStyle(sb.createRasterSymbolizer());

		DefaultMapContext map = new DefaultMapContext();
		map.addLayer(gc2, style);
		JMapFrame fr = new JMapFrame(map);
		fr.setSize(800, 600);
		fr.setLocationRelativeTo(null);
		fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		fr.setVisible(true);
		return null;
	}
}