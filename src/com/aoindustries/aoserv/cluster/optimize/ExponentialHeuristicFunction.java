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
 * Adds up all the non-optimal states of the analyzed cluster giving more weight
 * to higher level problems.  Adds in <code>g</code> to prefer shorter paths.
 * 
 * The values are:
 * <pre>
 * NONE = 0
 * LOW = 4
 * MEDIUM = 8
 * HIGH = 16
 * CRITICAL = 1024
 * </pre>
 *
 * @author  AO Industries, Inc.
 */
public class ExponentialHeuristicFunction implements HeuristicFunction {

    public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
        AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

        // Include g to prefer shorter paths
        long total = g;

        // Add each result
        List<Result> results = new ArrayList<Result>();
        analysis.addAllResults(results, true);
        for(Result result : results) {
            AlertLevel alertLevel = result.getAlertLevel();
            switch(alertLevel) {
                case NONE :
                    throw new AssertionError("Should not get non-optimal results");
                case LOW :
                    total += 4;
                    break;
                case MEDIUM :
                    total += 8;
                    break;
                case HIGH :
                    total += 16;
                    break;
                case CRITICAL :
                    total += 1024; // Try to avoid this at all costs
                    break;
                default :
                    throw new AssertionError("Unexpected value for alertLevel: "+alertLevel);
            }
        }
        return total;
    }
}
