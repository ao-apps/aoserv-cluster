/*
 * Copyright 2008-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.analyze.AlertLevel;
import com.aoindustries.aoserv.cluster.analyze.AnalyzedClusterConfiguration;
import com.aoindustries.aoserv.cluster.analyze.Result;
import com.aoindustries.aoserv.cluster.analyze.ResultHandler;

/**
 * Adds up all the non-optimal states of the analyzed cluster giving more weight
 * to higher level problems.  Adds in <code>g*.00001</code> to prefer shorter paths.  Each
 * type of problem is scaled by how far off the state is when possible.
 * 
 * This is not thread safe.
 * 
 * The values are:
 * <pre>
 * BASE = 1.5
 *
 * NONE = 0
 * LOW = deviation
 * MEDIUM = BASE * deviation
 * HIGH = BASE*BASE * deviation
 * CRITICAL = 1024 + BASE*BASE*BASE * deviation
 * </pre>
 *
 * @author  AO Industries, Inc.
 */
public class ExponentialDeviationHeuristicFunction implements HeuristicFunction, ResultHandler<Object> {

    private static final double BASE = 1.5;

    private double total;
    
    public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
        AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

        // Include g to prefer shorter paths - this is meant to be just a tie breaker and to minimally
        // affect the path otherwise
        total = g*.00001;

        // Add each result
        analysis.getAllResults(this, AlertLevel.LOW);

        return total;
    }

    public boolean handleResult(Result<?> result) {
        AlertLevel alertLevel = result.getAlertLevel();
        switch(alertLevel) {
            case NONE :
                throw new AssertionError("Should only get non-optimal results");
            case LOW :
                total += result.getDeviation();
                break;
            case MEDIUM :
                total += BASE * result.getDeviation();
                break;
            case HIGH :
                total += BASE*BASE * result.getDeviation();
                break;
            case CRITICAL :
                total += 1024 + BASE*BASE*BASE * result.getDeviation(); // Try to avoid this at all costs
                break;
            default :
                throw new AssertionError("Unexpected value for alertLevel: "+alertLevel);
        }
        return true;
    }
}
