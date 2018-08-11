package tpc.mc.emc.runtime.impls.impl164.emc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import tpc.mc.emc.tech.Technique;

/**
 * Tech Board
 * */
public final class TechBoard implements Serializable, Iterable<Technique>, Cloneable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<QuickSlot, Technique> quick = new ConcurrentHashMap<>();
	private Map<UUID, Technique> status = new ConcurrentHashMap<>();
	
	/**
	 * Get the tech in the quick slot
	 * */
	public Technique quick(QuickSlot slot) {
		assert(slot != null);
		
		return this.quick.get(slot);
	}
	
	/**
	 * Set, return the old
	 * */
	public Technique quick(QuickSlot slot, Technique tech) {
		Objects.requireNonNull(slot);
		
		return tech == null ? this.quick.remove(slot) : this.quick.put(slot, tech);
	}
	
	/**
	 * Get the tech by id
	 * */
	public Technique deal(UUID uuid) {
		assert(uuid != null);
		
		return this.status.get(uuid);
	}
	
	/**
	 * Check if the tech is available
	 * */
	public boolean check(Technique tech) {
		assert(tech != null);
		
		return this.status.containsKey(tech.identifier());
	}
	
	/**
	 * Change the available status of the tech
	 * */
	public void toggle(Technique tech, boolean status) {
		Objects.requireNonNull(tech);
		
		if(status) this.status.put(tech.identifier(), tech);
		else this.status.remove(tech.identifier());
	}
	
	/**
	 * Flush the data from the given exporter, return itself
	 * */
	public TechBoard accept(TechBoard exporter) {
		assert(exporter != null);
		
		this.quick = new ConcurrentHashMap<>(exporter.quick);
		this.status = new ConcurrentHashMap<>(exporter.status);
		
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
	public Iterator<Technique> iterator() {
		return Collections.unmodifiableCollection(this.status.values()).iterator();
	}
	
	/**
	 * Peek
	 * */
	public Collection<Technique> avail() {
		return Collections.unmodifiableCollection(this.status.values());
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
