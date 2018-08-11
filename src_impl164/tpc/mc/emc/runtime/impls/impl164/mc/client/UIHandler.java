package tpc.mc.emc.runtime.impls.impl164.mc.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumChatFormatting;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Tessellator;
import tpc.mc.emc.runtime.impls.impl164.emc.ContextImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.OptionImpl;
import tpc.mc.emc.runtime.impls.impl164.emc.QuickSlot;
import tpc.mc.emc.runtime.impls.impl164.emc.TechBoard;
import tpc.mc.emc.runtime.impls.impl164.mc.NBT;
import tpc.mc.emc.runtime.impls.impl164.mc.network.PacketIB;
import tpc.mc.emc.runtime.util.Reflect;
import tpc.mc.emc.tech.Technique;

/**
 * GUI Handler
 * */
public final class UIHandler {
	
	private static final Method DTEXT = Reflect.located(GuiContainer.class, "func_102021_a", List.class, int.class, int.class);
	private static final TechBoard TMP = new TechBoard();
	private static final String NONE = EnumChatFormatting.OBFUSCATED + "ANONYMOUS";
	
	private static QuickSlot SELECTED;
	private static long PAGETIMER0;
	private static long PAGETIMER1;
	private static int PAGE;
	
	/**
	 * Handle GuiInventory
	 * */
	public static final void handle(GuiContainer gui, int left, int top, int mouseX, int mouseY) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		
		mouseX -= left;
		mouseY -= top;
		
		//in area
		if(26 <= mouseX && mouseX <= 77 && 8 <= mouseY && mouseY <= 77) {
			int headX = 61;
			int headY = 14;
			int bodyX = 68;
			int bodyY = 42;
			int feetX = 62;
			int feetY = 70;
			
			boolean head = check0(headX, headY, 2.5F, mouseX, mouseY);
			boolean body = check0(bodyX, bodyY, 2.5F, mouseX, mouseY);
			boolean feet = check0(feetX, feetY, 2.5F, mouseX, mouseY);
			
			GameSettings settings = mc.gameSettings;
			FontRenderer font = mc.fontRenderer;
			int heigh = font.FONT_HEIGHT;
			int heighx = (heigh >>> 1);
			
			//offset to global pos
			GL11.glTranslatef(0, 0, 100);
			headX += left;
			bodyX += left;
			feetX += left;
			headY += top;
			bodyY += top;
			feetY += top;
			
			//draw joints(the blue point|red point when reach)
			joint(headX, headY, head || SELECTED == QuickSlot.HEAD);
			joint(bodyX, bodyY, body || SELECTED == QuickSlot.BODY);
			joint(feetX, feetY, feet || SELECTED == QuickSlot.FEET);
			
			//check selected(it will turn red)
			if(Mouse.isButtonDown(0) && mouseX >= 56) {
				SELECTED = null;
				
				if(head) SELECTED = QuickSlot.HEAD;
				else if(body) SELECTED = QuickSlot.BODY;
				else if(feet) SELECTED = QuickSlot.FEET;
			}
			
			//prepare draw
			ContextImpl conte = new OptionImpl(player).alloc();
			TechBoard board = TMP;
			conte.export(board);
			boolean peek = true;
			
			Technique headTech = board.quick(QuickSlot.HEAD);
			Technique bodyTech = board.quick(QuickSlot.BODY);
			Technique feetTech = board.quick(QuickSlot.FEET);
			Locale locale = Locale.getDefault(); //CUSTOM LANG ENV
			String info;
			
			//draw (line and current tech in quick slot)
			if(headTech != null) {
				line(headX, headY, headX = left + 97, headY = top + 6, 0xAF0000FF, 0);
				info = firstSafely(headTech.info(locale));
				
				if(info != null) textEntry(headX, headY + heigh, info, gui);
				else gui.drawString(font, NONE, headX, headY - heighx, 0xFFFF0000);
			} if(bodyTech != null) {
				line(bodyX, bodyY, bodyX = left + 102, bodyY = top + 35, 0xAF0000FF, 0);
				info = firstSafely(bodyTech.info(locale));
				
				if(info != null) textEntry(bodyX, bodyY + heigh, info, gui);
				else gui.drawString(font, NONE, bodyX, bodyY - heighx, 0xFFFF0000);
			} if(feetTech != null) {
				line(feetX, feetY, feetX = left + 93, feetY = top + 75, 0xAF0000FF, 0);
				info = firstSafely(feetTech.info(locale));
				
				if(info != null) textEntry(feetX, feetY + heigh, info, gui);
				else gui.drawString(font, NONE, feetX, feetY - heighx, 0xFFFF0000);
			}
			
			//draw choices board
			if(SELECTED != null) {
				Technique seledTech = board.quick(SELECTED);
				ArrayList<Technique> avails = new ArrayList<>(board.avail());
				
				boolean up = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(settings.keyBindRight.keyCode);
				boolean down = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(settings.keyBindLeft.keyCode);
				
				//accept keyboard page modify
				if(up || down) {
					if(PAGETIMER0 == 0) {
						PAGETIMER0 = Minecraft.getSystemTime();
						
						if(up) PAGE++;
						if(down) PAGE--;
					} else {
						PAGETIMER1 += (Minecraft.getSystemTime() - PAGETIMER0) >>> 1;
						
						//quick modify
						if(PAGETIMER1 >= 100) {
							if(up) PAGE++;
							if(down) PAGE--;
							
							PAGETIMER1 = 0;
						}
					}
				} else PAGETIMER0 = PAGETIMER1 = 0;
				
				//modify page
				if(PAGE * 4 >= avails.size()) PAGE = 0;
				if(PAGE < 0) PAGE = (avails.size() - 1) >>> 2;
				
				//move origin
				left += 25;
				top += 7;
				
				//draw board back ground
				rect(left, top, left + 30, top + 71, 0x4B000000, 0);
				
				//draw page
				gui.drawString(font, String.valueOf(PAGE).concat("/").concat(String.valueOf((avails.size() - 1) >>> 2)), left + 30, top + 71 - heighx, 0xFFFFFFFF);
				List<String> rest_info = null;
				
				//draw avails
				for(int i = PAGE * 4, l = Math.min(i + 4, avails.size()); i < l; ++i) {
					Technique tech = avails.get(i);
					Iterable<String> info_src = tech.info(locale);
					info = firstSafely(info_src);
					int top0 = top + (i & 3) * 18;
					
					//special draw
					if(tech.equals(seledTech)) rect(left, top0, left + 30, top0 + 18, 0x6E0076FF, 0);
					if(info == null) rect(left, top0, left + 30, top0 + 18, 0x5BFF0000, 0);
					
					//draw basic info
					if(info != null) gui.drawString(font, font.trimStringToWidth(info, 28), left + 4, top0 + heigh, 0x9EFFFFFF);
					
					//draw rest info&handle tech
					if(top0 < mouseY - 6 + top && mouseY - 6 + top < top0 + 18 && mouseX < 56) {
						rect(left, top0, left + 30, top0 + 18, 0x55FFFF00, 0); //draw select
						
						//get rest info
						if(info != null) {
							List<String> pointer = rest_info = new ArrayList<>();
							info_src.forEach((x) -> pointer.add(x));
						}
						
						//handle tech
						if(Mouse.isButtonDown(0)) {
							ItemStack dragged = player.inventory.getItemStack();
							
							//judge
							if(dragged == null) {
								if(seledTech != tech) { //change select tech
									board.quick(SELECTED, seledTech = tech);
									peek = false;
								}
							} else { //item tech bind
								if(!NBT.compare(dragged.stackTagCompound, tech.identifier())) Minecraft.getMinecraft().getNetHandler().addToSendQueue(new PacketIB(tech));
							}
						} else if(Mouse.isButtonDown(1)) {
							if(seledTech != null) {
								board.quick(SELECTED, seledTech = null);
								peek = false;
							}
						}
					}
				}
				
				//draw rest info
				if(rest_info != null && rest_info.size() > 0) textEntry(mouseX + left - 19, mouseY + top - 6, rest_info, gui);
			}
			
			//close context
			if(!peek) conte.accept(board);
			conte.close();
		}
	}
	
	/**
	 * Get the postfix of the itemstack
	 * */
	public static final void postfix(List tips, EntityPlayer ep, ItemStack is) {
		assert(ep != null);
		assert(is != null);
		assert(tips != null);
		
		NBTTagCompound nbt = is.stackTagCompound;
		if(!NBT.has(nbt)) return;
		
		try(ContextImpl conte = new OptionImpl(ep).alloc()) {
			TechBoard board = TMP;
			conte.peek(board);
			
			Technique binded = board.deal(NBT.get(nbt));
			if(binded == null) return;
			
			String str = firstSafely(binded.info(Locale.getDefault()));
			if(str == null) str = EnumChatFormatting.RED + NONE;
			
			tips.add(str);
		}
	}
	
	/**
	 * Get safely
	 * */
	private static final String firstSafely(Iterable<String> iter) {
		if(iter == null) return null;
		Iterator<String> i = iter.iterator();
		if(i == null || !i.hasNext()) return null;
		
		return i.next();
	}
	
	/**
	 * Check if in bound
	 * */
	private static final boolean check0(float x, float y, float bound, float px, float py) {
		return (x - bound) <= px && px <= (x + bound) && (y - bound) <= py && py <= (y + bound);
	}
	
	/**
	 * Call the special method
	 * */
	public static final void textEntry(float x, float y, String content, GuiContainer gui) {
		textEntry(x, y, Arrays.asList(content), gui);
	}
	
	/**
	 * Call the special method
	 * */
	public static final void textEntry(float x, float y, List<String> content, GuiContainer gui) {
		try {
			DTEXT.invoke(gui, content, Math.round(x) - 8, Math.round(y));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Draw a tech joint
	 * */
	public static final void joint(float x, float y, boolean selected) {
		Tessellator tesser = Tessellator.instance;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		if(selected) GL11.glColor3f(195, 0, 0);
		else GL11.glColor3f(0, 93, 149);
		tesser.startDrawingQuads();
		
		//draw
		if(selected) {
			x -= 1;
			y -= 1;
			
			tesser.addVertex(x, y, 0);
			tesser.addVertex(x, y + 2, 0);
			tesser.addVertex(x + 2, y + 2, 0);
			tesser.addVertex(x + 2, y, 0);
		} else {
			tesser.addVertex(x, y, 0);
			tesser.addVertex(x, y + 1.5, 0);
			tesser.addVertex(x + 1.5, y + 1.5, 0);
			tesser.addVertex(x + 1.5, y, 0);
		}
		
		tesser.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	/**
	 * Draw a line, ARGB
	 * */
	public static final void line(float x0, float y0, float x1, float y1, int color, float zlevel) {
		Tessellator tesser = Tessellator.instance;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		tesser.startDrawing(GL11.GL_LINES);
		tesser.addVertex(x0, y0, zlevel);
		tesser.addVertex(x1, y1, zlevel);
		tesser.draw();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	/**
	 * Draw a rect
	 * */
	public static final void rect(float x0, float y0, float x1, float y1, int color, float zlevel) {
		Tessellator tesser = Tessellator.instance;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		tesser.startDrawingQuads();
		tesser.addVertex(x0, y0, zlevel);
		tesser.addVertex(x0, y1, zlevel);
		tesser.addVertex(x1, y1, zlevel);
		tesser.addVertex(x1, y0, zlevel);
		tesser.draw();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
