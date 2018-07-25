package tpc.mc.emc.runtime.impls.impl164;

import net.minecraft.src.MathHelper;
import tpc.mc.emc.platform.standard.IMath;
import tpc.mc.emc.runtime.impls.IImpl;
import tpc.mc.emc.runtime.impls.impl164.mc.CodeRuler;
import tpc.mc.emc.runtime.util.Injector;

final class Impl164 extends IImpl {
	
	/**
	 * Internal Constructor
	 * */
	private Impl164() {
		super("Mojang", "NullaDev", "0.0.0");
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
