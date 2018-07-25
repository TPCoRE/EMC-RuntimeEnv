package tpc.mc.emc.runtime.impls.impl164.mc.client;

import java.util.Arrays;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.util.IHolder;
import tpc.mc.emc.tech.Board;
import tpc.mc.emc.tech.Pool;

/**
 * Input (Keyboard/Mouse) Handler
 * */
public final class InputHandler {
	
	public static final KeyBinding ACT_BODYTECH = new KeyBinding("Act BodyTech(Double Click)", Keyboard.KEY_F);
	
	private static final long INIT = 0;
	private static final IHolder<Long> TIMER_HEAD = new IHolder.Single<Long>().iset(INIT);
	private static final IHolder<Long> TIMER_BODY = new IHolder.Single<Long>().iset(INIT);
	private static final IHolder<Long> TIMER_FEET = new IHolder.Single<Long>().iset(INIT);
	
	/**
	 * Register new keybinding
	 * */
	public static final KeyBinding[] handle(KeyBinding[] src) {
		assert(src != null);
		
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
		final int feet = sett.keyBindJump.keyCode;
		final int head = sett.keyBindForward.keyCode;
		final int body = ACT_BODYTECH.keyCode;
		
		//receive
		final int key = Keyboard.getEventKey();
		final boolean press = Keyboard.getEventKeyState();
		
		//-------------------------HANDLE TECH ACT
		
		//check act
		if(key == head) {
			click2(TIMER_HEAD, () -> act(0, player));
		} else if(key == body) {
			click2(TIMER_BODY, () -> act(1, player));
		} else if(key == feet) {
			click2(TIMER_FEET, () -> act(2, player));
		}
	}
	
	/**
	 * Act the tech, mode: 0=head, 1=body, 2=feet
	 * */
	private static final Object act(int mode, EntityPlayer player) {
		Pool tech = null;
		
		try(IContext conte = new OptionImpl(player).alloc()) {
			Board board = conte.ipeek();
			
			switch(mode) {
			case 0: tech = board.headtech(); break;
			case 1: tech = board.bodytech(); break;
			case 2: tech = board.feettech(); break;
			default: assert(false);
			}
			
			System.out.println(mode);
			if(tech != null) conte.act(tech);
		}
		
		return null;
	}
	
	/**
	 * Check double click
	 * */
	private static final boolean click2(IHolder<Long> timer, Supplier proxy) {
		long get = timer.get();
		
		if(Keyboard.getEventKeyState()) {
			long t = Minecraft.getSystemTime();
			
			if(get < 0 && t + get < 250) {
				proxy.get();
				timer.set(t);
				
				return true;
			}
			
			timer.set(t);
		} else if(get > 0) timer.set(-get);
		
		return false;
	}
}
