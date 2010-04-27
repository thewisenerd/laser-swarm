package com.google.code.laserswarm.simulation;

import java.lang.reflect.Field;
import java.util.List;

import com.google.code.laserswarm.util.RetrievalExecption;
import com.google.common.collect.Lists;

public abstract class SimVarUtil {

	@SuppressWarnings("unchecked")
	public static <T> List<T> getField(Field f, List<SimVars> values) throws RetrievalExecption {
		try {
			List<T> vals = Lists.newLinkedList();
			for (SimVars simVars : values) {
				vals.add((T) f.get(simVars));
			}

			return vals;
		} catch (IllegalArgumentException e) {
			throw new RetrievalExecption(e);
		} catch (IllegalAccessException e) {
			throw new RetrievalExecption(e);
		}
	}

	public static <T> List<T> getField(String fieldName, List<SimVars> values) throws RetrievalExecption {
		Field f;
		try {
			f = SimVars.class.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new RetrievalExecption(e);
		}
		return getField(f, values);
	}
}
