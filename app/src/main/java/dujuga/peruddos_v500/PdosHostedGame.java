/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dujuga.peruddos_v500;

import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexis
 */
public class PdosHostedGame extends PdosGame{
    PdosServer mDaddy;
    
    
    public PdosHostedGame(PdosPlayer creator, int index, PdosServer daddy) {
        super(creator, index);
        mDaddy = daddy;
    }
    
    
    
    /**
     * Main method of the PdosGame.
     * Alert the PdosPlayer that he has start a game then waits for others players to join.
     * When the room is full, wakes up all Player and launch a game.
     * [TO REMOVE] Waits 10 seconds then designates the creator as the winner.
     * At least, alert the server that the game has ended and stop himself.
     */
    @Override
    public void run(){
        int currentJ; /* variable contenant l'id du joueur en train de jouer */
        int repServ = -1;
        
        sendTo(0, "Vous avez créer une partie.");
        
        waitingLoop();
        System.out.println("Mon id est : " + mIdPdosGame);
        if(!mListPlayer.isEmpty()){
            unregister();
            broadcast("La partie est pleine !");
            wakeUpAllForStart();
            try {
                sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PdosGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            for(int i = 0; i < mListPlayer.size(); i++){
                mListPlayer.get(i).initializePlayer();
            }
            
            /* variable contenant l'id du joueur courant */
            currentJ = (int) (Math.random() * mListPlayer.size());
            System.out.println("Premier tour");
            broadcast("La partie commence !");
            firstProposition(currentJ);
            wakeUpAll();
            endGame();
        }
        unregister();
        System.out.println("Fin de partie.");
    }
    
    /**
     * Remove the PdosGame in the ArrayList of the PdosGame of the server.
     */
    private void unregister(){
        int returned = -1;
        
        do{
            if(mDaddy.askForRoom())
                returned = mDaddy.delRoom(mIdPdosGame); 
            
            try{
                sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(PdosGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } while(returned == -1);
        System.out.println("La partie : " + mIdPdosGame + " a été retiré.");
    }
    
    /**
     * Loop where the PdosGame expected player to join. Check all seconds that connected player are connected.
     */
    protected void waitingLoop(){
        int actually = 1;
        
        sendTo(0, "Attente de l'arrivée de nouveaux joueurs.");
        
        do{
            /* Si nouveau joueur */
            if(mListPlayer.size() > actually){
                sendTo(mListPlayer.size()-1, "Bienvenue dans la partie de " + this.getCreatorPseudonym());
                broadcast("Le joueur " + mListPlayer.get(actually).getPseudonym() + " a rejoint la partie.");
                actually = mListPlayer.size();
            }
            
            /* Si le PdosPlayer créateur n'est pas encore en activité */
            for(int i = 0; i < mListPlayer.size(); i++){
                checkPlayer(i);
                actually = mListPlayer.size();
            }
            
            try {
                sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PdosGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while(mListPlayer.size() < 6 && !mListPlayer.isEmpty());
        
    }
    
}
