package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NetHandler;
import net.minecraft.src.Packet;
import tpc.mc.emc.tech.Technique;

/**
 * Tech lost and get packet
 * */
public final class PacketLG extends Packet {
	
	private int entityID;
	private LGMap map;
	
	/**
	 * Public Constructor, notice that only server can send the packet
	 * */
	public PacketLG(EntityPlayer player, LGMap map) {
		assert(player != null);
		assert(map != null);
		
		map = map.clone();
		assert(!map.empty());
		
		//check access
		if(player.worldObj.isRemote) throw new IllegalAccessError();
		
		this.map = map;
		this.entityID = player.entityId;
	}
	
	@Override
	@SuppressWarnings("resource")
	public void readPacketData(DataInput var1) throws IOException {
		this.entityID = var1.readInt();
		try {
			this.map = (LGMap) new ObjectInputStream(new InputStream() {
				
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
		if(var1.isServerHandler()) return; //ignore client sent
		
		if(this.map != null) this.map.flush((EntityPlayer) Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID));
	}
	
	@Override
	public int getPacketSize() {
		return 0; //UNKNOWN!
	}
}
