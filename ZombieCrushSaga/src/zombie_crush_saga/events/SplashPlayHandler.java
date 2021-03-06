package zombie_crush_saga.events;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import zombie_crush_saga.ui.ZombieCrushSagaMiniGame;
import static zombie_crush_saga.ZombieCrushSagaConstants.*;

/**
 * This event handler responds to when the user requests to
 * start a new game. 
 * 
 * @author Richard McKenna
 * @version 1.0
 */
public class SplashPlayHandler implements ActionListener
{
    // HERE'S THE GAME WE'LL UPDATE
    private ZombieCrushSagaMiniGame miniGame;
    
    /**
     * This constructor just stores the game for later.
     * 
     * @param initGame the game to update
     */
    public SplashPlayHandler(ZombieCrushSagaMiniGame initGame)
    {
        miniGame = initGame;
    }
    
    /**
     * Here is the event response. This code is executed when
     * the user clicks on the button for starting a new game,
     * which can be done when the application starts up, during
     * a game, or after a game has been played. Note that the game 
     * data is already locked for this thread before it is called, 
     * and that it will be unlocked after it returns.
     * 
     * @param ae the event object for the button press
     */
    @Override
    public void actionPerformed(ActionEvent ae)
    {
  
        if (miniGame.isCurrentScreenState(SPLASH_SCREEN_STATE))
        {
            // GO TO THE GAME
            miniGame.switchToSagaScreen();
            
        }
        
       
    }
}