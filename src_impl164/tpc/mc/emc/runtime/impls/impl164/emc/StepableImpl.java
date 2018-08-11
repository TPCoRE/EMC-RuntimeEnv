package tpc.mc.emc.runtime.impls.impl164.emc;

import tpc.mc.emc.Stepable;
import tpc.mc.emc.platform.standard.IOption;
import tpc.mc.emc.tech.Technique;

/**
 * Stepable Entry
 * */
public final class StepableImpl implements Stepable {
	
	private final Technique tech;
	private Stepable internal;
	
	/**
	 * Constructor
	 * */
	public StepableImpl(IOption opt, Technique tech) {
		assert(tech != null);
		
		this.tech = tech;
		this.internal = tech.tack(opt);
	}
	
	public Technique tech() {
		return this.tech;
	}
	
	@Override
	public synchronized Stepable next() {
		Stepable internal = this.internal;
		
		if(internal != null) {
			this.internal = internal.next();
			return this;
		} else return null;
	}
}
