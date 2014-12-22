package zombie_crush_saga.data;

import zombie_crush_saga.ui.ZombieCrushSagaZombie;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import zombie_crush_saga.ZombieCrushSaga.ZombieCrushSagaPropertyType;
import mini_game.MiniGame;
import mini_game.MiniGameDataModel;
import mini_game.SpriteType;
import properties_manager.PropertiesManager;
import static zombie_crush_saga.ZombieCrushSagaConstants.*;
import zombie_crush_saga.ui.ZombieCrushSagaMiniGame;
import zombie_crush_saga.ui.ZombieCrushSagaPanel;

/**
 * This class manages the game data for Zombie Crush Saga
 *
 * @author Luigi Keh & Richard McKenna
 */
public class ZombieCrushSagaDataModel extends MiniGameDataModel
{
	// THIS CLASS HAS A REFERERENCE TO THE MINI GAME SO THAT IT
	// CAN NOTIFY IT TO UPDATE THE DISPLAY WHEN THE DATA MODEL CHANGES
	private MiniGame miniGame;

	private boolean zombieCrusherActive;

	//keeps track of player lives.
	private int lives;
	private int moves;
	private int levelType; //0 = swap, 1 = jelly

	private int starGoal[]; //the star goals for the current level.

	//keeps track of score
	private int score;  //the score for the currentLevel

	// THE LEVEL GRID REFERS TO THE LAYOUT FOR A GIVEN LEVEL, MEANING
	// HOW MANY zombies FIT INTO EACH CELL WHEN FIRST STARTING A LEVEL
	private ZombieCrushSagaBoardSpace boardSpace;
	private ZombieCrushSagaBoardJelly boardJelly;
	private ZombieCrushSagaBoardZombies boardZombies;
	private int[][] boardScore; //this is the scoreboard.
	private boolean renderScore;

	// LEVEL GRID DIMENSIONS
	private int gridColumns;
	private int gridRows;

	// THESE ARE THE Zombies THE PLAYER HAS MATCHED
	private ArrayList<ZombieCrushSagaZombie> stackZombies;

	// THESE ARE THE zombies THAT ARE MOVING AROUND, AND SO WE HAVE TO UPDATE
	private ArrayList<ZombieCrushSagaZombie> movingZombies;

	// THIS IS A SELECTED zombie, MEANING THE FIRST OF A PAIR THE PLAYER
	// IS TRYING TO MATCH. THERE CAN ONLY BE ONE OF THESE AT ANY TIME
	private ZombieCrushSagaZombie selectedZombie;
	private boolean badMove;
	private ZombieCrushSagaZombie badZombie1;
	private ZombieCrushSagaZombie badZombie2;

	// THE INITIAL LOCATION OF zombies BEFORE BEING PLACED IN THE GRID
	private int unassignedZombiesX;
	private int unassignedZombiesY;

	// THESE ARE USED FOR TIMING THE GAME
	private GregorianCalendar startTime;
	private GregorianCalendar endTime;

	// THE REFERENCE TO THE FILE BEING PLAYED
	private String currentLevel;

	/**
	 * Constructor for initializing this data model, it will create
	 * the data structures for storing zombies, but not the zombie grid
	 * itself, that is dependent of file loading, and so should be
	 * subsequently initialized.
	 *
	 * @param initMiniGame The ZombieCrushSaga game UI.
	 */
	public ZombieCrushSagaDataModel(MiniGame initMiniGame)
	{
		// KEEP THE GAME FOR LATER
		miniGame = initMiniGame;

		// INIT THESE FOR HOLDING MATCHED AND MOVING zombies
		stackZombies = new ArrayList<ZombieCrushSagaZombie>();
		movingZombies = new ArrayList<ZombieCrushSagaZombie>();
		badMove = false;
		lives = 5;          //change later to be dependent
		moves = 0;
		starGoal = new int[3];
		boardSpace = new ZombieCrushSagaBoardSpace();
		boardJelly = new ZombieCrushSagaBoardJelly();
		renderScore = false;
		zombieCrusherActive = false;
	}

	// INIT METHODS - AFTER CONSTRUCTION, THESE METHODS SETUP A GAME FOR USE
	// - initZombies
	// - initZombie
	// - initLevelGrid
	// - initSpriteType

	/**
	 * This method loads the zombies, creating an individual sprite for each. Note
	 * that zombies may be of various types, which is important during the zombie
	 * matching tests.
	 */
	public void initZombies()
	{
		//get image path
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		String imgPath = props.getProperty(ZombieCrushSagaPropertyType.IMG_PATH);
		int spriteTypeID = 0;
		SpriteType sT;

		// THIS IS A  BLANK zombie
		String blankZombieFileName = props.getProperty(ZombieCrushSagaPropertyType.ZOMBIE_BLANK_IMAGE_NAME);
		BufferedImage blankZombieImage = miniGame.loadImageWithColorKey(imgPath + blankZombieFileName, COLOR_KEY);
		((ZombieCrushSagaPanel)(miniGame.getCanvas())).setBlankZombieImage(blankZombieImage);

		// THIS IS A HIGHLIGHTED BLANK zombie FOR WHEN THE PLAYER SELECTS ONE
		String blankZombieSelectedFileName = props.getProperty(ZombieCrushSagaPropertyType.ZOMBIE_SELECTED_IMAGE_NAME);
		BufferedImage blankZombieSelectedImage = miniGame.loadImageWithColorKey(imgPath + blankZombieSelectedFileName, COLOR_KEY);
		((ZombieCrushSagaPanel)(miniGame.getCanvas())).setBlankZombieSelectedImage(blankZombieSelectedImage);

		// We make 81 Zombies to represent the max amount of possible zombies on the board.
		ArrayList<String> zombies = props.getPropertyOptionsList(ZombieCrushSagaPropertyType.ZOMBIE_COLORS);

		//make 81 zombies
		for(int i = 0; i < NUM_ZOMBIES; i++ ) {

			ArrayList<String> imgFiles = new ArrayList<String>();

			for(int j = 0; j < zombies.size(); j++){
				imgFiles.add(imgPath + zombies.get(j));
			}

			sT = initZombieSpriteType(imgFiles, ZOMBIE_SPRITE_TYPE_PREFIX + spriteTypeID);
			initZombie(sT, ZOMBIE_TYPE);
			spriteTypeID++;

		}

	}

	/**
	 * Helper method for loading the zombies, it constructs the prescribed
	 * zombie type using the provided sprite type.
	 *
	 * @param sT The sprite type to use to represent this zombie during rendering.
	 *
	 * @param zombieType The type of zombie. Note that there are 3 broad categories.
	 */
	private void initZombie(SpriteType sT, String zombieType)
	{
		// CONSTRUCT THE zombie
		ZombieCrushSagaZombie newZombie = new ZombieCrushSagaZombie(sT, unassignedZombiesX, unassignedZombiesY, 0, 0, INVISIBLE_STATE, zombieType);

		// AND ADD IT TO THE STACK
		stackZombies.add(newZombie);
	}

	public void initBoardScore(int[][] initBoardScore){
		boardScore = initBoardScore;
	}

	/**
	 * Called after a level has been selected, it initializes the grid
	 * so that it is the proper dimensions.
	 *
	 * @param initGrid The grid distribution of zombies, where each cell
	 * specifies the number of zombies to be stacked in that cell.
	 *
	 * @param initGridColumns The columns in the grid for the level selected.
	 *
	 * @param initGridRows The rows in the grid for the level selected.
	 */
	public void initLevelGrid(int[][] initSpace, int[][] initJelly)
	{
		//KEEP ALL THE GRID INFO
		//convert int to boolean
		boolean[][] booleanArray = new boolean[9][9];

		for(int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if(initSpace[i][j] == 1)
					booleanArray[i][j] = true;
				else
					booleanArray[i][j] = false;
			}
		}

		boardSpace.initBoard(booleanArray);

		boolean[][] booleanJellyArray = new boolean[9][9];
		for(int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if(initJelly[i][j] == 1)
					booleanJellyArray[i][j] = true;
				else
					booleanJellyArray[i][j] = false;
			}
		}
		boardJelly.initBoard(booleanJellyArray);

		// MAKE ALL THE zombies VISIBLE
		enableZombies(true);
	}

	/**
	 * This helper method initializes a sprite type for a Zombie or set of
	 * similar Zombies to be created.
	 */
	private SpriteType initZombieSpriteType(ArrayList<String> imgFiles, String spriteTypeID)
	{

		// WE'LL MAKE A NEW SPRITE TYPE FOR EACH GROUP OF SIMILAR LOOKING zombieS
		SpriteType sT = new SpriteType(spriteTypeID);
		addSpriteType(sT);

		// LOAD THE ART

		BufferedImage img = null;

		for(int i = 0; i < imgFiles.size(); i++) {

			img = miniGame.loadImageWithColorKey(imgFiles.get(i), COLOR_KEY);
			Image tempImage = img.getScaledInstance(ZOMBIE_IMAGE_WIDTH, ZOMBIE_IMAGE_HEIGHT, BufferedImage.SCALE_SMOOTH);
			img = new BufferedImage(ZOMBIE_IMAGE_WIDTH, ZOMBIE_IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			img.getGraphics().drawImage(tempImage, 0, 0, null);

			// WE'LL USE THE SAME IMAGE FOR ALL STATES

			//these are in a specific order. abc order. do not change
			switch(i){
			case 0:
				sT.addState(ZOMBIE_BLUE_STATE, img); break;
			case 1:
				sT.addState(ZOMBIE_GREEN_STATE, img); break;
			case 2:
				sT.addState(ZOMBIE_ORANGE_STATE, img); break;
			case 3:
				sT.addState(ZOMBIE_PURPLE_STATE, img); break;
			case 4:
				sT.addState(ZOMBIE_RED_STATE, img); break;
			case 5:
				sT.addState(ZOMBIE_YELLOW_STATE, img); break;
			case 6:
				sT.addState(ZOMBIE_BLUE_STRIPED_HORIZONTAL_STATE, img); break;
			case 7:
				sT.addState(ZOMBIE_GREEN_STRIPED_HORIZONTAL_STATE, img); break;
			case 8:
				sT.addState(ZOMBIE_ORANGE_STRIPED_HORIZONTAL_STATE, img); break;
			case 9:
				sT.addState(ZOMBIE_PURPLE_STRIPED_HORIZONTAL_STATE, img); break;
			case 10:
				sT.addState(ZOMBIE_RED_STRIPED_HORIZONTAL_STATE, img); break;
			case 11:
				sT.addState(ZOMBIE_YELLOW_STRIPED_HORIZONTAL_STATE, img); break;
			case 12:
				sT.addState(ZOMBIE_BLUE_STRIPED_VERTICAL_STATE, img); break;
			case 13:
				sT.addState(ZOMBIE_GREEN_STRIPED_VERTICAL_STATE, img); break;
			case 14:
				sT.addState(ZOMBIE_ORANGE_STRIPED_VERTICAL_STATE, img); break;
			case 15:
				sT.addState(ZOMBIE_PURPLE_STRIPED_VERTICAL_STATE, img); break;
			case 16:
				sT.addState(ZOMBIE_RED_STRIPED_VERTICAL_STATE, img); break;
			case 17:
				sT.addState(ZOMBIE_YELLOW_STRIPED_VERTICAL_STATE, img); break;
			case 18:
				sT.addState(ZOMBIE_BLUE_WRAPPED_STATE, img); break;
			case 19:
				sT.addState(ZOMBIE_GREEN_WRAPPED_STATE, img); break;
			case 20:
				sT.addState(ZOMBIE_ORANGE_WRAPPED_STATE, img); break;
			case 21:
				sT.addState(ZOMBIE_PURPLE_WRAPPED_STATE, img); break;
			case 22:
				sT.addState(ZOMBIE_RED_WRAPPED_STATE, img); break;
			case 23:
				sT.addState(ZOMBIE_YELLOW_WRAPPED_STATE, img); break;
			case 24:
				sT.addState(ZOMBIE_FIVE_STATE, img); break;
			default:
				System.out.println("too many color states. not accounted for."); break;
			}

		}

		sT.addState(INVISIBLE_STATE, img);

		return sT;
	}

	// ACCESSOR METHODS

	/**
	 * Accessor method for getting the level currently being played.
	 *
	 * @return The level name used currently for the game screen.
	 */
	public String getCurrentLevel()
	{
		return currentLevel;
	}

	/**
	 * Accessor method for getting the number of zombie columns in the game grid.
	 *
	 * @return The number of columns (left to right) in the grid for the level
	 * currently loaded.
	 */
	public int getGridColumns()
	{
		return gridColumns;
	}

	/**
	 * Accessor method for getting the number of zombie rows in the game grid.
	 *
	 * @return The number of rows (top to bottom) in the grid for the level
	 * currently loaded.
	 */
	public int getGridRows()
	{
		return gridRows;
	}

	public ZombieCrushSagaBoardJelly getBoardJelly(){
		return boardJelly;
	}

	public int[][] getBoardScore(){
		return boardScore;
	}

	public ZombieCrushSagaBoardSpace getBoardSpace(){
		return boardSpace;
	}

	public ZombieCrushSagaBoardZombies getBoardZombies(){
		return boardZombies;
	}

	/**
	 * Accessor method for getting the stack zombies.
	 *
	 * @return The stack zombies, which are the zombies the matched zombies
	 * are placed in.
	 */
	public ArrayList<ZombieCrushSagaZombie> getStackZombies()
	{
		return stackZombies;
	}

	/**
	 * Accessor method for getting the moving zombies.
	 *
	 * @return The moving zombies, which are the zombies currently being
	 * animated as they move around the game.
	 */
	public Iterator<ZombieCrushSagaZombie> getMovingZombies()
	{
		return movingZombies.iterator();
	}

	/**
	 * Mutator method for setting the currently loaded level.
	 *
	 * @param initCurrentLevel The level name currently being used
	 * to play the game.
	 */
	public void setCurrentLevel(String initCurrentLevel)
	{
		currentLevel = initCurrentLevel;
	}

	public int getLives() {
		return lives;
	}

	public int getMoves() {
		return moves;
	}

	public boolean getZombieCrusherActive(){
		return zombieCrusherActive;
	}

	public boolean getRenderScore(){
		return renderScore;
	}

	public int getLevelType(){
		return levelType;
	}

	public int getScore(){
		return score;
	}

	public boolean hasMovingZombies(){
		return (!movingZombies.isEmpty());
	}

	public void setMoves(int initMoves) {
		moves = initMoves;
	}

	public void setStarGoal(int[] initStarGoal){
		for(int i = 0; i < 3; i++){
			starGoal[i] = initStarGoal[i];
		}
	}

	public void setLives(int initLives) {
		lives = initLives;
	}

	public void setScore(int initScore){
		score = initScore;

	}

	public void setLevelType(int initLevelType){
		levelType = initLevelType;  
	}

	public void setZombieCrusherActive(boolean newState){
		zombieCrusherActive = newState;
	}

	/**
	 * 0 = 1 star
	 * 1 = 2 star
	 * 2 = 3 star
	 * 
	 * @param starNum the number of stars. 0 = 1 star... 2 = 3 stars.
	 * @return the star goal you specified.
	 */
	 public int getStarGoal(int starNum){
		return starGoal[starNum];
	 }


	 /**
	  * Used to calculate the x-axis pixel location in the game grid for a zombie
	  * placed at column with stack position z.
	  *
	  * @param column The column in the grid the zombie is located.
	  *
	  * @return The x-axis pixel location of the zombie
	  */
	 public int calculateZombieXInGrid(int column)
	 {
		 return (int)(BOARD_FIRST_SPACE_X + (column * ZOMBIE_IMAGE_WIDTH));
	 }

	 /**
	  * Used to calculate the y-axis pixel location in the game grid for a zombie
	  * placed at row with stack position z.
	  *
	  * @param row The row in the grid the zombie is located.
	  *
	  * @return The y-axis pixel location of the zombie
	  */
	 public int calculateZombieYInGrid(int row)
	 {
		 return (int)(BOARD_FIRST_SPACE_Y + (row * ZOMBIE_IMAGE_WIDTH));
	 }

	 /**
	  * Used to calculate the grid column for the x-axis pixel location.
	  *
	  * @param x The x-axis pixel location for the request.
	  *
	  * @return The column that corresponds to the x-axis location x.
	  */
	 public int calculateGridCellColumn(int x)
	 {

		 if(x < BOARD_FIRST_SPACE_X || x > BOARD_FIRST_SPACE_X + (BOARD_SPACE_SIZE * 9) - 1)
			 return -1;

		 float newX = x - BOARD_FIRST_SPACE_X;
		 int column = ((int)newX) / ((int)BOARD_SPACE_SIZE);

		 return column ;

	 }

	 /**
	  * Used to calculate the grid row for the y-axis pixel location.
	  *
	  * @param y The y-axis pixel location for the request.
	  *
	  * @return The row that corresponds to the y-axis location y.
	  */
	 public int calculateGridCellRow(int y)
	 {
		 if(y < BOARD_FIRST_SPACE_Y || y > BOARD_FIRST_SPACE_Y + (BOARD_SPACE_SIZE * 9) - 1)
			 return -1;

		 float newY = y - BOARD_FIRST_SPACE_Y;
		 int row = ((int)newY) / ((int)BOARD_SPACE_SIZE);

		 return row ;

	 }

	 // TIME TEXT METHODS
	 // - timeToText
	 // - gameTimeToText

	 /**
	  * This method creates and returns a textual description of
	  * the timeInMillis argument as a time duration in the format
	  * of (H:MM:SS).
	  *
	  * @param timeInMillis The time to be represented textually.
	  *
	  * @return A textual representation of timeInMillis.
	  */
	 public String timeToText(long timeInMillis)
	 {
		 // FIRST CALCULATE THE NUMBER OF HOURS,
		 // SECONDS, AND MINUTES
		 long hours = timeInMillis/MILLIS_IN_AN_HOUR;
		 timeInMillis -= hours * MILLIS_IN_AN_HOUR;
		 long minutes = timeInMillis/MILLIS_IN_A_MINUTE;
		 timeInMillis -= minutes * MILLIS_IN_A_MINUTE;
		 long seconds = timeInMillis/MILLIS_IN_A_SECOND;

		 // THEN ADD THE TIME OF GAME SUMMARIZED IN PARENTHESES
		 String minutesText = "" + minutes;
		 if (minutes < 10)   minutesText = "0" + minutesText;
		 String secondsText = "" + seconds;
		 if (seconds < 10)   secondsText = "0" + secondsText;
		 return hours + ":" + minutesText + ":" + secondsText;
	 }

	 /**
	  * This method builds and returns a textual representation of
	  * the game time. Note that the game may still be in progress.
	  *
	  * @return The duration of the current game represented textually.
	  */
	 public String gameTimeToText()
	 {
		 // CALCULATE GAME TIME USING HOURS : MINUTES : SECONDS
		 if ((startTime == null) || (endTime == null))
			 return "";
		 long timeInMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
		 return timeToText(timeInMillis);
	 }

	 // GAME DATA SERVICE METHODS
	 // -enableZombies
	 // -findMove
	 // -moveAllZombiesToStack
	 // -moveZombies
	 // -playWinAnimation
	 // -processMove
	 // -selectZombie
	 // -undoLastMove


	 public void deselectSelectedZombie(){
		 //if there is a selectedZombie, deselect it.
		 if(selectedZombie != null){
			 selectedZombie.deselect();
			 selectedZombie = null;
		 }
	 }

	 /**
	  * This method can be used to make all of the zombies either visible (true)
	  * or invisible (false). This should be used when switching between the
	  * splash and game screens.
	  *
	  * @param enable Specifies whether the zombies should be made visible or not.
	  */
	 public void enableZombies(boolean enable)
	 {
		 // GO THROUGH ALL OF THEM
		 for (ZombieCrushSagaZombie zombie : stackZombies)
		 {
			 // AND SET THEM PROPERLY
			 if (enable)
				 zombie.randomColor();
			 else
				 zombie.setState(INVISIBLE_STATE);
		 }
	 }


	 /**
	  * This method updates all the necessary state information
	  * to process the move argument. Assumes the move is valid.
	  *
	  * @param move The move to make. Note that a move specifies
	  * the cell locations for a match.
	  */
	 public void processMove(ZombieCrushSagaMove move)
	 {
		 // REMOVE THE MOVE zombies FROM THE GRID
		 ZombieCrushSagaZombie zombie1 = boardZombies.getZombie(move.row1, move.col1);
		 ZombieCrushSagaZombie zombie2 = boardZombies.getZombie(move.row2, move.col2);

		 // MAKE SURE BOTH ARE UNSELECTED
		 zombie1.deselect();
		 zombie2.deselect();

		 // SEND THEM TO THE swap places
		 zombie1.setTarget(zombie2.getX(), zombie2.getY());
		 zombie1.startMovingToTarget(MAX_ZOMBIE_VELOCITY);
		 zombie2.setTarget(zombie1.getX(), zombie1.getY());
		 zombie2.startMovingToTarget(MAX_ZOMBIE_VELOCITY);

		 // MAKE SURE THEY MOVE
		 movingZombies.add(zombie1);
		 movingZombies.add(zombie2);

		 //swap the two in the data.
		 boardZombies.swapTwo(zombie1, zombie2);

		 boardZombies.resetCurrentBoardScore();
		 boardZombies.setMultiplier(0);

		 //if at least one is the five bomb, do a special move.
		 if(zombie1.getZombieState().equals(ZOMBIE_FIVE_STATE) || zombie2.getZombieState().equals(ZOMBIE_FIVE_STATE)){
			 boardZombies.swapFiveBombTwo(zombie1, zombie2);
			 moves--;
		 }
		 else{   //neither of the two are five bombs.       
			 //check for matches
			 if(boardZombies.hasMatches()){ //if match found after move  
				 //resets the board's multipliers since this is the first match.
				 moves--;

				 //the update method checks for more matches.
			 }
			 else{ //make them move and move back
				 badZombie1 = zombie1;
				 badZombie2 = zombie2;

				 badMove = true; 
			 }
		 }

		 // AND MAKE SURE NEW zombieS CAN BE SELECTED
		 selectedZombie = null;

	 }

	 /**
	  * This method attempts to select the selectZombie argument. Note that
	  * this may be the first or second selected zombie. If a zombie is already
	  * selected, it will attempt to process a match/move.
	  *
	  * @param selectZombie The zombie to select.
	  */
	 public void selectZombie(ZombieCrushSagaZombie selectZombie)
	 {

		 //if the zombie is invisible.
		 if(selectZombie.isInvisible())
			 return;

		 //if the zombie crusher is active, we gotta mark that zombie for destruction
		 if(this.getZombieCrusherActive()){
			 boardZombies.useZombieSmasher(selectZombie);
			 boardZombies.resetCurrentBoardScore();
			 boardZombies.setMultiplier(0);
			 return;
		 }

		 // IF IT'S ALREADY THE SELECTED zombie, DESELECT IT
		 if (selectZombie == selectedZombie)
		 {
			 deselectSelectedZombie();
			 return;
		 }

		 //we may need this for later
		 //        //if the selectTile isn't free, return nothing.
		 //        if(!this.checkZombieIsFree(selectZombie)){
		 //            lastIncorrectSelectedTile = selectZombie;
		 //            selectZombie.setState(INCORRECTLY_SELECTED_STATE);
		 //            miniGame.getAudio().play(ZombieCrushSagaPropertyType.NO_MATCH_AUDIO_CUE.toString(), false);
		 //            return;
		 //        }

		 // IT'S FREE
		 //if one is not selected, select a new "selectedZombie",
		 if (selectedZombie == null)
		 {
			 selectedZombie = selectZombie;
			 if(!selectedZombie.isSelected()) //if it is not selected
				 selectedZombie.toggleSelected();
			 //miniGame.getAudio().play(ZombieCrushSagaPropertyType.SELECT_AUDIO_CUE.toString(), false);
		 }
		 else
		 {
			 ZombieCrushSagaMove move = new ZombieCrushSagaMove();

			 //if they're neighboring
			 if (selectedZombie.isAdjacentZombie(selectZombie))
			 {
				 // YES, FILL IN THE MOVE AND RETURN IT
				 move.col1 = selectedZombie.getGridColumn();
				 move.row1 = selectedZombie.getGridRow();
				 move.col2 = selectZombie.getGridColumn();
				 move.row2 = selectZombie.getGridRow();

				 this.processMove(move);

			 }
			 else{//if it's not neighboring, this zombie is the new selectedZombie.
				 this.deselectSelectedZombie();
				 selectedZombie = selectZombie;
				 selectZombie.toggleSelected();
			 }

		 } 

	 }

	 // OVERRIDDEN METHODS
	 // - checkMousePressOnSprites
	 // - endGameAsWin
	 // - endGameAsLoss
	 // - reset
	 // - updateAll
	 // - updateDebugText

	 /**
	  * This method provides a custom game response for handling mouse clicks
	  * on the game screen. We'll use this to close game dialogs as well as to
	  * listen for mouse clicks on grid cells.
	  *
	  * @param game The ZombieCrushSaga game.
	  *
	  * @param x The x-axis pixel location of the mouse click.
	  *
	  * @param y The y-axis pixel location of the mouse click.
	  */
	 @Override
	 public void checkMousePressOnSprites(MiniGame game, int x, int y)
	 {
		 // FIGURE OUT THE CELL IN THE GRID
		 int col = calculateGridCellColumn(x);
		 int row = calculateGridCellRow(y);

		 //if we're outside of the grid complete, don't do anything.
		 if( col == -1 || row == -1)
			 return;

		 // DISABLE THE STATS DIALOG IF IT IS OPEN
		 if (game.getGUIDialogs().get(STATS_DIALOG_TYPE).getState().equals(VISIBLE_STATE))
		 {
			 game.getGUIDialogs().get(STATS_DIALOG_TYPE).setState(INVISIBLE_STATE);
			 return;
		 }

		 //test for valid space later
		 if(movingZombies.isEmpty()){
			 // GET AND TRY TO SELECT THe zombie in that cell, IF THERE IS ONE
			 ZombieCrushSagaZombie testZombie = boardZombies.getZombie(row, col);
			 //testZombie cannot be null
			 if(testZombie != null)
				 if (testZombie.containsPoint(x, y))
					 selectZombie(testZombie);

		 }

	 }

	 @Override
	 public void checkMouseDragOnSprites(MiniGame game, int x, int y, boolean drag)
	 {
		 // FIGURE OUT THE CELL IN THE GRID
		 int col = calculateGridCellColumn(x);
		 int row = calculateGridCellRow(y);

		 //if we're outside of the grid complete, don't do anything.
		 if( col == -1 || row == -1)
			 return;

		 //test for valid space later
		 if(movingZombies.isEmpty() && selectedZombie == null){
			 // GET AND TRY TO SELECT THE Zombie IN THAT CELL, IF THERE IS ONE
			 ZombieCrushSagaZombie testZombie = boardZombies.getZombie(row, col);
			 if(testZombie != null)  //Zombie can't be null
				 if (testZombie.containsPoint(x, y))
					 selectZombie(testZombie);
		 }
		 else if(movingZombies.isEmpty() && selectedZombie != null){

			 ZombieCrushSagaZombie testZombie = boardZombies.getZombie(row, col);

			 //testZombie cannot be null
			 if(testZombie != null){
				 //if testZombie is not adjacent and not adjacent to the selectedZombie
				 if(!selectedZombie.isAdjacentZombie(testZombie)){
					 if(testZombie != selectedZombie)    //as long as these two aren't the same.
						 if (testZombie.containsPoint(x, y)){

							 selectedZombie.deselect();
							 selectedZombie = null;
							 selectZombie(testZombie);
							 return;
						 }
				 }
				 if(testZombie != selectedZombie)
					 if (testZombie.containsPoint(x, y))
						 selectZombie(testZombie);
			 }
		 }

	 }

	 /**
	  * Called when the game is won, it will record the ending game time, update
	  * the player record, display the win dialog, and play the win animation.
	  */
	 @Override
	 public void endGameAsLoss()
	 {
		 // UPDATE THE GAME STATE USING THE INHERITED FUNCTIONALITY
		 super.endGameAsLoss();

		 // RECORD THE TIME IT TOOK TO COMPLETE THE GAME
		 // long gameTime = endTime.getTimeInMillis() - startTime.getTimeInMillis();

		 // RECORD IT AS A LOSS
		 //((ZombieCrushSagaMiniGame)miniGame).getPlayerRecord().addLoss(currentLevel);
		 ((ZombieCrushSagaMiniGame)miniGame).savePlayerRecord();

		 // DISPLAY THE LOSS DIALOG & hide stats
		 miniGame.getGUIDialogs().get(LOSS_DIALOG_TYPE).setState(VISIBLE_STATE);
		 miniGame.getGUIDialogs().get(STATS_DIALOG_TYPE).setState(INVISIBLE_STATE);
		 miniGame.getGUIButtons().get(GAMEPLAY_REPLAY_BUTTON_TYPE).setState(VISIBLE_STATE);
		 miniGame.getGUIButtons().get(GAMEPLAY_REPLAY_BUTTON_TYPE).setEnabled(true);

		 //disable the smasher button.
		 miniGame.getGUIButtons().get(GAMEPLAY_SMASHER_BUTTON_TYPE).setState(INVISIBLE_STATE);
		 miniGame.getGUIButtons().get(GAMEPLAY_SMASHER_BUTTON_TYPE).setEnabled(false);

		 this.setZombieCrusherActive(false);

		 // AND PLAY THE Loss AUDIO
		 //        miniGame.getAudio().stop(ZombieCrushSagaPropertyType.SPLASH_SCREEN_SONG_CUE.toString());
		 //        miniGame.getAudio().stop(ZombieCrushSagaPropertyType.GAMEPLAY_SONG_CUE.toString());
		 //        miniGame.getAudio().play(ZombieCrushSagaPropertyType.LOSS_AUDIO_CUE.toString(), false);
	 }

	 /**
	  * Called when the game is won, it will record the ending game time, update
	  * the player record, display the win dialog, and play the win animation.
	  */
	 @Override
	 public void endGameAsWin()
	 {

		 // UPDATE THE GAME STATE USING THE INHERITED FUNCTIONALITY
		 super.endGameAsWin();

		 // RECORD THE TIME IT TOOK TO COMPLETE THE GAME
		 //        long gameTime = endTime.getTimeInMillis() - startTime.getTimeInMillis();

		 // RECORD IT AS A WIN
		 ((ZombieCrushSagaMiniGame)miniGame).getPlayerRecord().addWin(currentLevel, score);
		 ((ZombieCrushSagaMiniGame)miniGame).savePlayerRecord();

		 // DISPLAY THE WIN DIALOG & hide stats
		 //miniGame.getGUIDialogs().get(WIN_DIALOG_TYPE).setState(VISIBLE_STATE);
		 miniGame.getGUIDialogs().get(STATS_DIALOG_TYPE).setState(VISIBLE_STATE);

		 //disable the smasher button.
		 miniGame.getGUIButtons().get(GAMEPLAY_SMASHER_BUTTON_TYPE).setState(INVISIBLE_STATE);
		 miniGame.getGUIButtons().get(GAMEPLAY_SMASHER_BUTTON_TYPE).setEnabled(false);

		 this.setZombieCrusherActive(false);
		 // AND PLAY THE WIN ANIMATION
		 //  playWinAnimation();

		 // AND PLAY THE WIN AUDIO
		 //        miniGame.getAudio().stop(ZombieCrushSagaPropertyType.SPLASH_SCREEN_SONG_CUE.toString());
		 //        miniGame.getAudio().stop(ZombieCrushSagaPropertyType.GAMEPLAY_SONG_CUE.toString());
		 //        miniGame.getAudio().play(ZombieCrushSagaPropertyType.WIN_AUDIO_CUE.toString(), false);
	 }


	 public void randomizeBoardZombies(){
		 boardZombies.randomBoard();
	 }

	 /**
	  * Called when a game is started, the game grid is reset.
	  *
	  * @param game
	  */
	 @Override
	 public void reset(MiniGame game)
	 {
		 int zombieCounter = 0;
		 int row, column;
		 selectedZombie = null;

		 score = 0;

		 //make a brand new zombie board.
		 boardZombies = new ZombieCrushSagaBoardZombies();
		 boardZombies.setBoardSpace(boardSpace);
		 boardZombies.setBoardJelly(boardJelly);

		 //link the boardZombie's boardScore with this.boardScore;
		 initBoardScore(boardZombies.getBoardScore());

		 //place one zombie on each of the 81 spaces.
		 for (ZombieCrushSagaZombie zombie : stackZombies)
		 {

			 row = zombieCounter / 9;
			 column = zombieCounter % 9;

			 zombie.setX(BOARD_FIRST_SPACE_X + (BOARD_SPACE_SIZE * column));
			 zombie.setY(BOARD_FIRST_SPACE_Y + (BOARD_SPACE_SIZE * row));

			 zombie.setGridCell(row, column);

			 //if the board is active, place a zombie.
			 if(boardSpace.getBoardSpace(row, column)){
				 zombie.randomColor();
				 boardZombies.putZombie(row, column, zombie);   
			 }
			 else{
				 boardZombies.putZombie(row, column, null); 
				 zombie.setState(INVISIBLE_STATE);
			 }

			 //row, column

			 zombieCounter++;

		 }

		 // AND START ALL UPDATES
		 beginGame();

		 // CLEAR ANY WIN OR LOSS DISPLAY
		 miniGame.getGUIDialogs().get(WIN_DIALOG_TYPE).setState(INVISIBLE_STATE);
		 miniGame.getGUIDialogs().get(LOSS_DIALOG_TYPE).setState(INVISIBLE_STATE);
		 miniGame.getGUIDialogs().get(STATS_DIALOG_TYPE).setState(INVISIBLE_STATE);

		 miniGame.getGUIButtons().get(GAMEPLAY_REPLAY_BUTTON_TYPE).setState(INVISIBLE_STATE);
		 miniGame.getGUIButtons().get(GAMEPLAY_REPLAY_BUTTON_TYPE).setEnabled(false);
	 }

	 /*
	  *
	  * a special update for the keyhandler when you cheat
	  */

	 public void updateForce(){
		 ZombieCrushSagaZombie theZombie;

		 for(int row = 0; row < BOARD_ROWS; row++){
			 for(int column = 0; column < BOARD_COLUMNS; column++){
				 theZombie = boardZombies.getZombie(row, column);

				 if(theZombie == null)
					 continue;

				 theZombie.setTarget(theZombie.getX(), theZombie.getY());
				 theZombie.startMovingToTarget(MAX_ZOMBIE_VELOCITY);

				 movingZombies.add(theZombie);
			 }
		 }
	 }


	 public boolean[][] convertIntArrayToBooleanArray(int[][] theIntArray){

		 boolean[][] theBooleanArray = new boolean[BOARD_ROWS][BOARD_COLUMNS];
		 for(int row = 0; row < BOARD_ROWS; row++){
			 for(int column = 0; column < BOARD_COLUMNS; column++){
				 if(theIntArray[row][column] == 0)
					 theBooleanArray[row][column] = false;
				 else
					 theBooleanArray[row][column] = true;
			 }
		 }

		 return theBooleanArray;
	 }

	 public void updateBoardJelly(boolean[][] theBoard){
		 for(int row = 0; row < BOARD_ROWS; row++){
			 for(int column = 0; column < BOARD_COLUMNS; column++){
				 //if the jelly exists and a match was made in that spot.
				 if(boardJelly.getBoardJelly(row, column) && theBoard[row][column])
					 boardJelly.putBoardJelly(row, column, false);
			 }
		 }

	 }

	 /**
	  * Called each frame, this method updates all the game objects.
	  *
	  * @param game The ZombieCrushSaga game to be updated.
	  */
	 @Override
	 public void updateAll(MiniGame game)
	 {
		 // MAKE SURE THIS THREAD HAS EXCLUSIVE ACCESS TO THE DATA
		 try
		 {
			 game.beginUsingData();

			 // WE ONLY NEED TO UPDATE AND MOVE THE MOVING zombies
			 for (int i = 0; i < movingZombies.size(); i++)
			 {
				 // GET THE NEXT zombie
				 ZombieCrushSagaZombie zombie = movingZombies.get(i);

				 // THIS WILL UPDATE IT'S POSITION USING ITS VELOCITY
				 zombie.update(game);

				 // IF IT'S REACHED ITS DESTINATION, REMOVE IT
				 // FROM THE LIST OF MOVING zombies
				 if (!zombie.isMovingToTarget())
				 {
					 movingZombies.remove(zombie);
				 }
			 }

			 //if all moving as stopped, we can check the board for more matches
			 if(movingZombies.isEmpty()){
				 //stop rendering score when stuff stops moving.
				 renderScore = false;

				 //this swaps back and reverses the bad move
				 if(badMove == true){
					 badZombie1.setTarget(badZombie2.getX(), badZombie2.getY());
					 badZombie1.startMovingToTarget(MAX_ZOMBIE_VELOCITY);
					 badZombie2.setTarget(badZombie1.getX(), badZombie1.getY());
					 badZombie2.startMovingToTarget(MAX_ZOMBIE_VELOCITY);

					 boardZombies.swapTwo(badZombie1, badZombie2);

					 movingZombies.add(badZombie1);
					 movingZombies.add(badZombie2);
					 badMove = false;
				 }

				 //check win/loss conditions
				 //if there are no more matches and no special actions, game ends
				 if(!boardZombies.hasMatches() && !boardZombies.fiveBombSwapped() && !boardZombies.getSpecialActionExecuted()){
					 //no more moves, and it's a swap level, you win.
					 if(moves == 0) {
						 renderScore = false;
						 if(levelType == 0){
							 //a star has been reached, you win.
							 if(score >= starGoal[0])
								 endGameAsWin();
							 else    //no stars reached
								 endGameAsLoss();
						 }
						 else{   //levelType is jelly
							 //if jelly count is 0 and you've reached a star, you win. else, you lose
							 if(boardJelly.countJelly() == 0 && score >= starGoal[0])
								 endGameAsWin();
							 else
								 endGameAsLoss();
						 }
					 }
					 //if there are no jellies, and you reach a star, and it's a jelly level, you win
					 if(boardJelly.countJelly() == 0 && score >= starGoal[0] && levelType == 1){
						 endGameAsWin();
					 }
				 }

				 ZombieCrushSagaZombie tempZombie;

				 //if stuff needs to be cleared or be dropped,
				 //or five bomb has been swapped.
				 //we need to process the board.
				 //or zombie smasher was activated
				 if(boardZombies.hasMatches() || boardZombies.fiveBombSwapped() || boardZombies.getSpecialActionExecuted()){

					 //mark zombies for deletion
					 boardZombies.checkBoardMatches();

					 score += boardZombies.getCurrentBoardScore();

					 System.out.println("---------------------------");
					 System.out.println("update boardMatches:\n" + boardZombies.toStringBoardMatches());
					 System.out.println("update boardSpecial:\n" + boardZombies.toStringBoardSpecialZombies());

					 //delete marked zombies for deletion.
					 boardZombies.processMatches();

					 renderScore = true;

					 int[][] updatedBoardJelly = boardZombies.getLastBoardMatches();                    
					 this.updateBoardJelly(convertIntArrayToBooleanArray(updatedBoardJelly));

					 //move every zombie down where they're supposed to be.
					 for(int row = 0; row < BOARD_ROWS; row++){
						 for(int column = 0; column < BOARD_COLUMNS; column++){
							 tempZombie = boardZombies.getZombie(row, column);

							 //the processed board will be completely processed
							 //in it, the zombies will be in their correct future grid coords
							 //the x and y coords are not updated
							 //if you have a zombie, move it down from its x-y coords to its grid coords
							 if(tempZombie != null){
								 tempZombie.setTarget(calculateZombieXInGrid(tempZombie.getGridColumn()), calculateZombieYInGrid(tempZombie.getGridRow()));
								 tempZombie.startMovingToTarget(MAX_ZOMBIE_VELOCITY);
								 if(!movingZombies.contains(tempZombie))     //unnecessary?
										 movingZombies.add(tempZombie);
							 }
						 }
					 }
				 }
			 }

			 // IF THE GAME IS STILL ON, THE TIMER SHOULD CONTINUE
			 if (inProgress())
			 {
				 // KEEP THE GAME TIMER GOING IF THE GAME STILL IS
				 endTime = new GregorianCalendar();  //implemented in future levels.
			 }
		 }
		 finally
		 {
			 // MAKE SURE WE RELEASE THE LOCK WHETHER THERE IS
			 // AN EXCEPTION THROWN OR NOT
			 game.endUsingData();
		 }
	 }

	 /**
	  * This method is for updating any debug text to present to
	  * the screen. In a graphical application like this it's sometimes
	  * useful to display data in the GUI.
	  *
	  * @param game The ZombieCrushSaga game about which to display info.
	  */
	 @Override
	 public void updateDebugText(MiniGame game)
	 {
	 }
}