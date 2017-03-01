/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dujuga.peruddos_v500;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Dujuga
 * In this file, will be implements the main Server of the game PERUDDOS.
 */
public class PdosServer {  
    String errMessage;
    private boolean lockOnClient = false;
    private boolean lockOnRoom = false;
    private ArrayList <PdosPlayer> myClients = new ArrayList();
    private ArrayList <PdosGame> myRooms = new ArrayList();
    private ArrayList <Boolean> isHosted = new ArrayList();
    private int numberOfClient = 0; /* Variable qui sert à compter le nombre de client courant. */
    private final int serverPort = 18000;
    
    /**
     * Try to add a client. Can fail if :
     *      - there is no lock on the client list.
     *      - the pseudonym of the client is already taken.
     * In the case of a fail, this method return -1.
     * Otherwise, return the id of the player.
     * @param newP  : PdosPlayer to add.
     * @return      : Return the id of the player if there is a success, -1 in case of an error. 
     */
    public int addClient(PdosPlayer newP){      
        
        int sauv = myClients.size();        /* int keep the initial number of players */
        int returned = myClients.size();    /* for know if the registration works */
        
        if(lockOnClient == false)           /* if there's no lock on the table, return error */
            return -1;
        
        /* Verify no one has already this pseudonym. */
        for(int i = 0; i < myClients.size(); i++){
            if(newP.getPseudonym().compareTo(myClients.get(i).getPseudonym()) == 0){
                lockOnClient = false;
                return -1;
            }
        }
        
        myClients.add(newP);                /* if there is a lock, add the client to the table */
        returned = myClients.size(); 
        lockOnClient = false;               /* remove the lock */
        
        if(returned > sauv)                 /* if the size has increase, return the id in the table as the id */
            return returned;
        else
            return -1;                      /* if not, return a error */
    }
    
    /**
     * Try to add a game. Can fail if there is no lock on the room list.
     * In the case of a fail, this method return -1.
     * Otherwise, return the id of the game.
     * @param creator   : PdosPlayer who create the game.
     * @return          : Return the id of the game if there is a success, -1 in case of an error.
     */
    public int addRoom(PdosPlayer creator){
        int sauv = myRooms.size();        /* int keep the initial number of players */
        int returned = myRooms.size();    /* for know if the registration works */
        
        if(lockOnRoom == false)           /* if there's no lock on the table, return error */
            return -1;
        
        /* Create Room */
        myRooms.add(new PdosHostedGame(creator, returned, this));
        creator.setInGame(true);
        returned = myRooms.size(); 
        lockOnRoom = false;               /* remove the lock */
        isHosted.add(true);
        System.out.println(sauv + " " + returned);
        
        if(returned > sauv) {                /* return the id */
            myRooms.get(sauv).setIdGame(sauv);
            return returned-1;
        }
        else
            return -1;                      /* if not, return a error */
    }
    
    /**
     * Ask a lock on the client list. If there is already a lock, return false.
     * return true otherwise and put the lock at true.
     * @return  : if the lock of the client sucess.
     */
    public boolean askForClient(){
        if(lockOnClient){            
            return false;
        }
        else{
            /* if not, lock */
            lockOnClient = true;
            return true;
        }      
    }
    
    /**
     * Ask a lock on the room list. If there is already a lock, return false.
     * return true otherwise and put the lock at true.
     * @return  : if the lock of the room sucess.
     */
    public boolean askForRoom(){
        if(lockOnRoom){            
            return false;
        }
        else{
            /* if not, lock */
            lockOnRoom = true;
            return true;
        }      
    }
     
    /**
     * Announce on the System.out the address of the server and his port.
     * @throws UnknownHostException 
     */
    private void declarationAtLaunch()throws UnknownHostException {
       String adresseipServeur  = InetAddress.getLocalHost().getHostAddress(); 
       System.out.println("Mon adresse est " + adresseipServeur + ":" + serverPort );
    } 
    
    /**
     * Suppress the room placed at the index in ArrayList PdosGame. 
     * Can fail if it can't obtain a lock on the room list.
     * @param index     : the id of the game to remove.
     * @return          : -1 if it fails.
     *                  : 0 if it success.
     */
    public int delRoom(int index){
        if(lockOnRoom == false)
            return -1;
        
        myRooms.remove(index);
        isHosted.remove(index);
        
        for(int i = 0; i < myRooms.size(); i++){
            myRooms.get(i).setIdGame(myRooms.get(i).getIndex() - 1);
        }
        
        lockOnRoom = false;
        return 0;
    }
    
    /**
     * Verify if there's already an instance of the application.
     * @return  : true if there is.
     *          : false otherwise.
     */
    private boolean existingServer(){
        Socket sock = null;
        try{
            sock = new Socket("127.0.0.1", 18000);
            return true;
        }
        catch(IOException ioe){
            return false;
        }
    }
    
    /**
     * @return the number of clients.
     */
    public int getNumberOfClient(){
        return myClients.size();
    }
    
    /**
     * @return the number of rooms.
     */
    public int getNumberOfRoom(){
        return myRooms.size();
    }
    
    /**
     * @return the ArrayList of PdosGames.
     */
    public ArrayList<PdosGame> getRooms(){
        return myRooms;
    }
    
    public Boolean isGameHosted(int index){
        if(isHosted.get(index) == true)
            return true;
        return false;
    }
    
    /**
     * Invokes the method askToJoin of the game placed with the index in the ArrayList PdosGame
     * for the PdosPlayer newP.
     * @param newP      PdosPlayer to add
     * @param index     Id of the party to join.
     * @return          the result of the method askToJoin of the party.
     */
    public String joinGame(PdosPlayer newP, int index){
        if(isHosted.get(index))
            return "" + myRooms.get(index).askToJoin(newP);
        
        else{
            System.out.println("LOG : " + myRooms.get(index).getIp());
            return myRooms.get(index).getIp();
            
        }
    }
    
    /**
     * Launch the index th games in a new thread.
     * @param index 
     */
    public void launchGame(int index){
        myRooms.get(index).start();
    }
    
    /**
     * Main method
     * @param args 
     */
    public static void main(String[] args) {
        PdosServer mainServ = new PdosServer(); /* instance de la classe principale */
        if(mainServ.existingServer()){ /* Test si serveur deja existant*/
            System.out.println("Serveur deja existant.");
            System.exit(0);
        }
        System.out.println("Création du serveur.");
        try {
            mainServ.declarationAtLaunch();
            mainServ.socketHandler();
        } catch (UnknownHostException ex) {
            Logger.getLogger(PdosServer.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public void removeLinkedRoom(PdosPlayer creator){
        for(int i = 0; i < myRooms.size(); i++){
            if(creator.getPseudonym().compareTo(myRooms.get(i).mListPlayer.get(0).getPseudonym()) == 0){
                myRooms.remove(i);
            }
        }
    }
    
    public int referRoom(PdosPlayer creator){
        int sauv = myRooms.size();        /* int keep the initial number of players */
        int returned = myRooms.size();    /* for know if the registration works */
        
        if(lockOnRoom == false)           /* if there's no lock on the table, return error */
            return -1;
        
        /* Create Room */
        myRooms.add(new PdosLinkedGame(creator, returned));
        creator.setInGame(true);
        returned = myRooms.size(); 
        lockOnRoom = false;               /* remove the lock */
        isHosted.add(false);
        System.out.println(sauv + " " + returned);
        
        if(returned > sauv) {                /* return the id */
            myRooms.get(sauv).setIdGame(sauv);
            return returned-1;
        }
        else
            return -1;                      /* if not, return a error */
    }
    
    /**
     * Show on the System.out, all players registered.
     */
    public void showClients(){
        for(int i = 0; i < myClients.size(); i++){
            System.out.println(i + " : " +myClients.get(i).getPseudonym());
        }
    }    
    
    /**
     * Listen for new connection.
     */
    private void socketHandler(){
        ServerSocket sockEcoute = null;    //Déclaration du serverSocket.
        Socket sockService = null;         //Déclaration du socket de service.
        boolean getClient = true;   //Permet de stopper l'écoute de nouveaux clients.
        /* Rappel des étapes d'une connexion : */
            /* Création sock écoute + bind */
            try{
                sockEcoute = new ServerSocket(serverPort);

                while(getClient){
                    try{
                        sockService = sockEcoute.accept();
                    }
                    catch(IOException ioe){
                        System.out.println("Erreur de création du socket service : " + ioe.getMessage());
                    }

                    /* CREER UN eTHREAD POUR LA tESTION DU CLIENT */
                    PdosPlayer player = new PdosPlayer(sockService, numberOfClient, this);
                    player.start();
                }
            }
            catch(IOException ioe){
                System.out.println("Erreur de création du server socket : " + ioe.getMessage());
                /* DEVRAIT-ON GERER DES LOGS ? */
            }
            
    }
}
