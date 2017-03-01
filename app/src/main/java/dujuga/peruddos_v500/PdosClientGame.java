/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dujuga.peruddos_v500;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexis
 */
public class PdosClientGame extends PdosGame {
    
    ArrayList <PdosPlayer> mListPlayer = new ArrayList();
    PdosClient mDaddy;
    protected int mNumber = 1;
    protected int mValor = 1;
    
    public PdosClientGame(PdosClient daddy, PdosPlayer creator, int index) {
        super(creator, index);
        mListPlayer.add(creator);
        mDaddy = daddy;
    }
    
    /**
     * Send the message given in parameters to the PdosPlayer with the numberPlayer index in the ArrayList of PdosPlayer.
     * @param numberPlayer  : index, in int, of the player.
     * @param message       : message, in String, to send.
     */
    @Override
    protected void sendTo(int numberPlayer, String message) {
        if(numberPlayer >= 0 && numberPlayer < mListPlayer.size()){
            try{
                mListPlayer.get(numberPlayer).send(message);
            } catch (IOException ex) {
                ejectPlayer(numberPlayer);
            }
        }        
    }
    
    /**
     * For the index player, ask to client for outbid the current load.
     * @param index
     * @return if the player has disconnect during the method
     */
    @Override
    protected boolean outbid(int index){
        int nbDice = 0;
        int valorDice = 0;
        boolean loseConnection = false;
        
        do{
            do{
                try {
                    nbDice = mListPlayer.get(index).listenInt("Enchère - Veuillez donner un nombre de dés : ");
                } catch (IOException ex) {
                    System.out.print("Il y avait " + mListPlayer.size());
                    loseConnection = true;
                }
                if(nbDice < mNumber && !loseConnection)
                        sendTo(index, "Veuillez donner une valeur supérieure à " + mNumber + ".");
            } while(nbDice < mNumber && !loseConnection);

            do{
                try {
                    valorDice = mListPlayer.get(index).listenInt("Enchère - Veuillez donner une valeur de dés : ");
                } catch (IOException ex) {
                    System.out.print("Il y avait " + mListPlayer.size());
                    loseConnection = true;
                }
                if((valorDice < 1 || valorDice > 6) && !loseConnection)
                    sendTo(index, "Veuillez donner une valeur comprise dans [1,6].");
            } while((valorDice < 1 || valorDice > 6) && !loseConnection);
            
            if(!(nbDice > mNumber || valorDice > mValor) && !loseConnection)
                sendTo(index, "Veuillez entrer une surchère valide.");
        } while(!(nbDice > mNumber || valorDice > mValor) && !loseConnection);
        
        if(!loseConnection){
            mNumber = nbDice;
            mValor = valorDice;
            broadcast(mListPlayer.get(index).getPseudonym() + " a parié : " + mNumber + " " + mValor);
        }
        
        return loseConnection;
    }
    
    /**
     * Verify if the index player has lost his connection during the party
     * @param index
     * @param firstP    specify if the player plays first.
     */
    @Override
    protected void loseConnectionDuringGame(int index, boolean firstP){
        int nextPlayer = index;
        
        this.ejectPlayer(index);
        
        if(nextPlayer == mListPlayer.size())
            nextPlayer = 0;
        
        if(firstP)
            this.firstProposition(nextPlayer);
        else
            this.proposition(nextPlayer);
        
    }
    
    /**
     * Broadcast the message given in parameters to all connected players.
     * @param message 
     */
    @Override
    protected void broadcast(String message){
        for(int i = 0; i < mListPlayer.size(); i++){
            sendTo(i, message);
        }
    }
    
    /**
     * Broadcast to every players in the party that the creator won then ejects every players. 
     */
    @Override
    protected void endGame(){
        broadcast("Un gros GG à " + mListPlayer.get(0).getPseudonym() + " : victoire écrasante !");
        
        for(int i = mListPlayer.size() - 1; i >= 0; i--)
            ejectPlayer(i);
    }
    
    /**
     * Handle the proposition and the turn of the first player.
     * @param index     : index of the current player.
     */
    @Override
    protected void firstProposition(int index){
        int sommDice = 0;
        String resp = null;        
        mNumber = 0;
        mValor = 0;
        int nextPlayer;
        boolean loseConnection = false;
                
        everybodyToss(); /* Mélange de tous les dés */
        
        System.out.println("Index : " + index);
        
        /* Vérification si le joueur a toujours des dés */
        if(didThePlayerLose(index) && index >= mListPlayer.size()){
            index = 0;
        }
         
        /* Verify there is only one player left */
        if(mListPlayer.size() != 1){
            broadcast(" - Tour de " + mListPlayer.get(index).getPseudonym() + " - "); /* annonce générale */
            /* Envoie du nombre de dés restant */
            for(int i = 0; i < mListPlayer.size(); i++)
                sommDice += mListPlayer.get(i).getNumberOfDices();

            sendTo(index, "Il reste : " + sommDice + " dés.");
            
            loseConnection = outbid(index);

            if(!loseConnection){
                nextPlayer = index + 1;

                if(index == mListPlayer.size()-1)
                    nextPlayer = 0;

                proposition(nextPlayer);
            }
            else
                loseConnectionDuringGame(index, true);
        }
    }
    
    /**
     * Verify of the index-1 player is a liar or not.
     * @param index 
     */
    @Override
    protected void liar(int index){
        int jCurrent = index;
        int jPrec = index - 1;
        String pseudonymPrec = "";
        int cptDice = 0;
        
        if(jCurrent == 0)
            jPrec = mListPlayer.size() - 1;
        
        pseudonymPrec = mListPlayer.get(jPrec).getPseudonym();
        
        broadcast(mListPlayer.get(jCurrent).getPseudonym() + " a dénoncé " + mListPlayer.get(jPrec).getPseudonym() + ".");
        
        for(int i = 0; i < mListPlayer.size(); i++){
            cptDice += mListPlayer.get(i).getThatDice(mValor);
        }
        
        broadcast("Il y a " + cptDice + " dés de valeur " + mValor + ".");
        
        //Vérification de la correspondance Pseudo - Id.
        if(mListPlayer.get(jPrec).getPseudonym().compareTo(pseudonymPrec) !=0 ){
            broadcast(pseudonymPrec + " s'est déconnecté, par peur du scandale.");
            firstProposition(jPrec);
        }
        else{
            if(cptDice >= mNumber){
                broadcast(mListPlayer.get(jPrec).getPseudonym() + " n'a pas menti !");
                broadcast(mListPlayer.get(jCurrent).getPseudonym() + " perd un dé !");
                mListPlayer.get(jCurrent).loseDice();
                firstProposition(jCurrent);
            }
            else{
                broadcast(mListPlayer.get(jPrec).getPseudonym() + " a bel et bien menti !");
                broadcast(mListPlayer.get(jPrec).getPseudonym() + " perd un dé !");
                mListPlayer.get(jPrec).loseDice();
                firstProposition(jPrec);
            }
        }
    }
    
    /**
     * Verify if the index player has succeed a "Tout pile".
     * @param index
     */
    @Override
    protected void exactly(int index){
        int cptDice = 0;
        int sizeDep = mListPlayer.size();
        String pseudonym = mListPlayer.get(index).getPseudonym();
        int intDep = index;
        
        broadcast(mListPlayer.get(index).getPseudonym() + " tente un tout pile !");
        
        for(int i = 0; i < mListPlayer.size(); i++){
            cptDice += mListPlayer.get(i).getThatDice(mValor);
        }
        
        broadcast("Il y a " + cptDice + " dés de valeur " + mValor + ".");
        
        if(sizeDep != mListPlayer.size()){
            intDep = -1;
            for(int i = 0; i < mListPlayer.size(); i++){
                if(mListPlayer.get(i).getPseudonym().compareTo(pseudonym) == 0){
                    intDep = i;
                }
            }
        }
        
        if(intDep != -1){
            if(cptDice == mNumber){
                broadcast(mListPlayer.get(intDep).getPseudonym() + " a réussi son tout pile ! Tu gagnes un dé !");
                mListPlayer.get(intDep).addDice();
            }
            else{
                broadcast("Bien essayé " + mListPlayer.get(intDep).getPseudonym() + ", mais c'est raté ! Tu perds un dé.");
                mListPlayer.get(intDep).loseDice();
            }  
        }
        else
            broadcast(pseudonym + " s'est échappé avant les résultats !");
    }
    
    /**
     * Handle the proposition and the turn of the current player.
     * @param index     : index of the current player.
     */
    @Override
    protected void proposition(int index){
        int sommDice = 0;
        int resp = 0;
        int valorDice = 0;
        int nbDice = 0;
        int nextPlayer;
        int precPlayer;
        boolean loseConnection = false;
        
        /* Vérification si le joueur a toujours des dés */        
        if(didThePlayerLose(index) && index == mListPlayer.size()){
            index = 0;
        }
        
        if(didThePlayerLose(index) && index == 0 && mListPlayer.size() > 1){
            index++;
        }
        
        broadcast(" - Tour de " + mListPlayer.get(index).getPseudonym() + " - "); /* annonce générale */

        /* Verify there is only one player left */
        if(mListPlayer.size() != 1){
            /* Envoie du nombre de dés restant */
            for(int i = 0; i < mListPlayer.size(); i++)
                sommDice += mListPlayer.get(i).getNumberOfDices();

            this.sendTo(index, "Il reste : " + sommDice + " dés.");

            precPlayer = index - 1;
            nextPlayer = index + 1;

            if(index == mListPlayer.size()-1)
                nextPlayer = 0;
            if(index == 0)
                precPlayer =  mListPlayer.size()-1;

            do{
                sendTo(index, "Que voulez-vous faire ? ( 1: Surenchérir/ 2: Menteur/ 3: Tout pile).");

                try {
                    resp = mListPlayer.get(index).listenInt();
                } catch (IOException ex) {
                    //TBC
                    System.out.println("DECONNEXION !");
                    loseConnection = true; /* il faut sortir de cette méthode */
                }            

                switch(resp){
                    case 1: /* Surenchère */
                        this.outbid(index);
                        this.proposition(nextPlayer);
                        break;
                    case 2: /* Menteur */
                        this.liar(index);
                        break;
                    case 3: /* Tout pile */
                        this.exactly(index);
                        this.firstProposition(index);
                        break;
                    default: /* rien */
                        System.out.println("On passe dans le default.");
                        break;
                }

            } while(resp != 1 && resp != 2 && resp != 3 && loseConnection == false && !didAPlayerWin());

            if(loseConnection){
                loseConnectionDuringGame(index, false);
            }
        }
    }
    
     /** 
     * Alert the player with the i index in the list that he is kicked.
     * Then kick him.
     * @param i : index of the PdosPlayer in the mListPlayer.
     */
    @Override
    protected void ejectPlayer(int i){
        //String msg = mListPlayer.get(i).getPseudonym();
        mListPlayer.get(i).setInGame(false);
        mListPlayer.get(i).setGame(null);
        mListPlayer.remove(i);
    }
    
    /**
     * Toss all dices in the game.
     */
    @Override
    protected void everybodyToss(){
        for(int i = 0; i < mListPlayer.size(); i++){
            mListPlayer.get(i).tossDices();
            mListPlayer.get(i).showDices();
        }
    }
    
    /**
     * Verify if the player placed in index place has lose.
     * @param index     : index of the PdosPlayer.
     * @return          : if the player has lose.
     */
    @Override
    protected boolean didThePlayerLose(int index){
        boolean returned = false;
        if(index < mListPlayer.size()){
            if(index > 0)
                returned = (mListPlayer.get(index).hasDice() && mListPlayer.get(index).isAlive());
            else
                returned = mListPlayer.get(index).hasDice();
            
            if(!returned){
                broadcast(mListPlayer.get(index).getPseudonym() + " a perdu et est éliminé.");
                if(mListPlayer.get(index).isAlive()){
                    try {
                        mListPlayer.get(index).send("END");
                    } catch (IOException ex) {
                        Logger.getLogger(PdosClientGame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                this.ejectPlayer(index);
                return !returned;
            }
            
            return !returned;
        }
        return false;
    }
    
    @Override
    public void run(){
        int currentJ; /* variable contenant l'id du joueur en train de jouer */
        
        sendTo(0, "Vous avez créer une partie.");
        System.out.println("Il y a " + mListPlayer.size() + " joueurs.");

        this.waitingLoop();
        if(!mListPlayer.isEmpty()){
            broadcast("La partie est pleine !");
            try {
                mDaddy.send("ENDREC");
            } catch (IOException ex) {
                Logger.getLogger(PdosClientGame.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            //System.out.println("Premier tour");
            broadcast("La partie commence !");
            this.firstProposition(currentJ);
            
            broadcast("END");
            wakeUpAll();
            //broadcast("END");
            endGame();
        }
        mDaddy.notifyMe();
        System.out.println("Fin de partie.");
    }
    
    private void waitingLoop(){
        ServerSocket sockEcoute = null;    //Déclaration du serverSocket.
        Socket sockService = null;         //Déclaration du socket de service.
        boolean getClient = true;   //Permet de stopper l'écoute de nouveaux clients.
        /* Rappel des étapes d'une connexion : */
            /* Création sock écoute + bind */
            try{
                sockEcoute = new ServerSocket(18050);

                while(getClient){
                    try{
                        sockService = sockEcoute.accept();
                    }
                    catch(IOException ioe){
                        System.out.println("Erreur de création du socket service : " + ioe.getMessage());
                    }

                    mListPlayer.add(new PdosPlayer(sockService, mListPlayer.size(), this));
                    mListPlayer.get(mListPlayer.size()-1).start();
                    broadcast("Un joueur a rejoint la partie (" + mListPlayer.size() + "/6)");
                    
                    if(mListPlayer.size() >= 6){
                        getClient = false;
                    }
                    sleep(1000);
                }
            }
            catch(IOException ioe){
                System.out.println("Erreur de création du serveur socket : " + ioe.getMessage());
            } catch (InterruptedException ex) {
            Logger.getLogger(PdosClientGame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
