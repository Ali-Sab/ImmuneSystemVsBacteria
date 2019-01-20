
/*
 * Purpose: Game about the human immune system. Tower defense where antibodies are towers and white blood cells are blockades.
 * All enemies come from top of screen. Bacteria and viruses are enemies. 
 * Author: Alison Sabuwala
 * Date: 28/05/2017
 */


import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main extends Applet implements Runnable, MouseListener, MouseMotionListener {
	
	// Main objects
	static Thread th;
	private Image dbImage;
	private Graphics dbg;
	private BufferedImage img;
	
	// Objects (towers, blockades, bacteria, viruses and a random). All required rectangles as well.
	private Antibody[] towers;
	private Rectangle[] towerRect;
	private Shot[][] shots;
	private Rectangle[][] shotRect;
	private WhiteBloodCell[] blockades;
	private Rectangle[] blockadeRect;
	private Bacteria[] bacteria;
	private Rectangle[] bacteriaRect;
	private Virus[] viruses;
	private Rectangle[] virusRect;
	private BetterRect[][] grid;
	private Random random = new Random();
	
	// VARIABLES ARE NON-ADJUSTABLE, AS THEY WOULD RENDER ALL SAVE GAMES UNUSABLE. 
	//DELETE ALL SAVE GAMES IF YOU WANT TO CHANGE CORE GAME SETTINGS
	private final int appletWidth = 750;			// applet bounds
	private final int appletHeight = 600;
	private final int towerNum = 10;				// max numbers of towers, blockades, bacteria, viruses and shots are set here
	private final int blockadeNum = 96;
	private final int bacteriaNum = 40;
	private final int virusNum = 10;
	private final int shotNum = bacteriaNum + virusNum;	// one shot for each enemy, per tower
	private final int trueTimer = 1000;				// original timers and values, used when starting new game
	private final int trueShotTimer = 850;
	private final int trueBacteriaHealth = 1;
	private final int trueBlockadeHealth = 2;
	private final int trueLives = 10;

	private int dragger;							// stores the object that is being dragged
	private boolean waveBacteriaOn = false;			// flags for whether the waves for each object are still going
	private boolean waveVirusOn = false;
	private boolean waveOn = false;
	private boolean gameOn = true;					// flag for if the game is frozen or not
	private boolean bacteriaChecker;				// checks if enough bacteria or viruses have been created
	private boolean virusChecker;
	private boolean pathFound;						// checked if path is available
	private int distanceX;							// store horizontal and vertical distance
	private int distanceY;
	private int distance;							// stores diagonal distance
	private int bacteriaCount;						// check number of bacteria/viruses 
	private int virusCount;
	private int bacteriaHealth = trueBacteriaHealth;// sets all health values based on bacteria health
	private int virusHealth = bacteriaHealth * 2;
	private int bacteriaFilter;						// how many bacteria/viruses are allowed to be made per wave
	private int virusFilter;
	private int loadPresses = 0;					// button presses check if user has pressed button 3 times consecutively 
	private int newGamePresses = 0;
	private boolean delayButton = true;				// check if more upgrades are still possible
	private boolean damageButton = true;
	private long bacteriaTimer;						// spawn timers
	private long bacteriaRandomTimer = random.nextInt(trueTimer);
	private long virusTimer;
	private long virusRandomTimer = random.nextInt(trueTimer * 2);
	private long[] shotTimer;						// stores how often towers can shoot
	private int[][] pathX;							// stores path data from path finder
	private int[][] pathY;
	private Loader[] loader;						// stores save files into objects for simplicity
	
	// save game variables
	private int saveNumber;							// save file number
	private int waveCount = 0;						// starting wave
	private int points = 120;						// starting points
	private int killed = 0;							// enemies killed
	private int lives = trueLives;					// lives left
	private long shotDelayTimer = trueShotTimer;	// shot and blockade upgrades
	private int blockadeHealth = trueBlockadeHealth;
	private int[] towerX;							// stores x and y coordinates of towers and blockades for saving and loading
	private int[] towerY;
	private int[] blockadeX;
	private int[] blockadeY;
		
	private final int shotSpeed = 3;				// object speeds
	private final int bacteriaSpeed = -2;
	private final int virusSpeed = -1;
	
	private boolean mouseClickTower = false;		// mouse flags
	private boolean mouseClickBlockade = false;
	
	private String shotSound = "sounds/shotSound.wav"; // Shot sound
	private String backSound = "sounds/background.wav";
	
	public void init() {
		resize(appletWidth, appletHeight);			// changing applet settings
		setBackground(Color.black);
		setFocusable(true);
		addMouseListener(this);						// creating mouse listener object, this game requires a mouse
		addMouseMotionListener(this);
		towers = new Antibody[towerNum];			// initialize arrays of objects
		towerRect = new Rectangle[towerNum];
		shots = new Shot[towerNum][shotNum];
		shotRect = new Rectangle[towerNum][shotNum];
		blockades = new WhiteBloodCell[blockadeNum];
		blockadeRect = new Rectangle[blockadeNum];
		towerX = new int[towerNum];
		towerY = new int[towerNum];
		blockadeX = new int[blockadeNum];
		blockadeY = new int[blockadeNum];
		bacteria = new Bacteria[bacteriaNum];
		bacteriaRect = new Rectangle[bacteriaNum];
		viruses = new Virus[virusNum];
		virusRect = new Rectangle[virusNum];
		shotTimer = new long[towerNum];
		grid = new BetterRect[(int) (appletWidth/60) + 1][(int) ((appletHeight - 150)/56) + 2];	// the grid has set dimensions with relation to the applet dimensions
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				grid[i][j] = new BetterRect(i*60, j*56 - 56, 60, 56);		// creates the better rectangle objects (nodes for path finding) 
			}
		}
		pathX = new int[grid[0].length - 1][grid.length * grid[0].length];	// create a path for each row a bacterium can spawn in, as many possible move locations as rectangles in the grid
		pathY = new int[grid[0].length - 1][pathX[0].length];
		loader = new Loader[50];											// maximum of 50 save slots
	}
	
	public void start() {
		th = new Thread(this);									// starts Thread
		th.start();
	}

	public void stop() {
		th.stop();
	}

	public void destroy() {
		th.stop();
	}
	
	public void run() {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		while (true) {
			waveOn = false;											// checks if wave is on
			bacteriaChecker = false;								// sets all values to false
			virusChecker = false;
			for (int i = 0; i < bacteria.length; i++) {
				if (bacteria[i] != null) {
					bacteriaChecker = true;
					break;
				}
			}
			for (int i = 0; i < viruses.length; i++) {
				if (viruses[i] != null) {
					virusChecker = true;
					break;
				}
			}
			if (bacteriaChecker || virusChecker || waveBacteriaOn || waveVirusOn) {	// if either one of the values does not remain false, a wave is on
				waveOn = true;
			}
			
			if (!waveOn && shotDelayTimer <= trueShotTimer / 10)					// checks to see if more shot timer upgrades are allowed
				delayButton = false;
			else
				delayButton = true;
			
			if (!waveOn && blockadeHealth >= trueBacteriaHealth * 20)				// checks to see if more blockade health upgrades are allowed
				damageButton = false;
			else 
				damageButton = true;
			
			if (bacteriaCount >= bacteriaFilter) {									// checks to see if enough bacteria have spawned
				waveBacteriaOn = false;
				bacteriaCount = 0;													// resets if enough have spawned and ends wave
			}
			
			for (int i = 0; i < bacteria.length; i++) {
				if (waveBacteriaOn && bacteria[i] == null && bacteriaTimer + bacteriaRandomTimer < System.currentTimeMillis()) {	// checks to see if bacteria should be spawning
					int tempY = (int) (random.nextInt(appletHeight-152)/56);														// determines spawning row
					bacteria[i] = new Bacteria(appletWidth + 100, tempY * 56, random.nextInt(2), bacteriaHealth, tempY, 0);			// creates bacteria at random y coordinate with random picture
					if (bacteria[i].getPic() == 0)
						bacteriaRect[i] = new Rectangle(bacteria[i].getX(), bacteria[i].getY(), 44, 48);
					else
						bacteriaRect[i] = new Rectangle(bacteria[i].getX(), bacteria[i].getY(), 44, 40);
					bacteriaCount++;																								// measures total bacteria spawned
					bacteriaTimer = System.currentTimeMillis() + 300;																// resets spawn timer
					bacteriaRandomTimer = random.nextInt(trueTimer);																// randomizers spawn timer
				}
			}
			
			if (virusCount >= virusFilter) {										// checks to see if enough viruses have spawned
				waveVirusOn = false;
				virusCount = 0;														// resets if enough have spawned and ends wave
			}
			
			for (int i = 0; i < viruses.length; i++) {
				if (waveVirusOn && viruses[i] == null && virusTimer + virusRandomTimer < System.currentTimeMillis()) {		
					viruses[i] = new Virus(appletWidth + 100, random.nextInt(appletHeight-170), virusHealth);
					virusRect[i] = new Rectangle(viruses[i].getX(), viruses[i].getY(), 90, 68);
					virusCount++;
					virusTimer = System.currentTimeMillis() + 300;
					virusRandomTimer = random.nextInt(trueTimer * 2);
				}
			}
			
			for (int i = 0; i < bacteria.length; i++) {												// all bacteria move in their specified paths, if available
				if (bacteria[i] != null) {
					if (bacteria[i].getX() > -44) {
						if (pathFound) {
							if (pathX[bacteria[i].getStartingY()][bacteria[i].getStep()] <= 60) {
								bacteria[i].moveX(bacteriaSpeed);
							} else {
								if (bacteria[i].getX() < pathX[bacteria[i].getStartingY()][bacteria[i].getStep()]) {	// only one direction at a time, to prevent crossing over blockade diagonally
									bacteria[i].moveX(-bacteriaSpeed);
								}
								else if (bacteria[i].getX() > pathX[bacteria[i].getStartingY()][bacteria[i].getStep()]) {
									bacteria[i].moveX(bacteriaSpeed);
								}
								else if (bacteria[i].getY() < pathY[bacteria[i].getStartingY()][bacteria[i].getStep()]) {
									bacteria[i].moveY(-bacteriaSpeed);
								}
								else if (bacteria[i].getY() > pathY[bacteria[i].getStartingY()][bacteria[i].getStep()]) {
									bacteria[i].moveY(bacteriaSpeed);
								}
								if (bacteria[i].getX() == pathX[bacteria[i].getStartingY()][bacteria[i].getStep()] && bacteria[i].getY() == pathY[bacteria[i].getStartingY()][bacteria[i].getStep()]) {
									bacteria[i].setStep(bacteria[i].getStep() + 1);					// if bacteria has made it to correct position, starts the next step
								}
							}
						}
						else {
							bacteria[i].moveX(bacteriaSpeed);										// if no path, bacteria goes straight forward
						}
						bacteriaRect[i].setLocation(bacteria[i].getX(), bacteria[i].getY());		// rectangle follows bacteria
					}
					else {						
						bacteria[i] = null;															// if bacteria reaches end of map, lives lost and bacteria nullified
						bacteriaRect[i] = null;
						lives--;
						for (int n = 0; n < towers.length; n++) {									// any shots on route to bacteria will also be deleted and timers reset
							if (shots[n][i] != null) {
								shots[n][i] = null;
								shotTimer[n] = 0;
							}
						}
					}
				}
				
				for (int j = 0; j < blockades.length; j++) {										// if bacteria hits a blockade
					if (blockades[j] != null && bacteria[i] != null) {
						if (blockadeRect[j].intersects(bacteriaRect[i])) {							// delete bacteria and damage blockade, player gains points
							bacteria[i].setHealth(bacteria[i].getHealth() - blockadeHealth);
							blockades[j].setHealth(blockades[j].getHealth() - bacteriaHealth);
							if (bacteria[i].getHealth() <= 0) {
								for (int n = 0; n < towers.length; n++) {				// if bacterium has no more health, nullify bacterium and other incoming shots
									if (shots[n][i] != null) {
										shots[n][i] = null;
										shotTimer[n] = 0;
									}
								}
								bacteria[i] = null;
								bacteriaRect[i] = null;
								points += 2;
								killed++;
							}
							if (blockades[j].getHealth() <= 0) {									// if blockade has no more health, nullify blockade
								blockades[j] = null;
								blockadeRect[j] = null;
							}
							for (int n = 0; n < towers.length; n++) {								// any on route shots are nullified
								if (shots[n][i] != null) {
									shots[n][i] = null;
									shotTimer[n] = 0;
								}
							}
						}
					}
				}
				
				for (int j = 0; j < towers.length; j++) {								// if any tower's shot hits it's linked bacterium,
					if (bacteria[i] != null && shots[j][i] != null) {
						if (bacteriaRect[i].intersects(shotRect[j][i])) {				// shot collision detection
							bacteria[i].setHealth(bacteria[i].getHealth() - 1);			// do one damage to bacterium
							shots[j][i] = null;
							shotRect[j][i] = null;
							if (bacteria[i].getHealth() <= 0) {
								for (int n = 0; n < towers.length; n++) {				// if bacterium has no more health, nullify bacterium and other incoming shots
									if (shots[n][i] != null) {
										shots[n][i] = null;
										shotTimer[n] = 0;
									}
								}
								bacteria[i] = null;
								bacteriaRect[i] = null;
								points += 2;
								killed++;
							}
						}
					}
				}
			}
			
			for (int i = 0; i < viruses.length; i++) {
				if (viruses[i] != null) {
					if (viruses[i].getX() > -90) {											// movement code for viruses
						viruses[i].moveX(virusSpeed);										// slower than bacteria
						virusRect[i].setLocation(viruses[i].getX(), viruses[i].getY());
					} else {																// if virus reaches end of map, lives lost and virus nullified
						viruses[i] = null;
						virusRect[i] = null;												// viruses do not follow paths, they destroy blockades and go straight forward
						lives-=3;
						for (int n = 0; n < towers.length; n++) {
							if (shots[n][i + bacteriaNum] != null) {
								shots[n][i + bacteriaNum] = null;
								shotTimer[n] = 0;
							}
						}
					}
				}
				
				for (int j = 0; j < blockades.length; j++) {								// virus will usually destroy blockades
					if (blockades[j] != null && viruses[i] != null) {
						if (blockadeRect[j].intersects(virusRect[i])) {
							viruses[i].setHealth(viruses[i].getHealth() - blockadeHealth);
							blockades[j].setHealth(blockades[j].getHealth() - virusHealth);
							if (viruses[i].getHealth() <= 0) {
								for (int n = 0; n < towers.length; n++) {
									if (shots[n][i + bacteriaNum] != null) {
										shots[n][i + bacteriaNum] = null;
										shotTimer[n] = 0;
									}
								}
								viruses[i] = null;
								virusRect[i] = null;
								points +=5;													// more points for killing virus
								killed++;
							}
							if (blockades[j].getHealth() <= 0) {
								blockades[j] = null;
								blockadeRect[j] = null;
							}
							for (int n = 0; n < towers.length; n++) {
								if (shots[n][i + bacteriaNum] != null) {
									shots[n][i + bacteriaNum] = null;
									shotTimer[n] = 0;
								}
							}
						}
					}
				}
				
				for (int j = 0; j < towers.length; j++) {
					if (viruses[i] != null && shots[j][i + bacteriaNum] != null) {		// viruses have more health than bacteria
						if (virusRect[i].intersects(shotRect[j][i + bacteriaNum])) {	// shot collision
							viruses[i].setHealth(viruses[i].getHealth() - 1);
							shots[j][i + bacteriaNum] = null;
							shotRect[j][i + bacteriaNum] = null;
							if (viruses[i].getHealth() <= 0) {
								for (int n = 0; n < towers.length; n++) {
									if (shots[n][i + bacteriaNum] != null) {
										shots[n][i + bacteriaNum] = null;
										shotTimer[n] = 0;
									}
								}
								viruses[i] = null;
								virusRect[i] = null;
								points +=5;												// more points for killing virus
								killed++;
							}
						}
					}
				}
			}
			
			for (int i = 0; i < towers.length; i ++) {
				if (towers[i] != null) {
					for (int j = 0; j < bacteria.length; j++) {
						if (bacteria[j] != null) {															// code for firing at bacteria
							distanceX = (int) (bacteriaRect[j].getCenterX() - (towers[i].getX() + 44));		// uses Pythagorean theorem
							distanceY = (int) (bacteriaRect[j].getCenterY() - (towers[i].getY() + 38));
							if (distanceX < 0)
								distanceX *= -1;															// if distance is negative, inverse signs
							if (distanceY < 0)
								distanceY *= -1;
							distance = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
							if (distance < 200 && shots[i][j] == null && shotTimer[i] < System.currentTimeMillis()) {	// if bacterium is in range, fire
								switch (random.nextInt(5)) {															// random color
								case 0:
									shots[i][j] = towers[i].generateShot("red");
									break;
								case 1:
									shots[i][j] = towers[i].generateShot("blue");
									break;
								case 2:
									shots[i][j] = towers[i].generateShot("green");
									break;
								case 3:
									shots[i][j] = towers[i].generateShot("yellow");
									break;
								case 4:
									shots[i][j] = towers[i].generateShot("white");
									break;
								}
								Sound(shotSound);
								shotRect[i][j] = new Rectangle(shots[i][j].getX(), shots[i][j].getY(), 11, 11);
								shotTimer[i] = System.currentTimeMillis() + shotDelayTimer;							// show timer resetter
								break;
							}
						}
					}
					for (int j = 0; j < viruses.length; j++) {														// code for firing at viruses
						if (viruses[j] != null) {
							distanceX = (int) (virusRect[j].getCenterX() - (towers[i].getX() + 44));
							distanceY = (int) (virusRect[j].getCenterY() - (towers[i].getY() + 38));
							if (distanceX < 0)
								distanceX *= -1;
							if (distanceY < 0)
								distanceY *= -1;
							distance = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
							if (distance < 200 && shots[i][j + bacteriaNum] == null && shotTimer[i] < System.currentTimeMillis()) {
								switch (random.nextInt(5)) {
								case 0:
									shots[i][j + bacteriaNum] = towers[i].generateShot("red");
									break;
								case 1:
									shots[i][j + bacteriaNum] = towers[i].generateShot("blue");
									break;
								case 2:
									shots[i][j + bacteriaNum] = towers[i].generateShot("green");
									break;
								case 3:
									shots[i][j + bacteriaNum] = towers[i].generateShot("yellow");
									break;
								case 4:
									shots[i][j + bacteriaNum] = towers[i].generateShot("white");
									break;
								}
								Sound(shotSound);
								shotRect[i][j + bacteriaNum] = new Rectangle(shots[i][j + bacteriaNum].getX(), shots[i][j + bacteriaNum].getY(), 11, 11);
								shotTimer[i] = System.currentTimeMillis() + shotDelayTimer;
								break;
							}
						}
					}
				}
			}
			for (int i = 0; i < towers.length; i++) {
				for (int j = 0; j < shots[0].length; j++) {
					if (shots[i][j] != null) {
						if (j < bacteriaNum) {
							if (shots[i][j].getX() > bacteriaRect[j].getCenterX()) {		// shots are given directions in this code
								shots[i][j].moveX(-shotSpeed);								// they move in the direction of the target object until the shots are nullified
							} else {
								shots[i][j].moveX(shotSpeed);
							}
							if (shots[i][j].getY() > bacteriaRect[j].getCenterY()) {
								shots[i][j].moveY(-shotSpeed);
							} else {
								shots[i][j].moveY(shotSpeed);
							}
						} else {
							if (shots[i][j].getX() > virusRect[j - bacteriaNum].getCenterX()) {
								shots[i][j].moveX(-shotSpeed);
							} else {
								shots[i][j].moveX(shotSpeed);
							}
							if (shots[i][j].getY() > virusRect[j - bacteriaNum].getCenterY()) {
								shots[i][j].moveY(-shotSpeed);
							} else {
								shots[i][j].moveY(shotSpeed);
							}
						}
						shotRect[i][j].setLocation(shots[i][j].getX(), shots[i][j].getY());
					}
				}
			}
			
			if (lives <= 0) {							// if lives are 0 or below, new game is automatically started (player must save before losing wave)
				newGame();
			}
			
			// repaint applet
			repaint();

			try {
				Thread.sleep(7);											// makes game faster
			} catch (InterruptedException ex) {
				// do nothing
			}

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		}
	}
	
	/* Purpose: When this method is used, all objects are nullified and values reset to wave 0 values
	 * No inputs or outputs.
	 */
	public void newGame() {
		for (int i = 0; i < towers.length; i++) {
			towers[i] = null;
			towerRect[i] = null;
			for (int j = 0; j < shots[0].length; j++) {
				shots[i][j] = null;
				shotRect[i][j] = null;
			}
		}
		for (int i = 0; i < blockades.length; i++) {
			blockades[i] = null;
			blockadeRect[i] = null;
		}
		for (int i = 0; i < bacteria.length; i++) {
			bacteria[i] = null;
			bacteriaRect[i] = null;
		}
		for (int i = 0; i < viruses.length; i++) {
			viruses[i] = null;
			virusRect[i] = null;
		}
		
		waveBacteriaOn = false;
		waveVirusOn = false;
		points = 120;
		killed = 0;
		waveCount = 0;
		shotDelayTimer = trueShotTimer;
		blockadeHealth = trueBlockadeHealth;
		lives = trueLives;
		gameOn = true;
	}
	
	/* Purpose: When this method is used, all objects are nullified and the game seems like it's frozen, only new game and load game can be clicked.
	 * No inputs or outputs.
	 */
	public void endGame() {
		for (int i = 0; i < towers.length; i++) {
				towers[i] = null;
				towerRect[i] = null;
				for (int j = 0; j < shots[0].length; j++) {
					shots[i][j] = null;
					shotRect[i][j] = null;
				}
		}
		for (int i = 0; i < blockades.length; i++) {
			blockades[i] = null;
			blockadeRect[i] = null;
		}
		for (int i = 0; i < bacteria.length; i++) {
			bacteria[i] = null;
			bacteriaRect[i] = null;
		}
		for (int i = 0; i < viruses.length; i++) {
			viruses[i] = null;
			virusRect[i] = null;
		}
		
		waveBacteriaOn = false;
		waveVirusOn = false;
		gameOn = false;
	}
	
	/* Purpose: Loads a save file and starts the game with its data.
	 * Input the save file number.
	 * No outputs.
	 */
	public void loadGame(int loadNum) {
		for (int i = 0; i < towerX.length; i++) {						// clears all coordinate data
			towerX[i] = 0;
			towerY[i] = 0;
		}
		for (int i = 0; i < blockadeX.length; i++) {
			blockadeX[i] = 0;
			blockadeY[i] = 0;
		}
		
		FileInputStream File1; 											// creates input steam object
		BufferedReader In; 	  						 					// creates the reader object
		String fileInput = ""; 											// create string to store data 
		try  {
			File1 = new FileInputStream("gameSaves.txt"); 				// Create a new file input stream that is connected to file named "gameSaves.txt" 
			In = new BufferedReader(new InputStreamReader(File1));		// Connect BufferedReader to the File object
			int lineFinder = (loadNum - 1) * 232;						// find out where save file information starts by using this calculation
			int numLines = 0;
			while (numLines != lineFinder ) {							// go to save file line number
				fileInput = In.readLine();
				numLines++;
			}
			while (fileInput != null && (fileInput != "" || numLines == 0) && numLines < lineFinder + 233) {	
				fileInput = In.readLine();											// while there is still valid text being read and still reading the correct save file
				switch (numLines % 232) {											// read line and store input into "fileInput" string
				case 5:																// depending on which number is selected (remainder helps find which one, because numbers are in a pattern)
					waveCount = Integer.parseInt(fileInput);						// these select lines have special data to import to game
					break;															// all data is being set as they are imported
				case 7:
					lives = Integer.parseInt(fileInput);
					break;
				case 9:
					killed = Integer.parseInt(fileInput);
					break;
				case 11:
					points = Integer.parseInt(fileInput);
					break;
				case 13:
					shotDelayTimer = Integer.parseInt(fileInput);
					break;
				case 15:
					blockadeHealth = Integer.parseInt(fileInput);
					break;
				case 17:															// loads all coordinate data and stores it in the in-game variables
					for (int i = 0; i < towerX.length; i++) {
						towerX[i] = Integer.parseInt(fileInput);
						fileInput = In.readLine();
						numLines++;
					}
					break;
				case 28:
					for (int i = 0; i < towerY.length; i++) {
						towerY[i] = Integer.parseInt(fileInput);
						fileInput = In.readLine();
						numLines++;
					}
					break;
				case 39:
					for (int i = 0; i < blockadeX.length; i++) {
						blockadeX[i] = Integer.parseInt(fileInput);
						fileInput = In.readLine();
						numLines++;
					}
					break;
				case 136:
					for (int i = 0; i < blockadeY.length; i++) {
						blockadeY[i] = Integer.parseInt(fileInput);
						fileInput = In.readLine();
						numLines++;
					}
					break;
				}
			numLines++;													// increases line number by one each time
			} 
		In.close(); 													// closes stream and BufferedReader
		} 
	catch (FileNotFoundException fnfe) {								// if file doesn't exist, prompt user, won't happen though because the output method ensures that it is created
			System.out.println("Error - this file does not exist"); 
		}  
	catch (IOException ioe) {											// if input/error, prompt user
			System.out.println("error = " + ioe.toString() ); 
		}
		waveBacteriaOn = false;											// turn off all wave flags
		waveVirusOn = false;
		
		for (int i = 0; i < towerX.length; i++) {						// place all towers and blockades
			if (towerX[i] != 0) {
				towers[i] = new Antibody(towerX[i], towerY[i]);
				towerRect[i] = new Rectangle(towers[i].getX() - 44, towers[i].getY() - 38, 88, 77);
			}
		}
		
		for (int i = 0; i < blockadeX.length; i++) {
			if (blockadeX[i] != 0) {
				blockades[i] = new WhiteBloodCell(blockadeX[i], blockadeY[i], blockadeHealth);
				blockadeRect[i] = new Rectangle(blockades[i].getX(), blockades[i].getY(), 49, 56);
			}
		}
		gameOn = true;													// set game to on
	}
	
	/* Purpose: Path finding AI for each row. Not imported, modified version of A* algorithm.
	 * Outputs whether the path is possible or impossible.
	 */
	public boolean findPath() {
		for (int i = 0; i < pathX.length - 1; i++) {											// resets all grid data
			for (int d = 0; d < grid.length; d++) {
				for (int j = 0; j < grid[0].length; j++) {
					grid[d][j].setOpen(2);
					grid[d][j].setWalkable(false);
					grid[d][j].setParent(null);
				}
			}
			
			for (int d = 0; d < grid.length; d++) {												// sets all grids on the screen to walkable and removes them from all lists
				for (int j = 1; j < grid[0].length - 1; j++) {
					grid[d][j].setOpen(0);
					grid[d][j].setWalkable(true);
				}
			}
		
			for (int d = 1; d < grid.length - 1; d++) {											// sets all rectangles where collision occurs to non-walkable
				for (int j = 1; j < grid[0].length - 1; j++) {
					for (int n = 0; n < blockades.length; n++) {
						if (blockades[n] != null && blockadeRect[n].intersects(grid[d][j]) && grid[d][j].getY() >= 0 && grid[d][j].getY() < appletHeight - 150) {
							grid[d][j].setWalkable(false);
							grid[d][j].setOpen(2);
						}
					}
				}
			}
			
			for (int d = 0; d < grid.length; d++) {										// resets movement costs and heuristic values for all nodes
				for (int n = 0; n < grid[0].length; n++) {
					grid[d][n].setHValue(0);
					grid[d][n].setGValue(0);
				}
			}
			
			boolean checker = false;													// checker determines whether the path finding for one row is done or not
			int x = grid.length - 2;													// set starting node
			int y = i + 1;
			pathX[i][0] = (int) grid[x][y].getX();										// input starting coordinates
			pathY[i][0] = (int) grid[x][y].getY();
			grid[x][y].setOpen(2);														// sets starting rectangle to have no parent node and to add it to closed list
			grid[x][y].setParent(null);
			for (int d = 0; d < grid.length; d++) {
				for (int n = 0; n < grid[0].length; n++) {								// sets all heuristic values, with focus on making horizontal distance seem farther
					int tempD = d;
					int tempN = n;
					while (tempD != 0) {
						grid[d][n].setHValue(grid[d][n].getHValue() + 15);
						tempD--;
					}
					while (tempN != y) {
						if (tempN > y) {
							grid[d][n].setHValue(grid[d][n].getHValue() + 10);
							tempN--;
						} else if (tempN < y) {
							grid[d][n].setHValue(grid[d][n].getHValue() + 10);
							tempN++;
						}
					}
				}
			}
			int counter = 0;														// counts number of steps
			while (!checker) {														// until path is found
				counter++;															// next step right away because first step is already used
				if (x < grid.length - 2 && grid[x + 1][y].isWalkable() && grid[x + 1][y].isOpen() == 0) {	// set all nodes around
					grid[x + 1][y].setOpen(1);																//to be on open list, set movement cost and
					grid[x + 1][y].setGValue(grid[x][y].getGValue() + 1);									//set parent of nodes to current node
					grid[x + 1][y].setParent(grid[x][y]);
				}
				if (x > 0 && grid[x - 1][y].isWalkable() && grid[x - 1][y].isOpen() == 0) {
					grid[x - 1][y].setOpen(1);
					grid[x - 1][y].setGValue(grid[x][y].getGValue() + 1);
					grid[x - 1][y].setParent(grid[x][y]);
				}
				if (grid[x][y + 1].isWalkable() && grid[x][y + 1].isOpen() == 0) {
					grid[x][y + 1].setOpen(1);
					grid[x][y + 1].setGValue(grid[x][y].getGValue() + 1);
					grid[x][y + 1].setParent(grid[x][y]);
				}
				if (y > 0 && grid[x][y - 1].isWalkable() && grid[x][y - 1].isOpen() == 0) {
					grid[x][y - 1].setOpen(1);
					grid[x][y - 1].setGValue(grid[x][y].getGValue() + 1);
					grid[x][y - 1].setParent(grid[x][y]);
				}
									// if a nearby node is open, check if its parent has a smaller GValue, if it does not, set the node's parent to the current node 
				if (x < grid.length - 2 && grid[x + 1][y].isOpen() == 1 && grid[x + 1][y].isWalkable() && grid[x + 1][y].getParent() != null && grid[x][y].getGValue() <= grid[x + 1][y].getParent().getGValue()) {
					grid[x + 1][y].setParent(grid[x][y]);
				}
				if (x > 0 && grid[x - 1][y].isOpen() == 1 && grid[x - 1][y].isWalkable() && grid[x - 1][y].getParent() != null && grid[x][y].getGValue() <= grid[x - 1][y].getParent().getGValue()) {
					grid[x - 1][y].setParent(grid[x][y]);
				}
				if (y > 0 && grid[x][y - 1].isOpen() == 1 && grid[x][y - 1].isWalkable() && grid[x][y - 1].getParent() != null && grid[x][y].getGValue() <= grid[x][y - 1].getParent().getGValue()) {
					grid[x][y - 1].setParent(grid[x][y]);
				}
				if (grid[x][y + 1].isOpen() == 1 && grid[x][y + 1].isWalkable() && grid[x][y + 1].getParent() != null && grid[x][y].getGValue() <= grid[x][y + 1].getParent().getGValue()) {
					grid[x][y + 1].setParent(grid[x][y]);
				}
				
				
				int lowestF = 10000;												// go in the direction that has the lowest total value (GValue + HValue)
				if (x < grid.length - 2 && grid[x + 1][y].isOpen() == 1) {			// do this by finding the node around that has the lowest F value
					lowestF = grid[x + 1][y].getFValue();
				}
				if (x > 0 && grid[x - 1][y].isOpen() == 1 && grid[x - 1][y].getFValue() < lowestF) {
					lowestF = grid[x - 1][y].getFValue();
				}
				if (y > 0 && grid[x][y - 1].isOpen() == 1 && grid[x][y - 1].getFValue() < lowestF) {
					lowestF = grid[x][y - 1].getFValue();
				}
				
				if (grid[x][y + 1].isOpen() == 1 && grid[x][y + 1].getFValue() < lowestF) {
					lowestF = grid[x][y + 1].getFValue();
				}
				
				if (x < grid.length - 2 && grid[x + 1][y].isOpen() == 1 && lowestF == grid[x + 1][y].getFValue()) {	// which ever node has the lowest F value will become the child node and current node will move there
					grid[x + 1][y].setGParent(grid[x][y]);
					x++;
					grid[x][y].setOpen(2);
				} else if (x > 0 && grid[x - 1][y].isOpen() == 1 && lowestF == grid[x - 1][y].getFValue()) {
					grid[x - 1][y].setGParent(grid[x][y]);
					x--;
					grid[x][y].setOpen(2);
				} else if (grid[x][y + 1].isOpen() == 1 && lowestF == grid[x][y + 1].getFValue()) {
					grid[x][y + 1].setGParent(grid[x][y]);
					y++;
					grid[x][y].setOpen(2);
				} else if (y > 1 && grid[x][y - 1].isOpen() == 1 && y > 0 && lowestF == grid[x][y - 1].getFValue()) {
					grid[x][y - 1].setGParent(grid[x][y]);
					y--;
					grid[x][y].setOpen(2);
				}
				
				if (lowestF == 10000) {								// if no node is available, current node backtracks to it's direct parent node, not real parent node 
					if (grid[x][y].getGParent() == null) {
						checker = true;
						return false;								// if one path is impossible, all paths are, therefore, no path is available
					} else if (grid[x][y].getGParent() != null) {
						int tempX = x;
						int tempY = y;
						x = (int) (grid[tempX][tempY].getGParent().getX() / 60);
						y = (int) (grid[tempX][tempY].getGParent().getY() / 56) + 1;
					}
				}
				
				pathX[i][counter] = (int) grid[x][y].getX();		// record path data, will later be overridden by faster path data
				pathY[i][counter] = (int) grid[x][y].getY();		// this is for the counter
				for (int j = 0; j < counter; j++) {
					if (pathX[i][j] == pathX[i][counter] && pathY[i][j] == pathY[i][counter]) {
						counter = j;
					}
				}
				
				if (x <= 0) {										// if node hits the end of the map, path is possible and loop ends
					checker = true;
				}
				
				if (counter >= pathX[0].length - 1) {				// if number of loops has become greater than number of path slots, path is impossible
					checker = true;
					return false;
				}
			}
			
			BetterRect rect = grid[x][y].getParent();				// starting from the ending node, backtracks using parent to find out how many coordinates there are
			int pathLength = 0;
			for (int n = counter; n > 0; n--) {
				rect = rect.getParent();
				if (rect == null) {
					pathLength = 1 + counter - n;					// records number of coords here
					break;
				}
			}
			
			rect = grid[x][y].getParent();							// records coods into path data
			for (int n = pathLength; n > 0; n--) {
				pathX[i][n] = (int) rect.getX();
				pathY[i][n] = (int) rect.getY();
				rect = rect.getParent();
			}
			
		}
		return true;												// returns that the path is possible if no other return value is passed
	}
	
	/* Purpose: Sorts the loaders for the save file in alphabetical order
	 * No inputs or outputs, the method directly accesses class data.
	 */
	public void sortLoaders() {
		Loader tempLoader;																		// swapping variable
		int tempIndexNum = 0;																	// stores the indexNum of the smallest string lexicographically
		for (int j = 0; j < loader.length; j++) {
			tempIndexNum = j;																	// sets the first row the minimum
			for (int rowIndexNum1 = j + 1; rowIndexNum1 < loader.length; rowIndexNum1++) {		// creates a second rowIndexNum for comparisons within first loop
				if (loader[rowIndexNum1] != null && loader[tempIndexNum] != null && loader[rowIndexNum1].getName().compareTo(loader[tempIndexNum].getName()) < 0) {
					tempIndexNum = rowIndexNum1;				// if the comparing string is lexicographically smaller than the current min, changes min values
				}
			}
			tempLoader = loader[tempIndexNum];					// changes the entire loader, so all values included
			loader[tempIndexNum] = loader[j];					// swapping process
			loader[j] = tempLoader;
		}
	}
	
	public void mousePressed (MouseEvent e) {
		if (!mouseClickTower && !mouseClickBlockade && gameOn && !waveOn && e.getX() > 150 && e.getX() < 238 && e.getY() > 500 && e.getY() < 577 && points >= 50) {
			for (int i = 0; i < towers.length; i++) {		// if tower button is pressed and nothing else is already pressed
				if (towers[i] == null) {					// create a tower and its rectangle
					towers[i] = new Antibody(e.getX(), e.getY());
					towerRect[i] = new Rectangle(towers[i].getX(), towers[i].getY(), 88, 77);
					dragger = i;							// dragger specifies which tower was made
					mouseClickTower = true;					// tower will follow mouse until tower is placed
					points -= 50;							// tower costs 50 points
					break;
				}
			}
		}
		if (!mouseClickTower && !mouseClickBlockade && gameOn && !waveOn && e.getX() > 240 && e.getX() < 289 && e.getY() > 510 && e.getY() < 566 && points >= 10) {
			for (int i = 0; i < blockades.length; i++) {		// if blockade button is pressed and nothing else is already pressed
				if (blockades[i] == null) {						// create a blockade and its rectangle
					blockades[i] = new WhiteBloodCell(e.getX(), e.getY(), blockadeHealth);
					blockadeRect[i] = new Rectangle(e.getX(), e.getY(), 49, 56);
					dragger = i;							// dragger specifies which blockade was made
					mouseClickBlockade = true;				// blockade will follow mouse until tower is placed
					points -= 10;							// blockade costs 50 points
					break;
				}
			}
		}
		if (gameOn && !waveOn && e.getX() > 510 && e.getX() < 610 && e.getY() > 500 && e.getY() < 540) {
			waveCount++;										// if start wave button is pressed, sets all new values
			waveBacteriaOn = true; 
			waveVirusOn = true;									// initiates path finder and sets waves to on
			bacteriaFilter = 10 + (int) Math.pow(2, waveCount);
			virusFilter = (int) (0.3 * waveCount);
			if (bacteriaFilter > bacteriaNum || bacteriaFilter < 0)
				bacteriaFilter = bacteriaNum;
			if (virusFilter > virusNum || virusFilter < 0)
				virusFilter = virusNum;
			bacteriaHealth = (int) (1 + waveCount * 0.143);
			virusHealth = bacteriaHealth * 2;
			pathFound = findPath();
			Sound(backSound);
		}
		if (gameOn && delayButton && e.getX() > 300 && e.getX() < 500 && e.getY() > 500 && e.getY() < 540 && points >= 25 + 35*((trueShotTimer - shotDelayTimer)/100)) {
			points -= 25 + 35*((trueShotTimer - shotDelayTimer)/100);	// if delay button is pressed, takes points and reduces shot delay
			shotDelayTimer -= trueShotTimer/10;
			
		}
		if (gameOn && damageButton && e.getX() > 300 && e.getX() < 500 && e.getY() > 550 && e.getY() < 590 && points >= 20 + 15*(blockadeHealth - trueBlockadeHealth)) {
			points -= 20 + 15*(blockadeHealth-trueBlockadeHealth); 		// if damage button is pressed, takes points and increases blockade health
			blockadeHealth++;
			
		}
		if (gameOn && !waveOn && e.getX() > 510 && e.getX() < 610 && e.getY() > 550 && e.getY() < 590) {
			JFrame frame = new JFrame();
			String saveName = JOptionPane.showInputDialog(frame, "Enter the name of the save file");
			if (saveName != null && saveName != "") {
				for (int i = 0; i < towerX.length; i++) {					// if save button is pressed, gather coordinates of all towers and blockades
					if (towers[i] != null) {								// store them
						towerX[i] = towers[i].getX();
						towerY[i] = towers[i].getY();
					} else {
						towerX[i] = 0;
						towerY[i] = 0;
					}
				}
				for (int i = 0; i < blockadeX.length; i++) {
					if (blockades[i] != null) {
						blockadeX[i] = blockades[i].getX();
						blockadeY[i] = blockades[i].getY();
					} else {
						blockadeX[i] = 0;
						blockadeY[i] = 0;
					}
				}
				
				FileInputStream File1; 											// creates input steam object
				BufferedReader In; 	  						 					// creates the reader object
				String fileInput = ""; 											// create string to store data 
				try  {															// try this...
					File1 = new FileInputStream("gameSaves.txt"); 				// Create a new file input stream that is connected to file named "gameSaves.txt" 
					In = new BufferedReader(new InputStreamReader(File1));		// Connect BufferedReader to the File object 
					fileInput = In.readLine();									// read line and store input into "fileInput" string
					int numLines = 1;											// starts the line count to calculate which number is being read
					if (fileInput == null) 										// if there is no data, selected save file number is 0
						saveNumber = 0;
					while (fileInput != null && fileInput != "") {				// while there is still valid text being read, a null result means that there is no more data to read
						fileInput = In.readLine();								// read line and store input into "fileInput" string
						switch (numLines % 232) {								// depending on which number is selected (remainder helps find which one, because numbers are in a pattern)
						case 1:
							saveNumber = Integer.parseInt(fileInput);			// the 2nd line of the save pattern is the save file number
							break;
						}
						numLines++;												// increases line number by one each time
					} 
					In.close(); 												// closes steam and BufferedReader
				} 
				catch (FileNotFoundException fnfe) {								// if file doesn't exist, prompt user, won't happen though because the output method ensures that it is created
					System.out.println("Error - this file does not exist"); 
				}  
				catch (IOException ioe) {											// if input/error, prompt user
					System.out.println("error = " + ioe.toString() ); 
				}
				BufferedWriter bw = null;											// creates buffered writer
				try {
					File1 = new FileInputStream("gameSaves.txt"); 				// Create a new file input stream that is connected to file named "gameSaves.txt" 
					In = new BufferedReader(new InputStreamReader(File1));		// Connect BufferedReader to the File object 
					fileInput = In.readLine();									// read line and store input into "fileInput" string
					int lineFinder = (saveNumber) * 232;						// finds out which line to start reading from
					int numLines = 0;
					bw = new BufferedWriter(new PrintWriter("gameSaves.txt"));	// declared object
					while (numLines < lineFinder && fileInput != null) {		// until reader is boosted to selected line, keep writing what is read, to not retain other save files
						bw.write(fileInput);
						bw.newLine();
						fileInput = In.readLine();
						numLines++;
					}
					bw.write("Game Save Number");							// outputs headers and creates lines in the middle to help user understand the formatting
					bw.newLine();
					numLines++;
					bw.write(String.valueOf(saveNumber + 1));
					bw.newLine();
					numLines++;
					bw.write("Game Save Name");									
					bw.newLine();
					numLines++;
					bw.write(saveName);
					bw.newLine();
					numLines++;
					bw.write("Wave #:");
					bw.newLine();
					numLines++;
					bw.write(String.valueOf(waveCount));
					bw.newLine();
					numLines++;
					bw.write("Lives:");
					bw.newLine();
					numLines++;
					bw.write(String.valueOf(lives));
					bw.newLine();
					numLines++;
					bw.write("Enemies Killed:");
					bw.newLine();
					numLines++;
					bw.write(String.valueOf(killed));
					bw.newLine();
					numLines++;
					bw.write("Points:");
					bw.newLine();
					numLines++;
					bw.write(String.valueOf(points));
					bw.newLine();
					numLines++;
					bw.write("Shot Delay Timer:");
					bw.newLine();
					numLines++;
					bw.write(String.valueOf(shotDelayTimer));
					bw.newLine();
					numLines++;
					bw.write("Blockade Health:");
					bw.newLine();
					numLines++;
					bw.write(String.valueOf(blockadeHealth));
					bw.newLine();
					numLines++;
					bw.write("Tower X:");
					bw.newLine();
					numLines++;
					bw.flush();
					int tempLines = numLines;
					while (tempLines + towerX.length != numLines) {
						for (int i = 0; i < towerX.length; i++) {		// prints out all coords into txt file
							bw.write(String.valueOf(towerX[i]));
							bw.newLine();
							bw.flush();
							numLines++;
						}
					}
					bw.write("Tower Y:");
					bw.newLine();
					numLines++;
					bw.flush();
					tempLines = numLines;
					while (tempLines + towerY.length != numLines) {
						for (int i = 0; i < towerY.length; i++) {		// prints out all coords into txt file
							bw.write(String.valueOf(towerY[i]));
							bw.newLine();
							bw.flush();
							numLines++;
						}
					}
					bw.write("Blockade X:");
					bw.newLine();
					numLines++;
					bw.flush();
					tempLines = numLines;
					while (tempLines + blockadeX.length != numLines) {
						for (int i = 0; i < blockadeX.length; i++) {	// prints out all coords into txt file
							bw.write(String.valueOf(blockadeX[i]));
							bw.newLine();
							bw.flush();
							numLines++;
						}
					}
					bw.write("Blockade Y:");
					bw.newLine();
					numLines++;
					bw.flush();
					tempLines = numLines;
					while (tempLines + blockadeY.length != numLines) {
						for (int i = 0; i < blockadeY.length; i++) {	// prints out all coords into txt file
							bw.write(String.valueOf(blockadeY[i]));
							bw.newLine();
							bw.flush();
							numLines++;
						}
					}
				} catch (IOException ioe) {								// catches io exceptions
					ioe.printStackTrace();
				} finally {												// after all that, if bw was made, close the stream and it
					if (bw != null) 
					try {
						bw.close();
					} catch (IOException ioe2) {
						System.out.println(ioe2);						// ignore error
					}
				}
			}
		}
		if (!waveOn && e.getX() > 620 && e.getX() < 720 && e.getY() > 500 && e.getY() < 540) {
			loadPresses++;												// if load button was pressed, increase consecutive count by 1
			if (loadPresses >= 3) {										// if consecutive count has reached 3, give options to user after preparing loaders
				loadPresses = 0;
				String saveName = "";
				int saveNum = 0;
				int wave = 0;
				int points = 0;
				int lives = 0;
				int kills = 0;
				int selection = 0;
				int loadNum = -1;
				endGame();												// freeze game first
				for (int i = 0; i < loader.length; i++) {				// reset all loaders, loaders simply save data for sorting and searching
					loader[i] = null;
				}
				FileInputStream File1; 											// creates input steam object
				BufferedReader In; 	  						 					// creates the reader object (NOTICE: THIS IS THE NEW VERSION OF READING, previous is deprecated)
				String fileInput = ""; 											// create string to store data 
				try  {															// try this...
					File1 = new FileInputStream("gameSaves.txt"); 				// Create a new file input stream that is connected to file named "gameSaves.txt" 
					In = new BufferedReader(new InputStreamReader(File1));		// Connect BufferedReader to the File object 
					fileInput = In.readLine();									// read line and store input into "fileInput" string
					int numLines = 1;											// starts the line count to calculate which number is being read
					if (fileInput == null) {									// if data doesn't exist, prompt user
						JFrame frame = new JFrame();
						JOptionPane.showMessageDialog(frame, "There is no save data.");
						selection = 6;											// options will not show and loading will stop
					}
					while (fileInput != null && fileInput != "") {				// while there is still valid text being read, a null result means that there is no more data to read
						fileInput = In.readLine();								// read line and store input into "fileInput" string
						switch (numLines % 232) {								// depending on which number is selected (remainder helps find which one, because numbers are in a pattern)
						case 1:
							saveNum = Integer.parseInt(fileInput);				// begin storing data into loaders
							break;
						case 3:
							saveName = fileInput;
							break;
						case 5:
							wave = Integer.parseInt(fileInput);
							break;
						case 7:
							lives = Integer.parseInt(fileInput);
							break;
						case 9:
							kills = Integer.parseInt(fileInput);
							break;
						case 11:
							points = Integer.parseInt(fileInput);
							break;
						}
						if (numLines % 232 == 231)								// initialize loader with stored data
							loader[numLines / 232] = new Loader (saveName, wave, points, lives, kills, saveNum);
						numLines++;												// increases line number by one each time
					} 
					In.close(); 												// closes stream and BufferedReader
				} 
				catch (FileNotFoundException fnfe) {							// if file doesn't exist, prompt user, won't happen though because the output method ensures that it is created
					System.out.println("Error - this file does not exist"); 
				}  
				catch (IOException ioe) {										// if input/error, prompt user
					System.out.println("error = " + e.toString() ); 
				}
				
				int saveGames, counter, low, high, mid, tempNum;				// creates all required variables for loading game saves
				String searchTerm;
				Loader tempLoader;												// swapper object
				while (selection != 6) {										// 6 is the cancel option
					JFrame frame = new JFrame();
					selection = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter a number:\n1-List all save files\n2-Search for save file and load it"
							+ "\n3-Search for save file and see information\n4-Find save game with highest wave\n5-Find save game with highest points"
							+ "\n6-Cancel and start new game"));
					switch (selection) {
					case 1:														// list option
						String main = "Saves:\n";
						sortLoaders();											// sorts in alphabetical order
						for (int i = 0; i < loader.length; i++) {				// creates list in a string
							if (loader[i] != null) {
								main = main.concat(loader[i].getName() + "\n");
							}
						}
						frame = new JFrame();
						JOptionPane.showMessageDialog(frame, main);				// displays string in a message prompt
						break;
					case 2:														// load option
						sortLoaders();											// sorts in alphabetical order
						saveGames = 0;
						for (int i = 0; i < loader.length; i++) {
							if (loader[i] != null) {							// finds out how many loaders are active
								saveGames++;
							}
						}
						frame = new JFrame();
						searchTerm = JOptionPane.showInputDialog(frame, "Enter name of save file");	// prompts user for selected save file name
						counter = 0;														// counter is used to prevent infinite loop
						low = 0;															// marks the lowest point to search
						high = saveGames;													// marks the highest point to search
						mid = 0;															// creates a variable to store the middle number
						while (low < high && counter < loader.length * 2) {					// as long as low and high are different numbers
							mid = low + (high - low) / 2;									// sets the middle number to be in the middle of the current low and high points
							if (loader[mid].getName().compareTo(searchTerm) < 0)			// if the middle string is larger than the one at mid point
								low = mid;													// sets low to mid to bring up the mid
							else if (loader[mid].getName().compareTo(searchTerm) > 0)		// if the middle string is smaller than the one at mid point
								high = mid;													// sets high to mid to bring down the mid
							else {
								loadNum = mid;												// returns the correct index value
								loadGame(loader[loadNum].getSaveNum());						// begins loading game with selected save file number
								selection = 6;												// ends loading option menu
								break;
							}
							counter++;
						}
						if (loadNum == -1) {												// if loadNum was never found, save game doesn't exist
							frame = new JFrame();
							JOptionPane.showMessageDialog(frame, "File doesn't exist.");
						}
						break;
					case 3:														// information option
						sortLoaders();											// sorts in alphabetical order
						saveGames = 0;
						for (int i = 0; i < loader.length; i++) {
							if (loader[i] != null) {
								saveGames++;
							}
						}
						frame = new JFrame();
						searchTerm = JOptionPane.showInputDialog(frame, "Enter name of save file");
						counter = 0;
						low = 0;															// marks the lowest point to search
						high = saveGames;													// marks the highest point to search
						mid = 0;															// creates a variable to store the middle number
						while (low < high && counter < loader.length * 2) {						// as long as low and high are different numbers
							mid = low + (high - low) / 2;										// sets the middle number to be in the middle of the current low and high points
							if (loader[mid].getName().compareTo(searchTerm) < 0)				// if the middle string is larger than the one at mid point
								low = mid;														// sets low to mid to bring up the mid
							else if (loader[mid].getName().compareTo(searchTerm) > 0)			// if the middle string is smaller than the one at mid point
								high = mid;														// sets high to mid to bring down the mid
							else {
								loadNum = mid;													// returns the correct index value
								frame = new JFrame();											// gives information on the specified game save
								JOptionPane.showMessageDialog(frame, "Name: " + loader[mid].getName() + "\nWave: " + loader[mid].getWave() + "\nKills: " + loader[mid].getKills() + "\nPoints: " + loader[mid].getPoints() + "\nLives: " + loader[mid].getLives());
								break;
							}
							counter++;
						}
						if (loadNum == -1) {
							frame = new JFrame();
							JOptionPane.showMessageDialog(frame, "File doesn't exist.");
						}
						break;
					case 4:																		// highest wave option
						tempNum = 0;															// Stores the array index number
						for (int indexNum = 0; indexNum < loader.length; indexNum++) {
							tempNum = indexNum;													// stores the index number here for comparisons in the next for loop
							for (int indexNum1 = indexNum + 1; indexNum1 < loader.length; indexNum1++) {
								if (loader[tempNum] != null && loader[indexNum1] != null && loader[tempNum].getWave() < loader[indexNum1].getWave())						// if the stored array number is smaller than the number that is being checked
										tempNum = indexNum1;							// stores the index number if its value is larger
							}
							tempLoader = loader[indexNum];								// swapping process happens here
							loader[indexNum] = loader[tempNum];
							loader[tempNum] = tempLoader;
						}
						frame = new JFrame();											// displays game save with the highest wave
						JOptionPane.showMessageDialog(frame, "Name: " + loader[0].getName() + "\nWave: " + loader[0].getWave() + "\nKills: " + loader[0].getKills() + "\nPoints: " + loader[0].getPoints() + "\nLives: " + loader[0].getLives());
						break;
					case 5:																		// highest point option
						tempNum = 0;															// Stores the array index number
						for (int indexNum = 0; indexNum < loader.length; indexNum++) {
							tempNum = indexNum;													// stores the index number here for comparisons in the next for loop
							for (int indexNum1 = indexNum + 1; indexNum1 < loader.length; indexNum1++) {
								if (loader[tempNum] != null && loader[indexNum1] != null && loader[tempNum].getPoints() < loader[indexNum1].getPoints())						// if the stored array number is smaller than the number that is being checked
										tempNum = indexNum1;						// stores the index number if its value is smaller
							}
							tempLoader = loader[indexNum];								// swapping process happens here
							loader[indexNum] = loader[tempNum];
							loader[tempNum] = tempLoader;
						}
						frame = new JFrame();											// displays game save with the highest points
						JOptionPane.showMessageDialog(frame, "Name: " + loader[0].getName() + "\nWave: " + loader[0].getWave() + "\nKills: " + loader[0].getKills() + "\nPoints: " + loader[0].getPoints() + "\nLives: " + loader[0].getLives());
						break;
					}
				}
			}
		}
		else
			loadPresses = 0;															// if anywhere outside load button is pressed, reset consecutive count
		
		if (e.getX() > 620  && e.getX() < 720 && e.getY() > 550 && e.getY() < 590) {
			newGamePresses++;															// starts a new game if new game button is pressed three times consecutively
			if (newGamePresses >= 3) {
				newGamePresses = 0;
				newGame();
			}
		}
		else
			newGamePresses = 0;
		
		if (!mouseClickTower) {															// if mouse is not clicking a tower at the moment and mouse clicks a tower
			for (int i = 0; i < towers.length; i++) {									// delete tower and refund points
				if (towers[i] != null && towerRect[i].contains(e.getX(), e.getY())) {
					towers[i] = null;
					towerRect[i] = null;
					points += 50;
				}
			}
		}
		
		if (!mouseClickBlockade) {														// if mouse is not clicking a blockade at the moment and mouse clicks a blockade
			for (int i = 0; i < blockades.length; i++) {								// delete blockade and refund points
				if (blockades[i] != null && blockadeRect[i].contains(e.getX(), e.getY())) {
					blockades[i] = null;
					blockadeRect[i] = null;
					points += 10;
				}
			}
		}
		if (mouseClickTower) {															// if mouse clicks again after holding tower, places tower
			boolean overlap = false;
			for (int j = 1; j < grid.length - 2; j++) {
				for (int n = 2; n < grid[0].length - 1; n++) {
					if (grid[j][n].contains(e.getX(), e.getY())) {						// make sure that the tower fits into a grid spot
						for (int i = 0; i < towers.length; i++) {
							if (towers[i] != null && dragger != i && towerRect[i].intersects(towerRect[dragger])) {
								overlap = true;										// two towers should never overlap
								break;												// a blockade and tower can overlap
							}
						}
						for (int i = 0; i < blockades.length; i++) {
							if (blockades[i] != null && dragger != i && blockadeRect[i].intersects(towerRect[dragger])) {
								overlap = true;											// two blockades should never overlap
								break;													// a blockade and tower can overlap
							}
						}
						if (overlap == false) {
							towers[dragger].setLocation((int) grid[j][n].getX(), (int) grid[j][n].getY());
							towerRect[dragger].setLocation(towers[dragger].getX() - 44, towers[dragger].getY() - 38);
							mouseClickTower = false;
							break;
						}
						break;
					}
				}
			}
		}
		if (mouseClickBlockade) {														// if mouse clicks again after holding blockade, places blockade
			boolean overlap = false;
			for (int j = 1; j < grid.length - 2; j++) {
				for (int n = 1; n < grid[0].length - 1; n++) {
					if (grid[j][n].contains(e.getX(), e.getY())) {						// make sure that the blockade fits into a grid spot
						for (int i = 0; i < towers.length; i++) {
							if (towers[i] != null && dragger != i && towerRect[i].intersects(blockadeRect[dragger])) {
								overlap = true;										// two towers should never overlap
								break;												// a blockade and tower can overlap
							}
						}
						for (int i = 0; i < blockades.length; i++) {
							if (blockades[i] != null && dragger != i && blockadeRect[i].intersects(blockadeRect[dragger])) {
								overlap = true;											// two blockades should never overlap
								break;													// a blockade and tower can overlap
							}
						}
						if (overlap == false) {
							blockades[dragger].setLocation((int) grid[j][n].getX(), (int) grid[j][n].getY());
							blockadeRect[dragger].setLocation(blockades[dragger].getX(), blockades[dragger].getY());
							mouseClickBlockade = false;
							break;
						}
						break;
					}
				}
			}
		}
	}
	
	public void mouseReleased (MouseEvent e) {
		
	}
	
	public void mouseDragged(MouseEvent e) {
		
	}

	public void mouseMoved(MouseEvent e) {
		if (mouseClickTower) {														// if mouse has clicked tower, tower will follow mouse
			for (int j = 1; j < grid.length - 2; j++) {
				for (int n = 2; n < grid[0].length - 1; n++) {
					if (grid[j][n].contains(e.getX(), e.getY())) {					// make sure that the tower fits into a grid spot
						towers[dragger].setLocation((int) grid[j][n].getX(), (int) grid[j][n].getY());
						towerRect[dragger].setLocation(towers[dragger].getX() - 44, towers[dragger].getY() - 38);
					}
				}
			}
		}
		if (mouseClickBlockade) {													// if mouse has clicked blockade, blockade will follow mouse
			for (int j = 1; j < grid.length - 2; j++) {
				for (int n = 1; n < grid[0].length - 1; n++) {
					if (grid[j][n].contains(e.getX(), e.getY())) {						// make sure that the blockade fits into a grid spot
						blockades[dragger].setLocation((int) grid[j][n].getX(), (int) grid[j][n].getY());
						blockadeRect[dragger].setLocation(blockades[dragger].getX(), blockades[dragger].getY());
					}
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	
	public void update(Graphics g) {									// updates graphics
		if (dbImage == null) {
			dbImage = createImage(this.getSize().width, this.getSize().height);
			dbg = dbImage.getGraphics();
		}

		dbg.setColor(getBackground());
		dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);

		dbg.setColor(getForeground());
		paint(dbg);

		g.drawImage(dbImage, 0, 0, this);
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.yellow);																// draws the grid in yellow
		for (int i = 0; i < grid.length - 1; i++) {
			for (int j = 1; j < grid[0].length - 1; j++) {
				g.drawRect((int) grid[i][j].getX(),(int) grid[i][j].getY(),(int) grid[i][j].getWidth(),(int) grid[i][j].getHeight());
			}
		}
		
		for (int i = 0; i < towers.length; i++) {												// draws all images
			if (towers[i] != null)
				towers[i].drawAntibody(g);
			for (int j = 0; j < shots[0].length; j++) {
			if (shots[i][j] != null)
				shots[i][j].drawShot(g);
			}	
		}
		for (int i = 0; i < blockades.length; i++) {
			if (blockades[i] != null)
				blockades[i].drawCell(g);
		}
		for (int i = 0; i < bacteria.length; i++) {
			if (bacteria[i] != null)
				bacteria[i].drawBacterium(g);
		}
		for (int i = 0; i < viruses.length; i++) {
			if (viruses[i] != null)
				viruses[i].drawVirus(g);
		}
		
		img = null; 												// initializes object, to prevent error
		try {
			img = ImageIO.read(Main.class.getResource("/imgs/antibody1.png")); // reads the
																	// antibody
																	// picture
		} catch (IOException ioe) {
			ioe.printStackTrace(); 									// catches errors
		}
		g.drawImage(img, 150, 500, null); 							// draws the image at the
																	// specified x and y
																	// coordinates
		try {
			img = ImageIO.read(Main.class.getResource("/imgs/white_blood_cell.png")); // reads the
																	// white blood cell
																	// picture
		} catch (IOException ioe) {
			ioe.printStackTrace(); // catches errors
		}
		g.drawImage(img, 240, 510, null); // draws the image at the
													// specified x and y
		
		g.setColor(Color.white);											// Prints game information
		g.drawString("Wave: " + waveCount, 10, 510);
		g.drawString("Enemies Killed: " + killed, 10, 530);
		g.drawString("Immunity Points: " + points, 10, 550);
		g.drawString("Lives: " + lives, 10, 570);
		
		if (gameOn && !waveOn) {											// draws the buttons only when they should be available
			g.setColor(Color.red);
			g.fillRect(510, 500, 100, 40);
			g.fillRect(510, 550, 100, 40);
			g.fillRect(620, 500, 100, 40);
			g.setColor(Color.white);
			g.drawString("Start Wave", 530, 522);
			g.drawString("Save Game", 530, 572);
			g.drawString("Load Game", 637, 522);
		}
		if (!waveOn) {
			g.setColor(Color.red);
			g.fillRect(620, 500, 100, 40);
			g.setColor(Color.white);
			g.drawString("Load Game", 637, 522);
		}
		if (gameOn && !waveOn && delayButton) {
			g.setColor(Color.red);
			g.fillRect(300, 500, 200, 40);
			g.setColor(Color.white);
			g.drawString("Upgrade Shot Firing Speed", 322, 522);
		}
		if (gameOn && !waveOn && damageButton) {
			g.setColor(Color.red);
			g.fillRect(300, 550, 200, 40);
			g.setColor(Color.white);
			g.drawString("Upgrade Blockade Damage", 330, 572);
		}
		
		g.setColor(Color.red);
		g.fillRect(620, 550, 100, 40);
		g.setColor(Color.white);
		g.drawString("New Game", 640, 572);
		
		for (int i = 0; i < towers.length; i++) {
			if (towers[i] != null) {
				g.drawRect((int) towerRect[i].getX(), (int) towerRect[i].getY(), (int) towerRect[i].getWidth(), (int) towerRect[i].getHeight());
			}
		}
		
	}
	
	/*
	 * Returns information about this applet. An applet should override this
	 * method to return a String containing information about the author,
	 * version, and copyright of the JApplet.
	 *
	 * @return a String representation of information about this JApplet
	 */
	public String getAppletInfo() {
		// provide information about the applet
		return "Title:   \nAuthor:   \nA simple applet example description. ";
	}

	/**
	 * Returns parameter information about this JApplet. Returns information
	 * about the parameters than are understood by this JApplet. An applet
	 * should override this method to return an array of Strings describing
	 * these parameters. Each element of the array should be a set of three
	 * Strings containing the name, the type, and a description.
	 *
	 * @return a String[] representation of parameter information about this
	 *         JApplet
	 */
	public String[][] getParameterInfo() {
		// provide parameter information about the applet
		String paramInfo[][] = { { "firstParameter", "1-10", "description of first parameter" },
				{ "status", "boolean", "description of second parameter" },
				{ "images", "url", "description of third parameter" } };
		return paramInfo;
	}
	
	/* Purpose: Plays selected sound file.
	 * Input: Selected sound file.
	 * Output: None.
	 */
	public void Sound(String fileName) {
		try {
			File url = new File(fileName);		// creates a file to read from, and a clip to play
			Clip clip;
			clip = AudioSystem.getClip();		// creates a audio clip
			AudioInputStream ais;
			ais = AudioSystem.getAudioInputStream(url);	// adds file data to audio input stream
			clip.open(ais);								// opens music stream
			clip.start();								// plays music or sound
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
}
