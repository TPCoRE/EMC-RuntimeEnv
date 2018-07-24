package tpc.mc.emc.runtime.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * ASM Helper
 * */
public final class Injector {
	
	/**
	 * toArrayByte
	 * */
	public static final byte[] write(ClassNode cn) {
		return write(cn, 0);
	}
	
	/**
	 * toArrayByte
	 * */
	public static final byte[] write(ClassNode cn, int flags) {
		assert(cn != null);
		
		ClassWriter cw = new ClassWriter(flags);
		cn.accept(cw);
		
		return cw.toByteArray();
	}
	
	/**
	 * Create ClassNode from byte[]
	 * */
	public static final ClassNode read(byte[] b, int flags) {
		assert(b != null);
		
		ClassReader cr = new ClassReader(b);
		ClassNode cn = new ClassNode();
		cr.accept(cn, flags);
		
		return cn;
	}
	
	/**
	 * Create ClassNode from byte[]
	 * */
	public static final ClassNode read(byte[] b) {
		return read(b, 0);
	}
	
	/**
	 * Find a special method
	 * */
	public static final MethodNode find(Predicate<MethodNode> proxy, Iterator<MethodNode> mns) {
		assert(mns != null);
		assert(proxy != null);
		
		while(mns.hasNext()) {
			MethodNode mn = mns.next();
			
			if(proxy.test(mn)) return mn;
		}
		
		//no found
		return null;
	}
	
	/**
	 * Find a special method
	 * */
	public static final MethodNode find(Predicate<MethodNode> proxy, ClassNode cn) {
		assert(cn != null);
		
		return find(proxy, cn.methods.iterator());
	}
	
	/**
	 * Inject code at some special locations
	 * */
	public static final void inject(Predicate<AbstractInsnNode> proxy, InsnList ns, MethodNode accepter) {
		assert(accepter != null);
		
		inject(proxy, ns, accepter.instructions);
	}
	
	/**
	 * Inject code at some special locations
	 * */
	public static final void inject(Predicate<AbstractInsnNode> proxy, InsnList ns, InsnList accepter) {
		assert(proxy != null);
		assert(ns != null);
		assert(accepter != null);
		
		//roll
		for(int i = 0, l = accepter.size(); i < l; ++i) {
			AbstractInsnNode n = accepter.get(i);
			
			if(proxy.test(n)) accepter.insertBefore(n, copy(ns));
		}
	}
	
	/**
	 * Inject code at some special locations
	 * */
	public static final void inject(Predicate<MethodNode> proxy, Predicate<AbstractInsnNode> proxy0, InsnList ns, Iterator<MethodNode> mns) {
		assert(proxy != null);
		assert(proxy0 != null);
		assert(ns != null);
		assert(mns != null);
		
		//roll methods
		while(mns.hasNext()) {
			MethodNode mn = mns.next();
			
			if(proxy.test(mn)) inject(proxy0, ns, mn);
		}
	}
	
	/**
	 * Inject code at some special locations
	 * */
	public static final void inject(Predicate<MethodNode> proxy, Predicate<AbstractInsnNode> proxy0, InsnList ns, ClassNode cn) {
		assert(cn != null);
		
		inject(proxy, proxy0, ns, cn.methods.iterator());
	}
	
	/**
	 * Get a copy
	 * */
	public static final InsnList copy(InsnList src) {
		assert(src != null);
		
		InsnList r = new InsnList();
		Map<LabelNode, LabelNode> map = new HashMap<>();
		Iterator<AbstractInsnNode> iter;
		
		//roll codes and find labels
		iter = src.iterator();
		while(iter.hasNext()) {
			AbstractInsnNode n = iter.next();
			
			if(n.getType() == AbstractInsnNode.LABEL && !map.containsKey(n)) map.put((LabelNode) n, new LabelNode());
		}
		
		//clone
		iter = src.iterator();
		while(iter.hasNext()) r.add(iter.next().clone(map));
		
		return r;
	}
	
	public static final Predicate<AbstractInsnNode> RETURN = (x) -> x.getOpcode() == Opcodes.RETURN;
	public static final Predicate<MethodNode> INIT = (x) -> x.name.equals("<init>");
	public static final Predicate<MethodNode> CLINIT = (x) -> x.name.equals("<clinit>");
}
