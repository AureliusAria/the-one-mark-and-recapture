/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.mark_and_recapture;

import core.DTNHost;
import core.Settings;
import java.util.List;

/**
 *
 * @author acer
 */
public class Observer {
    public static final String MARK_PREFIX = "markPrefix";
    public static final String  DEFAULT_MARK_PREFIX = "markMessage";
    private static Observer instance;
    
    private String markPrefix = "markMessage";
    protected List<DTNHost> observer;

//    public Observer(Settings s) {
//        if (s.contains(MARK_PREFIX)){
//            this.markPrefix = s.getSetting(MARK_PREFIX);
//        } else {
//            this.markPrefix = DEFAULT_MARK_PREFIX;
//        }
//        
//    }

    public Observer() {
    }

    public String getMarkPrefix() {
        return this.markPrefix;
    }

    public static Observer getInstance() {
        if (instance == null){
            return instance = new Observer();
        }
        return instance;
    }
    
    
    
}
