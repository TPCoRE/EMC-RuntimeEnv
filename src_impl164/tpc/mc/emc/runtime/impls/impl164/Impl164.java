package tpc.mc.emc.runtime.impls.impl164;

import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.src.MathHelper;
import tpc.mc.emc.platform.standard.IMath;
import tpc.mc.emc.runtime.impls.IImpl;
import tpc.mc.emc.runtime.impls.impl164.mc.CodeRuler;
import tpc.mc.emc.runtime.util.Injector;

final class Impl164 extends IImpl {
	
	/**
	 * Internal Constructor
	 * */
	private Impl164() throws MalformedURLException {
		super("Mojang", "NullaDev", "0.0.0", new URL("https://mojang.com/"), new URL("https://github.com/TPCoRE/EMC-RuntimeEnv"));
	}
	
	@Override
	public IMath math() {
		return new IMath() {
			
			@Override
			public float sin(float i) {
				return MathHelper.sin(i);
			}
			
			@Override
			public float cos(float i) {
				return MathHelper.cos(i);
			}
		};
	}
	
	@Override
	public byte[] rule(byte[] klass, String name) {
		return CodeRuler.rule(klass, name);
	}
}
