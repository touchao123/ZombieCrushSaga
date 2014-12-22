package zombie_crush_saga.events;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import zombie_crush_saga.data.ZombieCrushSagaDataModel;
import zombie_crush_saga.file.ZombieCrushSagaFileManager;
import zombie_crush_saga.ui.ZombieCrushSagaMiniGame;
import static zombie_crush_saga.ZombieCrushSagaConstants.*;

/**
 * This event handler responds to when the user selects
 * a ZombieCrushSaga level to play on the splash screen.
 * 
 * @author Richard McKenna
 */
public class GameplayQuitHandler implements ActionListener
{
    // HERE'S THE GAME WE'LL UPDATE
    private ZombieCrushSagaMiniGame miniGame;
    
    /**
     * This constructor just stores the game and the level to
     * load for later.
     *     
     * @param initGame The game to update.
     * 
     * @param initLevelFile The level to load when the user requests it. 
     */
    public GameplayQuitHandler(  ZombieCrushSagaMiniGame initGame)
    {
        miniGame = initGame;
    }
    
    /**
     * Here is the event response. This code is executed when
     * the user clicks on a button for selecting a level
     * which is how the user starts a game. Note that the game 
     * data is already locked for this thread before it is called, 
     * and that it will be unlocked after it returns.
     *
     * @param ae the event object for the button press
     */
    @Override
    public void actionPerformed(ActionEvent ae)
    {
        // WE ONLY LET THIS HAPPEN IF THE SPLASH SCREEN IS VISIBLE
        
        if (miniGame.isCurrentScreenState(GAMEPLAY_SCREEN_STATE) )
        {
            
            // GET THE GAME'S DATA MODEL, WHICH IS ALREADY LOCKED FOR US
            ZombieCrushSagaDataModel data = (ZombieCrushSagaDataModel)miniGame.getDataModel();
            
            //if stuff is moving, it doesn't work
            if(!data.hasMovingZombies()){
                // UPDATE THE DATA
                ZombieCrushSagaFileManager fileManager = miniGame.getFileManager();
                
                fileManager.loadLevel(data.getCurrentLevel());
                
                data.deselectSelectedZombie();
                data.setZombieCrusherActive(false);
                
                // GO TO THE GAME
                miniGame.switchToLevelScoreScreen();
            }
        }
    }
}