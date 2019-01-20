import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Bacteria {
	private int xPos, yPos, picture, health, startingY, steps; 	// stores x and y coordinates respectively, also stores
																//the selected picture, health, which row the bacteria started in, and the step the bacteria is in
	private BufferedImage img; // object used to load images

	// Constructors
	public Bacteria (int xPos, int yPos, int picture, int health, int startingY, int steps) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.picture = picture;
		this.health = health;
		this.startingY = startingY;
		this.steps = steps;
	}

	/*
	 * Purpose: Moves right or left, depending on positive or negative speed.
	 * Input: The speed (negative for left, positive for right). Output: None.
	 */
	public void moveX (int speed) {
		xPos += speed;
	}
	
	/* Purpose: Moves up or down.
	 * Input: The speed.
	 * No output.
	 */
	public void moveY (int speed) {
		yPos += speed;
	}

	/*
	 * Purpose: Retrieve object's x-coordinate. Input: None. Output:
	 * x-coordinate.
	 */
	public int getX() {
		return xPos;
	}

	/*
	 * Purpose: Retrieve object's y-coordinate. 
	 * Input: None. 
	 * Output: y-coordinate.
	 */
	public int getY() {
		return yPos;
	}
	
	/* Purpose: Returns the selected picture using an int value.
	 * No inputs.
	 * Outputs picture.
	 */
	public int getPic() {
		return picture;
	}
	
	/* Purpose: Retrieve health of bacterium.
	 * Input: None.
	 * Output: Health.
	 */
	public int getHealth() {
		return health;
	}
	
	/* Purpose: Sets the health of the bacterium.
	 * Input: New health.
	 * Output: None.
	 */
	public void setHealth(int newHealth) {
		health = newHealth;
	}
	
	/* Purpose: Returns starting row.
	 * No inputs, outputs starting row via int.
	 * 
	 */
	public int getStartingY()  {
		return startingY;
	}
	
	/* Purpose: Updates the step.
	 * Input: The new current step.
	 * Output: None.
	 */
	public void setStep(int steps) {
		this.steps = steps;
	}
	
	/* Purpose: Returns the current step.
	 * No inputs, outputs the current step.
	 */
	public int getStep() {
		return steps;
	}

	/*
	 * Purpose: This method is called to draw the image of the object at the
	 * object's xPos and yPos. Input: Graphics object to draw with. Output:
	 * Nothing, except for the drawn image.
	 */
	public void drawBacterium(Graphics g) {
		img = null; // initializes object, to prevent error
		if (picture == 0) {
			try {
				img = ImageIO.read(Main.class.getResource("/imgs/bacteria1.png")); // reads the 1st
																		// bacteria
																		// picture
			} catch (IOException ioe) {
				ioe.printStackTrace(); // catches errors
			}
		}
		else {
			try {
				img = ImageIO.read(Main.class.getResource("/imgs/bacteria2.png")); // reads the 2nd
																		// bacteria
																		// picture
			} catch (IOException ioe) {
				ioe.printStackTrace(); // catches errors
			}
		}
		g.drawImage(img, xPos, yPos, null); // draws the image at the
													// specified x and y
													// coordinates, without an
													// ImageObserver object.
	} 
}
