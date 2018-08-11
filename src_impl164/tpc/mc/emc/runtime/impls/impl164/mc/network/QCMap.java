package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.src.EntityPlayer;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.QuickSlot;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;
import tpc.mc.emc.tech.Technique;

public final class QCMap implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final TechBoard TMP = new TechBoard();
	
	private final EnumMap<QuickSlot, Technique> internal;
	
	/**
	 * Public
	 * */
	public QCMap() {
		this(new EnumMap<>(QuickSlot.class));
	}
	
	/**
	 * Internal
	 * */
	private QCMap(EnumMap<QuickSlot, Technique> map) {
		assert(map != null);
		
		this.internal = map;
	}
	
	/**
	 * Set the LG status of the given tech
	 * */
	public synchronized QCMap set(QuickSlot slot, Technique tech) {
		assert(slot != null);
		
		this.internal.put(slot, tech);
		return this;
	}
	
	/**
	 * Check the LG status of the given tech
	 * */
	public synchronized Technique get(QuickSlot slot) {
		assert(slot != null);
		
		return this.internal.get(slot);
	}
	
	@Override
	public synchronized QCMap clone() {
		return new QCMap(this.internal.clone());
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
		Iterator<Map.Entry<QuickSlot, Technique>> iter = this.internal.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<QuickSlot, Technique> entry = iter.next();
			
			board.quick(entry.getKey(), entry.getValue());
		}
		
		//close the context
		conte.accept(board);
		conte.close();
	}
}
