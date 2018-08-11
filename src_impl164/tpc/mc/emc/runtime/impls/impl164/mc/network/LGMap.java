package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.src.EntityPlayer;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;
import tpc.mc.emc.tech.Technique;

/**
 * Teches lost and get map
 * */
public final class LGMap implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final TechBoard TMP = new TechBoard();
	
	private final Map<Technique, Boolean> internal;
	
	/**
	 * Public
	 * */
	public LGMap() {
		this(new HashMap<>());
	}
	
	/**
	 * Internal
	 * */
	private LGMap(Map<Technique, Boolean> map) {
		assert(map != null);
		
		this.internal = map;
	}
	
	/**
	 * Set the LG status of the given tech
	 * */
	public synchronized LGMap set(Technique tech, boolean status) {
		assert(tech != null);
		
		this.internal.put(tech, status);
		return this;
	}
	
	/**
	 * Check the LG status of the given tech
	 * */
	public synchronized boolean check(Technique tech) {
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
	void flush(EntityPlayer player) {
		ContextImpl conte = new OptionImpl(player).alloc();
		TechBoard board = TMP;
		conte.export(board);
		
		//toggle the tech
		Iterator<Map.Entry<Technique, Boolean>> iter = this.internal.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Technique, Boolean> entry = iter.next();
			
			board.toggle(entry.getKey(), entry.getValue());
		}
		
		//close the context
		conte.accept(board);
		conte.close();
	}
}
