package tpc.mc.emc.runtime.impls.impl164.emc;

import net.minecraft.src.EntityPlayer;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.platform.standard.IOption;

/**
 * IOption FOR 1.6.4
 * */
public final class OptionImpl implements IOption, Cloneable {
	
	final EntityPlayer player;
	
	/**
	 * Public Use
	 * */
	public OptionImpl(EntityPlayer player) {
		assert(player != null);
		
		this.player = player;
	}
	
	@Override
	public boolean model() {
		return this.player.worldObj.isRemote;
	}
	
	@Override
	public ContextImpl alloc() {
		return new ContextImpl(this);
	}
	
	@Override
	public OptionImpl clone() {
		return new OptionImpl(this.player);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(!obj.getClass().equals(OptionImpl.class)) return false;
		
		OptionImpl opt = (OptionImpl) obj;
		
		return this.player.equals(opt.player) && this.model() == opt.model();
	}
}
