/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dujuga.peruddos_v500;

import java.net.Socket;

/**
 *
 * @author Alexis
 */
public class PdosLinkedGame extends PdosGame {
    String mIp;
    
    public String getIp(){
        return mIp;
    }
    
    public PdosLinkedGame(PdosPlayer creator, int index) {
        super(creator, index);
        mIp = creator.getIp();
    }
    
    @Override
    public void run(){
        
    }
    
}
