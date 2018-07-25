package tpc.mc.emc.runtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import tpc.mc.emc.platform.standard.EMC;
import tpc.mc.emc.platform.standard.IMath;
import tpc.mc.emc.runtime.impls.IImpl;
import tpc.mc.emc.runtime.util.Reflect;
import tpc.mc.emc.runtime.util.Selector;
import tpc.mc.emc.runtime.util.Shutdown;

/**
 * Boost EMC RuntimeEnv
 * */
public final class Bootstrap {
	
	/**
	 * Start method invoked by Instrumentation API
	 * */
	public static final void premain(String arg, Instrumentation inst) throws Throwable {
		System.out.println("EMC RuntimeEnv Start! With EMC " + EMC.current());
		
		//INIT SELECTOR
		Reflect.located(Selector.class, "SELECTED").set(null, new Selector().select(arg != null ? arg.split(",") : null));
		IImpl current = Selector.select();
		
		System.out.println("EMC Platform Implement " + current);
		
		//INIT IMATH
		Reflect.located(IMath.class, "IMPL").set(null, current.math());
		
		//REGISTER CLASS TRANSFORMER
		inst.addTransformer(new ClassFileTransformer() {
			
			@Override
			public byte[] transform(ClassLoader arg0, String arg1, Class<?> arg2, ProtectionDomain arg3, byte[] arg4) throws IllegalClassFormatException {
				try {
					return current.rule(arg4, arg1);
				} catch(Throwable e) {
					e.printStackTrace();
					
					//EXIT
					Shutdown.exit(1);
					return null;
				}
			}
		}, true);
		
		//RETRANSFORM
		Class[] cs = inst.getAllLoadedClasses();
		for(Class c : cs) 
			if(inst.isModifiableClass(c) && !c.getName().startsWith("java.lang.invoke.Lambda")) inst.retransformClasses(c);
		
		/*
		 * FOR '!c.getName().startsWith("java.lang.invoke.Lambda")'
		 * TRY THE CODE BELOW
		 * 
		 * static void premain(String arg, Instrumentation inst) {
		 * 		
		 * 		A.C();
		 * 		
		 * 		inst.retransformClasses(A.class); //THIS WILL CRASH THE JVM, AND 'inst.isModifiableClass(c)' RETURN TRUE!
		 * }
		 * 
		 * static class A {
		 * 		
		 * 		static Supplier B = () -> null;
		 * 		
		 * 		static void C() {}
		 * }
		 * 
		 * FINALLY I FIND IF THERE IS A FILTER '!c.getName().startsWith("java.lang.invoke.Lambda")', IT WILL RUN SAFELY
		 * */
	}
}
