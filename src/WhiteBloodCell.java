import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class WhiteBloodCell {
	private int xPos, yPos, health; // stores x and y coordinates respectively, also health
	private BufferedImage img; // object used to load images

	// Constructors
	public WhiteBloodCell (int xPos, int yPos, int health) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.health = health;
	}
	
	/*
	 * Purpose: Sets a new location, used for dragging the white blood cell.
	 * Input: New x and y coordinates.
	 * Output: None.
	 */
	public void setLocation (int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	/*
	 * Purpose: Retrieve object's x-coordinate. Input: None. Output:
	 * x-coordinate.
	 */
	public int getX() {
		return xPos;
	}

	/*
	 * Purpose: Retrieve object's y-coordinate. Input: None. Output:
	 * y-coordinate.
	 */
	public int getY() {
		return yPos;
	}
	
	/* Purpose: Retrieve health of white blood cell.
	 * Input: None.
	 * Output: Health.
	 */
	public int getHealth() {
		return health;
	}
	
	/* Purpose: Sets the health of the white blood cell.
	 * Input: New health.
	 * Output: None.
	 */
	public void setHealth(int newHealth) {
		health = newHealth;
	}
	
	/*
	 * Purpose: This method is called to draw the image of the object at the
	 * object's xPos and yPos. 
	 * Input: Graphics object to draw with. 
	 * Output: Nothing, except for the drawn image.
	 */
	public void drawCell(Graphics g) {
		img = null; // initializes object, to prevent error
		try {
			img = ImageIO.read(Main.class.getResource("/imgs/white_blood_cell.png")); // reads the
																	// white blood cell
																	// picture
		} catch (IOException ioe) {
			ioe.printStackTrace(); // catches errors
		}
		g.drawImage(img, xPos, yPos, null); // draws the image at the
													// specified x and y
													// coordinates, without an
													// ImageObserver object.
	} 
}
