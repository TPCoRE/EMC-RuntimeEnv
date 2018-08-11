package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.Packet;
import net.minecraft.src.WorldServer;

/**
 * Quick slot change packet
 * */
public final class PacketQC extends Packet {
	
	private int entityID;
	private QCMap map;
	
	/**
	 * Constructor
	 * */
	public PacketQC(EntityPlayer player, QCMap map) {
		assert(player != null);
		assert(map != null);
		assert(!map.empty());
		
		this.entityID = player.worldObj.isRemote ? -1 : player.entityId; //client send to server no need entityid
		this.map = map;
	}
	
	@Override
	@SuppressWarnings("resource")
	public void readPacketData(DataInput var1) throws IOException {
		this.entityID = var1.readInt();
		
		try {
			this.map = (QCMap) new ObjectInputStream(new InputStream() {
				
				@Override
				public int read() throws IOException {
					return var1.readUnsignedByte();
				}
			}).readObject();
		} catch(Throwable e) {
			this.map = null;
		}
	}
	
	@Override
	@SuppressWarnings("resource")
	public void writePacketData(DataOutput var1) throws IOException {
		var1.writeInt(this.entityID);
		
		//write LGMAP
		new ObjectOutputStream(new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				var1.write(b);
			}
		}).writeObject(this.map);
	}
	
	@Override
	public void processPacket(NetHandler var1) {
		if(this.map == null) return;
		EntityPlayer target = this.entityID == -1 ? ((NetServerHandler) var1).playerEntity : (EntityPlayer) Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID);
		
		this.map.flush(target);
		
		//send back
		if(var1.isServerHandler()) {
			((WorldServer) target.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(target, new PacketQC(target, this.map.clone()));
		}
	}
	
	@Override
	public int getPacketSize() {
		return 0; //UNKNOWN
	}
}
