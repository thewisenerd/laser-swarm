package com.google.code.laserswarm.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.code.laserswarm.simulation.SimVars;
import com.google.common.io.LineReader;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class NonVolatileList implements List<SimVars> {

	private final File			file;
	private final FileWriter	fw;

	private long				lines	= -1L;

	private static final Logger	logger	= Logger.get(NonVolatileList.class);

	public NonVolatileList(File file) throws IOException {
		this.file = file;
		this.fw = new FileWriter(file);

		if (!file.exists())
			file.createNewFile();
	}

	@Override
	public void add(int arg0, SimVars arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(SimVars arg0) {
		try {
			fw.append(arg0.serialize() + "\n");
			lines++;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean addAll(Collection<? extends SimVars> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends SimVars> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			logger.err(e, "cannot clear the file (recreating file)");
		}
	}

	@Override
	public boolean contains(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SimVars get(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<SimVars> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<SimVars> listIterator() {
		try {
			return new ListIterator<SimVars>() {
				private long		current	= 0L;
				private LineReader	reader	= new LineReader(new FileReader(file));

				@Override
				public boolean hasNext() {
					return current + 2 < lines; // has one black line
				}

				@Override
				public SimVars next() {
					try {
						StringBuffer line = new StringBuffer(reader.readLine());
						String str;
						do {
							str = reader.readLine();
							line.append(str);
						} while (!str.contains("com.google.code.laserswarm.simulation.SimVars"));

						SimVars.DESERIALIZER.processLine(line.toString());
						current++;
						return SimVars.DESERIALIZER.getResult();
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void add(SimVars e) {
					NonVolatileList.this.add(e);
				}

				@Override
				public boolean hasPrevious() {
					return false;
				}

				@Override
				public int nextIndex() {
					// return 0;
					throw new UnsupportedOperationException();
				}

				@Override
				public SimVars previous() {
					return null;
				}

				@Override
				public int previousIndex() {
					// return 0;
					throw new UnsupportedOperationException();
				}

				@Override
				public void set(SimVars e) {
					throw new UnsupportedOperationException();
				}
			};
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			return null;
		}
	}

	@Override
	public ListIterator<SimVars> listIterator(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SimVars remove(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SimVars set(int arg0, SimVars arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		if (lines == -1L)
			lines = count();
		return (int) lines;
	}

	@Override
	public List<SimVars> subList(int arg0, int arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}

	public int count() {
		int count = 0;
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			byte[] c = new byte[1024];
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
			}
		} catch (IOException e) {
		}
		return count;
	}

}
