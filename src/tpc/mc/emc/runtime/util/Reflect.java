package tpc.mc.emc.runtime.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * Reflect API Helper
 * */
public final class Reflect {
	
	/**
	 * Get a method safely
	 * */
	public static final Method located(Class<?> klass, String name, Class<?>... params) {
		try {
			Method m;
			
			try {
				m = klass.getMethod(name, params);
			} catch(java.lang.Throwable e) {
				m = klass.getDeclaredMethod(name, params);
				m.setAccessible(true);
			}
			
			return m;
		} catch(Throwable e) {}
		
		//safely return
		return null;
	}
	
	/**
	 * Get a method safely
	 * */
	public static final Method located(String klass, String name, Class... params) {
		try {
			return located(Class.forName(klass), name, params);
		} catch(Throwable e) {}
		
		//safely return
		return null;
	}
	
	/**
	 * Get a field safely
	 * */
	public static final Field located(Class<?> klass, String name) {
		try {
			Field f;
			
			try {
				f = klass.getField(name);
			} catch(java.lang.Throwable e) {
				f = klass.getDeclaredField(name);
				f.setAccessible(true);
			}
			
			//try access modifiers
			int modi = f.getModifiers();
			if(Modifier.isStatic(modi) && Modifier.isFinal(modi)) {
				MODIFIERS.get().set(f, modi & ~Modifier.FINAL);
			}
			
			//retrun result
			return f;
		} catch(Throwable e) {}
		
		//safely return
		return null;
	}
	
	/**
	 * Get a field of the given class safely
	 * */
	public static final Field located(String klass, String name) {
		try {
			return located(Class.forName(klass), name);
		} catch(Throwable e) {}
		
		//safely return
		return null;
	}
	
	/**
	 * Return in the future
	 * */
	public static final Supplier<Field> ilocate(Class<?> klass, String name) {
		return new Supplier<Field>() {
			
			private Field cache = null;
			
			@Override
			public synchronized final Field get() {
				return cache == null ? cache = located(klass, name) : cache;
			}
		};
	}
	
	/**
	 * Return in the future
	 * */
	public static final Supplier<Field> ilocate(String klass, String name) {
		return new Supplier<Field>() {
			
			private Field cache = null;
			
			@Override
			public synchronized final Field get() {
				return cache == null ? cache = located(klass, name) : cache;
			}
		};
	}
	
	/**
	 * Return in the future
	 * */
	public static final Supplier<Method> ilocate(Class<?> klass, String name, Class... params) {
		return new Supplier<Method>() {
			
			private Method cache = null;
			
			@Override
			public synchronized final Method get() {
				return cache == null ? cache = located(klass, name, params) : cache;
			}
		};
	}
	
	/**
	 * Return in the future
	 * */
	public static final Supplier<Method> ilocate(String klass, String name, Class... params) {
		return new Supplier<Method>() {
			
			private Method cache = null;
			
			@Override
			public synchronized final Method get() {
				return cache == null ? cache = located(klass, name, params) : cache;
			}
		};
	}
	
	private static final Supplier<Field> MODIFIERS = ilocate(Field.class, "modifiers");
}
