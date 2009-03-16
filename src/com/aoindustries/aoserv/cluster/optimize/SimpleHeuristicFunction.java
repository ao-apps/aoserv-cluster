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
import com.aoindustries.aoserv.cluster.analyze.ResultHandler;

/**
 * Simply counts the non-optimal nodes, adds <code>g</code> to prefer shorter paths.
 *
 * This is not thread safe.
 * 
 * @author  AO Industries, Inc.
 */
public class SimpleHeuristicFunction implements HeuristicFunction, ResultHandler<Object> {

    int count;

    public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
        AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

        count = g;

        // Add each result
        analysis.getAllResults(this, AlertLevel.LOW);

        return count;
    }

    public boolean handleResult(Result<?> result) {
        assert result.getAlertLevel().compareTo(AlertLevel.NONE)>0 : "Should only get non-optimal results, got "+result.getAlertLevel();
        count++;
        return true;
    }
}
