package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.Packet;
import net.minecraft.src.WorldServer;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.StepableImpl;
import tpc.mc.emc.runtime.util.Deque;
import tpc.mc.emc.runtime.util.Reflect;
import tpc.mc.emc.tech.Board;
import tpc.mc.emc.tech.Pool;

/**
 * Tech act packet
 * */
public final class PacketAct extends Packet {
	
	private static final Supplier<Field> COMMOSTEP = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_164_COMMOSTEP");
	private static final Supplier<Field> MODELSTEP = Reflect.ilocate("net.minecraft.src.AbstractClientPlayer", "EMC_164_MODELSTEP");
	private static final Board TMP = new Board();
	
	private int entityID;
	private Pool tech;
	
	/**
	 * Create a packet
	 * */
	public PacketAct(Pool tech, EntityPlayer player) {
		assert(tech != null);
		assert(player != null);
		
		this.tech = tech;
		this.entityID = player.worldObj.isRemote ? -1 : player.entityId; //client send to server no need entityid
	}
	
	@Override
	public void readPacketData(DataInput var1) throws IOException {
		this.tech = Pool.values()[var1.readInt()];
		this.entityID = var1.readInt();
	}
	
	@Override
	public void writePacketData(DataOutput var1) throws IOException {
		var1.writeInt(this.tech.ordinal());
		var1.writeInt(this.entityID);
	}
	
	@Override
	public void processPacket(NetHandler var1) {
		EntityPlayer target = this.entityID == -1 ? ((NetServerHandler) var1).playerEntity : (EntityPlayer) Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID);
		Pool acted = this.tech;
		boolean server = var1.isServerHandler();
		
		//process packet
		try {
			if(!server || avail((EntityPlayerMP) target, acted)) {
				Deque<StepableImpl> deque = (Deque<StepableImpl>) COMMOSTEP.get().get(target);
				deque.head(new StepableImpl(acted));
				
				if(!server) {
					deque = (Deque) MODELSTEP.get().get(target);
					deque.head(new StepableImpl(acted));
				}
			}
			
			//send back
			if(server) {
				((WorldServer) target.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(target, new PacketAct(acted, target));
			}
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int getPacketSize() {
		return 8;
	}
	
	/**
	 * Internal Helper, check if the tech is available(SERVER ONLY
	 * */
	private static final boolean avail(EntityPlayerMP target, Pool tech) {
		try(IContext conte = new OptionImpl(target).alloc()) {
			return conte.iexport(TMP).available(tech);
		}
	}
}
