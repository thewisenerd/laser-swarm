package com.google.code.laserswarm.util;

import com.lyndir.lhunath.lib.system.logging.Logger;

public abstract class DebugUtil {

	private static final Logger	logger	= Logger.get(DebugUtil.class);

	public static void showMemUsage() {
		logger.inf("Memory usage:\nfreeMemory %s\nmaxMemory %s \ntotalMemory %s", Runtime.getRuntime()
				.freeMemory(), //
				Runtime.getRuntime().maxMemory(),// 
				Runtime.getRuntime().totalMemory());
	}

}
