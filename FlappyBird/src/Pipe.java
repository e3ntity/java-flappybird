import java.util.Random;
import java.awt.Graphics2D;

public class Pipe {
	private boolean active;
	private int gapSize;
	private Sprite spritePipeUpper,
				spritePipeLower;
	
	public Pipe(String spritePathU, String spritePathL, int gapSize) {
		Random rand;
		int pipeWidth, pipeHeight, lowerPipeUpperBound;
		
		this.gapSize = gapSize;
		
		rand = new Random();
		lowerPipeUpperBound = rand.nextInt((int)Math.floor((Game.windowHeight * 0.75) - this.gapSize)) + this.gapSize;
		
		pipeWidth = (int)(Game.windowWidth / 600 * 60);
		pipeHeight = (int)(Game.windowHeight * 0.75);
		
		this.spritePipeUpper = new Sprite(null, Game.windowWidth, lowerPipeUpperBound - (int)(Game.windowHeight * 0.75) - this.gapSize, pipeWidth, pipeHeight);
		this.spritePipeLower = new Sprite(null, Game.windowWidth, lowerPipeUpperBound, pipeWidth, pipeHeight);
		
		this.spritePipeUpper.loadImage(spritePathU);
		this.spritePipeLower.loadImage(spritePathL);
		
		this.active = true;
	}
	
	public boolean detectCollision(Sprite sprite) {
		return (Sprite.detectCollision(this.spritePipeUpper, sprite) || Sprite.detectCollision(this.spritePipeLower, sprite));
	}
	
	public boolean detectScore(Sprite sprite) {
		if (this.active && sprite.getX() > this.getSpriteL().getX() + this.getSpriteL().getWidth())
		{
			this.active = false;
			return true;
		}
		
		return false;
	}
	
	public void evaluate() {
		this.spritePipeUpper.moveX(-100 * Game.deltaTime);
		this.spritePipeLower.moveX(-100 * Game.deltaTime);
	}
	
	public void draw(Graphics2D g) {
		this.spritePipeUpper.draw(g);
		this.spritePipeLower.draw(g);
	}
	
	public Sprite getSpriteU() {
		return this.spritePipeUpper;
	}
	
	public Sprite getSpriteL() {
		return this.spritePipeLower;
	}
}