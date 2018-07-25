package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.src.EntityPlayer;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.tech.Board;
import tpc.mc.emc.tech.Pool;

/**
 * Teches lost and get map
 * */
public final class LGMap implements Cloneable {
	
	private static final Board TMP = new Board();
	
	final Map<Pool, Boolean> internal;
	
	/**
	 * Public
	 * */
	public LGMap() {
		this(new HashMap<>());
	}
	
	/**
	 * Internal
	 * */
	private LGMap(Map<Pool, Boolean> map) {
		assert(map != null);
		
		this.internal = map;
	}
	
	/**
	 * Set the LG status of the given tech
	 * */
	public synchronized LGMap set(Pool tech, boolean status) {
		assert(tech != null);
		
		this.internal.put(tech, status);
		return this;
	}
	
	/**
	 * Check the LG status of the given tech
	 * */
	public synchronized boolean check(Pool tech) {
		assert(tech != null);
		
		return this.internal.get(tech);
	}
	
	@Override
	public synchronized LGMap clone() {
		return new LGMap(new HashMap<>(this.internal));
	}
	
	/**
	 * Whether the LGMap has data
	 * */
	public synchronized boolean empty() {
		return this.internal.isEmpty();
	}
	
	/**
	 * Notice that the change will only be commit in local memory, so it may make some error, use it carefully
	 * */
	synchronized void flush(EntityPlayer player) {
		IContext conte = new OptionImpl(player).alloc();
		Board board = conte.iexport(TMP);
		
		//toggle the tech
		Iterator<Map.Entry<Pool, Boolean>> iter = this.internal.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Pool, Boolean> entry = iter.next();
			
			board.toggle(entry.getKey(), entry.getValue());
		}
		
		//close the context
		conte.accept(board);
		conte.close();
	}
}
