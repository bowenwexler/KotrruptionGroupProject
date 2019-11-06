

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

public class SimpleMenu implements Scene {

	public static interface SelectableObject
	{
		void select();
		void deselect();
		void update(int delta);
		void draw();
	}

	public static class SelectableText  extends Text implements SelectableObject
	{
		private float activeR, activeG, activeB;
		private float inactiveR, inactiveG, inactiveB;

		public SelectableText(int x, int y, int w, int h, String text, 
				float aR, float aG, float aB, float iR, float iG, float iB)
		{
			super(x,y,w,h,text);
			activeR=aR;
			activeG=aG;
			activeB=aB;
			inactiveR=iR;
			inactiveG=iG;
			inactiveB=iB;
		}

		public void select()
		{
			this.setColor(activeR, activeG, activeB);
		}

		public void deselect()
		{
			this.setColor(inactiveR, inactiveG, inactiveB);
		}


	}

	public void addTitle(String text, int x, int y)
	{
		menuTitle = new ColorChangeText(x, y, 40, 40, text);
	}
	
	private enum colorStates {TO_WHITE, TO_RED, TO_GREEN, TO_BLUE};

    public class ColorChangeText extends Text
    {
        colorStates color = colorStates.TO_RED;
        private boolean reddening = false;
        public ColorChangeText(int x, int y, int w, int h, String text){
            super(x,y,w,h, text);
        }

        public void update(int delta){
            float rate = 1.5f;
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
	
	private class Item
	{
		public SelectableObject label;
		public Scene scene;

		public Item(SelectableObject label, Scene scene)
		{
			this.label=label;
			this.scene=scene;
		}

	}
	
	private class Background extends GameObject
	{
		private Texture bg;
		
		public Background(int width, int height, int x, int y, String image)
		{
			bg = new Texture("res/"+image,0.1f);
			this.hitbox.setSize(width,height);
			this.hitbox.setLocation(x, y);
			this.setColor(0f,0f,0f,.5f);
		}
		
		public void draw()
		{
			bg.draw(this);
		}
	}

	
	private class Counter
	{
		int mCount = 0;
		Counter(int mouseCount)
		{
			mCount = mouseCount;
		}
		
		public void update(int delta)
		{
			text = new Text(30,0, 30, 30, "Clicks: " + mCount);
			text.draw();
		}
	}
	
	public void addBackground(String image)
	{
		menuBackground = new Background(900,900, -130, -100, image);
	}
	
	public void addCounter(int mouseCount)
	{
		Counter = new Counter(mouseCount);
	}
	
	public void addTimeScore(double time)
	{
		timeScore = new Text(230, 0, 30, 30, "Time Wasted: " + (int)time + " seconds!");
		victory.play();
	}
	
	private LinkedList<Item> items;
	Text text;
	Text timeScore;
	Counter Counter;
	private Background menuBackground;
	private ColorChangeText menuTitle;
	private int selected;
	double timeHold;
	private boolean go=false;
	Sound victory;
	
	public void timeSet()
	{
		timeHold = GLFW.glfwGetTime();
	}

	public SimpleMenu()
	{
		victory = new Sound("res/victory.wav");
		items=new LinkedList<>();
		selected=0;
		go=false;
	}

	public void reset()
	{
		go=false;
		select(0);
	}

	public void addItem(SelectableObject label, Scene scene)
	{
		items.add(new Item(label, scene));
	}

	public void select(int p)
	{
		items.get(selected).label.deselect();
		items.get(p).label.select();
		selected=p;
	}

	public void go()
	{
		go=true;
	}
	
	public void onKeyEvent(int key, int scancode, int action, int mods)  
	{
		if (action==org.lwjgl.glfw.GLFW.GLFW_PRESS)
		{
			if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_UP)
			{
				select((selected+items.size()-1)%items.size());
			}
			else if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN)
			{
				select((selected+1)%items.size());
			}
			else if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
			{
				go();
			}
		}
		
	};

	public Scene drawFrame(int delta)
	{
		glClearColor(.0f, .0f, .0f, .0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		if (go) 
		{ 
			GLFW.glfwSetTime(timeHold);
			return items.get(selected).scene; 
		}

		for (Item item : items)
		{	
			item.label.update(delta);
			item.label.draw();
		}
		menuTitle.update(delta);
		menuTitle.draw();
		if (menuBackground != null)
		{
			menuBackground.draw();
		}
		if (Counter != null)
		{
			Counter.update(delta);
		}
		
		if (timeScore != null)
		{
			timeScore.draw();
		}
		
		return this;

	}

}
