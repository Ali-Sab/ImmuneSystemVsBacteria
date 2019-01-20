import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Virus {
	private int xPos, yPos, health; // stores x and y coordinates respectively
	private BufferedImage img; // object used to load images

	// Constructors
	public Virus (int xPos, int yPos, int health) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.health = health;
	}

	/*
	 * Purpose: Moves right or left, depending on positive or negative speed.
	 * Input: The speed (negative for left, positive for right). Output: None.
	 */
	public void moveX(int speed) {
		xPos += speed;
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
	
	/* Purpose: Retrieve health of virus.
	 * Input: None.
	 * Output: Health.
	 */
	public int getHealth() {
		return health;
	}
	
	/* Purpose: Sets the health of the virus.
	 * Input: New health.
	 * Output: None.
	 */
	public void setHealth(int newHealth) {
		health = newHealth;
	}

	/*
	 * Purpose: This method is called to draw the image of the object at the
	 * object's xPos and yPos. Input: Graphics object to draw with. Output:
	 * Nothing, except for the drawn image.
	 */
	public void drawVirus(Graphics g) {
		img = null; // initializes object, to prevent error
		try {
			img = ImageIO.read(Main.class.getResource("/imgs/virus1.png")); // reads the
																	// virus
																	// picture
		} catch (IOException ioe) {
			ioe.printStackTrace(); // catches errors
		}
		g.drawImage(img, xPos, yPos, null); // draws the image at the
													// specified x and y
													// coordinates, without an
													// ImageObserver object.
	} // "x - 20" is to place the x-coordinate at the left most part of the
		// picture
}
