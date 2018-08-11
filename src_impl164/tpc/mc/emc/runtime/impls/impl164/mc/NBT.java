package tpc.mc.emc.runtime.impls.impl164.mc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.QuickSlot;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;

/**
 * NBT helper
 * */
public final class NBT {
	
	private static final TechBoard TMP = new TechBoard();
	private static final String MOST = "EMC_NULLADEV_IB_M";
	private static final String LAST = "EMC_NULLADEV_IB_L";
	private static final String TECHBOARD = "EMC_NULLADEV_TB";
	
	/**
	 * Write to the given player
	 * */
	public static final void acceptTechboard(EntityPlayer accepter, NBTTagCompound exporter) {
		try(ContextImpl conte = new OptionImpl(accepter).alloc()) {
			TechBoard board = readTechboard(exporter);
			if(board == null) return;
			
			conte.accept(board);
		}
	}
	
	/**
	 * Write to the given nbt
	 * */
	public static final void exportTechboard(EntityPlayer exporter, NBTTagCompound accepter) {
		try(ContextImpl conte = new OptionImpl(exporter).alloc()) {
			TechBoard board = TMP;
			conte.peek(board);
			
			writeTechboard(accepter, board);
		}
	}
	
	/**
	 * Write the techboard to the given nbt
	 * */
	public static final void writeTechboard(NBTTagCompound nbt, TechBoard board) {
		assert(nbt != null);
		
		//try write
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			new ObjectOutputStream(bos).writeObject(board);
			
			//write to techboard
			nbt.setByteArray(TECHBOARD, bos.toByteArray());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Read from nbt, if there isn't techboard in the given nbt, it will return null, or it will return the techboard in the nbt
	 * */
	public static final TechBoard readTechboard(NBTTagCompound nbt) {
		assert(nbt != null);
		if(!nbt.hasKey(TECHBOARD)) return null;
		
		//try read
		try {
			return (TechBoard) new ObjectInputStream(new ByteArrayInputStream(nbt.getByteArray(TECHBOARD))).readObject();
		} catch(ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Check if has special tag, passing null is allowed
	 * */
	public static final boolean has(NBTTagCompound nbt) {
		return nbt != null && nbt.hasKey(MOST) && nbt.hasKey(LAST);
	}
	
	/**
	 * Compare the special tag, passing null is allowed
	 * */
	public static final boolean compare(NBTTagCompound a, NBTTagCompound b) {
		boolean flag = has(a);
		
		if(flag != has(b)) return false;
		if(!flag) return true;
		
		return a.getLong(MOST) == b.getLong(MOST) && a.getLong(LAST) == b.getLong(LAST);
	}
	
	/**
	 * Check if the given has the special tag, passing null means check if it is existed
	 * */
	public static final boolean compare(NBTTagCompound nbt, UUID id) {
		if(!has(nbt)) return id == null;
		if(id == null) return false;
		
		return nbt.getLong(MOST) == id.getMostSignificantBits() && nbt.getLong(LAST) == id.getLeastSignificantBits();
	}
	
	/**
	 * Change the special tag in the given nbt, passing null means delete it
	 * */
	public static final void change(NBTTagCompound nbt, UUID uuid) {
		assert(nbt != null);
		
		if(uuid == null) {
			nbt.removeTag(MOST);
			nbt.removeTag(LAST);
		} else {
			nbt.setLong(MOST, uuid.getMostSignificantBits());
			nbt.setLong(LAST, uuid.getLeastSignificantBits());
		}
	}
	
	/**
	 * get the uuid, if non-existed it will return null
	 * */
	public static final UUID get(NBTTagCompound tag) {
		if(!has(tag)) return null;
		
		return new UUID(tag.getLong(MOST), tag.getLong(LAST));
	}
}
