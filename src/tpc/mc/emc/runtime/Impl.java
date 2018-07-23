package tpc.mc.emc.runtime;

import tpc.mc.emc.platform.standard.IMath;

/**
 * Platform Implement
 * */
public interface Impl {
	
	/**
	 * Get math implement
	 * */
	public IMath math();
	
	/**
	 * Ruler the given class
	 * */
	public byte[] rule(byte[] klass, String name);
}
