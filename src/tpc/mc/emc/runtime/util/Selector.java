package tpc.mc.emc.runtime.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import tpc.mc.emc.err.UnsupportedException;
import tpc.mc.emc.runtime.impls.IImpl;

/**
 * RuntimeEnv selector
 * */
public final class Selector {
	
	private final Collection<String> suggs;
	
	/**
	 * Create a selector without any suggestions
	 * */
	public Selector() {
		this(new String[0]);
	}
	
	/**
	 * Create a selector with some suggestions
	 * */
	public Selector(String... suggs) {
		assert(suggs != null);
		
		if(suggs.length == 0) this.suggs = Collections.EMPTY_SET;
		else {
			this.suggs = new HashSet<>(Arrays.asList(suggs.clone()));
			this.suggs.removeIf(INVALIDS);
		}
	}
	
	/**
	 * Select an implement with more suggestions
	 * */
	public final IImpl select(String... extsuggs) {
		assert(extsuggs != null);
		
		Collection<String> trace = new LinkedHashSet<>();
		Collection<String> tmp;
		IImpl result;
		
		//collect data from supports
		tmp = SUPPCACHE;
		trace.addAll(tmp);
		result = select0(tmp);
		
		//collect data from suggs
		if(result == null) {
			tmp = this.suggs;
			trace.addAll(tmp);
			result = select0(tmp);
		}
		
		//collect data from extsuggs
		if(result == null && extsuggs.length != 0) {
			tmp = new HashSet<>(Arrays.asList(extsuggs.clone()));
			tmp.removeIf(INVALIDS);
			trace.addAll(tmp);
			result = select0(tmp);
		}
		
		//check
		if(result == null) {
			if(trace.isEmpty()) throw new UnsupportedException("Empty MCVersion Unsupported!");
			else throw new UnsupportedException("Unsupported MCVersions ".concat(trace.toString()));
		}
		
		return result;
	}
	
	/**
	 * Get the implement that selected by Bootstrap
	 * */
	public static final IImpl select() {
		return SELECTED;
	}
	
	/**
	 * Select an implement with suggestions
	 * */
	private static final IImpl select0(Collection<String> suggestions) {
		if(suggestions.isEmpty()) return null;
		
		//prepare
		Iterator<String> iter = suggestions.iterator();
		IImpl result = null;
		String version = null;
		
		//roll to find
		while(iter.hasNext()) {
			version = iter.next().trim();
			
			//try get
			try {
				String internalVersion = version.replace(".", "");
				
				//find constructor
				Constructor con = Class.forName("tpc.mc.emc.runtime.impls.impl".concat(internalVersion).concat(".Impl").concat(internalVersion)).getDeclaredConstructor();
				con.setAccessible(true);
				
				//init
				result = (IImpl) con.newInstance();
				break; //break the loop
			} catch(Throwable e) {}
		}
		
		//config info
		if(result != null) {
			try {
				Reflect.located(IImpl.class, "version_mc").set(result, version);
			} catch(Throwable e) {
				throw new BootstrapMethodError("Unknown Error", e);
			}
		}
		
		//return the result
		return result;
	}
	
	/**
	 * Collect support data
	 * */
	private static final Collection<String> support() {
		Collection<String> data = new LinkedHashSet<>();
		String ver;
		
		//collect data
		if((ver = support_forge()) != null) data.add(ver);
		
		data.removeIf(INVALIDS);
		return data;
	}
	
	/**
	 * Get the mcversion from forge API
	 * */
	private static final String support_forge() {
		try {
			return (String) Class.forName("net.minecraftforge.common.MinecraftForge").getField("MC_VERSION").get(null);
		} catch(Throwable e) {
			return null;
		}
	}
	
	private static final IImpl SELECTED = null; //SET BY BOOTSTRAP
	private static final Predicate<String> INVALIDS = (x) -> x == null || x.trim().isEmpty();
	private static final Collection<String> SUPPCACHE = support();
}
