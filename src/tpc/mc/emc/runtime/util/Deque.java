package tpc.mc.emc.runtime.util;

import java.util.function.Predicate;

/**
 * A thread-safe unbound deque
 * */
public final class Deque<E> {
	
	private final Node first;
	private final Node last;
	
	/**
	 * Create an instance with no data
	 * */
	public Deque() {
		this.first = new Node();
		this.last = new Node();
		
		//INIT
		this.first.next(this.last);
		this.last.prev(this.first);
	}
	
	/**
	 * Add at the head, null means delete it, if the deque is empty it will do nothing, return itself
	 * */
	public final synchronized Deque<E> head(E elem) {
		Node first = this.first;
		Node n;
		
		if(elem == null) {
			if(this.empty()) return this;
			
			first.next = n = first.next.next;
			n.prev = first;
		} else {
			n = first.next;
			first.next = n.prev = new Node().val(elem).next(n).prev(first);
		}
		
		return this;
	}
	
	/**
	 * Add the tail, null means delete it, if the deque is empty it will do nothing, return itself
	 * */
	public final synchronized Deque<E> tail(E elem) {
		Node last = this.last;
		Node n;
		
		if(elem == null) {
			if(this.empty()) return this;
			
			last.prev = n = last.prev.prev;
			n.next = last;
		} else {
			n = last.prev;
			last.prev = n.next = new Node().val(elem).next(last).prev(n);
		}
		
		return this;
	}
	
	/**
	 * Have a look at the head of the deque, null for empty
	 * */
	public final synchronized E head() {
		if(this.empty()) return null;
		
		return this.first.next.val;
	}
	
	/**
	 * Have a look at the tail of the deque, null for empty
	 * */
	public final synchronized E tail() {
		if(this.empty()) return null;
		
		return this.last.prev.val;
	}
	
	/**
	 * Check if it is empty
	 * */
	public final synchronized boolean empty() {
		return this.first.next == this.last;
	}
	
	/**
	 * Clean all data, return itself
	 * */
	public final synchronized Deque<E> clear() {
		this.first.next(this.last);
		this.last.prev(this.first);
		
		return this;
	}
	
	/**
	 * Across the deque
	 * */
	public final synchronized Deque<E> check(Predicate<E> proxy) {
		assert(proxy != null);
		
		Node n = this.first;
		Node last = this.last;
		
		while(n != last) {
			n = n.next;
			
			if(proxy.test(n.val)) return this;
		}
		
		return this;
	}
	
	/**
	 * Box an element, Internal
	 * */
	private final class Node {
		
		E val;
		Node next;
		Node prev;
		
		private final Node next(Node ne) {
			this.next = ne;
			return this;
		}
		
		private final Node prev(Node ne) {
			this.prev = ne;
			return this;
		}
		
		private final Node val(E elem) {
			this.val = elem;
			return this;
		}
	}
}
