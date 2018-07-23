package tpc.mc.emc.runtime.util;

import tpc.mc.emc.err.UnsupportedException;

/**
 * Shutdown the vm
 * */
public final class Shutdown {
	
	/**
	 * I will try my best
	 * */
	public static final void exit(int status) {
		try {
			System.exit(status);
		} catch(Throwable e) {
			try {
				Reflect.located("java.lang.Shutdown", "exit", int.class).invoke(null, status);
			} catch(Throwable ie) {
				ie.addSuppressed(e);
				
				throw new UnsupportedException("No idea!", ie);
			}
		}
	}
}
