import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Antibody {
	private int xPos, yPos; // stores x and y coordinates respectively
	private BufferedImage img; // object used to load images

	// Constructors
	public Antibody (int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	/*
	 * Purpose: Generates a shot from the coordinates of this object.
	 * Input: None. 
	 * Output: Returns the newly created Shot object.
	 */
	public Shot generateShot(String color) {
		return new Shot(xPos, yPos, color);
	}

	/*
	 * Purpose: Sets a new location, used for dragging the tower.
	 * Input: New x and y coordinates.
	 * Output: None.
	 */
	public void setLocation (int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	/*
	 * Purpose: Retrieve object's x-coordinate. 
	 * Input: None. 
	 * Output: x-coordinate.
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

	/*
	 * Purpose: This method is called to draw the image of the object at the
	 * object's xPos and yPos. 
	 * Input: Graphics object to draw with. 
	 * Output: Nothing, except for the drawn image.
	 */
	public void drawAntibody(Graphics g) {
		img = null; // initializes object, to prevent error
		try {
			img = ImageIO.read(Main.class.getResource("/imgs/antibody1.png")); // reads the
																	// antibody
																	// picture
		} catch (IOException ioe) {
			ioe.printStackTrace(); // catches errors
		}
		g.drawImage(img, xPos - 44, yPos - 38, null); // draws the image at the
													// specified x and y
													// coordinates, without an
													// ImageObserver object.
	} // "x - 20" is to place the x-coordinate at the left most part of the
		// picture
}
