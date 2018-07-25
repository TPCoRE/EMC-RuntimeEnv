package tpc.mc.emc.runtime.impls.impl164.emc;

import tpc.mc.emc.Stepable;
import tpc.mc.emc.tech.Pool;

/**
 * Stepable Entry
 * */
public final class StepableImpl implements Stepable {
	
	private Stepable internal;
	private boolean inited;
	private final Pool tech;
	private final OptionImpl opt;
	
	/**
	 * Public Constructor
	 * */
	public StepableImpl(Pool tech) {
		this(tech, null);
	}
	
	/**
	 * Public Constructor
	 * */
	public StepableImpl(Pool tech, OptionImpl opt) {
		assert(tech != null);
		
		this.tech = tech;
		this.opt = opt;
	}
	
	@Override
	public synchronized Stepable next() {
		if(!this.inited) {
			assert(this.opt != null);
			
			this.internal = this.tech.tack(this.opt);
			this.inited = true;
		}
		
		if(this.internal != null) {
			if((this.internal = this.internal.next()) == null) return null;
		} else return null;
		
		return this;
	}
	
	public Pool tech() {
		return this.tech;
	}
	
	public boolean opted() {
		return this.opt != null;
	}
}
