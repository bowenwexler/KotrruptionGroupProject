import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.lwjgl.opengl.GL11.*;

public class KotGame extends Game implements Scene 
{
	public static void main(String[] args)
	{
		// construct a DemoGame object and launch the game loop
		KotGame game = new KotGame();
		game.registerGlobalCallbacks();

		SimpleMenu menu = new SimpleMenu();
		
		menu.addItem(new SimpleMenu.SelectableText(240, 400, 20, 20, "Launch Game", 1, 0, 0, 1, 1, 1), game);
		menu.addItem(new SimpleMenu.SelectableText(240, 360, 20, 20, "Exit", 1, 0, 0, 1, 1, 1), null);
		menu.addBackground("kotblini.png");
		menu.addTitle("Kotrruption", 200, 280);
		menu.select(0);

		game.setScene(menu);
		game.gameLoop();
	}
	
	private List<Target> targets;
	private List<Bullet> bullets;
	private List<EnemyBullet> badBullets;
	private List<Background> backgrounds;
	private Player player;
	private int score;
	public boolean timer = true;
	private int count = 0;
	Text text;
	Sound end;
	double currTime;
	Sound hunger;
	Sound theme;
	Sound humanoid;
	
	public static int mouseCount = 0;
	Counter Counter;
	timeDisplay timeDisplay;
	
	public KotGame()
	{
		initUI(600, 800, "Kotrruption");
		glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		
		humanoid = new Sound("res/humanoid.wav");
		theme = new Sound("res/cooker.wav");
		end = new Sound("res/end.wav");
		hunger = new Sound("res/ihunger.wav");
		
		targets = new java.util.LinkedList<>();
		bullets = new java.util.LinkedList<>();
		badBullets = new java.util.LinkedList<>();
		backgrounds = new java.util.LinkedList<>();
		backgrounds.add(new Background (900, 1600, 0, 0,-1600));
		backgrounds.add(new Background (900, 1600, 0, 1599,-1600));
		
		player = new Player();
		Counter = new Counter();
		timeDisplay = new timeDisplay();
		
		//humanoid.play();
		//humanoid.setLoop(true);
	}
	
	public void onMouseEvent(int button, int action, int mods)  
	{ 
		if (button==0 && action == GLFW.GLFW_PRESS)
		{
			mouseCount++;
			hunger.play();
		}
		if (mouseCount >= 10)
		{
			SimpleMenu menu = new SimpleMenu();
			
			menu.addItem(new SimpleMenu.SelectableText(240, 400, 20, 20, "Launch Game", 1, 0, 0, 1, 1, 1), currScene);
			menu.addItem(new SimpleMenu.SelectableText(240, 360, 20, 20, "Exit", 1, 0, 0, 1, 1, 1), null);
			
			menu.addBackground("kotblini.png");
			menu.addTitle("Kotrruption", 200, 280);
			menu.select(0);
			
			SimpleMenu victory = new SimpleMenu();
			victory.addTimeScore(GLFW.glfwGetTime());
			victory.addItem(new SimpleMenu.SelectableText(240, 400, 20, 20, "Main Menu", 1, 0, 0, 1, 1, 1), menu);
			victory.addItem(new SimpleMenu.SelectableText(240, 360, 20, 20, "Exit", 1, 0, 0, 1, 1, 1), null);
			victory.select(0);
			victory.addTitle("Victoyr", 250, 280);
			victory.addBackground("kotVictory.png");
			victory.addCounter(mouseCount);
			setScene(victory);
			mouseCount = 0;
		}
	}
	
	public void onKeyEvent(int key, int scancode, int action, int mods)
	{
		if (Game.ui.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE))
		{
			SimpleMenu pause = new SimpleMenu();
			end.play();
			pause.addItem(new SimpleMenu.SelectableText(240, 400, 20, 20, "Resume Game", 1, 0, 0, 1, 1, 1), currScene);
			pause.addItem(new SimpleMenu.SelectableText(240, 360, 20, 20, "Exit", 1, 0, 0, 1, 1, 1), null);
			pause.select(0);
			pause.addTitle("pause", 220, 280);
			pause.addBackground("kotPause.png");
			pause.addCounter(mouseCount);
			pause.timeSet();
			setScene(pause);
		}
	}
	
	private class Counter
	{	
		public void update(int delta)
		{
			text = new Text(70,0, 30, 30, "Clicks: " + mouseCount);
			text.draw();
		}
	}
	
	private enum colorStates {TO_WHITE, TO_RED, TO_GREEN, TO_BLUE};

    private class ColorChangeText extends Text
    {
        colorStates color = colorStates.TO_RED;
        private boolean reddening = false;
        public ColorChangeText(int x, int y, int w, int h, String text){
            super(x,y,w,h, text);
        }

        public void update(int delta){
            float rate = 0.2f;
            switch (color){
                case TO_WHITE:
                    if (this.r >= 0.9 && this.g >= 0.9 && this.b >= 0.9){
                        this.color = colorStates.TO_RED;
                    }
                    else {
                        this.r += (delta*rate)/255f;
                        this.g += (delta*rate)/255f;
                    }
                    break;
                case TO_RED:
                    if (this.r >= 0.9 && this.g <= 0.1 && this.b <= 0.1){
                        this.color = colorStates.TO_GREEN;
                    }
                    else {
                        this.b -= (delta*rate)/255f;
                        this.g -= (delta*rate)/255f;
                    }
                    break;
                case TO_GREEN:
                    if (this.g >= 0.9 && this.r <= 0.1 && this.b <= 0.1){
                        this.color = colorStates.TO_BLUE;
                    }
                    else {
                        this.r -= (delta*rate)/255f;
                        this.g += (delta*rate)/255f;
                    }
                    break;
                case TO_BLUE:
                    if (this.b >= 0.9 && this.r <= 0.1 && this.g <= 0.1){
                        this.color = colorStates.TO_WHITE;
                    }
                    else {
                        this.b += (delta*rate)/255f;
                        this.g -= (delta*rate)/255f;
                    }
                    break;
                default:
                    color = colorStates.TO_WHITE;
                    break;
            }
        }
    }
	
	public void spawnTargets(int count)
	{
		Random r = new Random();
		
		
		for (int i=0; i<count; i++)
		{
			int x = r.nextInt(500);
			int y = r.nextInt(200);
			targets.add(new Target(player, x, y));
		}
	}
	
	public Scene drawFrame(int delta)
	{
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		if (targets.isEmpty())
		{
			spawnTargets(10);
		}
		
		// check for deactivated objects
		Iterator<Target> it = targets.iterator();
		while (it.hasNext()) 
		{
			GameObject o = it.next();
			if (! o.isActive())
			{
				it.remove();
			}
		}
		
		Iterator<Bullet> it2 = bullets.iterator();
		while (it2.hasNext()) 
		{
			GameObject o = it2.next();
			if (! o.isActive())
			{
				it2.remove();
			}
		}
		
		Iterator<EnemyBullet> it3 = badBullets.iterator();
		while (it3.hasNext()) 
		{
			GameObject o = it3.next();
			if (! o.isActive())
			{
				it3.remove();
			}
		}
		
		Iterator<Background> it4 = backgrounds.iterator();
		while (it4.hasNext()) 
		{
			GameObject o = it4.next();
			if (! o.isActive())
			{
				it4.remove();
			}
		}
		
		
		for (GameObject bg : backgrounds)
		{
			bg.update(delta);
		}
		
		for (GameObject bg2 : backgrounds)
		{
			bg2.draw();
		}
		
		if (player.isActive())
		{
			player.update(delta);
		}
		
		for (GameObject t : targets)
		{
			t.update(delta);
		}
		for (GameObject b : bullets)
		{
			b.update(delta);
		}
		for (GameObject b2 : badBullets)
		{
			b2.update(delta);
		}
		for (GameObject b : bullets)
		{
			b.draw();
		}
		for (GameObject b2 : badBullets)
		{
			b2.draw();
		}
		for (GameObject t : targets)
		{
			t.draw();
		}
		if (player.isActive())
		{
			player.draw();
		}
		else
		{
			end.play();
			hunger.play();
		}
		
		timeDisplay.update(delta);
		Counter.update(delta);
		
		return this;
	}
	
	private class timeDisplay
	{
		public void update(int delta)
		{
			text = new Text(230, 0, 30, 30, "Time wasted: " + (int)GLFW.glfwGetTime() + " seconds");
			text.draw();
		}
	}
	
	private class Background extends GameObject
	{
		private Texture bg = new Texture("res/BGStarfield.png",1f);
		private int breakPoint;
		
		public Background(int width, int height, int x, int y, int bp)
		{
			this.hitbox.setSize(width,height);
			this.hitbox.setLocation(x, y);
			breakPoint = bp;
		}
		
		public void draw()
		{
			bg.draw(this);
		}
		
		public void update(int delta)
		{
			
			if (this.hitbox.y < breakPoint)
			{
				this.hitbox.setLocation(0,1550);
			}
			else
			{
				this.hitbox.translate(0, -4);
			}
		}
	}
	
	private class Target extends GameObject
	{
		private Player player;
		private int size=80;
		private Texture texture=null;
		private boolean enemyTimer = true;
		private int enemyCount = 0;
		
		public Target(Player p, int x, int y)
		{
			if (texture==null)
			{
				texture = new Texture("res/kotblini.png",1f);
			}
			this.player = p;
			this.hitbox.setSize(size, size);
			//this.setColor(0,0,0);
			this.hitbox.setLocation(x,y);
		}
		
		public void draw()
		{
			texture.draw(this);
		}

		public void update(int delta)
		{
			enemyCount++;
			if (enemyCount > 100)
			{
				enemyTimer = true;
				enemyCount = 0;
			}
			for (GameObject b : bullets)
			{
				if (b.intersects(this))
				{
					this.deactivate();
					b.deactivate();
				}
				if (b.getHitbox().y < 0)
				{
					b.deactivate();
				}
			}
			if (enemyTimer)
			{
				badBullets.add(new EnemyBullet(this));
				
				enemyTimer = false;
			}
		}
	}
	
	private class Bullet extends GameObject
	{
		public Bullet(Player p)
		{
			this.hitbox.setSize(5, 5);
			this.hitbox.setLocation(p.getHitbox().x,p.getHitbox().y);
			this.setColor(1,1,0,1);
		}
		
		public void update(int delta)
		{
			float speed=0.70f;
			this.hitbox.translate(0,  (int)(-speed*delta));
		}
	}
	
	private class EnemyBullet extends GameObject
	{
		public EnemyBullet(Target t)
		{
			this.hitbox.setSize(5, 5);
			this.hitbox.setLocation(t.getHitbox().x+30,t.getHitbox().y+30);
			this.setColor(0,1,0,1);
		}
		
		public void update(int delta)
		{
			
			float speed=0.70f;
			this.hitbox.translate(0,  (int)(speed*delta));
		}
	}
	
	private class Player extends GameObject
	{	
		public Player()
		{
			this.hitbox.setSize(30, 30);
			this.hitbox.setLocation(Game.ui.getWidth()/2-15, Game.ui.getHeight()-45);
			this.setColor(1,0,0,1);
		}
		
		// this allows you to steer the player object
		public void update(int delta)
		{
			count++;
			if (count > 15)
			{
				timer = true;
				count = 0;
			}
			for (GameObject b : badBullets)
			{
				if (b.intersects(this))
				{
					this.deactivate();
					b.deactivate();
				}
				if (b.getHitbox().y > 800)
				{
					b.deactivate();
				}
			}
			float speed=0.25f;
			if (Game.ui.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT))
			{
				this.hitbox.translate((int)(-speed*delta), 0);
			}
			if (Game.ui.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT))
			{
				this.hitbox.translate((int)(speed*delta),0);
			}
			if (Game.ui.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE))
			{
				if (timer)
				{
					bullets.add(new Bullet(this));
					
					timer = false;
				}
				
			}
		}
	}
}