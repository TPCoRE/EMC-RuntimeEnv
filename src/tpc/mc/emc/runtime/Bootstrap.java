package tpc.mc.emc.runtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import tpc.mc.emc.platform.standard.IMath;
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
		Reflect.access(Reflect.located(Selector.class, "USER")).set(null, arg);
		Impl impl = Selector.impl();
		
		//set IMath INSTANCE
		Reflect.access(Reflect.located(IMath.class, "INSTANCE")).set(null, impl.math());
		
		//register transformer
		inst.addTransformer(new ClassFileTransformer() {
			
			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				try {
					return impl.rule(classfileBuffer, className);
				} catch(Throwable e) {
					e.printStackTrace();
					
					//exit the vm
					Shutdown.exit(1);
					return null;
				}
			}
		}, true);
		
		//retransform
		Class[] cs = inst.getAllLoadedClasses();
		for(Class c : cs)
			if(inst.isModifiableClass(c)) inst.retransformClasses(c);
	}
}
