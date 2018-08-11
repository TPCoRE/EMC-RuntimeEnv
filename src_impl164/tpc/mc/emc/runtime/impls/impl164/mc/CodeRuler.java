package tpc.mc.emc.runtime.impls.impl164.mc;

import java.io.File;
import java.util.UUID;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import tpc.mc.emc.platform.standard.EMC;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;
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
		/*case "net/minecraft/src/ModelBiped": //这里调用client-model的stepable
			cn = check(Injector.read(klass), name);
			MethodNode mn = Injector.find((x) -> x.name.equals("setRotationAngles") && x.desc.equals("(FFFFFFLnet/minecraft/src/Entity;)V"), cn);
			
			//check&prepare
			assert(mn != null);
			InsnList ns = new InsnList();
			
			//coding, 调用stepable
			ns.add(new VarInsnNode(Opcodes.ALOAD, 7));
			ns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/src/EntityPlayer"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/Walker", "walk", "(Lnet/minecraft/src/EntityPlayer;)V"));
			
			//inject
			Injector.inject(Injector.RETURN, ns, mn);
			break;*/
		case "net/minecraft/src/Entity": //这里为特殊雷击加效果
			cn = check(Injector.read(klass), name);
			MethodNode mn = Injector.find((x) -> x.name.equals("onStruckByLightning") && x.desc.equals("(Lnet/minecraft/src/EntityLightningBolt;)V"), cn);
			
			//check&prepare
			assert(mn != null);
			InsnList ns = new InsnList();
			
			//coding
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/Walker", "postfixLB", "(Lnet/minecraft/src/Entity;Lnet/minecraft/src/EntityLightningBolt;)V"));
			
			//inject
			Injector.inject(Injector.RETURN, ns, mn);
			break;
		case "net/minecraft/src/EntityPlayer": //这里储存，调用client|server的stepable，添加context lock, tech board，储存techboard2nbt，解决科技死亡消失
			cn = check(Injector.read(klass), name);
			mn = Injector.find((x) -> x.name.equals("onUpdate") && x.desc.equals("()V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding, 调用stepable
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/Walker", "walk", "(Lnet/minecraft/src/EntityPlayer;)V"));
			mn.instructions.insert(ns); //inject
			
			//coding <init>* for new field
			ns = new InsnList();
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new InsnNode(Opcodes.DUP));
			//for common stepable
			ns.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Collections", "synchronizedList", "(Ljava/util/List;)Ljava/util/List;"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/EntityPlayer", "EMC_NULLADEV_164_COMMOSTEP", "Ljava/util/List;"));
			//for context lock
			ns.add(new TypeInsnNode(Opcodes.NEW, "java/util/concurrent/locks/ReentrantLock"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new InsnNode(Opcodes.ICONST_0));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/concurrent/locks/ReentrantLock", "<init>", "(Z)V"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/EntityPlayer", "EMC_NULLADEV_164_CONTELOCK", "Ljava/util/concurrent/locks/ReentrantLock;"));
			//for techboard
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/runtime/impls/impl164/emc/TechBoard"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/runtime/impls/impl164/emc/TechBoard", "<init>", "()V"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/EntityPlayer", "EMC_NULLADEV_164_TECHBOARD", "Ltpc/mc/emc/runtime/impls/impl164/emc/TechBoard;"));
			
			//tmp addition，这里添加一些初始科技，下个版本删除, server only
			ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/World", "isRemote", "Z"));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/EntityPlayer", "EMC_NULLADEV_164_TECHBOARD", "Ltpc/mc/emc/runtime/impls/impl164/emc/TechBoard;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/emc/Temporary", "addition", "(ZLtpc/mc/emc/runtime/impls/impl164/emc/TechBoard;)V"));
			
			//inject new fields, <init>
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_NULLADEV_164_COMMOSTEP", "Ljava/util/List;", null, null));
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_NULLADEV_164_CONTELOCK", "Ljava/util/concurrent/locks/ReentrantLock;", null, null));
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_NULLADEV_164_TECHBOARD", "Ltpc/mc/emc/runtime/impls/impl164/emc/TechBoard;", null, null));
			Injector.inject(Injector.INIT, Injector.RETURN, ns, cn);
			mn = Injector.find((x) -> x.name.equals("readEntityFromNBT") && x.desc.equals("(Lnet/minecraft/src/NBTTagCompound;)V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding readEntityFromNBT
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/NBT", "acceptTechboard", "(Lnet/minecraft/src/EntityPlayer;Lnet/minecraft/src/NBTTagCompound;)V"));
			
			//inject readEntityFromNBT
			mn.instructions.insert(ns);
			mn = Injector.find((x) -> x.name.equals("writeEntityToNBT") && x.desc.equals("(Lnet/minecraft/src/NBTTagCompound;)V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding writeEntityToNBT
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/NBT", "exportTechboard", "(Lnet/minecraft/src/EntityPlayer;Lnet/minecraft/src/NBTTagCompound;)V"));
			
			//inject writeEntityToNBT
			Injector.inject(Injector.RETURN, ns, mn);
			mn = Injector.find((x) -> x.name.equals("clonePlayer") && x.desc.equals("(Lnet/minecraft/src/EntityPlayer;Z)V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding clonePlayer
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "<init>", "(Lnet/minecraft/src/EntityPlayer;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "alloc", "()Ltpc/mc/emc/runtime/impls/impl164/emc/ContextImpl;"));
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "<init>", "(Lnet/minecraft/src/EntityPlayer;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "alloc", "()Ltpc/mc/emc/runtime/impls/impl164/emc/ContextImpl;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/ContextImpl", "peekEnd", "()Ltpc/mc/emc/runtime/impls/impl164/emc/TechBoard;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/ContextImpl", "accept", "(Ltpc/mc/emc/runtime/impls/impl164/emc/TechBoard;)Ltpc/mc/emc/runtime/impls/impl164/emc/ContextImpl;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/ContextImpl", "close", "()V"));
			
			//inject clonePlayer
			mn.instructions.insert(ns);
			mn.maxStack += 2;
			break;
		/*case "net/minecraft/src/AbstractClientPlayer": //这里储存client-model的stepable
			cn = check(Injector.read(klass), name);
			ns = new InsnList();
			
			//coding for model stepable
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Collections", "synchronizedList", "(Ljava/util/List;)Ljava/util/List;"));
			ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/src/AbstractClientPlayer", "EMC_NULLADEV_164_MODELSTEP", "Ljava/util/List;"));
			
			//inject
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "EMC_NULLADEV_164_MODELSTEP", "Ljava/util/List;", null, null));
			cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "EMC_NULLADEV_164_MODELMARK", "I", null, new Integer(0)));
			Injector.inject(Injector.INIT, Injector.RETURN, ns, cn);
			break;*/
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
			
			//coding PacketQC
			ns.add(new LdcInsnNode(new Integer(-3)));
			ns.add(new InsnNode(Opcodes.ICONST_1));
			ns.add(new InsnNode(Opcodes.ICONST_1));
			ns.add(new LdcInsnNode(Type.getType("Ltpc/mc/emc/runtime/impls/impl164/mc/network/PacketQC;")));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/src/Packet", "addIdClassMapping", "(IZZLjava/lang/Class;)V"));
			
			//coding PacketIB
			ns.add(new LdcInsnNode(new Integer(-4)));
			ns.add(new InsnNode(Opcodes.ICONST_1));
			ns.add(new InsnNode(Opcodes.ICONST_0));
			ns.add(new LdcInsnNode(Type.getType("Ltpc/mc/emc/runtime/impls/impl164/mc/network/PacketIB;")));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/src/Packet", "addIdClassMapping", "(IZZLjava/lang/Class;)V"));
			
			//inject
			Injector.inject(Injector.RETURN, ns, mn);
			break;
		case "net/minecraft/src/ServerConfigurationManager": //在玩家进入游戏(或者重生)的时候发送强制同步包, readNBT哪里的发包在玩家进入阶段直接被忽略了（因玩家还没加入网络，见ServerConfigurationManager.initializeConnectionToPlayer）
			cn = check(Injector.read(klass), name);
			mn = Injector.find((x) -> x.name.equals("initializeConnectionToPlayer") && x.desc.equals("(Lnet/minecraft/src/INetworkManager;Lnet/minecraft/src/EntityPlayerMP;)V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding initializeConnectionToPlayer
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 2));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "<init>", "(Lnet/minecraft/src/EntityPlayer;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "alloc", "()Ltpc/mc/emc/runtime/impls/impl164/emc/ContextImpl;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/ContextImpl", "sync", "()Ltpc/mc/emc/runtime/impls/impl164/emc/ContextImpl;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/ContextImpl", "close", "()V"));
			
			//inject initializeConnectionToPlayer
			Injector.inject(Injector.RETURN, ns, mn);
			mn = Injector.find((x) -> x.name.equals("respawnPlayer") && x.desc.equals("(Lnet/minecraft/src/EntityPlayerMP;IZ)Lnet/minecraft/src/EntityPlayerMP;"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding respawnPlayer
			ns.add(new TypeInsnNode(Opcodes.NEW, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "<init>", "(Lnet/minecraft/src/EntityPlayer;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/OptionImpl", "alloc", "()Ltpc/mc/emc/runtime/impls/impl164/emc/ContextImpl;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/ContextImpl", "sync", "()Ltpc/mc/emc/runtime/impls/impl164/emc/ContextImpl;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tpc/mc/emc/runtime/impls/impl164/emc/ContextImpl", "close", "()V"));
			
			//inject respawnPlayer
			Injector.inject(Injector.ARETURN, ns, mn);
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
		case "net/minecraft/src/GuiInventory": //这里在玩家背包GUI注入科技栏GUI
			cn = check(Injector.read(klass), name);
			mn = Injector.find((x) -> x.name.equals("drawScreen") && x.desc.equals("(IIF)V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding hook
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/GuiInventory", "guiLeft", "I"));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/GuiInventory", "guiTop", "I"));
			ns.add(new VarInsnNode(Opcodes.ILOAD, 1));
			ns.add(new VarInsnNode(Opcodes.ILOAD, 2));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/client/UIHandler", "handle", "(Lnet/minecraft/src/GuiContainer;IIII)V"));
			
			//inject
			Injector.inject(Injector.RETURN, ns, mn);
			mn.maxStack += 1;
			break;
		case "net/minecraft/src/ItemStack": //给被绑定的物品以附魔效果(贴图)，并在item name里显示绑定的科技名称，在onstopusing里act绑定的科技
			cn = check(Injector.read(klass), name);
			mn = Injector.find((x) -> x.name.equals("hasEffect") && x.desc.equals("()Z"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding hook hasEffect
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/ItemStack", "stackTagCompound", "Lnet/minecraft/src/NBTTagCompound;"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/NBT", "has", "(Lnet/minecraft/src/NBTTagCompound;)Z"));
			ns.add(new InsnNode(Opcodes.IOR));
			
			//inject hasEffect
			Injector.inject(Injector.IRETURN, ns, mn);
			mn = Injector.find((x) -> x.name.equals("getTooltip") && x.desc.equals("(Lnet/minecraft/src/EntityPlayer;Z)Ljava/util/List;"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			
			//coding hook getTooltip
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/emc/runtime/impls/impl164/mc/client/UIHandler", "postfix", "(Ljava/util/List;Lnet/minecraft/src/EntityPlayer;Lnet/minecraft/src/ItemStack;)V"));
			
			//inject getTooltip
			Injector.inject(Injector.ARETURN, ns, mn);
			break;
		/*case "net/minecraft/src/Minecraft": //为EMC注册材质包
			cn = check(Injector.read(klass), name);
			mn = Injector.find((x) -> x.name.equals("startGame") && x.desc.equals("()V"), cn);
			
			//check&prepare
			assert(mn != null);
			ns = new InsnList();
			String str0 = Impl164.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String str1 = EMC.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String str2;
			
			//coding IMPL RES
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/Minecraft", "defaultResourcePacks", "Ljava/util/List;"));
			ns.add(new TypeInsnNode(Opcodes.NEW, str2 = new File(str0).isDirectory() ? "net/minecraft/src/FolderResourcePack" : "net/minecraft/src/FileResourcePack"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new TypeInsnNode(Opcodes.NEW, "java/io/File"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new LdcInsnNode(str0));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, str2, "<init>", "(Ljava/io/File;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
			ns.add(new InsnNode(Opcodes.POP));
			
			//coding EMC RES
			ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/src/Minecraft", "defaultResourcePacks", "Ljava/util/List;"));
			ns.add(new TypeInsnNode(Opcodes.NEW, str2 = new File(str1).isDirectory() ? "net/minecraft/src/FolderResourcePack" : "net/minecraft/src/FileResourcePack"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new TypeInsnNode(Opcodes.NEW, "java/io/File"));
			ns.add(new InsnNode(Opcodes.DUP));
			ns.add(new LdcInsnNode(str1));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, str2, "<init>", "(Ljava/io/File;)V"));
			ns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
			ns.add(new InsnNode(Opcodes.POP));
			
			//inject
			mn.instructions.insert(ns);
			break;*/
		}
		
		//return the result
		return cn == null ? null : Injector.write(cn, 0);
	}
	
	/**
	 * Check Fool
	 * */
	private static final ClassNode check(ClassNode cn, String name) {
		if(!cn.name.equals(name)) throw new RuntimeException();
		
		return cn;
	}
}
