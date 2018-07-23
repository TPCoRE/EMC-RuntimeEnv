package tpc.mc.emc.runtime.util;

import java.util.concurrent.Callable;

/**
 * Callable without exception
 * */
@FunctionalInterface
public interface SafeCallable<V> extends Callable<V> {
	
	public V call();
}
