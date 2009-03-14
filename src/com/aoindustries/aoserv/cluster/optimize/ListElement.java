/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.analyze.AnalyzedClusterConfiguration;
import java.util.List;

class ListElement implements Comparable<ListElement> {

    final ClusterConfiguration clusterConfiguration;
    final HeuristicFunction heuristicFunction;
    final List<Transition> transitions;
    final double heuristic;

    ListElement(
        ClusterConfiguration clusterConfiguration,
        HeuristicFunction heuristicFunction,
        List<Transition> transitions
    ) {
        this.clusterConfiguration = clusterConfiguration;
        this.heuristicFunction = heuristicFunction;
        this.transitions = transitions;
        this.heuristic = heuristicFunction.getHeuristic(clusterConfiguration, transitions.size());
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

    /**
     * Checks if this is a goal state.
     * 
     * @see AnalyzedClusterConfiguration#isOptimal()
     */
    boolean isGoal() {
        return new AnalyzedClusterConfiguration(clusterConfiguration).isOptimal();
    }
}
