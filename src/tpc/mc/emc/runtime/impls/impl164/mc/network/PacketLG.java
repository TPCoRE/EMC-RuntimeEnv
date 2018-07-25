package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NetHandler;
import net.minecraft.src.Packet;
import tpc.mc.emc.tech.Board;
import tpc.mc.emc.tech.Pool;

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
	public void readPacketData(DataInput var1) throws IOException {
		this.entityID = var1.readInt();
		
		LGMap map = this.map = new LGMap();
		Pool[] vals = Pool.values();
		
		//accept LGMAP DATA
		for(int i = 0, l = var1.readInt(); i < l; ++i) {
			map.set(vals[var1.readInt()], var1.readByte() != 0);
		}
	}
	
	@Override
	public void writePacketData(DataOutput var1) throws IOException {
		var1.writeInt(this.entityID);
		
		//write LGMAP
		Iterator<Map.Entry<Pool, Boolean>> iter = this.map.internal.entrySet().iterator();
		var1.writeInt(this.map.internal.size());
		
		//write entries
		while(iter.hasNext()) {
			Map.Entry<Pool, Boolean> entry = iter.next();
			
			//write entry
			var1.writeInt(entry.getKey().ordinal());
			var1.writeByte(entry.getValue() ? 1 : 0);
		}
	}
	
	@Override
	public void processPacket(NetHandler var1) {
		if(var1.isServerHandler()) return; //ignore client sent
		
		this.map.flush((EntityPlayer) Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID));
	}
	
	@Override
	public int getPacketSize() {
		return 2 + (2 + this.map.internal.size() * 5);
	}
}
