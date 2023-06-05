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
public class ReportEstimation extends Report implements UpdateListener{
    private static Map<DTNHost, Map<Double, Integer>> estimasi;
    private static List<Double> intervalTime;
    private double lastUpdate = 0;
    private double updateInterval= 7200;

    public ReportEstimation() {
        estimasi = new HashMap<DTNHost, Map<Double, Integer>>();
        intervalTime = new  ArrayList<Double>();
        lastUpdate = 0;
        updateInterval =3600;
    }
    
    
    @Override
    public void updated(List<DTNHost> hosts) {
          if(SimClock.getTime() - lastUpdate >= updateInterval){
              lastUpdate = SimClock.getTime();
              intervalTime.add(lastUpdate);
              
              List<DTNHost> observer = SimScenario.getInstance().getObserver();
              for(DTNHost obs : observer){
                  for(DTNHost host : hosts){
                      if(obs == host){
                          MessageRouter mr = obs.getRouter();
                          RoutingDecisionEngine de = ((DecisionEngineRouter)mr).getDecisionEngine();
                          ObserverNode ob = (ObserverNode) de;
                          Map<Double, Integer> innerMap = new HashMap<Double, Integer>();
                          ArrayList<Integer> listEs = new ArrayList<>();
                          
                          int getEstimasi = ob.getEstimation();
                          
                          innerMap.put(lastUpdate, getEstimasi);
                          if(!estimasi.containsKey(obs)){
                              estimasi.put(obs, innerMap);
                          } else {
                              estimasi.get(obs).put(lastUpdate, getEstimasi);
                          }
//                          if(!estimasi.containsKey(obs)){
//                              ArrayList listEstimasi = new ArrayList(); 
//                              estimasi.put(obs, new HashMap<Double, ArrayList<Integer>>());
//                              estimasi.get(obs).put(lastUpdate, listEstimasi);
//                          }else{
//                              int es;
//                              es = ob.getEstimation();
//                              Map<Double, ArrayList<Integer>> existingMap = estimasi.get(obs);
//                              if(!existingMap.containsKey(lastUpdate)){
//                                  ArrayList<Integer> existingList = existingMap.get(lastUpdate);
//                                  if(existingList != null){
//                                      existingList.add(es);
//                                  }
//                              }
//                          }
                          //done();
                          
                      }
                  }
//                  
              }
          }
    }
    
    @Override
    public void done(){
        String obs ;
        String interval;
        String estimasiPerObs;
        for(Map.Entry<DTNHost, Map<Double,Integer>> entry : estimasi.entrySet()){
            obs = "" + entry.getKey();
            Map<Double, Integer> innerMap = entry.getValue();
            
            write(obs);
            
            for(Map.Entry<Double, Integer> innerEntry : innerMap.entrySet()){
                estimasiPerObs = BigDecimal.valueOf(innerEntry.getKey()) + " : " + innerEntry.getValue();
                write(estimasiPerObs);
            }
        }
//        for(Map.Entry<DTNHost,ArrayList<Integer>> es : estimasi.entrySet()){
//            obs = "Obs : " + es.getKey();
//            write(obs);
//            for(Integer value : es.getValue() ){
//                estimasiPerObs = updateInterval + " : " + value + "\n";
//                write(estimasiPerObs);
//            }
//            
//        }
       
       super.done();
    }
    
}
