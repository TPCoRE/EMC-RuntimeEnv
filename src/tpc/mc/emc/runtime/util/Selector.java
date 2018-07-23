package tpc.mc.emc.runtime.util;

import tpc.mc.emc.err.UnsupportedException;
import tpc.mc.emc.runtime.Impl;

/**
 * RuntimeEnv selector
 * */
public final class Selector {
	
	/**
	 * Get the current implement
	 * */
	public static final Impl impl() {
		return STATIC.call();
	}
	
	/**
	 * Truly get
	 * */
	private static final Impl $impl() {
		String version = null;
		
		//collect data
		if(version == null) version = support_forge();
		if(version == null) version = support_user();
		
		try {
			String internalVersion = version.replace(".", "");
			return (Impl) Class.forName("tpc.mc.emc.runtime.impl".concat(internalVersion).concat(".Impl").concat(internalVersion)).newInstance();
		} catch(Throwable e) {
			if(version != null && !version.trim().isEmpty()) throw new UnsupportedException("Unsupported MCVersion '".concat(version).concat("'"));
			else throw new UnsupportedException("Empty MCVersion Unsupported!");
		}
	}
	
	/**
	 * Get the mcversion from forge API
	 * */
	private static final String support_forge() {
		try {
			return (String) Class.forName("net.minecraftforge.common.MinecraftForge").getField("MC_VERSION").get(null);
		} catch(Throwable e) {
			return null;
		}
	}
	
	/**
	 * Get the mcversion from user
	 * */
	private static final String support_user() {
		return USER;
	}
	
	private static final String USER = null; //WILL BE SET BY BOOTSTRAP, NOTICE THAT IT MAY STILL BE NULL
	private static final SafeCallable<Impl> STATIC = () -> $impl();
}
