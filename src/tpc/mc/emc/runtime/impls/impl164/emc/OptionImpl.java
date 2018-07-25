package tpc.mc.emc.runtime.impls.impl164.emc;

import net.minecraft.src.EntityPlayer;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.platform.standard.IOption;

/**
 * IOption FOR 1.6.4
 * */
public final class OptionImpl extends IOption {
	
	public final EntityPlayer player;
	public final Object renderPlayer;
	
	/**
	 * Public Use, Client&Server
	 * */
	public OptionImpl(EntityPlayer player) {
		this(player, null);
	}
	
	/**
	 * Public Use, Client&Server&Client-model
	 * */
	public OptionImpl(EntityPlayer player, Object model) {
		assert(player != null);
		
		this.player = player;
		this.renderPlayer = model;
	}
	
	@Override
	public boolean client() {
		return this.player.worldObj.isRemote;
	}
	
	@Override
	public boolean model() {
		return this.renderPlayer != null;
	}
	
	@Override
	public ContextImpl alloc() {
		return new ContextImpl(this);
	}
	
	@Override
	public IOption clone() {
		return new OptionImpl(this.player, this.renderPlayer);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(!obj.getClass().equals(OptionImpl.class)) return false;
		
		OptionImpl opt = (OptionImpl) obj;
		
		if(opt.client() != this.client()) return false;
		if(opt.renderPlayer != this.renderPlayer) return false;
		if(!this.player.equals(opt.player)) return false;
		
		//pass
		return true;
	}
}
