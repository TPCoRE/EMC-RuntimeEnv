package tpc.mc.emc.runtime.impls.impl164.mc;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.RenderManager;
import tpc.mc.emc.Stepable;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.StepableImpl;
import tpc.mc.emc.runtime.util.Deque;
import tpc.mc.emc.runtime.util.Reflect;

/**
 * Hooks
 * */
public final class Hooked {
	
	private static final Supplier<Field> MODELMARK = Reflect.ilocate("net.minecraft.src.AbstractClientPlayer", "EMC_164_MODELMARK");
	private static final Supplier<Field> COMMOSTEP = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_164_COMMOSTEP");
	private static final Supplier<Field> MODELSTEP = Reflect.ilocate("net.minecraft.src.AbstractClientPlayer", "EMC_164_MODELSTEP");
	
	/**
	 * Update stepables
	 * */
	public static final void walk(EntityPlayer player) throws Throwable {
		final boolean model;
		OptionImpl opt = new OptionImpl(player, (model = check0()) ? RenderManager.instance.getEntityRenderObject(haven0(player)) : null);
		Deque<StepableImpl> deque = model ? (Deque<StepableImpl>) MODELSTEP.get().get(player) : (Deque<StepableImpl>) COMMOSTEP.get().get(player);
		
		//check model duplicate
		if(model) {
			int ticked = player.ticksExisted;
			
			//lock the player(model concurrent?)
			synchronized(player) {
				if(MODELMARK.get().getInt(player) == ticked) return;
				else MODELMARK.get().setInt(player, ticked);
			}
		}
		
		//lock the deque
		synchronized(deque) {
			StepableImpl mark = null;
			StepableImpl head;
			
			//roll
			while((head = deque.head()) != mark) {
				if(!head.opted()) head = new StepableImpl(head.tech(), opt);
				StepableImpl nextstep = (StepableImpl) head.next();
				
				deque.head(null);
				if(nextstep != null) {
					if(mark == null) mark = nextstep;
					
					deque.tail(nextstep);
				}
			}
		}
	}
	
	/**
	 * The haven that can avoid compiler optimization
	 * */
	private static final Entity haven0(Object obj) {
		return (Entity) obj;
	}
	
	/**
	 * Check if it is come from Model
	 * */
	private static final boolean check0() {
		StackTraceElement[] arr = Thread.currentThread().getStackTrace();
		
		//trace
		for(int i = 0, l = arr.length; i < l; ++i) {
			if(arr[i].getClassName().equals("net.minecraft.src.ModelBiped")) return true;
		}
		
		//no found
		return false;
	}
}
