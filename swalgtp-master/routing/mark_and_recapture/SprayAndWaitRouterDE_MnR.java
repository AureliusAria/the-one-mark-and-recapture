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
public class SprayAndWaitRouterDE_MnR implements RoutingDecisionEngine, ObserverNode {

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
    private Set<DTNHost> markNode;
    private Set<DTNHost> recaptureNode;
    private Map<DTNHost, ArrayList<DTNHost>> markMessage;

    public SprayAndWaitRouterDE_MnR(Settings s) {
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

    public SprayAndWaitRouterDE_MnR(SprayAndWaitRouterDE_MnR proto) {
        this.initialNrofCopies = proto.initialNrofCopies;
        this.initialNrofMark = proto.initialNrofMark;
        this.isBinary = proto.isBinary; //this.isBinary = proto.isBinary;
        this.observerNode = proto.observerNode;
        this.mark = proto.mark;
      //  this.markPrefix = cs.markPrefix;
        this.markNode = new HashSet<DTNHost>();
        this.recaptureNode = new HashSet<DTNHost>();
        this.interval = proto.interval;
        this.estimation = proto.estimation;
        this.markMessage = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
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
            //System.out.println("sampai sini");
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
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
       // System.out.println("mark");
        String markPrefix = Observer.getInstance().getMarkPrefix();
        DTNHost thisHost = null;
        if (otherHost.isRadioActive() == true){
            Collection<Message> messages = otherHost. getMessageCollection();
            List<DTNHost> listHops = m.getHops();
            Iterator it = listHops.iterator();
            while(it.hasNext()){
                thisHost = (DTNHost)it.next();
            }
            if(m.getPrefix().equals(markPrefix)){
                if(m.getTo() == otherHost){
                    return true;
                }
                
                Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
                it = messages.iterator();
                while (it.hasNext()) {
                    Message temp = (Message) it.next();
                    if(!thisHost.equals(temp.getFrom()) && nrofMark > 1){
                        return true;
                    }
                }
                // memulai recapture
                it = messages.iterator();
                while (it.hasNext()) {
                    Message temp = (Message) it.next();
                    if(m.getPrefix().equals(markPrefix) ){
                        if(thisHost.equals(temp.getFrom())){
                            this.recaptureNode.add(otherHost);
                            this.markNode.add(otherHost);
                        } else{
                            this.recaptureNode.add(otherHost);
                        }
                    }
                }
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
        return new SprayAndWaitRouterDE_MnR(this);
    }

    private SprayAndWaitRouterDE_MnR getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + "with other routers of same type";

        return (SprayAndWaitRouterDE_MnR) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
       // System.out.println("update");
        double currentTime = SimClock.getIntTime();
        Set<String> messageToDelete = new HashSet<>();
        String markPrefix = Observer.getInstance().getMarkPrefix();     
        Collection<Message> messagesCollection = host.getMessageCollection();
        
        for (Iterator<Message> iterator  = messagesCollection.iterator(); iterator.hasNext();) {
            Message msg = iterator.next();
            
            if(msg.getPrefix().equals(markPrefix)){
                if(msg.getFrom() == host){
                    int messageTtl = msg.getTtl();
                    if(messageTtl <= 0){
                        messageToDelete.add(msg.getId());
                        msg.removeProperty(MSG_MARK_PROPERTY);
                        this.setEstimation(0);
                        this.markNode.clear();
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
        
        if(currentTime - lastUpdate >= interval){
            for(Message m : host.getMessageCollection()){
                if(m.getPrefix().equals(markPrefix)){
                    if(host.equals(m.getFrom())){
                        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
                        if(nrofMark == 1){
                            System.out.println("");
                            System.out.println("Node " + host.getName());
                            System.out.println("nrofMark " + nrofMark);
                            System.out.println("Interval " + SimClock.getIntTime());
                            System.out.println("TTL "+ m.getTtl());
                            
                            if(!this.markNode.isEmpty()){
                                int tempEstimation = (initialNrofMark * this.recaptureNode.size()) / this.markNode.size();
                                this.setEstimation(tempEstimation);
                                System.out.println("Mark " + initialNrofMark);
                                System.out.println("Recapture " + this.recaptureNode.size());
                                System.out.println("m " + this.markNode.size());
                                System.out.println("Estimasi " + this.getEstimation());
                                System.out.println("");
                            }
                        }
                    }
                }
            }
        }
        this.lastUpdate = currentTime - currentTime % interval;
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
}
