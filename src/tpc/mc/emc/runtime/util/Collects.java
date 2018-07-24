package tpc.mc.emc.runtime.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Collection Helper
 * */
public final class Collects {
	
	/**
	 * Return itself
	 * */
	public static final <T> T[] concat(T[] a) {
		assert(a != null);
		
		return a;
	}
	
	/**
	 * Return itself
	 * */
	public static final <T> T concat(T a) {
		return a;
	}
	
	/**
	 * Concat a and b
	 * */
	public static final <T> T[] concat(T[] a, T... b) {
		assert(a != null);
		
		T[] result = forward(a, b.length);
		System.arraycopy(b, 0, result, a.length, b.length);
        
        return result;
	}
	
	/**
	 * Concat a and b
	 * */
	public static final <T> T[] concat(T[] a, T b) {
		assert(a != null);
		
		T[] result = forward(a, 1);
		result[a.length] = b;
		
		return result;
	}
	
	/**
	 * Concat a and b
	 * */
	public static final <T> T[] concat(T a, T... b) {
		assert(b != null);
		
		T[] result = backward(b, 1);
		result[0] = a;
		
		return result;
	}
	
	/**
	 * Empty the front of the array
	 * */
	public static final <T> T[] backward(T[] a, int len) {
		assert(a != null);
		assert(len > 0);
		
		T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + len);
		System.arraycopy(a, 0, result, len, a.length);
		
		return result;
	}
	
	/**
	 * Empty the back of the array
	 * */
	public static final <T> T[] forward(T[] a, int len) {
		assert(a != null);
		assert(len > 0);
		
		return Arrays.copyOf(a, a.length + len);
	}
	
	/**
	 * Iterate a array
	 * */
	public static final <T> Iterator<T> iterate(T[] a) {
		assert(a != null);
		int max = a.length;
		
		return new Iterator<T>() {
			
			private int index = 0;
			
			@Override
			public boolean hasNext() {
				return this.index < max;
			}
			
			@Override
			public T next() {
				return a[this.index++];
			}
		};
	}
}
