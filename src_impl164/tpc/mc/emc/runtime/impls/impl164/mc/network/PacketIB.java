package tpc.mc.emc.runtime.impls.impl164.mc.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet103SetSlot;
import tpc.mc.emc.runtime.impls.impl164.mc.NBT;
import tpc.mc.emc.tech.Technique;

/**
 * Item bind tech packet
 * */
public final class PacketIB extends Packet {
	
	private UUID identifier;
	
	/**
	 * Public constructor
	 * */
	public PacketIB(Technique tech) {
		assert(tech != null);
		
		this.identifier = tech.identifier();
	}
	
	@Override
	public void readPacketData(DataInput var1) throws IOException {
		this.identifier = new UUID(var1.readLong(), var1.readLong());
	}
	
	@Override
	public void writePacketData(DataOutput var1) throws IOException {
		var1.writeLong(this.identifier.getMostSignificantBits());
		var1.writeLong(this.identifier.getLeastSignificantBits());
	}
	
	@Override
	public void processPacket(NetHandler var1) {
		if(!var1.isServerHandler()) return; //ignore
		
		ItemStack stack = ((NetServerHandler) var1).playerEntity.inventory.getItemStack();
		if(stack != null) {
			NBTTagCompound nbt = stack.stackTagCompound;
			if(nbt == null) stack.stackTagCompound = nbt = new NBTTagCompound();
			
			//check changed
			if(!NBT.compare(nbt, this.identifier)) {
				NBT.change(nbt, this.identifier);
				
				//mark dirty, send back
				((NetServerHandler) var1).sendPacketToPlayer(new Packet103SetSlot(-1, -1, stack));
			}
		}
	}
	
	@Override
	public int getPacketSize() {
		return 16;
	}
}
