/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.mark_and_recapture;


import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Aurelius Aria Baras Panyapa
 * Kelas routing ini menggunakan decisionEngine Router dan algoritma mark and recapture
 */
public class SprayAndWaitRouterDE_MarkandRecapture implements RoutingDecisionEngine, ObserverNode {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String NROF_MARK = "nrofMark";
    public static final String BINARY_MODE = "binaryMode";
    public static final String RECAPTURE_INTERVAL = "recaptureInterval";
    public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouterDE_MnR";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."+ "copies";
    public static final String MSG_MARK_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";
    
    protected int initialNrofCopies;
    protected boolean isBinary;
    public int initialNrofMark;
    public static final int DEFAULT_INTERVAL = 3600;
    private double lastUpdate = Double.MAX_VALUE;
    protected boolean observerNode;
    //public final String markPrefix;
    private int estimation;
    private int interval;
    private int mark;
    private String mId;
    private Set<DTNHost> markedNode;
    private Set<DTNHost> recaptureNode;
    private Map<DTNHost, Map<Double,Integer>> estimasi;
    //private Map<DTNHost, ArrayList<DTNHost>> markMessage;
    private List<Message> listMarkMessage;

    public SprayAndWaitRouterDE_MarkandRecapture(Settings s) {
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            isBinary = false; // default value
        }

        if (s.contains(NROF_COPIES)) {
            initialNrofCopies = s.getInt(NROF_COPIES);
        }
         if (s.contains(NROF_MARK)){
            initialNrofMark = s.getInt(NROF_MARK);
        }
        
        if (s.contains(RECAPTURE_INTERVAL)){
            interval = s.getInt(RECAPTURE_INTERVAL);
        } else {
            interval = DEFAULT_INTERVAL;
        }
        
       // this.markPrefix = s.getSetting(MARK_PREFIX);
        this.observerNode = false;
        this.mark = 0;
        this.estimation = 0;
    }

    public SprayAndWaitRouterDE_MarkandRecapture(SprayAndWaitRouterDE_MarkandRecapture proto) {
        this.initialNrofCopies = proto.initialNrofCopies;
        this.initialNrofMark = proto.initialNrofMark;
        this.isBinary = proto.isBinary; //this.isBinary = proto.isBinary;
        this.observerNode = proto.observerNode;
        this.mark = proto.mark;
      //  this.markPrefix = cs.markPrefix;
        this.markedNode = new HashSet<DTNHost>();
        this.recaptureNode = new HashSet<DTNHost>();
        this.interval = proto.interval;
        this.estimation = proto.estimation;
        //this.markMessage = new HashMap<>();
        this.listMarkMessage = new ArrayList<>();
        this.estimasi = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        Collection<Message> messages = peer. getMessageCollection();
        String markPrefix = Observer.getInstance().getMarkPrefix();
        
        double currentTime = SimClock.getIntTime();
        if(peer.isRadioActive() == true && !isObserver(peer)){
            for(Message m : thisHost.getMessageCollection()){
            Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
            if(nrofMark == 1 && m.getPrefix().equals(markPrefix) ){
           
               for(Iterator<Message> iterator = messages.iterator(); iterator.hasNext();){
                   Message msg = iterator.next();
                   
//                    if(m.getTtl() >= 0 ){
                        if(thisHost.equals(msg.getFrom()) && !recaptureNode.contains(peer)&& m.getId().equals(msg.getId())){
                                  
                                 this.markedNode.add(peer);
                                 
                           }
                        if(!recaptureNode.contains(peer)){
                             recaptureNode.add(peer);
                        } 
//                    }
                }
               
            }
        }
        }
        
        this.lastUpdate = currentTime - currentTime % interval;     
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        DTNHost thisHost = null;
        
        List<DTNHost> listHop = m.getHops();
        Iterator it = listHop.iterator();
        
        if(it.hasNext()){
            thisHost = (DTNHost) it.next();
        }
        
        if (thisHost.isRadioActive() == false){
           
            return false;
        }
        
        
        if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())){
            m.addProperty(MSG_MARK_PROPERTY, initialNrofMark);
           //System.out.println("mark Message");
            
            return true;
        } else{
            m.addProperty(MSG_COUNT_PROPERTY, copy());
            return true;
        }
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
        
        if (isBinary) {
            if(m.getPrefix().equals(Observer.getInstance().getMarkPrefix())){
                nrofMark = (int) Math.ceil(nrofMark / 2.0);
            }
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())) {
                nrofMark = 1;
            }
            nrofCopies = 1;
        }
        m.updateProperty(MSG_MARK_PROPERTY, nrofMark);
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        //ystem.out.println(nrofCopies);
        if (nrofCopies > 1) {
            
            return true;
        }
        return false;
    
    }
    
    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
       
       if(isObserver(otherHost)){
           return false;
       }
       String cekHost = ""+ thisHost;
        String markPrefix = Observer.getInstance().getMarkPrefix();
        
        
        if (otherHost.isRadioActive() == true){
            Collection<Message> messages = otherHost. getMessageCollection();
            if(m.getPrefix().equals(markPrefix)){
                if(m.getTo() == otherHost){
                    return true;
                }
                
                Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
                
                for (Message msg : messages) {
                    if(!thisHost.equals(msg.getFrom()) && nrofMark > 1){
                        return true;
                    }
                }
                
//                for (Message msg : messages) {
//                    if(m.getTtl() >= 0 && cekHost.startsWith("Obs")){
//                        if(!recaptureNode.contains(otherHost)){
//                            recaptureNode.add(otherHost);
//                            if(thisHost.equals(msg.getFrom())){
//                                this.markedNode.add(otherHost);
//
//                            } 
//                        }
//                        
//                    }
//                }
            }
        }
        
        
            
        
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return false;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
        if (isBinary) {
            if(m.getPrefix().equals(Observer.getInstance().getMarkPrefix())){
                nrofMark /= 2;
            }
            nrofCopies /= 2;
        } else {
            if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())) {
                nrofMark--;
            }
            nrofCopies--;
        }
        m.updateProperty(MSG_MARK_PROPERTY, nrofMark);
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new SprayAndWaitRouterDE_MarkandRecapture(this);
    }

    private SprayAndWaitRouterDE_MarkandRecapture getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + "with other routers of same type";

        return (SprayAndWaitRouterDE_MarkandRecapture) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
     
        
        double currentTime = SimClock.getIntTime();
        Set<String> messageToDelete = new HashSet<>();
        String markPrefix = Observer.getInstance().getMarkPrefix();     
        Collection<Message> messagesCollection = host.getMessageCollection();
        
        for (Iterator<Message> iterator  = messagesCollection.iterator(); iterator.hasNext();) {
            Message msg = iterator.next();
            
            if(msg.getPrefix().equals(markPrefix)){
                if(msg.getFrom() == host){
                    int messageTtl = msg.getTtl();
                    if(messageTtl <= 0 && !markedNode.isEmpty()){
                        int tempEstimation = (initialNrofMark * this.recaptureNode.size()) / this.markedNode.size();
                        this.setEstimation(tempEstimation);
                         Map<Double,Integer> innerMap = new HashMap<>();
                        innerMap.put(currentTime, tempEstimation);
                        if (!estimasi.containsKey(host)) {
                            estimasi.put(host, innerMap);
                                }else{
                                    estimasi.get(host).put(currentTime, tempEstimation);
                                }
                        System.out.println("Interval " + SimClock.getIntTime());
                        System.out.println("Node " + host.getName());
                        System.out.println("Mark " + initialNrofMark);
                        System.out.println("Recapture " + this.recaptureNode.size());
                        System.out.println("m " + this.markedNode.size());
                        System.out.println("Estimasi " + this.getEstimation());
                        System.out.println("");
                       
                        messageToDelete.add(msg.getId());
                        msg.removeProperty(MSG_MARK_PROPERTY);
                        this.setEstimation(0);
                        this.markedNode.clear();
                        this.recaptureNode.clear();
                    }
                }else{
                    int messageTtl = msg.getTtl();
                    if(messageTtl <= 0){
                        messageToDelete.add(msg.getId());
                        msg.removeProperty(MSG_MARK_PROPERTY);
                    }
                }
            }
        }
        
        for(String messageId : messageToDelete){
            //System.out.println("");
            host.deleteMessage(messageId, true);
        }
       
    }


    public void setEstimation(int estimation) {
        this.estimation = estimation;
    }
    
    private int copy(){
        return this.estimation / 2;
    }

    @Override
    public boolean getObservet() {
        return this.observerNode;
    }
    
    @Override
    public int getEstimation() {
        return estimation;
    }
    private boolean isObserver(DTNHost otherHost) {
        if (otherHost.toString().startsWith("Obs")) {
            return true;
        }
        return false;
    }

    @Override
    public Map<DTNHost, Map<Double, Integer>> getEstimasi() {
        return estimasi;
    }
    
}
