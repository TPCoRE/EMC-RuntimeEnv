package tpc.mc.emc.runtime.impls.impl164.mc.client;

import java.util.Arrays;
import java.util.UUID;

import org.lwjgl.input.Keyboard;
import org.omg.CORBA.LongHolder;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.ItemStack;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.QuickSlot;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;
import tpc.mc.emc.runtime.impls.impl164.mc.NBT;
import tpc.mc.emc.tech.Technique;

/**
 * Input (Keyboard/Mouse) Handler
 * */
public final class InputHandler {
	
	public static final KeyBinding ACT_BODYTECH = new KeyBinding("Act BodyTech(Double Click)", Keyboard.KEY_F);
	
	private static final LongHolder TIMER_HEAD = new LongHolder();
	private static final LongHolder TIMER_BODY = new LongHolder();
	private static final LongHolder TIMER_FEET = new LongHolder();
	private static final TechBoard TMP = new TechBoard();
	
	private static boolean SENTRY_ITEM = false;
	
	/**
	 * Register new keybinding
	 * */
	public static final KeyBinding[] handle(KeyBinding[] src) {
		assert(src != null);
		
		//check dup
		for(int i = 0, l = src.length; i < l; ++i) {
			if(src[i] == ACT_BODYTECH) return src;
		}
		
		//regisiter
		KeyBinding[] result = Arrays.copyOf(src, src.length + 1);
		result[src.length] = ACT_BODYTECH;
		
		return result;
	}
	
	/**
	 * Handle the input information
	 * */
	public static final void handle() {
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		if(player == null) return;
		
		//prepare
		GameSettings sett = Minecraft.getMinecraft().gameSettings;
		final int key = Keyboard.getEventKey();
		
		//-------------------------HANDLE TECH ACT
		
		//check act
		if(key == sett.keyBindForward.keyCode) {
			click2(TIMER_HEAD, () -> act(QuickSlot.HEAD, player));
		} else if(key == ACT_BODYTECH.keyCode) {
			click2(TIMER_BODY, () -> act(QuickSlot.BODY, player));
		} else if(key == sett.keyBindJump.keyCode) {
			click2(TIMER_FEET, () -> act(QuickSlot.FEET, player));
		}
		
		//check item bind tech act
		if(sett.keyBindUseItem.pressed) SENTRY_ITEM = true;
		else if(SENTRY_ITEM) {
			act(player.getHeldItem(), player);
			SENTRY_ITEM = false;
		}
	}
	
	/**
	 * Act the tech
	 * */
	private static final void act(QuickSlot slot, EntityPlayer player) {
		try(ContextImpl conte = new OptionImpl(player).alloc()) {
			TechBoard board = TMP;
			conte.peek(board);
			Technique tech = board.quick(slot);
			
			if(tech != null) conte.act(tech);
		}
	}
	
	/**
	 * Act the tech in stack
	 * */
	private static final void act(ItemStack stack, EntityPlayer ep) {
		if(stack == null) return;
		assert(ep != null);
		UUID bind = NBT.get(stack.stackTagCompound);
		if(bind == null) return;
		
		//try act
		try(ContextImpl conte = new OptionImpl(ep).alloc()) {
			TechBoard board = TMP;
			conte.peek(board);
			
			Technique tech = board.deal(bind);
			if(tech == null) return;
			
			conte.act(tech);
		}
	}
	
	/**
	 * Check double click
	 * */
	private static final boolean click2(LongHolder timer, Runnable proxy) {
		long get = timer.value;
		
		if(Keyboard.getEventKeyState()) {
			long t = Minecraft.getSystemTime();
			
			if(get < 0 && t + get < 250) {
				proxy.run();
				timer.value = t;
				
				return true;
			}
			
			timer.value = t;
		} else if(get > 0) timer.value = -get;
		
		return false;
	}
}
