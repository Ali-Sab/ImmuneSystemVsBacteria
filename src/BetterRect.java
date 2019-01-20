import java.awt.Rectangle;

public class BetterRect extends Rectangle {				// extending the Rectangle class makes it very easy to create the nodes, with bonus features of the rectangle class
	private int hValue, gValue, isOpen;
	private boolean isWalkable;
	private BetterRect parent, gParent;
	
	// Constructor
	public BetterRect (int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	/* Purpose: Returns whether the node is on the open list, the closed list, or no list at all.
	 * No inputs.
	 * Outputs the list status.
	 */
	public int isOpen () {
		return isOpen;
	}
	
	/* Purpose: Changes the list status.
	 * Input: The new list status.
	 * Output: None.
	 */
	public void setOpen(int isOpen) {
		this.isOpen = isOpen;
	}
	
	/* Purpose: Reports whether the node is walkable (if there is no blockade on it).
	 * Input: None.
	 * Output: A boolean value of whether it's walkable or not.
	 */
	public boolean isWalkable() {
		return isWalkable;
	}
	
	/* Purpose: Sets walkable status.
	 * Input: New status.
	 * No output.
	 */
	public void setWalkable(boolean walkable) {
		isWalkable = walkable;
	}
	
	/* Purpose: Return the heuristic value. Heuristic value is from this node the target node.
	 * No inputs, outputs the hValue.
	 * 
	 */
	public int getHValue() {
		return hValue;
	}
	
	/* Purpose: Return the movement cost value. Movement cost is from this node to starting node.
	 * No inputs, outputs the hValue.
	 * 
	 */
	public int getGValue() {
		return gValue;
	}
	
	/* Purpose: Returns the total value of the node. The lower the value, the more likely that this node will be used. 
	 * No inputs. Outputs hValue and gValue added together.
	 * 
	 */
	public int getFValue() {
		return hValue + gValue;
	}
	
	/* Purpose: Sets new hValue.
	 * Input is new hValue. 
	 * No outputs.
	 */
	public void setHValue (int hValue) {
		this.hValue = hValue;
	}
	
	/* Purpose: Sets new gValue.
	 * Input is new gValue. 
	 * No outputs.
	 */
	public void setGValue (int gValue) {
		this.gValue = gValue;
	}
	
	/* Purpose: Returns the gParent. The gParent is the direct parent node, not the best parent.
	 * This is to help the path finding efficiency.
	 * Input: None. Outputs gParent.
	 */
	public BetterRect getGParent() {
		return gParent;
	}
	
	/* Purpose: Sets the new gParent.
	 * Input: New gParent.
	 * Output: None.
	 */
	public void setGParent(BetterRect gParent) {
		this.gParent = gParent;
	}
	
	/* Purpose: Returns the real parent.
	 * Input: None.
	 * Output: Parent node in the form of a BetterRect object.
	 */
	public BetterRect getParent() {
		return parent;
	}
	
	/* Purpose: Sets the parent of this node.
	 * Input: Parent node.
	 * Output: None.
	 */
	public void setParent(BetterRect parent) {
		this.parent = parent;
	}
}
