package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.src.AbstractClientPlayer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.Packet;
import net.minecraft.src.WorldServer;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.StepableImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;
import tpc.mc.emc.runtime.util.Reflect;
import tpc.mc.emc.tech.Technique;

/**
 * Tech act packet
 * */
public final class PacketAct extends Packet {
	
	private static final Supplier<Field> COMMOSTEP = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_NULLADEV_164_COMMOSTEP");
	private static final TechBoard TMP = new TechBoard();
	
	private int entityID;
	private Object tech;
	
	/**
	 * Create a packet
	 * */
	public PacketAct(Technique tech, EntityPlayer player, boolean existed) {
		assert(tech != null);
		assert(player != null);
		
		this.tech = existed ? tech.identifier() : tech;
		this.entityID = player.worldObj.isRemote ? -1 : player.entityId; //client send to server no need entityid
	}
	
	@Override
	@SuppressWarnings("resource")
	public void readPacketData(DataInput var1) throws IOException {
		this.entityID = var1.readInt();
		
		//judge
		if(var1.readByte() != 0) {
			this.tech = new UUID(var1.readLong(), var1.readLong());
		} else {
			try {
				this.tech = new ObjectInputStream(new InputStream() {
					
					@Override
					public int read() throws IOException {
						return var1.readUnsignedByte();
					}
				}).readObject();
			} catch(Throwable e) {
				this.tech = Technique.NOP;
			}
		}
	}
	
	@Override
	public void writePacketData(DataOutput var1) throws IOException {
		var1.writeInt(this.entityID);
		
		//judge
		if(this.tech.getClass().equals(UUID.class)) {
			var1.writeByte(1);
			
			//write ID
			UUID id = (UUID) this.tech;
			var1.writeLong(id.getMostSignificantBits());
			var1.writeLong(id.getLeastSignificantBits());
		} else {
			var1.writeByte(0);
			
			//write tech
			new ObjectOutputStream(new OutputStream() {
				
				@Override
				public void write(int b) throws IOException {
					var1.writeByte(b);
				}
			}).writeObject(this.tech);
		}
	}
	
	@Override
	public void processPacket(NetHandler var1) {
		EntityPlayer target = this.entityID == -1 ? ((NetServerHandler) var1).playerEntity : (EntityPlayer) Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID);
		ContextImpl conte = new OptionImpl(target).alloc();
		TechBoard board = TMP;
		conte.peek(board);
		
		Technique acted = this.get(board);
		boolean server = var1.isServerHandler();
		
		//process packet
		try {
			if(acted != null) {
				if(!server || board.check(acted)) {
					((List<StepableImpl>) COMMOSTEP.get().get(target)).add(new StepableImpl(new OptionImpl(target), acted));
				}
				
				//send back
				if(server) {
					((WorldServer) target.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(target, new PacketAct(acted, target, this.tech.getClass().equals(UUID.class)));
				}
			}
		} catch(Throwable e) {
			throw new RuntimeException(e);
		} finally {
			conte.close();
		}
	}
	
	@Override
	public int getPacketSize() {
		return 0; //UNKNOWN
	}
	
	/**
	 * Internal Helper
	 * */
	private Technique get(TechBoard board) {
		if(this.tech.getClass().equals(UUID.class)) return board.deal((UUID) this.tech);
		return (Technique) this.tech;
	}
}
