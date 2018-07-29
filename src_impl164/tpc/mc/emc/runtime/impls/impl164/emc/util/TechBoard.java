package tpc.mc.emc.runtime.impls.impl164.emc.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.src.EntityPlayer;
import tpc.mc.emc.Stepable;
import tpc.mc.emc.platform.standard.IOption;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.tech.ITech;

/**
 * Tech Board, notice that it is thread-unsafe
 * */
public final class TechBoard implements Serializable, Iterable<ITech>, Cloneable {
	
	private Map<QuickSlot, ITech> quick = new EnumMap<>(QuickSlot.class);
	private Map<Long, ITech> status = new HashMap<>();
	
	/**
	 * Get the tech in the quick slot
	 * */
	public ITech quick(QuickSlot slot) {
		assert(slot != null);
		
		return this.quick.get(slot);
	}
	
	/**
	 * Set, return the old
	 * */
	public ITech quick(QuickSlot slot, ITech tech) {
		Objects.requireNonNull(slot);
		
		return this.quick.put(slot, tech);
	}
	
	/**
	 * Check if the tech is available
	 * */
	public boolean check(ITech tech) {
		assert(tech != null);
		
		return this.status.containsKey(tech.identifier());
	}
	
	/**
	 * Change the available status of the tech
	 * */
	public void toggle(ITech tech, boolean status) {
		Objects.requireNonNull(tech);
		
		if(status) this.status.put(tech.identifier(), tech);
		else this.status.remove(tech.identifier());
	}
	
	/**
	 * Flush the data from the given exporter, return itself
	 * */
	public TechBoard accept(TechBoard exporter) {
		assert(exporter != null);
		
		this.quick = new EnumMap<>(exporter.quick);
		this.status = new HashMap<>(exporter.status);
		
		return this;
	}
	
	/**
	 * Have a look, notice that you cant change the data in it, return itself
	 * */
	public TechBoard peek(TechBoard peeked) {
		assert(peeked != null);
		
		this.quick = Collections.unmodifiableMap(peeked.quick);
		this.status = Collections.unmodifiableMap(peeked.status);
		
		return this;
	}
	
	/**
	 * Clear the peeker status, and keep the peeked data
	 * */
	public TechBoard peeked() {
		return this.accept(this);
	}
	
	/**
	 * Peek all avails
	 * */
	@Override
	public Iterator<ITech> iterator() {
		return Collections.unmodifiableCollection(this.status.values()).iterator();
	}
	
	/**
	 * Check if the two are the same
	 * */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(!obj.getClass().equals(TechBoard.class)) return false;
		
		TechBoard other = (TechBoard) obj;
		
		return other.quick.equals(this.quick) && this.status.equals(other.status);
	}
	
	/**
	 * Get the hashcode of the board
	 * */
	@Override
	public int hashCode() {
		return this.quick.hashCode() ^ this.status.hashCode();
	}
	
	/**
	 * Get a copy
	 * */
	@Override
	public TechBoard clone() {
		return new TechBoard().accept(this);
	}
}
