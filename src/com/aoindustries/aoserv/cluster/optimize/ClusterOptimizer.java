/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.DomU;
import com.aoindustries.aoserv.cluster.DomUConfiguration;
import com.aoindustries.aoserv.cluster.DomUDiskConfiguration;
import com.aoindustries.aoserv.cluster.analyze.AnalyzedClusterConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * <p>
 * Optimizes the cluster using a best-first heuristic search.
 * </p>
 * <p>
 * The current optimization only tries to eliminate all problems.  A future version
 * of this should factory in the hardware and monthly costs to maximize the amount
 * of revenue we can make per cost.
 * </p>
 * <p>
 * The search is performed by exploring two possible moves:
 * <ol>
 *   <li>Swap DomU between primary and secondary Dom0 (live-migrate if same architecture or shutdown/create if different)</li>
 *   <li>Move the secondary storage to a different Dom0</li>
 * </ol>
 * </p>
 * <p>
 * The following transitions are possible, but can also occur less directly as
 * a result of the previous two transitions.  These are not yet implemented.
 * <ol>
 *   <li>pvmove primary storage to different physical volumes</li>
 *   <li>pvmove secondary storage to different physical volumes</li>
 * </ol>
 * </p>
 * <p>
 * To reduce the number of non-live-migrate swaps, this search could try to move
 * between same architectures in preference to different architectures.
 * </p>
 * TODO: Is there any way to reduce the search space when different pieces of hardware are identical?
 * 
 * TODO: To manage heap consumption, allow to optional parameters:
 *           maxPathLen
 *           something to make it a "Beam Search" (http://pages.cs.wisc.edu/~dyer/cs540/notes/search2.html)
 *
 * @author  AO Industries, Inc.
 */
public class ClusterOptimizer {

    private final ClusterConfiguration clusterConfiguration;
    private final HeuristicFunction heuristicFunction;
    private final boolean allowPathThroughCritical;

    public ClusterOptimizer(ClusterConfiguration clusterConfiguration, HeuristicFunction heuristicFunction, boolean allowPathThroughCritical) {
        this.clusterConfiguration = clusterConfiguration;
        this.heuristicFunction = heuristicFunction;
        this.allowPathThroughCritical = allowPathThroughCritical;
    }

    /**
     * Optimizes the cluster and returns the path to the first optimal configuration found or <code>null</code> if no
     * optimal configuration was found.
     */
    public List<Transition> getOptimizedClusterConfiguration() {
        return getOptimizedClusterConfiguration(null);
    }

    /**
     * Optimizes the cluster and returns the best path (possibly limited by an OptimizedResultHandler)
     * or <code>null</code> if no optimal configuration was found.
     *
     * Best-first search implementation.
     * 
     * TODO: Allow heuristic to return Double.POSITIVE_INFINITY to indicate no solution from that state:
     * TODO: (see http://pages.cs.wisc.edu/~dyer/cs540/notes/search2.html)
     *
     * TODO: Add in transition cost because the best path depends not on the number of steps, but the total time of the steps.
     * TODO: This could be a real-world estimate on how many seconds the operation would take.
     * 
     * TODO: If something MUST take a path through a CRITICAL state, try to use path with shortest time in CRITICAL
     * TODO: based on time estimates above.
     * 
     * @param  handler  if null, returns the first path found, not necessarily the shortest
     */
    public List<Transition> getOptimizedClusterConfiguration(OptimizedClusterConfigurationHandler handler) {

        // Reused inside loop below
        List<ClusterConfiguration> children = new ArrayList<ClusterConfiguration>();
        List<Transition> childTransitions = new ArrayList<Transition>();

        // Return value is stored here upon success or remains null on failure
        List<Transition> transitions = null;

        // Initialize the open list
        PriorityQueue<ListElement> openQueue = new PriorityQueue<ListElement>();
        Map<ClusterConfiguration,ListElement> openMap = new HashMap<ClusterConfiguration,ListElement>();
        {
            List<Transition> emptyTransitions = Collections.emptyList();
            ListElement openListElement = new ListElement(clusterConfiguration, heuristicFunction, emptyTransitions);
            openQueue.add(openListElement);
            openMap.put(clusterConfiguration, openListElement);
        }

        // Initialize the closed list
        Map<ClusterConfiguration,ListElement> closedMap = new HashMap<ClusterConfiguration,ListElement>();

        long loopCounter = 0;
        long existingOpenCount = 0;
        long existingClosedCount = 0;
        long openQueueRemoveCount = 0;
        long skipCriticalPathCount = 0;
        long lastDisplayTime = System.currentTimeMillis();
        while(!openQueue.isEmpty()) {
            loopCounter++;
            assert openQueue.size()==openMap.size() : "openQueue and openMap have different sizes";
            ListElement X = openQueue.remove();
            openMap.remove(X.clusterConfiguration);
            long currentTime = System.currentTimeMillis();
            long timeSince = currentTime - lastDisplayTime;
            if(timeSince<0 || timeSince>=60000) {
                System.out.println(
                    "        open:"+openMap.size()
                    + " closed:"+closedMap.size()
                    + " transitions:"+X.transitions.size()
                    + " heuristic:"+X.heuristic
                    + " existingOpen:"+existingOpenCount
                    + " existingClosed:"+existingClosedCount
                    + " openQueueRemove:"+openQueueRemoveCount
                    + " skipCriticalPath:"+skipCriticalPathCount
                );
                lastDisplayTime = currentTime;
            }
            // Is this the goal?
            if(X.isGoal()) {
                List<Transition> unmodifiablePath = Collections.unmodifiableList(X.transitions);
                boolean needsTrim = false;
                if(
                    transitions==null
                    || X.transitions.size()<transitions.size()
                ) {
                    transitions = unmodifiablePath;
                    needsTrim = true;
                }
                
                // Give handler a chance to cancel before trimming
                if(
                    handler==null
                    || !handler.handleOptimizedClusterConfiguration(unmodifiablePath, loopCounter)
                ) break;

                // Trim anything out of open/closed that has transitions.length>=this path
                if(needsTrim) {
                    //System.out.println(
                    //    "        Before trim: openQueue: "+openQueue.size()
                    //    + " openMap: "+openMap.size()
                    //    + " closedMap:"+closedMap.size()
                    //);
                    // openQueue and openMap
                    Iterator<Map.Entry<ClusterConfiguration,ListElement>> openIter = openMap.entrySet().iterator();
                    while(openIter.hasNext()) {
                        Map.Entry<ClusterConfiguration,ListElement> entry = openIter.next();
                        ListElement listElement = entry.getValue();
                        if(listElement.transitions.size()>=transitions.size()) {
                            openIter.remove();
                            if(!openQueue.remove(listElement)) throw new AssertionError("listElement not found in openQueue");
                        }
                    }
                    // closedMap
                    Iterator<Map.Entry<ClusterConfiguration,ListElement>> closedIter = closedMap.entrySet().iterator();
                    while(closedIter.hasNext()) {
                        Map.Entry<ClusterConfiguration,ListElement> entry = closedIter.next();
                        if(entry.getValue().transitions.size()>=transitions.size()) closedIter.remove();
                    }
                    //System.out.println(
                    //    "        After trim: openQueue: "+openQueue.size()
                    //    + " openMap: "+openMap.size()
                    //    + " closedMap:"+closedMap.size()
                    //);
                }
            } else {
                // generate children of X if depth limit not reached
                // max depth is determined by any path already found
                if(transitions==null || (X.transitions.size()+1)<transitions.size()) { // + 1 to match size of newTransitions below
                    generateChildren(X.clusterConfiguration, children, childTransitions);
                    //System.out.println("        children: "+children.size());
                    boolean xEndsCritical = allowPathThroughCritical ? true : X.transitions.isEmpty() ? true : new AnalyzedClusterConfiguration(X.transitions.get(X.transitions.size()-1).getAfterClusterConfiguration()).hasCritical();
                    // for each child of X do
                    for(int i=0, size=children.size(); i<size; i++) {
                        ClusterConfiguration child = children.get(i);
                        // Don't keep any path that has a transition from not having any critical to have at least one critical
                        boolean childHasCritical = allowPathThroughCritical ? false : new AnalyzedClusterConfiguration(child).hasCritical();
                        if(xEndsCritical || !childHasCritical) {
                            ListElement existingOpen = openMap.get(child);
                            if(existingOpen!=null) {
                                existingOpenCount++;
                                // if the child was reached by a shorter path
                                if((X.transitions.size()+1)<existingOpen.transitions.size()) { // + 1 to match size of newTransitions below
                                    // then give the state of open the shorter path

                                    // removing and adding back to open because a short path affects the heuristic and therefore
                                    // the position within the queue.
                                    openQueue.remove(existingOpen); // This runs in O(n)
                                    openQueueRemoveCount++;

                                    List<Transition> newTransitions = new ArrayList<Transition>(X.transitions.size()+1);
                                    newTransitions.addAll(X.transitions);
                                    newTransitions.add(childTransitions.get(i));
                                    ListElement openListElement = new ListElement(child, heuristicFunction, newTransitions);
                                    openQueue.add(openListElement);
                                    openMap.put(child, openListElement);
                                }
                            } else {
                                ListElement existingClosed = closedMap.get(child);
                                if(existingClosed!=null) {
                                    existingClosedCount++;
                                    // If the child was reached by a shorter path then
                                    if((X.transitions.size()+1)<existingClosed.transitions.size()) {
                                        // remove the state from closed
                                        closedMap.remove(child);
                                        // add the child to open
                                        List<Transition> newTransitions = new ArrayList<Transition>(X.transitions.size()+1);
                                        newTransitions.addAll(X.transitions);
                                        newTransitions.add(childTransitions.get(i));
                                        ListElement openListElement = new ListElement(child, heuristicFunction, newTransitions);
                                        openQueue.add(openListElement);
                                        openMap.put(child, openListElement);
                                    }
                                } else {
                                    // the child is not on open or closed
                                    // add the child to open
                                    List<Transition> newTransitions = new ArrayList<Transition>(X.transitions.size()+1);
                                    newTransitions.addAll(X.transitions);
                                    newTransitions.add(childTransitions.get(i));
                                    ListElement openListElement = new ListElement(child, heuristicFunction, newTransitions);
                                    openQueue.add(openListElement);
                                    openMap.put(child, openListElement);
                                }
                            }
                        } else skipCriticalPathCount++;
                    }
                }
            }
            // put X on closed
            closedMap.put(X.clusterConfiguration, X);
        }
        return transitions;
    }

    private static void generateChildren(ClusterConfiguration clusterConfiguration, List<ClusterConfiguration> children, List<Transition> childTransitions) {
        children.clear();
        childTransitions.clear();
        
        // Try swapping each of the primary/secondary pairs
        for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
            DomU domU = domUConfiguration.getDomU();
            if(!domU.isSecondaryDom0Locked()) {
                Dom0 primaryDom0 = domUConfiguration.getPrimaryDom0();
                Dom0 secondaryDom0 = domUConfiguration.getSecondaryDom0();
                if(!domU.isPrimaryDom0Locked()) {
                    ClusterConfiguration swappedClusterConfiguration = clusterConfiguration.liveMigrate(domU);
                    children.add(swappedClusterConfiguration);
                    childTransitions.add(
                        new MigrateTransition(
                            clusterConfiguration,
                            swappedClusterConfiguration,
                            domU,
                            primaryDom0,
                            secondaryDom0
                        )
                    );
                }

                // TODO: Adhere to or remove the locked flags for individual disks
                // Try moving secondary to any other Dom0 that has enough free extents
                int domUTotalExtents = 0;
                for(DomUDiskConfiguration domUDiskConfiguration : domUConfiguration.getDomUDiskConfigurations()) domUTotalExtents += domUDiskConfiguration.getDomUDisk().getExtents();

                for(Dom0 dom0 : clusterConfiguration.getCluster().getDom0s().values()) {
                    // Can't move to current primary or secondary
                    if(!dom0.equals(primaryDom0) && !dom0.equals(secondaryDom0)) {
                        boolean hasEnoughExtents;
                        if(domUTotalExtents==0) hasEnoughExtents = true;
                        else {
                            throw new RuntimeException("TODO: Finish method");
                        }
                        if(hasEnoughExtents) {
                            ClusterConfiguration movedClusterConfiguration = clusterConfiguration.moveSecondary(domU, dom0);
                            children.add(movedClusterConfiguration);
                            childTransitions.add(
                                new MoveSecondaryTransition(
                                    clusterConfiguration,
                                    movedClusterConfiguration,
                                    domU,
                                    secondaryDom0,
                                    dom0
                                )
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the starting clusterConfiguration.
     */
    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    /**
     * Gets the heuristic function used during the search.
     */
    public HeuristicFunction getHeuristicFunction() {
        return heuristicFunction;
    }
    
    public boolean allowsPathThroughCritical() {
        return allowPathThroughCritical;
    }
}
