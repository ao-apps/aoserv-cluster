/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.analyze.AlertLevel;
import com.aoindustries.aoserv.cluster.analyze.AnalyzedClusterConfiguration;
import com.aoindustries.aoserv.cluster.analyze.Result;
import java.util.ArrayList;
import java.util.List;

/**
 * Simply counts the non-optimal nodes, adds <code>g</code> to prefer shorter paths.
 *
 * @author  AO Industries, Inc.
 */
public class SimpleHeuristicFunction implements HeuristicFunction {

    /**
     * Used by an assertion in getHeuristic.
     * 
     * @see  #getHeuristic
     */
    private static boolean isAllNonOptimal(List<Result> results) {
        for(Result result : results) {
            if(result.getAlertLevel()==AlertLevel.NONE) return false;
        }
        return true;
    }

    public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
        AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

        List<Result> results = new ArrayList<Result>();
        analysis.addAllResults(results, true);
        assert isAllNonOptimal(results) : "Should not get non-optimal results";
        return g + results.size();
    }
}
