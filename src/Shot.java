import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Shot {
	private int xPos, yPos; // stores x and y coordinates respectively
	private String color;
	private BufferedImage img; // object used to load images

	// Constructors
	public Shot (int xPos, int yPos, String color) {
		this.xPos = xPos;
		this.yPos = yPos;
		color = color.toLowerCase();
		this.color = color;
	}

	/*
	 * Purpose: Moves right or left, depending on positive or negative speed.
	 * Input: The speed (negative for left, positive for right). Output: None.
	 */
	public void moveX(int speed) {
		xPos += speed;
	}
	
	/* Purpose: Moves up or down.
	 * Input: The speed.
	 * No output.
	 */
	public void moveY(int speed) {
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
	 * Purpose: Retrieve object's y-coordinate. Input: None. Output:
	 * y-coordinate.
	 */
	public int getY() {
		return yPos;
	}

	private int paint (Graphics g, int xPos, int yPos, int originalSize, int size, int segNum, int direction) {
		int endX = (int) (xPos + size * Math.cos((2*Math.PI/18)*(direction + segNum - 1) * 3));		// creates a variable to store the end x-coordinate and calculates it using trigonometry
		int endY = (int) (yPos - size * Math.sin((2*Math.PI/18)*(direction + segNum - 1) * 3));		// creates a variable to store the end y-coordinate and calculates it using trigonometry
		g.drawLine(xPos, yPos, endX, endY);
		if (size < 1) 
			return -1;
		else {
			if (segNum == 1)										// used to alternate angles to draw line at
				segNum = 2;
			else 
				segNum = 1;
			paint(g, endX, endY, -1, (int) (size * 0.7), segNum, direction);	// recursion
		}
		if (originalSize == size) {
			paint(g, xPos, yPos, -1, size, 1, 2);								// creates all line segments from original position
			paint(g, xPos, yPos, -1, size, 1, 3);
			paint(g, xPos, yPos, -1, size, 1, 4);
			paint(g, xPos, yPos, -1, size, 1, 5);
			paint(g, xPos, yPos, -1, size, 1, 6);
		}
		return -1;
	}
	
	/*
	 * Purpose: This method is called to draw the image of the object at the
	 * object's xPos and yPos. 
	 * Input: Graphics object to draw with. 
	 * Output: Nothing, except for the drawn image.
	 */
	public void drawShot(Graphics g) {											// sets the color from the constructor
		if (color == "red")
			g.setColor(Color.red);
		else if (color == "blue") 
			g.setColor(Color.blue);
		else if (color == "green") 
			g.setColor(Color.green);
		else if (color == "yellow")
			g.setColor(Color.yellow);
		else
			g.setColor(Color.white);
		paint(g, xPos, yPos, 5, 5, 1, 1);
	} 
}
