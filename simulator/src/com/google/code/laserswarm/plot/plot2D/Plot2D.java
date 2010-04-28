package com.google.code.laserswarm.plot.plot2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.media.jai.PlanarImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.geotools.coverage.grid.GridCoverage2D;
import org.ujmp.core.Matrix;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;

public class Plot2D extends JFrame {

	public static int findSqSize(BufferedImage im) {
		int s = Math.min(im.getWidth(), im.getHeight());
		System.out.println("Size = " + s);
		int n = 0;
		while (Math.pow(2, n) + 1 < s) {
			n++;
		}
		System.out.println("Math.pow(2, n) + 1  = " + Math.pow(2, n) + 1);
		System.out.println("n = " + n);
		return n;
	}

	public static BufferedImage format(BufferedImage im) {
		return format(im, findSqSize(im));
	}

	public static BufferedImage format(BufferedImage im, int n) {
		if (im.getWidth() != im.getHeight())
			im = squarify(im);

		/* Stretch the image to the correct scale */
		int s2 = (int) Math.pow(2, n) + 1;
		BufferedImage i2 = new BufferedImage(s2, s2, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2d = Graphics2D.class.cast(i2.getGraphics());
		g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
				java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		// g2d.setComposite(
		// AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transperancy));

		g2d.drawImage(im, 0, 0, s2, s2, null);
		return i2;
	}

	public static void main(String[] args) {
		ElevationModel dem = null;
		try {
			dem = DemReader.parseDem(new File("DEM/srtm_37_02-red.asc"));
		} catch (DemCreationException e1) {
		}
		Plot2D.format(mkImage(dem.getCoverage()));
		// Plot2D.make(dem.getCoverage());
	}

	public static void make(BufferedImage img) {
		new Plot2D(img);
	}

	public static void make(GridCoverage2D grid) {
		new Plot2D(mkImage(grid));
	}

	public static void make(Matrix m) {
		new Plot2D(mkImage(m));
	}

	public static BufferedImage mkImage(GridCoverage2D c) {
		PlanarImage pi = ((PlanarImage) c.getRenderedImage());
		return pi.getAsBufferedImage();
	}

	/**
	 * Convert a matrix to a buffered image (greyscale)
	 * 
	 * @param m
	 * @return
	 */
	public static BufferedImage mkImage(Matrix m) {
		BufferedImage im = new BufferedImage((int) m.getColumnCount(), (int) m.getRowCount(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = im.getGraphics();
		Graphics2D g2 = Graphics2D.class.cast(g);

		Iterable<long[]> iterator = m.allCoordinates();

		double min = m.getMinValue();
		double max = m.getMaxValue();
		double grad = 255 / (max - min);

		for (long[] coordinate : iterator) {
			int c2 = m.getAsInt(coordinate);
			int c = (int) ((c2 - min) * grad);
			g2.setColor(new Color(c, c, c, 255));
			g2.drawRect((int) coordinate[1], (int) coordinate[0], 1, 1);
		}
		return im;
	}

	/**
	 * Split the image of size (2m+1;2m+1) into sections of (2n+1;2n+1)
	 * 
	 * @param im
	 *            Source image
	 * @param n
	 *            size of the new tiles
	 * @return
	 */
	public static BufferedImage[][] split(BufferedImage im, int n) {
		int m = (int) ((Math.log(im.getWidth() - 1)) / (Math.log(2)));

		int tiles = (int) Math.pow(2, m - n);
		int tileSize = (int) Math.pow(2, n) + 1;
		BufferedImage[][] images = new BufferedImage[tiles][tiles];
		for (int row = 0; row < tiles; row++) {
			for (int col = 0; col < tiles; col++) {
				images[row][col] = im.getSubimage(row * (tileSize - 1), col * (tileSize - 1), // 
						tileSize, tileSize);
			}
		}

		return images;
	}

	/**
	 * Make the image square
	 * 
	 * @param im
	 * @return
	 */
	public static BufferedImage squarify(BufferedImage im) {
		if (im.getWidth() == im.getHeight())
			return im;

		int s = Math.min(im.getWidth(), im.getHeight());
		BufferedImage i = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
		i.getGraphics().drawImage(im, 0, 0, s, s, null);
		return i;
	}

	public Plot2D(final BufferedImage img) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		add(new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d = Graphics2D.class.cast(g);
				g2d.drawImage(img, 0, 0, getWidth(), getHeight(), null);
			}
		});
		setVisible(true);
	}
}
