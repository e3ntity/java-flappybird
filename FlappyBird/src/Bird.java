import java.awt.Rectangle;
import java.awt.Graphics2D;


public class Bird {
	private Sprite spriteBird;
	private float movement;
	private int ix, iy;		// "initial" x/y
	
	public Bird(String spritePath) {
		this.ix = Game.windowWidth / 2 - Game.windowWidth / Game.configGetInt(new String[] {"game", "initialResolution", "width"}) * Game.configGetInt(new String[] {"sprite", "bird", "width"});
		this.iy= Game.windowHeight / 2 - Game.windowHeight / Game.configGetInt(new String[] {"game", "initialResolution", "height"}) * Game.configGetInt(new String[] {"sprite", "bird", "height"});
		
		this.spriteBird = new Sprite(null, this.ix, this.iy, Game.windowWidth / 600 * 30, Game.windowWidth / 600 * 20);
		this.spriteBird.loadImage(spritePath);
		
		this.movement = 0;
	}
	
	public void reset() {
		this.spriteBird.setX(this.ix);
		this.spriteBird.setY(this.iy);
		this.movement = 0;
	}
	
	public void evaluate() {
		if (this.movement < Game.configGetInt(new String[] {"game", "speed", "bird", "maxY"}) * Game.configGetInt(new String[] {"game", "speed", "multiplier"}))
			this.movement += Game.configGetInt(new String[] {"game", "speed", "bird", "gravity"}) * Game.deltaTime;
		
		this.spriteBird.moveY(movement * Game.deltaTime);
	}
	
	public void flap() {
		this.movement = -Game.configGetFloat(new String[] {"game", "speed", "bird", "flap"}) * Game.configGetInt(new String[] {"game", "speed", "bird", "gravity"});
	}
	
	public void draw(Graphics2D g) {
		this.spriteBird.draw(g);
	}
	
	public boolean inBounds(Rectangle rect) {
		return this.spriteBird.inBounds(rect);
	}
	
	public void moveX(float x) {
		this.spriteBird.moveX(x);
	}
	
	public void moveY(float y) {
		this.spriteBird.moveY(y);
	}
	
	public float getX() {
		return this.spriteBird.getX();
	}
	
	public float getY() {
		return this.spriteBird.getY();
	}
	
	public float getMovement() {
		return this.movement;
	}
	
	public Sprite getSprite() {
		return this.spriteBird;
	}
}