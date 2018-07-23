package tpc.mc.emc.runtime.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Reflect API Helper
 * */
public final class Reflect {
	
	/**
	 * Accessible a object
	 * */
	public static final <T extends AccessibleObject> T access(T access) {
		try {
			if(!access.isAccessible()) access.setAccessible(true);
			else if(access instanceof Field) {
				Field f = (Field) access;
				
				//try access modifiers
				try {
					Field modifiers = Field.class.getDeclaredField("modifiers");
					modifiers.setAccessible(true);
					modifiers.set(f, f.getModifiers() & ~Modifier.FINAL);
				} catch(Throwable e) {}
			}
			
			return access;
		} catch(java.lang.Throwable e) {}
		
		return null;
	}
	
	/**
	 * Get a method safely
	 * */
	public static final Method located(Class<?> klass, String name, Class... params) {
		try {
			Method m;
			
			try {
				m = klass.getMethod(name, params);
			} catch(java.lang.Throwable e) {
				m = access(klass.getDeclaredMethod(name, params));
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
				f = access(klass.getDeclaredField(name));
			}
			
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
	public static final SafeCallable<Field> ilocate(Class<?> klass, String name) {
		return new SafeCallable<Field>() {
			
			private Field cache = null;
			
			@Override
			public Field call() {
				return cache == null ? cache = located(klass, name) : cache;
			}
		};
	}
	
	/**
	 * Return in the future
	 * */
	public static final SafeCallable<Field> ilocate(String klass, String name) {
		return new SafeCallable<Field>() {
			
			private Field cache = null;
			
			@Override
			public Field call() {
				return cache == null ? cache = located(klass, name) : cache;
			}
		};
	}
	
	/**
	 * Return in the future
	 * */
	public static final SafeCallable<Method> ilocate(Class<?> klass, String name, Class... params) {
		return new SafeCallable<Method>() {
			
			private Method cache = null;
			
			@Override
			public Method call() {
				return cache == null ? cache = located(klass, name, params) : cache;
			}
		};
	}
	
	/**
	 * Return in the future
	 * */
	public static final SafeCallable<Method> ilocate(String klass, String name, Class... params) {
		return new SafeCallable<Method>() {
			
			private Method cache = null;
			
			@Override
			public Method call() {
				return cache == null ? cache = located(klass, name, params) : cache;
			}
		};
	}
}
