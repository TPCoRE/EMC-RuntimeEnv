package tpc.mc.emc.runtime.impls.impl164.emc;

import net.minecraft.src.EntityPlayer;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.platform.standard.IOption;

/**
 * For 1.6.4
 * */
public final class OptionImpl extends IOption {
	
	public final EntityPlayer player;
	public final Object renderPlayer;
	
	/**
	 * Create an instance
	 * */
	public OptionImpl(EntityPlayer player) {
		this(player, null);
	}
	
	/**
	 * Create an opt with a render
	 * */
	public OptionImpl(EntityPlayer player, Object render) {
		assert(player != null);
		
		this.player = player;
		this.renderPlayer = render;
	}
	
	@Override
	public boolean model() {
		return this.renderPlayer != null;
	}
	
	@Override
	public IContext alloc() {
		return null; //TODO
	}
	
	@Override
	public OptionImpl clone() {
		return new OptionImpl(this.player, this.renderPlayer);
	}
}
