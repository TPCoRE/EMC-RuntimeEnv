package tpc.mc.emc.runtime.impls.impl164.mc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import tpc.mc.emc.runtime.util.Injector;

public final class CodeRuler {
	
	/**
	 * Ruler code for EMC
	 * */
	public static final byte[] rule(byte[] klass, String name) {
		if(name == null || klass == null) return null;
		ClassNode cn = null;
		
		//special handle
		switch(name) {
		case "net/minecraft/src/ModelBiped": //这里调用client-model的stepable
			cn = check(Injector.read(klass), name);
			MethodNode mn = Injector.find((x) -> x.name.equals("setRotationAngles") && x.desc.equals("(FFFFFFLnet/minecraft/src/Entity;)V"), cn);
			
			//check&prepare
			assert(mn != null);
			InsnList ns = new InsnList();
			
			//coding, 调用stepable
			ns.add(new VarInsnNode(Opcodes.ALOAD, 7));
			ns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/src/EntityPlayer"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/Hooked", "walk", "(Lnet/minecraft/src/EntityPlayer;)V"));
			
			//inject
			Injector.inject(Injector.RETURN, ns, mn);
			break;
		case "net/minecraft/src/EntityPlayer": //这里储存，调用client|server的stepable，添加context lock, tech board
			cn = check(Injector.read(klass), name);
			mn = Injector.find((x) -> x.name.equals("onUpdate") && x.desc.equals("()V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding, 调用stepable
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/Hooked", "walk", "(Lnet/minecraft/src/EntityPlayer;)V"));
			mn.instructions.insert(ns); //inject
			
			//coding <init>* for new field
			ns = new InsnList();
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new InsnNode(Opcodes.DUP));
			//for common stepable
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/runtime/util/Deque"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/runtime/util/Deque", "<init>", "()V"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/EntityPlayer", "EMC_164_COMMOSTEP", "Ltpc/mc/emc/runtime/util/Deque;"));
			//for context lock
			ns.add(new TypeInsnNode(Opcodes.NEW, "java/util/concurrent/atomic/AtomicBoolean"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new InsnNode(Opcodes.ICONST_0));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/concurrent/atomic/AtomicBoolean", "<init>", "(Z)V"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/EntityPlayer", "EMC_164_CONTELOCK", "Ljava/util/concurrent/atomic/AtomicBoolean;"));
			//for techboard
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/tech/Board"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new InsnNode(Opcodes.ICONST_0));
			ns.add(new TypeInsnNode(Opcodes.ANEWARRAY, "tpc/mc/emc/tech/Pool"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/tech/Board", "<init>", "([Ltpc/mc/emc/tech/Pool;)V"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/EntityPlayer", "EMC_164_TECHBOARD", "Ltpc/mc/emc/tech/Board;"));
			
			//inject
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_164_COMMOSTEP", "Ltpc/mc/emc/runtime/util/Deque;", null, null));
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_164_CONTELOCK", "Ljava/util/concurrent/atomic/AtomicBoolean;", null, null));
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_164_TECHBOARD", "Ltpc/mc/emc/tech/Board;", null, null));
			Injector.inject(Injector.INIT, Injector.RETURN, ns, cn);
			break;
		case "net/minecraft/src/AbstractClientPlayer": //这里储存client-model的stepable
			cn = check(Injector.read(klass), name);
			ns = new InsnList();
			
			//coding for model stepable
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/runtime/util/Deque"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/runtime/util/Deque", "<init>", "()V"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/AbstractClientPlayer", "EMC_164_MODELSTEP", "Ltpc/mc/emc/runtime/util/Deque;"));
			
			//inject
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_164_MODELSTEP", "Ltpc/mc/emc/runtime/util/Deque;", null, null));
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "EMC_164_MODELMARK", "I", null, new Integer(0)));
			Injector.inject(Injector.INIT, Injector.RETURN, ns, cn);
			break;
		case "net/minecraft/src/Packet": //这里注册client&server之间的通信包
			cn = check(Injector.read(klass), name);
			mn = Injector.find(Injector.CLINIT, cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding PacketAct
			ns.add(new LdcInsnNode(new Integer(-1)));
			ns.add(new InsnNode(Opcodes.ICONST_1));
			ns.add(new InsnNode(Opcodes.ICONST_1));
			ns.add(new LdcInsnNode(Type.getType("Ltpc/mc/emc/runtime/impls/impl164/mc/network/PacketAct;")));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/src/Packet", "addIdClassMapping", "(IZZLjava/lang/Class;)V"));
			
			//coding PacketLG
			ns.add(new LdcInsnNode(new Integer(-2)));
			ns.add(new InsnNode(Opcodes.ICONST_0));
			ns.add(new InsnNode(Opcodes.ICONST_1));
			ns.add(new LdcInsnNode(Type.getType("Ltpc/mc/emc/runtime/impls/impl164/mc/network/PacketLG;")));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/src/Packet", "addIdClassMapping", "(IZZLjava/lang/Class;)V"));
			
			//inject
			Injector.inject(Injector.RETURN, ns, mn);
			break;
		case "net/minecraft/src/GameSettings": //这里注册头部，身体，腿部科技的绑定按键
			cn = check(Injector.read(klass), name);
			ns = new InsnList();
			
			//add to keybingdings
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/GameSettings", "keyBindings", "[Lnet/minecraft/src/KeyBinding;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/client/InputHandler", "handle", "([Lnet/minecraft/src/KeyBinding;)[Lnet/minecraft/src/KeyBinding;"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/GameSettings", "keyBindings", "[Lnet/minecraft/src/KeyBinding;"));
			
			//inject
			Injector.inject(Injector.INIT, Injector.RETURN, ns, cn);
			break;
		case "net/minecraft/src/MovementInputFromOptions": //这里做按键检测，客户端会给服务端发包
			cn = check(Injector.read(klass), name);
			mn = Injector.find((x) -> x.name.equals("updatePlayerMoveState") && x.desc.equals("()V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding hook
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/client/InputHandler", "handle", "()V"));
			
			//inject
			Injector.inject(Injector.RETURN, ns, mn);
			break;
		}
		
		//return the result
		return cn == null ? null : Injector.write(cn);
	}
	
	/**
	 * Check Fool
	 * */
	private static final ClassNode check(ClassNode cn, String name) {
		if(!cn.name.equals(name)) throw new RuntimeException();
		
		return cn;
	}
}
