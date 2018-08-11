package tpc.mc.emc.runtime.impls.impl164.mc;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.src.AbstractClientPlayer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLightningBolt;
import net.minecraft.src.EntityLivingBase;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import net.minecraft.src.RenderManager;
import tpc.mc.emc.Stepable;
import tpc.mc.emc.platform.standard.IOption;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.QuickSlot;
import tpc.mc.emc.runtime.impls.impl164.emc.StepableImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;
import tpc.mc.emc.runtime.util.Reflect;
import tpc.mc.emc.tech.IAttribute;
import tpc.mc.emc.tech.Technique;

/**
 * Hooks
 * */
public final class Walker {
	
	private static final Supplier<Field> COMMOSTEP = Reflect.ilocate("net.minecraft.src.EntityPlayer", "EMC_NULLADEV_164_COMMOSTEP");
	
	/**
	 * Update stepables
	 * */
	public static final void walk(EntityPlayer player) throws Throwable {
		OptionImpl opt = new OptionImpl(player);
		List<StepableImpl> list = (List<StepableImpl>) COMMOSTEP.get().get(player);
		
		//add effects
		if(!player.worldObj.isRemote) {
			PotionEffect tmp;
			
			if((tmp = player.getActivePotionEffect(Potion.weakness)) == null || tmp.getDuration() <= 1) {
				if(player.getFoodStats().getFoodLevel() <= 4F) {
					if(tmp == null || tmp.getAmplifier() < 0 || tmp.getDuration() <= 1) player.addPotionEffect(new PotionEffect(Potion.weakness.id, 60));
				}
			} else {
				if(player.getActivePotionEffect(Potion.digSlowdown) == null) player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 60, 1));
				if(player.getActivePotionEffect(Potion.moveSlowdown) == null) player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 60, 1));
				if(player.getActivePotionEffect(Potion.blindness) == null) player.addPotionEffect(new PotionEffect(Potion.blindness.id, 60));
			}
		}
		
		//lock the list
		synchronized(list) {
			for(int i = 0, l = list.size(); i < l; ++i) {
				if(list.get(i).next() == null) {
					list.remove(i);
					l = list.size();
				}
			}
		}
	}
	
	/**
	 * Update onStruckByLightning postfix
	 * */
	public static final void postfixLB(Entity e, EntityLightningBolt elb) {
		if(!(elb instanceof EntityLitBoltSpeci)) return;
		if(!(e instanceof EntityLivingBase)) return;
		EntityLivingBase eb = (EntityLivingBase) e;
		
		//check
		if(eb instanceof EntityPlayer && ((EntityPlayer) eb).capabilities.disableDamage) return;
		
		eb.addPotionEffect(new PotionEffect(Potion.blindness.id, 0, 80));
		eb.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 1200, 200));
		eb.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 1200, 200));
		eb.addPotionEffect(new PotionEffect(Potion.weakness.id, 1200, 200));
		eb.setHealth(eb.getHealth() - 30 - eb.getRNG().nextInt(20));
	}
}
