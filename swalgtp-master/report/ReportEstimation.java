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
    private static Map<DTNHost, ArrayList<Integer>> estimasi;
    private static List<Double> intervalTime;
    private double lastUpdate = 0;
    private double updateInterval= 3600;

    public ReportEstimation() {
        estimasi = new HashMap<DTNHost, ArrayList<Integer>>();
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
                          if(!estimasi.containsKey(obs)){
                              ArrayList listEstimasi = new ArrayList(); 
                              estimasi.put(obs, listEstimasi);
                          }
                          ArrayList<Integer> es = estimasi.get(obs);
                          es.add(ob.getEstimation());
                          
                      }
                  }
//                  
              }
          }
    }
    
    @Override
    public void done(){
//        String print ;
//        for(Map.Entry<DTNHost,ArrayList<Integer>> es : estimasi.entrySet()){
//            print = "Obs : " + es.getKey() + " : " + es.getValue() + "\n";
//            write(print);
//        }
        String observerNode ;
        String listEstimasi;
        //write("Daftar Estimasi");
        for(Map.Entry<DTNHost,ArrayList<Integer>> es : estimasi.entrySet()){
            observerNode = es.getKey().toString();
            //write(lastUpdate + "");
            //write(observerNode);
            //out.print(es.getKey().toString());
           for(Integer listEs : es.getValue()){
              // out.print(" : "+ listEs);
              listEstimasi = listEs.toString();
               write(lastUpdate + "" + " : "+ observerNode +": "+ listEstimasi + "\n");
           }
           
        }
    }
    
}
