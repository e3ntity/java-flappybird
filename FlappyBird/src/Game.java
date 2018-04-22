import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JPanel implements KeyListener {
	/* Constants */
	private static final String configFile = "resource/config.json";
	private static final int gameStateMenu = 0,
							gameStateRunning = 1,
							gameStateEnded = 2;	// Pixels a pipe takes in space (left margin + pipe width + right margin)
	
	/* Static variables */
	protected static int windowWidth = 1200,
						windowHeight = 800;
	protected static float deltaTime;
	private static boolean debug = false;
	protected static JSONObject config = null;
	
	/* Variables */
	private int gameState,	// Indicates the state of the game
				framesPerSecond,
				score;
	private long frameDurationTime;
	private Sprite spriteMainBg, 
				spriteGround,
				spritePath;
	private Bird bird;
	private Pipe[] pipes;
	
	/* Methods */
	public Game (JFrame frame, JSONObject config) {
		Game.config = config;
		
		this.setFps(Game.configGetInt(new String[] {"game", "fps"}));
		this.gameState = Game.gameStateMenu;
		this.setupUI();
		frame.addKeyListener(this);
	}
	
	public static void main(String[] args) {
		File configFile;
		FileInputStream configFileStream;
		JSONObject config;
		byte[] configFileContent;
		Game game;
		JFrame frame;
		long frameBeginningTime;
		
		// Load configuration
		configFile = new File(Game.configFile);
		
		try {
			configFileStream = new FileInputStream(configFile);
		} catch (IOException e) {
			System.out.printf("in main: Failed to create file input stream for config: %s\n", e.getMessage());
			return;
		}
		
		configFileContent = new byte[(int)configFile.length()];
		
		try {
			configFileStream.read(configFileContent);
			configFileStream.close();
		} catch (IOException e) {
			System.out.printf("in main: failed to read config file: %s\n", e.getMessage());
			return;
		}
		
		try {
			config = new JSONObject(new String(configFileContent));
		} catch (JSONException e) {
			System.out.printf("in main: failed to setup json object for config: %s\n", e.getMessage());
			return;
		}
		
		// Initialise JFrame
		try {
			frame = new JFrame(config.getJSONObject("text").getString("title"));
		} catch (JSONException e) {
			System.out.printf("in main: failed to load title from config: %s\n", e.getMessage());
			return;
		}
		
		// Initialise Game
		game = new Game(frame, config);
		
		frame.add(game);
		frame.setSize(Game.windowWidth, Game.windowHeight);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Game Loop
		for(;;) {
			frameBeginningTime = System.currentTimeMillis();
			
			game.repaint();
			
			// Wait until frame is over
			while(frameBeginningTime > System.currentTimeMillis() - game.getFrameDuration());
		}
	}
	
	public void setupUI() {
		this.spriteMainBg = new Sprite(null, 0, 0, Game.windowWidth * 3, (int)(Game.windowHeight * 0.75));
		this.spriteGround = new Sprite(null, 0, (int)(Game.windowHeight * 0.75 + Game.windowHeight / 400 * 6), Game.windowWidth * 2, (int)(Game.windowHeight * 0.25));
		this.spritePath = new Sprite(null, 0, (int)(Game.windowHeight * 0.75), Game.windowWidth * 2, (int)(Game.windowHeight / 400 * 6));
		
		this.spriteMainBg.loadImage(Game.configGetString(new String[] {"sprite", "background", "path"}));
		this.spriteGround.loadImage(Game.configGetString(new String[] {"sprite", "ground", "path"}));
		this.spritePath.loadImage(Game.configGetString(new String[] {"sprite", "pathway", "path"}));
		
		this.bird = new Bird(Game.configGetString(new String[] {"sprite", "bird", "path"}));
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d;
		
		super.paint(g);
		
		g2d = (Graphics2D)g;
		// Turn on antialiasing
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw background etc.
		this.spriteMainBg.moveX((float)(-40 * Game.deltaTime));
		if (!this.spriteMainBg.inBounds(new Rectangle(-Game.windowWidth * 2, 0, Game.windowWidth * 5, (int)(Game.windowHeight * 0.75))))
			this.spriteMainBg.setX(0);
		
		this.spriteGround.moveX(-100 * Game.deltaTime);
		if (!this.spriteGround.inBounds(new Rectangle(-600, (int)(Game.windowHeight * 0.75), Game.windowWidth * 3, (int)(Game.windowHeight * 0.25))))
			this.spriteGround.setX(0);
		
		this.spritePath.moveX(-100 * Game.deltaTime);
		if (!this.spritePath.inBounds(new Rectangle(-600, (int)(Game.windowHeight * 0.75), Game.windowWidth * 3, (int)(Game.windowHeight * 0.25))))
			this.spritePath.setX(0);
		
		this.spriteMainBg.draw(g2d);
		this.spritePath.draw(g2d);
		
		// Check game state and handle accordingly
		if (this.gameState == Game.gameStateMenu) {
			this.handleMenu(g2d);
		}
		else if (this.gameState == Game.gameStateRunning) {
			this.handleRunning(g2d);
		}
		else if (this.gameState == Game.gameStateEnded) {
			this.handleEnded(g2d);
		} else {
			throw new java.lang.RuntimeException(String.format("in paint: unknown game state: %d", this.gameState));
		}
		
		this.spriteGround.draw(g2d);
		
		if (Game.debug)
		{
			Game.drawCenteredText(g2d,
					"Debug enabled",
					new Rectangle(0, 0, Game.windowWidth / 5, Game.windowHeight / 10),
					new Font("Courier New", Font.PLAIN, 28));
		}
	}

	private void handleMenu(Graphics2D g) {
		drawCenteredText(g,
				Game.configGetString(new String[] {"text", "title"}),
				new Rectangle(0, 0, Game.windowWidth, Game.windowHeight / 3),
				new Font("Ubuntu", Font.BOLD, 28));
		drawCenteredText(g,
				Game.configGetString(new String[] {"text", "menu", "instruction"}),
				new Rectangle(0, 0, Game.windowWidth, Game.windowHeight / 2),
				new Font("Ubuntu", Font.BOLD, 28));
		
		if (this.bird.getY() > Game.windowHeight * 0.45)
			this.bird.flap();
		this.bird.evaluate();
		this.bird.draw(g);
	}
	
	private void handleRunning(Graphics2D g) {
		Rectangle r;
		int i, j, maxPipeCount;
		Sprite s;
		
		this.bird.evaluate();
		this.bird.draw(g);
		
		maxPipeCount = Game.configGetInt(new String[] {"game", "maxPipeCount"});
		
		for (i = 0; i < maxPipeCount; i++)
		{
			this.pipes[i].evaluate();
			if (this.pipes[i].getSpriteL().inBounds(new Rectangle(Game.windowWidth - (int)(Game.windowWidth / maxPipeCount),
					0,
					(int)(Game.windowWidth / maxPipeCount * 2),
					Game.windowHeight * 2))) break;
		}
		
		if (i == maxPipeCount) {
			for (i = 0; i < maxPipeCount;)
			{
				s = this.pipes[i].getSpriteL();
				if (s.inBounds(new Rectangle(-s.getWidth() * 2,
						0,
						s.getWidth() * 2,
						Game.windowHeight * 2)))
				{
					for (j = 0; j < maxPipeCount - 1; j++)
						this.pipes[j] = this.pipes[j + 1];
					this.pipes[maxPipeCount - 1] = new Pipe(Game.configGetString(new String[] {"sprite", "pipeUpper", "path"}),
							Game.configGetString(new String[] {"sprite", "pipeLower", "path"}),
							(int)(Game.windowHeight * 0.2));
					
				} else break;
			}
		}
		
		for (i = 0; i < maxPipeCount; i++)
			this.pipes[i].draw(g);
		
		if (Game.debug)
		{
			g.setColor(Color.RED);
			r = this.bird.getSprite().getDimensions();
			g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
			g.drawRect(Game.windowWidth - (int)(Game.windowWidth / maxPipeCount), 0, (int)(Game.windowWidth / maxPipeCount * 2), Game.windowHeight);
			for (i = 0; i < maxPipeCount; i++) {
				r = this.pipes[i].getSpriteU().getDimensions();
				g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
				r = this.pipes[i].getSpriteL().getDimensions();
				g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
			}
			g.setColor(Color.BLACK);
		}
		
		
		if (!this.bird.inBounds(new Rectangle(0, (int)(-Game.windowHeight / 3), Game.windowWidth,  + (int)(Game.windowHeight / 3 + Game.windowHeight * 0.75 + Game.windowHeight / 400 * 6))))
			this.gameState = Game.gameStateEnded;
		
		for (i = 0; i < maxPipeCount; i++)	// TODO: We don't really need to check all pipes here. But overhead is little
		{
			s = this.bird.getSprite();
			if (this.pipes[i].detectCollision(s))
				this.gameState = Game.gameStateEnded;
			else if (this.pipes[i].detectScore(s))
			{
				this.score++;
			}
		}
		
		if (this.score > 0)
		{
			g.setColor(Color.WHITE);
			Game.drawCenteredText(g, String.format("%d", this.score), new Rectangle(0, 0, Game.windowWidth, (int)(Game.windowHeight * 0.75)), new Font("Ubuntu", Font.BOLD, 48));
			g.setColor(Color.BLACK);
		}
	}
	
	private void handleEnded(Graphics2D g) {
		int i;
		
		if (this.bird.inBounds(new Rectangle(0, -1000, Game.windowWidth, (int)(Game.windowHeight * 0.75 + Game.windowHeight / 400 * 6) + 1000)) )
			this.bird.evaluate();
		
		this.bird.moveX(-100 * Game.deltaTime);
		this.bird.draw(g);
		
		for (i = 0; i < Game.configGetInt(new String[] {"game", "maxPipeCount"}); i++)
		{
			this.pipes[i].evaluate();
			this.pipes[i].draw(g);
		}
			
		drawCenteredText(g,
				Game.configGetString(new String[] {"text", "end", "gameover"}),
				new Rectangle(0, 0, Game.windowWidth, Game.windowHeight / 3),
				new Font("Ubuntu", Font.BOLD, 36));
		drawCenteredText(g,
				Game.configGetString(new String[] {"text", "end", "instruction"}),
				new Rectangle(0, 0, Game.windowWidth, Game.windowHeight / 2),
				new Font("Ubuntu", Font.PLAIN, 28));
		g.setColor(Color.WHITE);
		drawCenteredText(g,
				String.format(Game.configGetString(new String[] {"text", "end", "score"}), this.score),
				new Rectangle(0, 0, Game.windowWidth, (int)(Game.windowHeight / 1.5)),
				new Font("Ubuntu", Font.BOLD, 28));
		g.setColor(Color.BLACK);

	}
	
	private static void drawCenteredText(Graphics2D g, String text, Rectangle rect, Font font) {
		FontMetrics metrics;
		int x, y;
		
		// Calculate position
		metrics = g.getFontMetrics(font);
		x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		
		// Draw
		g.setFont(font);
		g.drawString(text, x, y);
	}
	
	// Key listener overrides
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
		}
		
		if (this.gameState == Game.gameStateMenu)
		{
			int i;
			
			switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
					this.gameState = Game.gameStateRunning;
					this.score = 0;
					this.pipes = new Pipe[Game.configGetInt(new String[] {"game", "maxPipeCount"})];
					for (i = 0; i < Game.configGetInt(new String[] {"game", "maxPipeCount"}); i++)
						this.pipes[i] = new Pipe(Game.configGetString(new String[] {"sprite", "pipeUpper", "path"}), Game.configGetString(new String[] {"sprite", "pipeLower", "path"}), (int)(Game.windowHeight * 0.2));
					this.bird.flap();
					break;
				case KeyEvent.VK_D:
					Game.debug = true;
					break;
			}
		}
		else if (this.gameState == Game.gameStateRunning)
		{
			switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
					this.bird.flap();
					break;
			}
		} else if (this.gameState == Game.gameStateEnded)
		{
			switch(e.getKeyCode()) {
				case KeyEvent.VK_R:
					this.gameState = Game.gameStateMenu;
					this.bird.reset();
					break;
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {}
	
	// Settings stuff etc.
	private void setFps(int fps) {
		if (fps > 1000)
			fps = 1000;
		this.framesPerSecond = fps;
		this.frameDurationTime = 1000 / fps;
		Game.deltaTime = 1 / (float)fps;
	}
	
	protected int getFps() {
		return this.framesPerSecond;
	}
	
	protected long getFrameDuration() {
		return this.frameDurationTime;
	}
	
	protected static float configGetFloat(String[] d) {
		int i;
		JSONObject jo;
		
		try {
			jo = configGetDict(d[0]);
			for (i = 1; i < d.length - 1; i++) {
				jo = jo.getJSONObject(d[i]);
			}
			return (float)jo.getDouble(d[d.length - 1]);
		} catch (JSONException e) {
			System.out.printf("in configGetString: failed call to getInt: %s\n", e.getMessage());
		}
		
		return -1;
	}
	
	protected static int configGetInt(String[] d) {
		int i;
		JSONObject jo;
		
		try {
			jo = configGetDict(d[0]);
			for (i = 1; i < d.length - 1; i++) {
				jo = jo.getJSONObject(d[i]);
			}
			return jo.getInt(d[d.length - 1]);
		} catch (JSONException e) {
			System.out.printf("in configGetString: failed call to getInt: %s\n", e.getMessage());
		}
		
		return -1;
	}
	
	protected static String configGetString(String[] d) {
		int i;
		JSONObject jo;
		
		try {
			jo = configGetDict(d[0]);
			for (i = 1; i < d.length - 1; i++) {
				jo = jo.getJSONObject(d[i]);
			}
			return jo.getString(d[d.length - 1]);
		} catch (JSONException e) {
			System.out.printf("in configGetString: failed call to getInt: %s\n", e.getMessage());
		}
		
		return null;
	}
	
	protected static JSONObject configGetObject(String dict, String option) {
		try {
			return Game.configGetDict(dict).getJSONObject(option);
		} catch (JSONException e) {
			System.out.printf("in configGetObject: failed call to GetJSONObject: %s\n", e.getMessage());
		}
		return null;
	}

	protected static JSONObject configGetDict(String dict) {
		try {
			return Game.config.getJSONObject(dict);
		} catch (JSONException e) {
			System.out.printf("in configGetDict: failed call to getJSONObject: %s\n", e.getMessage());
		}
		return null;
		
	}
}