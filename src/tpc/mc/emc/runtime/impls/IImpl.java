package tpc.mc.emc.runtime.impls;

import tpc.mc.emc.platform.PlatformInfo;
import tpc.mc.emc.platform.standard.IMath;

/**
 * Platform Implement
 * */
public abstract class IImpl {
	
	private final String vendor_mc;
	private final String vendor_impl;
	private final String version_impl;
	private final String version_mc = null;
	
	/**
	 * Init Config
	 * */
	protected IImpl(String vendor_mc, String vendor_impl, String version_impl) {
		this.vendor_mc = vendor_mc;
		this.vendor_impl = vendor_impl;
		this.version_impl = version_impl;
	}
	
	/**
	 * Get a new math implement
	 * */
	public abstract IMath math();
	
	/**
	 * Ruler the given class
	 * */
	public abstract byte[] rule(byte[] klass, String name);
	
	/**
	 * Get the basic info
	 * */
	@Override
	public final String toString() {
		return "[" + this.vendor_impl + "]" + this.version_impl;
	}
	
	/**
	 * Get misc informations
	 * */
	public final <T> T misc(Type<T> typeIn) {
		assert(typeIn != null);
		
		if(typeIn == VENDOR_MC) return (T) this.vendor_mc;
		if(typeIn == VENDOR_IMPL) return (T) this.vendor_impl;
		if(typeIn == VERSION_IMPL) return (T) this.version_impl;
		if(typeIn == VERSION_MC) return (T) this.version_mc;
		
		//EMM?
		return null;
	}
	
	public static final Type<String> VENDOR_MC = new Type<>();
	public static final Type<String> VENDOR_IMPL = new Type<>();
	public static final Type<String> VERSION_IMPL = new Type<>();
	public static final Type<String> VERSION_MC = new Type<>();
	
	private static final class Type<T> {}
}
