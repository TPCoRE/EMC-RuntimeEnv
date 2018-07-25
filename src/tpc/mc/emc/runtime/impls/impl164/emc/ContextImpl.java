package tpc.mc.emc.runtime.impls.impl164.emc;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Packet;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraft.src.WorldServer;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.platform.standard.IOption;
import tpc.mc.emc.runtime.impls.impl164.mc.network.LGMap;
import tpc.mc.emc.runtime.impls.impl164.mc.network.PacketAct;
import tpc.mc.emc.runtime.impls.impl164.mc.network.PacketLG;
import tpc.mc.emc.runtime.util.Deque;
import tpc.mc.emc.runtime.util.Reflect;
import tpc.mc.emc.tech.Board;
import tpc.mc.emc.tech.Pool;

/**
 * IContext implement for 164
 * */
final class ContextImpl extends IContext {
	
	private static final Supplier<Field> CONTELOCK = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_164_CONTELOCK");
	private static final Supplier<Field> TECHBOARD = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_164_TECHBOARD");
	private static final Supplier<Field> COMMOSTEP = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_164_COMMOSTEP");
	private static final Supplier<Field> MODELSTEP = Reflect.ilocate("net.minecraft.src.AbstractClientPlayer", "EMC_164_MODELSTEP");
	
	private final OptionImpl option;
	private boolean released;
	private boolean peeker;
	private final Board content;
	private final Board backup;
	private final ConcurrentLinkedQueue<Packet> follow;
	
	/**
	 * Internal constructor
	 * */
	ContextImpl(OptionImpl opt) {
		assert(opt != null);
		
		try {
			//init context
			this.released = false;
			this.peeker = true;
			this.follow = new ConcurrentLinkedQueue<>();
			
			//lock context
			AtomicBoolean bool = (AtomicBoolean) CONTELOCK.get().get(opt.player);
			while(!bool.compareAndSet(false, true));
			
			//init content
			this.option = opt;
			this.content = (Board) TECHBOARD.get().get(opt.player);
			this.backup = this.content.clone();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized boolean verify(IOption opt) {
		return this.icheck().option.equals(opt);
	}
	
	@Override
	public synchronized boolean released() {
		return this.released;
	}
	
	@Override
	@CallerSensitive
	public synchronized void close() {
		assert(!this.released) : "Already closed";
		
		try {
			EntityPlayer player = this.option.player;
			Consumer<Packet> proxy = null;
			
			//config network proxy
			if(player.worldObj.isRemote) proxy = (x) -> Minecraft.getMinecraft().getNetHandler().addToSendQueue(x);
			else proxy = (x) -> ((WorldServer) player.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(player, x);
			
			//flush changed status
			if(!this.peeker && Reflection.getCallerClass() != LGMap.class) {
				Set<Pool> old = this.backup.availables();
				Set<Pool> cur = this.content.availables();
				LGMap result = new LGMap();
				Pool tech;
				
				//check lost
				Iterator<Pool> iter0 = old.iterator();
				while(iter0.hasNext()) {
					tech = (Pool) iter0.next();
					
					if(!cur.contains(tech)) result.set(tech, false);
				}
				
				//check get
				iter0 = cur.iterator();
				while(iter0.hasNext()) {
					tech = iter0.next();
					
					if(!old.contains(tech)) result.set(tech, true);
				}
				
				if(!result.empty()) proxy.accept(new PacketLG(player, result));
			}
			
			//send follow packet
			Queue<Packet> follow = this.follow;
			if(!follow.isEmpty()) {
				Packet sent;
				
				while((sent = follow.poll()) != null) {
					proxy.accept(sent);
				}
			}
			
			//released
			((AtomicBoolean) CONTELOCK.get().get(this.option.player)).set(false);
			this.released = true;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized void peek(Board peeker) {
		this.icheck().content.peek(peeker);
	}
	
	@Override
	public synchronized void export(Board accepter) {
		assert(accepter != null);
		
		accepter.accept(this.icheck().content);
	}
	
	@Override
	public synchronized void accept(Board exporter) {
		this.icheck().content.accept(exporter);
	}
	
	//----------------------------------------------------------------------
	
	@Override
	public synchronized boolean airborne() {
		return !this.icheck().option.player.onGround;
	}
	
	@Override
	public synchronized int tickes() {
		return this.icheck().option.player.ticksExisted;
	}
	
	@Override
	public synchronized void accel(double vx, double vy, double vz) {
		EntityPlayer player = this.icheck().option.player;
		
		if(player.worldObj.isRemote) player.addVelocity(vx, vy, vz);
		else player.moveEntity(vx, vy, vz);
	}
	
	@Override
	public synchronized void accel(double vl) {
		EntityPlayer player = this.icheck().option.player;
		Vec3 v = player.getLookVec();
		
		if(player.worldObj.isRemote) player.addVelocity(v.xCoord * vl, v.yCoord * vl, v.zCoord * vl);
		else player.moveEntity(v.xCoord * vl, v.yCoord * vl, v.zCoord * vl);
	}
	
	@Override
	public synchronized void scale(double factor) {
		EntityPlayer player = this.icheck().option.player;
		
		if(player.worldObj.isRemote) {
			player.motionX *= factor;
			player.motionY *= factor;
			player.motionZ *= factor;
		} else assert(false) : "Sorry...";
	}
	
	@Override
	public synchronized void particle(double lx, double ly, double lz, double vx, double vy, double vz) {
		EntityPlayer player = this.icheck().option.player;
		World w = player.worldObj;
		AxisAlignedBB bb = player.boundingBox;
		double x = (bb.maxX + bb.minX) * 0.5;
		double y = (bb.maxY + bb.minY) * 0.5;
		double z = (bb.maxZ + bb.minZ) * 0.5;
		
		//config in
		x += lx;
		y += ly;
		z += lz;
		
		if(w.isRemote) {
			w.spawnParticle("enchantmenttable", x, y, z, vx, vy, vz);
		} else assert(false) : "Sorry...";
	}
	
	@Override
	public synchronized void act(Pool tech) {
		EntityPlayer player = this.icheck().option.player;
		
		//add to send queue
		this.follow.add(new PacketAct(tech, player));
	}
	
	@Override
	public synchronized void check(Predicate<Pool> proxy) {
		assert(proxy != null);
		
		OptionImpl opt = this.icheck().option;
		Deque<StepableImpl> deque;
		
		try {
			if(opt.model()) deque = (Deque<StepableImpl>) MODELSTEP.get().get(opt.player);
			else deque = (Deque<StepableImpl>) COMMOSTEP.get().get(opt.player);
			
			deque.check((x) -> proxy.test(x.tech()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	@Deprecated
	public void jump() {
		//TODO
	}
	
	@Override
	@Deprecated
	public void rush() {
		//TODO
	}
	
	@Override
	public ContextImpl icheck() {
		return (ContextImpl) super.icheck();
	}
}
