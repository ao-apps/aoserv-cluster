/*
 * Copyright 2008-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;

public class ListElement implements Comparable<ListElement> {

    /**
     * This is null for the first element in the list.
     */
    final ListElement previous;
    
    /**
     * This is null for the first element in the list.
     */
    final Transition transition;
    
    final int pathLen;

    /**
     * The configuration after the transition.
     */
    final ClusterConfiguration clusterConfiguration;

    final double heuristic;

    ListElement(
        ListElement previous,
        Transition transition,
        ClusterConfiguration clusterConfiguration,
        double heuristic
    ) {
        this.previous = previous;
        this.transition = transition;
        this.pathLen = previous==null ? 0 : (previous.pathLen+1);
        assert clusterConfiguration!=null : "clusterConfiguration is null";
        this.clusterConfiguration = clusterConfiguration;
        this.heuristic = heuristic;
    }

    /**
     * Sorted by heuristic value, lowest to highest.
     */
    public int compareTo(ListElement other) {
        double h1 = heuristic;
        double h2 = other.heuristic;
        if(h1<h2) return -1;
        if(h2<h1) return 1;
        return 0;
    }
    
    public ListElement getPrevious() {
        return previous;
    }
    
    /**
     * Gets the transition, this is null for the initial state.
     */
    public Transition getTransition() {
        return transition;
    }

    public int getPathLen() {
        return pathLen;
    }

    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public double getHeuristic() {
        return heuristic;
    }
}
