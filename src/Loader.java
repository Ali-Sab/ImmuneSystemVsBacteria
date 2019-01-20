
public class Loader {
	private String name;
	private int wave, points, lives, kills, saveNum;
	
	// Constructor
	public Loader (String name, int wave, int points, int lives, int kills, int saveNum) {
		this.name = name;
		this.wave = wave;
		this.points = points;
		this.lives = lives;
		this.kills = kills;
		this.saveNum = saveNum;
	}
	
	/* Purpose: Returns the name of the save file.
	 * No inputs, outputs name.
	 */
	public String getName() {
		return name;
	}
	
	/* Purpose: Returns wave of the save file.
	 * No inputs, outputs wave.
	 */
	public int getWave() {
		return wave;
	}
	
	/* Purpose: Returns points of the save file.
	 * No inputs, outputs the points.
	 */
	public int getPoints() {
		return points;
	}
	
	/* Purpose: Returns the lives of the save file.
	 * No inputs, outputs number of lives.
	 */
	public int getLives() {
		return lives;
	}
	
	/* Purpose: Returns total enemies killed of the save file.
	 * Input: None.
	 * Output: Kills.
	 */
	public int getKills() {
		return kills;
	}
	
	/* Purpose: Stores the gave save number, used to location the game save in the game save txt document, which stores all the saves.
	 * Input: None.
	 * Output: Save number.
	 */
	public int getSaveNum() {
		return saveNum;
	}
}
