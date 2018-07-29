package tpc.mc.emc.runtime.impls.impl164.emc.util;

import tpc.mc.emc.Stepable;
import tpc.mc.emc.platform.standard.IOption;
import tpc.mc.emc.tech.ITech;

/**
 * Box of {@link Stepable}
 * */
public final class StepableImpl implements Stepable {
	
	private final ITech tech;
	private Stepable internal;
	
	/**
	 * Create an impl
	 * */
	public StepableImpl(ITech tech) {
		assert(tech != null);
		
		this.tech = tech;
	}
	
	/**
	 * Create an impl
	 * */
	public StepableImpl(ITech tech, IOption opt) {
		this(tech);
		
		this.internal = tech.tack(opt);
	}
	
	/**
	 * Get the next
	 * */
	@Override
	public Stepable next() {
		Stepable internal = this.internal;
		
		return internal == null ? null : (this.internal = internal.next());
	}
	
	/**
	 * Get the content tech
	 * */
	public ITech tech() {
		return this.tech;
	}
}
