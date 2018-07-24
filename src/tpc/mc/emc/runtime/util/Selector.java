package tpc.mc.emc.runtime.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import tpc.mc.emc.err.UnsupportedException;
import tpc.mc.emc.runtime.Impl;

/**
 * RuntimeEnv selector
 * */
public final class Selector {
	
	private final String[] suggs;
	
	/**
	 * Create a selector without any suggestions
	 * */
	public Selector() {
		this(new String[0]);
	}
	
	/**
	 * Create a selector with some suggestions
	 * */
	public Selector(String... suggs) {
		this.suggs = suggs;
	}
	
	/**
	 * Select a implements with extend suggestions
	 * */
	public final Impl select(String... extsuggs) {
		String[] versions = Collects.concat(supports(), Collects.concat(this.suggs, extsuggs));
		Iterator<String> iter = Collects.iterate(versions);
		Impl result = null;
		String version = null;
		
		//roll all to find
		while(iter.hasNext()) {
			version = iter.next();
			
			//try to get
			try {
				String internalVersion = version.replace(".", "");
				
				//find constructor
				Constructor con = Class.forName("tpc.mc.emc.runtime.impl".concat(internalVersion).concat(".Impl").concat(internalVersion)).getDeclaredConstructor();
				con.setAccessible(true);
				
				//init
				result = (Impl) con.newInstance();
				
				//break the loop
				break;
			} catch(Throwable e) {}
		}
		
		if(result == null) throw new UnsupportedException("Unsupported MCVersions " + Arrays.asList(versions));
		
		//config in
		try {
			Reflect.located(Impl.class, "version_mc").set(result, version);
		} catch(Throwable e) {
			throw new BootstrapMethodError("Unknown Error", e);
		}
		
		//return result
		return result;
	}
	
	/**
	 * Get the implements by Bootstrap
	 * */
	public static final Impl select() {
		return SELECTED;
	}
	
	/**
	 * Get all support
	 * */
	private static final String[] supports() {
		LinkedList<String> result = new LinkedList<>();
		String support = null;
		
		//collect data
		if((support = support_forge()) != null) result.add(support);
		
		return result.toArray(new String[result.size()]);
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
	
	private static final Impl SELECTED = null; //BE SET BY BOOTSTRAP
}
