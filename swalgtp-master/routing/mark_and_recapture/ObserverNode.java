/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.mark_and_recapture;

import core.DTNHost;
import java.util.Map;

/**
 *
 * @author aurelius_aria
 */
public interface ObserverNode {
    public boolean getObservet();
    public int getEstimation();
    public Map<DTNHost, Map<Double, Integer>> getEstimasi();
}
