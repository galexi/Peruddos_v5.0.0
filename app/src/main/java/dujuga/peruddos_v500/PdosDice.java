/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dujuga.peruddos_v500;

/**
 *
 * @author Dujuga
 */
public class PdosDice {
    int mValeur;
    
    /*
        Return the value of the dice.
    VERIFIED : OK
    */
    public int getValue(){
        return mValeur;
    }
    
    /*
        Gives to mValeur a random value in 1..6 range.
    VERIFIED : OK
    */
    public void toss(){
        mValeur = (int) (1 + (Math.random() * (7 - 1)));
    }
    
    /*
        Public constructor.
    */
    public PdosDice(){
        mValeur = 1;
    }
}
