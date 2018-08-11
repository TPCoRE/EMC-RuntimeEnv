package tpc.mc.emc.runtime.impls.impl164.emc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLightningBolt;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Minecraft;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Packet;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraft.src.WorldServer;
import sun.reflect.Reflection;
import tpc.mc.emc.err.ClosedException;
import tpc.mc.emc.err.UnsupportedException;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.platform.standard.IOption;
import tpc.mc.emc.runtime.impls.impl164.mc.EntityLitBoltSpeci;
import tpc.mc.emc.runtime.impls.impl164.mc.network.LGMap;
import tpc.mc.emc.runtime.impls.impl164.mc.network.PacketAct;
import tpc.mc.emc.runtime.impls.impl164.mc.network.PacketLG;
import tpc.mc.emc.runtime.impls.impl164.mc.network.PacketQC;
import tpc.mc.emc.runtime.impls.impl164.mc.network.QCMap;
import tpc.mc.emc.runtime.util.Reflect;
import tpc.mc.emc.tech.Technique;

/**
 * IContext implement for 164
 * */
public final class ContextImpl implements IContext {
	
	private static final Supplier<Field> CONTELOCK = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_NULLADEV_164_CONTELOCK");
	private static final Supplier<Field> TECHBOARD = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_NULLADEV_164_TECHBOARD");
	private static final Supplier<Field> COMMOSTEP = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_NULLADEV_164_COMMOSTEP");
	
	private final OptionImpl option;
	private boolean released;
	private boolean peeker;
	private final TechBoard content;
	private final TechBoard backup;
	private final ArrayList<Technique> follow;
	
	/**
	 * Internal constructor
	 * */
	ContextImpl(OptionImpl opt) {
		assert(opt != null);
		
		try {
			//init context
			this.released = false;
			this.peeker = true;
			this.follow = new ArrayList<>();
			
			//lock context
			((ReentrantLock) CONTELOCK.get().get(opt.player)).lock();
			
			//init content
			this.option = opt;
			this.backup = (this.content = (TechBoard) TECHBOARD.get().get(opt.player)).clone();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized boolean verify(IOption opt) {
		return this.idoubt().option.equals(opt);
	}
	
	@Override
	public synchronized boolean released() {
		return this.released;
	}
	
	/**
	 * Force network update, return itself
	 * */
	public synchronized ContextImpl sync() {
		EntityPlayer player = this.idoubt().option.player;
		Consumer<Packet> proxy = null;
		
		//config network proxy
		if(player.worldObj.isRemote) proxy = (x) -> Minecraft.getMinecraft().getNetHandler().addToSendQueue(x);
		else proxy = (x) -> ((WorldServer) player.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(player, x);
		
		//prepare
		LGMap lg = new LGMap();
		QCMap qc = new QCMap();
		List<Technique> follow = this.follow;
		TechBoard cur = this.content;
		int i, l;
		
		//commit lg
		Iterator<Technique> iter = cur.iterator();
		while(iter.hasNext()) lg.set(iter.next(), true);
		//commit qc
		QuickSlot[] slots = QuickSlot.values();
		for(i = 0, l = slots.length; i < l; ++i) qc.set(slots[i], cur.quick(slots[i]));
		
		//send if existed
		if(!lg.empty()) proxy.accept(new PacketLG(player, lg));
		if(!qc.empty()) proxy.accept(new PacketQC(player, qc));
		if(!follow.isEmpty()) {
			for(i = 0, l = follow.size(); i < l; ++i) {
				Technique tech = follow.get(i);
				
				proxy.accept(new PacketAct(tech, player, cur.check(tech)));
			}
			
			//clear up
			follow.clear();
		}
		
		//return itself
		return this;
	}
	
	@Override
	public synchronized void close() {
		this.idoubt();
		
		try {
			EntityPlayer player = this.option.player;
			Consumer<Packet> proxy = null;
			Class<?> caller = Reflection.getCallerClass(2);
			TechBoard cur = this.content;
			Technique tech;
			
			//config network proxy
			if(player.worldObj.isRemote) proxy = (x) -> Minecraft.getMinecraft().getNetHandler().addToSendQueue(x);
			else proxy = (x) -> ((WorldServer) player.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(player, x);
			
			//flush changed status
			if(!this.peeker && caller != LGMap.class && caller != QCMap.class) {
				TechBoard old = this.backup;
				LGMap result = new LGMap();
				
				//check lost
				Iterator<Technique> iter = old.iterator();
				while(iter.hasNext()) {
					tech = (Technique) iter.next();
					
					if(!cur.check(tech)) result.set(tech, false);
				}
				
				//check get
				iter = cur.iterator();
				while(iter.hasNext()) {
					tech = iter.next();
					
					if(!old.check(tech)) result.set(tech, true);
				}
				
				if(!result.empty()) proxy.accept(new PacketLG(player, result));
				
				QCMap result0 = new QCMap();
				QuickSlot[] var = QuickSlot.values();
				QuickSlot slot;
				
				for(int i = 0, l = var.length; i < l; ++i) {
					tech = cur.quick(slot = var[i]);
					
					if(!(tech == null ? old.quick(slot) == null : tech.equals(old.quick(slot)))) result0.set(slot, tech);
				}
				
				if(!result0.empty()) proxy.accept(new PacketQC(player, result0));
			}
			
			//send follow packet
			List<Technique> follow = this.follow;
			if(!follow.isEmpty()) {
				for(int i = 0, l = follow.size(); i < l; ++i) {
					tech = follow.get(i);
					
					proxy.accept(new PacketAct(tech, player, cur.check(tech)));
				}
			}
			
			//released
			((ReentrantLock) CONTELOCK.get().get(this.option.player)).unlock();
			this.released = true;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public synchronized TechBoard peekEnd() {
		TechBoard board = new TechBoard();
		this.peek(board);
		this.close();
		
		return board;
	}
	
	public synchronized void peek(TechBoard peeker) {
		peeker.peek(this.idoubt().content);
	}
	
	public synchronized void export(TechBoard accepter) {
		assert(accepter != null);
		
		accepter.accept(this.idoubt().content);
	}
	
	public synchronized ContextImpl accept(TechBoard exporter) {
		this.idoubt().content.accept(exporter);
		this.peeker = false;
		return this;
	}
	
	//----------------------------------------------------------------------
	
	@Override
	public synchronized boolean airborne() {
		return !this.idoubt().option.player.onGround;
	}
	
	@Override
	public synchronized int tickes() {
		return this.idoubt().option.player.ticksExisted;
	}
	
	@Override
	public synchronized void accel(double vx, double vy, double vz) {
		EntityPlayer player = this.idoubt().option.player;
		
		if(player.worldObj.isRemote) player.addVelocity(vx, vy, vz);
		//else player.moveEntity(vx, vy, vz);
	}
	
	@Override
	public synchronized double velocityX() { return this.idoubt().option.player.motionX; }
	@Override
	public synchronized double velocityY() { return this.idoubt().option.player.motionY; }
	@Override
	public synchronized double velocityZ() { return this.idoubt().option.player.motionZ; }
	
	@Override
	public synchronized void particle(double lx, double ly, double lz, double vx, double vy, double vz) {
		if(!this.idoubt().option.model()) throw new UnsupportedException();
		EntityPlayer player = this.option.player;
		World w = player.worldObj;
		AxisAlignedBB bb = player.boundingBox;
		double x = (bb.maxX + bb.minX) * 0.5;
		double y = (bb.maxY + bb.minY) * 0.5;
		double z = (bb.maxZ + bb.minZ) * 0.5;
		
		//config in
		x += lx;
		y += ly;
		z += lz;
		
		w.spawnParticle("enchantmenttable", x, y, z, vx, vy, vz);
	}
	
	@Override
	public synchronized void act(Technique tech) {
		//add to send queue
		this.idoubt().follow.add(tech);
	}
	
	@Override
	public synchronized void check(Consumer<Technique> proxy) {
		assert(proxy != null);
		
		OptionImpl opt = this.idoubt().option;
		List<StepableImpl> list;
		
		try {
			list = (List<StepableImpl>) COMMOSTEP.get().get(opt.player);
			
			synchronized(list) {
				for(int i = 0, l = list.size(); i < l; ++i) {
					proxy.accept(list.get(i).tech());
				}
			}
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public ContextImpl idoubt() {
		if(this.released()) throw new ClosedException();
		
		return this;
	}
	
	@Override
	public void clearFallStatus() {
		this.idoubt().option.player.fallDistance = 0;
	}
	
	@Override
	public Object lightning(double lx, double ly, double lz) {
		if(Double.isNaN(lx) || Double.isNaN(ly) || Double.isNaN(lz)) return null;
		EntityPlayer player = this.idoubt().option.player;
		
		//judge
		if(!player.worldObj.isRemote) {
			EntityLightningBolt result = new EntityLightningBolt(player.worldObj, player.posX + lx, player.posY + ly, player.posZ + lz);
			player.worldObj.addWeatherEffect(result);
			return result;
		} else return null;
	}
	
	@Override
	public void accel(double vl) {
		Vec3 v = this.idoubt().option.player.getLookVec();
		this.accel(v.xCoord * vl, v.yCoord * vl, v.zCoord * vl);
	}
	
	@Override
	public double[] raycast() {
		EntityPlayer player = this.idoubt().option.player;
		World w = player.worldObj;
		
		Vec3 start = w.isRemote ? player.getPosition(1F) : w.getWorldVec3Pool().getVecFromPool(player.posX, player.posY + 1.62D, player.posZ);
		Vec3 end = player.getLookVec();
        end = clip(w, start, start.addVector(end.xCoord * 512, end.yCoord * 512, end.zCoord * 512), player);
        
        return end == null ? new double[] { Double.NaN, Double.NaN, Double.NaN } : new double[] { end.xCoord - player.posX, end.yCoord - player.posY, end.zCoord - player.posZ };
	}

	@Override
	public boolean weak() {
		return this.idoubt().option.player.getActivePotionEffect(Potion.weakness) != null;
	}
	
	@Override
	public Object lightningSpeci(double lx, double ly, double lz) {
		if(Double.isNaN(lx) || Double.isNaN(ly) || Double.isNaN(lz)) return null;
		EntityPlayer player = this.idoubt().option.player;
		
		//judge
		if(!player.worldObj.isRemote) {
			EntityLitBoltSpeci result = new EntityLitBoltSpeci(player.worldObj, player.posX + lx, player.posY + ly, player.posZ + lz);
			player.worldObj.addWeatherEffect(result);
			return result;
		} else return null;
	}
	
	@Override
	public Object randLivingBase() {
		EntityPlayer player = this.idoubt().option.player;
		List l = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.expand(16D, 32D, 16D));
		
		return l.isEmpty() ? null : l.get(player.getRNG().nextInt(l.size()));
	}
	
	@Override
	public double posX(Object obj) {
		EntityPlayer ep = this.idoubt().option.player;
		if(!(obj instanceof Entity)) throw new IllegalArgumentException();
		
		return ((Entity) obj).posX - ep.posX;
	}
	
	@Override
	public double posY(Object obj) {
		EntityPlayer ep = this.idoubt().option.player;
		if(!(obj instanceof Entity)) throw new IllegalArgumentException();
		
		return ((Entity) obj).posY - ep.posY;
	}
	
	@Override
	public double posZ(Object obj) {
		EntityPlayer ep = this.idoubt().option.player;
		if(!(obj instanceof Entity)) throw new IllegalArgumentException();
		
		return ((Entity) obj).posZ - ep.posZ;
	}
	
	@Override
	public void tired() {
		EntityPlayer ep = this.idoubt().option.player;
		if(ep.worldObj.isRemote) return;
		
		ep.addExhaustion(0.5F);
	}
	
	@Override
	public void assault() {
		EntityPlayer ep = this.idoubt().option.player;
		if(ep.worldObj.isRemote) return;
		PotionEffect tmp;
		
		tmp = ep.getActivePotionEffect(Potion.damageBoost);
		if(tmp == null) ep.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 40, 2));
		else ep.addPotionEffect(new PotionEffect(Potion.damageBoost.id, tmp.getDuration() + 40, tmp.getAmplifier() + 2));
		
		tmp = ep.getActivePotionEffect(Potion.moveSpeed);
		if(tmp == null) ep.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 40, 3));
		else ep.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, tmp.getDuration() + 40, tmp.getAmplifier() + 3));
		
		tmp = ep.getActivePotionEffect(Potion.digSpeed);
		if(tmp == null) ep.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 40));
		else ep.addPotionEffect(new PotionEffect(Potion.digSpeed.id, tmp.getDuration() + 40, tmp.getAmplifier()));
		
		tmp = ep.getActivePotionEffect(Potion.jump);
		if(tmp == null) ep.addPotionEffect(new PotionEffect(Potion.jump.id, 40));
		else ep.addPotionEffect(new PotionEffect(Potion.jump.id, tmp.getDuration() + 40, tmp.getAmplifier()));
		
		ep.attackEntityFrom(DamageSource.causeIndirectMagicDamage(ep, ep), ep.getHealth() <= 2 ? 2 : ep.getHealth() * 0.58F);
	}
	
	/**
	 * Clip including entities
	 * */
	private static final Vec3 clip(World w, Vec3 start, Vec3 end, Entity excluded) {
		int var5 = MathHelper.floor_double(end.xCoord);
        int var6 = MathHelper.floor_double(end.yCoord);
        int var7 = MathHelper.floor_double(end.zCoord);
        int var8 = MathHelper.floor_double(start.xCoord);
        int var9 = MathHelper.floor_double(start.yCoord);
        int var10 = MathHelper.floor_double(start.zCoord);
        int var11 = w.getBlockId(var8, var9, var10);
        int var12 = w.getBlockMetadata(var8, var9, var10);
        Block var13 = Block.blocksList[var11];
        MovingObjectPosition hit;
        List l = new ArrayList<>();
        
        if(var11 > 0 && var13.canCollideCheck(var12, false)) {
        	hit = var13.collisionRayTrace(w, var8, var9, var10, start, end);
            if(hit != null) return hit.hitVec;
        }
        
        var11 = 200;
        while(var11-- >= 0) {
            if(Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord)) return null;
            if(var8 == var5 && var9 == var6 && var10 == var7) return null;
            
            boolean var39 = true;
            boolean var40 = true;
            boolean var41 = true;
            double var15 = 999.0D;
            double var17 = 999.0D;
            double var19 = 999.0D;
            
            if(var5 > var8) var15 = (double)var8 + 1.0D;
            else if(var5 < var8) var15 = (double)var8 + 0.0D;
            else var39 = false;
            
            if(var6 > var9) var17 = (double)var9 + 1.0D;
            else if(var6 < var9) var17 = (double)var9 + 0.0D;
            else var40 = false;
            
            if(var7 > var10) var19 = (double)var10 + 1.0D;
            else if(var7 < var10) var19 = (double)var10 + 0.0D;
            else var41 = false;
            
            double var21 = 999.0D;
            double var23 = 999.0D;
            double var25 = 999.0D;
            double var27 = end.xCoord - start.xCoord;
            double var29 = end.yCoord - start.yCoord;
            double var31 = end.zCoord - start.zCoord;

            if(var39) var21 = (var15 - start.xCoord) / var27;
            if(var40) var23 = (var17 - start.yCoord) / var29;
            if(var41) var25 = (var19 - start.zCoord) / var31;
            
            boolean var33 = false;
            byte var42;
            
            if(var21 < var23 && var21 < var25) {
                if(var5 > var8) var42 = 4;
                else var42 = 5;
                
                start.xCoord = var15;
                start.yCoord += var29 * var21;
                start.zCoord += var31 * var21;
            } else if(var23 < var25) {
                if(var6 > var9) var42 = 0;
                else var42 = 1;
                
                start.xCoord += var27 * var23;
                start.yCoord = var17;
                start.zCoord += var31 * var23;
            } else {
                if(var7 > var10) var42 = 2;
                else var42 = 3;
                
                start.xCoord += var27 * var25;
                start.yCoord += var29 * var25;
                start.zCoord = var19;
            }
            
            Vec3 var34 = w.getWorldVec3Pool().getVecFromPool(start.xCoord, start.yCoord, start.zCoord);
            var8 = (int)(var34.xCoord = (double)MathHelper.floor_double(start.xCoord));
            
            if(var42 == 5) {
                --var8;
                ++var34.xCoord;
            }
            
            var9 = (int)(var34.yCoord = (double)MathHelper.floor_double(start.yCoord));
            
            if(var42 == 1) {
                --var9;
                ++var34.yCoord;
            }
            
            var10 = (int)(var34.zCoord = (double)MathHelper.floor_double(start.zCoord));
            
            if(var42 == 3) {
                --var10;
                ++var34.zCoord;
            }
            
            int var35 = w.getBlockId(var8, var9, var10);
            int var36 = w.getBlockMetadata(var8, var9, var10);
            Block var37 = Block.blocksList[var35];
            
            if(var35 > 0 && var37.canCollideCheck(var36, false)) {
                hit = var37.collisionRayTrace(w, var8, var9, var10, start, end);
                if(hit != null) return hit.hitVec;
            }
            
        	w.getChunkFromBlockCoords(var8, var10).getEntitiesWithinAABBForEntity(excluded, AxisAlignedBB.getBoundingBox(var8, var9, var10, var8 + 1, var9 + 1, var10 + 1).expand(0.25, 0.25, 0.25), l, null);
        	if(!l.isEmpty()) return w.getWorldVec3Pool().getVecFromPool(var8, var9, var10);
        }
        
        return null;
	}
}
