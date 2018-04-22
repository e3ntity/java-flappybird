import java.io.IOException;
import java.io.File;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Sprite {
	private BufferedImage image;
	private float x, y;
	private int width, height;
	
	public Sprite(BufferedImage image, float x, float y, int width, int height) {
		/*
			Image can be null. In that case an image must later on be loaded via loadImage(String path)
		*/
		this.image = image;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void draw(Graphics2D g) {
		g.drawImage(this.image, (int)this.x, (int)this.y, this.width, this.height, null);
	}
	
	public void move(float x, float y) {
		this.x += x;
		this.y += y;
	}
	
	public void moveX(float x) {
		this.move(x, 0);
	}
	
	public void moveY(float y) {
		this.move(0, y);
	}
	
	public boolean inBounds(Rectangle bounds) {
		/*
			Checks if this sprite is within the bounds of a rectangle
			Returns false if not within bounds, else true
		*/
		if (this.x < bounds.getX() ||
			this.x + this.width > bounds.getX() + bounds.getWidth() ||
			this.y < bounds.getY() ||
			this.y + this.height > bounds.getY() + bounds.getHeight())
			return false;
		return true;
	}
	
	public static boolean detectCollision(Sprite s1, Sprite s2) {
		/*
			Checks if s1 and s2 overlap each other
			Returns true if they overlap, else false
		*/
		
		if ((s1.getX() >= s2.getX() && s1.getX() <= s2.getX() + s2.getWidth()) ||
				(s1.getX() <= s2.getX() && s1.getX() + s1.getWidth() >= s2.getX()))
		{
			if ((s1.getY() >= s2.getY() && s1.getY() <= s2.getY() + s2.getHeight()) ||
					(s1.getY() <= s2.getY() && s1.getY() + s1.getHeight() >= s2.getY()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean loadImage(String path) {
		BufferedImage img;
		
		img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			System.out.printf("in Sprite::loadImage: failed to read image: %s\n", e.getMessage());
			return false;
		}
		
		this.image = img;
		
		return true;
	}
	
	public Rectangle getDimensions() {
		return new Rectangle((int)this.x,
				(int)this.y,
				this.width,
				this.height);
	}
	
	public float getX() {
		return this.x;
	}
	
	public float getY() {
		return this.y;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
}