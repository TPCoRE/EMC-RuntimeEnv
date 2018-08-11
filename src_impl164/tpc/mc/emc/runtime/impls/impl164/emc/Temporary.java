package tpc.mc.emc.runtime.impls.impl164.emc;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import org.omg.CORBA.IntHolder;

import tpc.mc.emc.Stepable;
import tpc.mc.emc.platform.standard.IContext;
import tpc.mc.emc.platform.standard.IMath;
import tpc.mc.emc.platform.standard.IOption;
import tpc.mc.emc.tech.IAttribute;
import tpc.mc.emc.tech.Technique;

/**
 * 一些科技，下次更新会删除(只要一支持科技的世界生成)
 * */
@Deprecated
public enum Temporary implements IAttribute {
	
	/**
	 * Fight to win or die
	 * */
	WINORDIE {
		
		@Override
		public Stepable tack(IOption opt) {
			return new Stepable() {
				private int ticked = -1;
				
				@Override
				public Stepable next() {
					try(IContext conte = opt.alloc()) {
						if(this.ticked == -1) {
							//check duplicate act
							IntHolder counter = new IntHolder(0);
							conte.check((x) -> { if(x.attribute() == WINORDIE) counter.value++; });
							if(counter.value != 1) return null;
							
							this.ticked = conte.tickes(); //record
							
							//effect player
							conte.tired();
							if(conte.weak()) {
								conte.assault();
								conte.assault();
								
								conte.tired();
								conte.tired();
							} else if(RNG.nextInt(16) == 0) {
								conte.assault();
								
								conte.tired();
								conte.tired();
								conte.tired();
								conte.tired();
							}
						} else if(conte.tickes() - this.ticked > 30) return null; //end up
						
						//continue
						return this;
					}
				}
			};
		}
		
		@Override
		public Iterable<String> info(Locale loc) {
			if(loc == null) return null;
			
			if(loc.getLanguage().equals("zh")) return Arrays.asList("背水一战", "最后一次了!");
			if(loc.getLanguage().equals("en")) return Arrays.asList("Fight to Win or Die", "It is the last time!");
			
			return null;
		}
	}, 
	
	/**
	 * Lightning bolt storm
	 * */
	LITSTORM {
		private transient boolean delay;
		
		@Override
		public Stepable tack(IOption opt) {
			return new Stepable() {
				private int ticked = -1;
				
				@Override
				public Stepable next() {
					try(IContext conte = opt.alloc()) {
						if(this.ticked == -1) {
							//check duplicate act
							IntHolder counter = new IntHolder(0);
							conte.check((x) -> { if(x.attribute() == LITSTORM) counter.value++; });
							if(counter.value != 1) {
								delay = true;
								return null;
							}
							
							this.ticked = conte.tickes(); //record
							for(int i = 0, l = 30 + RNG.nextInt(40); i < l; ++i) conte.tired();
						} else {
							if(delay) { //delay dead line
								delay = false;
								this.ticked = conte.tickes();
							} else if(conte.tickes() - this.ticked > 20) return null; //end up
						}
						
						//check weak status
						Random rng = RNG;
						if(conte.weak() && rng.nextInt(32) != 0) return null;
						
						//粒子效果
						if(opt.model()) {
							for(int i = 0, l = 270 + rng.nextInt(48); i < l; ++i) {
								conte.particle(rng.nextDouble() * 16 * (rng.nextBoolean() ? 1 : -1), rng.nextDouble() * 16 * (rng.nextBoolean() ? 1 : -1), rng.nextDouble() * 16 * (rng.nextBoolean() ? 1 : -1), 0, 0, 0);
							}
						}
						
						//lit
						conte.tired();
						if(rng.nextInt(8) == 0) {
							Object handler = conte.randLivingBase();
							
							conte.tired();
							if(handler == null) return null; //end up
							conte.lightning(conte.posX(handler), conte.posY(handler), conte.posZ(handler));
							//tried 3 times
							conte.tired();
							conte.tired();
							conte.tired();
						}
						
						//continue
						return this;
					}
				}
			};
		}
		
		@Override
		public Iterable<String> info(Locale loc) {
			if(loc == null) return null;
			
			if(loc.getLanguage().equals("zh")) return Arrays.asList("雷电风暴", "召唤一次雷电风暴");
			if(loc.getLanguage().equals("en")) return Arrays.asList("Lightning Bolt Storm", "Summon a lightning bolt storm");
			
			return null;
		}
	}, 
	
	/**
	 * Lightning strike, summon a lightning Bolt
	 * */
	LITSTRIKE {
		
		@Override
		public Stepable tack(IOption opt) {
			return new Stepable() {
				
				@Override
				public Stepable next() {
					try(IContext conte = opt.alloc()) {
						//check duplicate act&weak
						IntHolder counter = new IntHolder(0);
						conte.check((x) -> { if(x.attribute() == LITSTRIKE) counter.value++; });
						if(counter.value != 1 || (conte.weak() && RNG.nextInt(16) != 0)) return null;
						
						//spawn bolt
						double[] pos = conte.raycast();
						conte.lightningSpeci(pos[0], pos[1], pos[2]);
						//tired
						for(int i = 0, l = 25 + RNG.nextInt(10); i < l; ++i) conte.tired();
						
						return null; //end up
					}
				}
			};
		}
		
		@Override
		public Iterable<String> info(Locale loc) {
			if(loc == null) return null;
			
			if(loc.getLanguage().equals("zh")) return Arrays.asList("雷击", "召唤一次雷击");
			if(loc.getLanguage().equals("en")) return Arrays.asList("Lightning Strike", "Summon a lightning bolt");
			
			return null;
		}
	}, 
	
	/**
	 * Direct your direction, force moving
	 * */
	DIRECTION {
		private transient boolean delay;
		
		@Override
		public Stepable tack(IOption opt) {
			return new Stepable() {
				private int ticked = -1;
				
				@Override
				public Stepable next() {
					try(IContext conte = opt.alloc()) {
						if(this.ticked == -1) {
							//check duplicate act
							IntHolder counter = new IntHolder(0);
							conte.check((x) -> { if(x.attribute() == DIRECTION) counter.value++; });
							if(counter.value != 1) {
								delay = true;
								return null;
							}
							
							this.ticked = conte.tickes(); //record
						} else {
							if(delay) { //delay dead line
								delay = false;
								this.ticked = conte.tickes();
							} else if(conte.tickes() - this.ticked > 4) return null; //end up
						}
						
						//check weak status
						Random rng = RNG;
						if(conte.weak() && rng.nextInt(16) != 0) return null;
						
						//粒子效果
						if(opt.model()) {
							for(int i = 0, l = 40 + rng.nextInt(20); i < l; ++i) {
								conte.particle(rng.nextDouble(), rng.nextDouble(), rng.nextDouble(), 0, 0, 0);
							}
						}
						
						//加速
						conte.accel(-conte.velocityX(), -conte.velocityY(), -conte.velocityZ());
						conte.accel(3);
						conte.clearFallStatus();
						//tried 3 times
						conte.tired();
						conte.tired();
						conte.tired();
						
						//continue
						return this;
					}
				}
			};
		}
		
		@Override
		public Iterable<String> info(Locale loc) {
			if(loc == null) return null;
			
			if(loc.getLanguage().equals("zh")) return Arrays.asList("定向冲刺", "突进！");
			if(loc.getLanguage().equals("en")) return Arrays.asList("Direction", "Rush!");
			
			return null;
		}
	}, 
	
	/**
	 * Double jump, allow player jump twice
	 * */
	DOUBLEJUMP {
		
		@Override
		public Stepable tack(IOption opt) {
			return new Stepable() {
				private boolean acted;
				
				@Override
				public Stepable next() {
					try(IContext conte = opt.alloc()) {
						if(!this.acted) { //first start!, jump
							//check duplicate act&weak status
							IntHolder counter = new IntHolder(0);
							conte.check((x) -> { if(x.attribute() == DOUBLEJUMP) counter.value++; });
							if(counter.value != 1 || (conte.weak() && RNG.nextInt(5) != 0)) return null;
							
							//do
							if(opt.model()) { //粒子效果
								IMath math = IMath.IMPL;
								
								double r = 0.65D;
								int di = 2;
								int y = 0;
								
								//spawn lay
								for(int lay = 0; lay < 3; ++lay) {
									y -= lay * r;
									di += 1;
									
									for(int i = 0; i < 180; i += di) {
										conte.particle(math.sin(i) * r, y, math.cos(i) * r, 0, 0, 0);
									}
									
									//post fix
									r -= 0.2D;
								}
							}
							
							//向上的加速效果
							conte.accel(0, -conte.velocityY() + 0.725, 0);
							conte.tired();
							
							//mark & clear fall down status
							this.acted = true;
							conte.clearFallStatus();
						} else if(!conte.airborne()) return null; //end up
						return this; //continue
					}
				}
			};
		}
		
		@Override
		public Iterable<String> info(Locale loc) {
			if(loc == null) return null;
			
			if(loc.getLanguage().equals("zh")) return Arrays.asList("双段跳", "允许你多跳一次");
			if(loc.getLanguage().equals("en")) return Arrays.asList("Double Jump", "Jump twice");
			
			return null;
		}
	};
	
	/**
	 * Addition，下个版本就跟着没了
	 * */
	public static final void addition(boolean isRemote, TechBoard board) {
		if(isRemote) return;
		
		board.toggle(new Technique(DOUBLEJUMP), true);
		board.toggle(new Technique(DIRECTION), true);
		board.toggle(new Technique(LITSTRIKE), true);
		board.toggle(new Technique(LITSTORM), true);
		board.toggle(new Technique(WINORDIE), true);
	}
	
	private static final Random RNG = new Random();
}
