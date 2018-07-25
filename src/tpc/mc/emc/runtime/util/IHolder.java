package tpc.mc.emc.runtime.util;

/**
 * It hold a value
 * */
public interface IHolder<V> {
	
	public void set(V newval);
	public V get();
	
	/**
	 * Helper
	 * */
	public default IHolder<V> iset(V newval) {
		this.set(newval);
		
		return this;
	}
	
	/**
	 * Thread safe depends on volatile
	 * */
	public static class Volatile<V> implements IHolder<V> {
		
		public volatile V val;
		
		@Override
		public void set(V newval) {
			this.val = newval;
		}
		
		@Override
		public V get() {
			return this.val;
		}
	}
	
	/**
	 * Thread safe depends on sync
	 * */
	public static class Sync<V> implements IHolder<V> {
		
		protected V val;
		
		@Override
		public synchronized void set(V newval) {
			this.val = newval;
		}
		
		@Override
		public synchronized V get() {
			return this.val;
		}
	}
	
	/**
	 * Thread unsafe
	 * */
	public static class Single<V> implements IHolder<V> {
		
		public V val;
		
		@Override
		public void set(V newval) {
			this.val = newval;
		}
		
		@Override
		public V get() {
			return this.val;
		}
	}
}
