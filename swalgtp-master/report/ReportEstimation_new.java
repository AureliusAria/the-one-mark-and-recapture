/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimClock;
import core.SimScenario;
import core.UpdateListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.mark_and_recapture.ObserverNode;

/**
 *
 * @author aurelius_aria
 */
public class ReportEstimation_new extends Report {
    

    public ReportEstimation_new() {
        super.init();
    }
    
    
   
    
    @Override
    public void done(){
        List<DTNHost> observer = SimScenario.getInstance().getObserver();
              for(DTNHost obs : observer){
                  String obser ;
                  String estimasiPerObs;
                  MessageRouter mr = obs.getRouter();
                  RoutingDecisionEngine de = ((DecisionEngineRouter)mr).getDecisionEngine();
                  ObserverNode ob = (ObserverNode) de;
                  for (Map.Entry<DTNHost,Map<Double,Integer>> entry : ob.getEstimasi().entrySet()) {
                      obser = ""+ entry.getKey();
                      Map<Double,Integer> innerMap = entry.getValue();
                      write(obser);
                      for(Map.Entry<Double,Integer> innerEntry : innerMap.entrySet()){
                          estimasiPerObs = BigDecimal.valueOf(innerEntry.getKey()) + " : " + innerEntry.getValue();
                          write(estimasiPerObs);
                      }
                  }
              }
        
       
       
       super.done();
    }

    
    
}
