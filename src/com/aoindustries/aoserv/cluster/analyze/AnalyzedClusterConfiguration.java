/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.Cluster;
import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.UnmodifiableArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Analyzes the cluster to find anything that is not optimal.  This will be
 * ran periodically from our NOC software in order to identify suboptimal
 * configurations.
 * 
 * TODO: Analyze DomUGroups
 * 
 * TODO: Add in the concept of node groups (no more than a certain number of Dom0 per group
 *       sharing resources).
 *
 * @author  AO Industries, Inc.
 */
public class AnalyzedClusterConfiguration {

    private final ClusterConfiguration clusterConfiguration;
    private final List<AnalyzedDom0Configuration> analyzedDom0Configurations;

    /**
     * Analyzes the cluster looking for any non-optimal configurations.
     * This will create a snapshot of the cluster results, subsequent changes
     * to the cluster will not effect these values.
     */
    public AnalyzedClusterConfiguration(ClusterConfiguration clusterConfiguration) {
        this.clusterConfiguration = clusterConfiguration;
        // Analyze each Dom0
        Cluster cluster = clusterConfiguration.getCluster();
        Map<String,Dom0> clusterDom0s = cluster.getDom0s();
        int size = clusterDom0s.size();
        if(size==0) analyzedDom0Configurations = Collections.emptyList();
        else if(size==1) {
            analyzedDom0Configurations = Collections.singletonList(
                new AnalyzedDom0Configuration(
                    clusterConfiguration,
                    clusterDom0s.values().iterator().next()
                )
            );
        } else {
            AnalyzedDom0Configuration[] dom0s = new AnalyzedDom0Configuration[clusterDom0s.size()];
            int index = 0;
            for(Dom0 dom0 : clusterDom0s.values()) {
                dom0s[index++] = new AnalyzedDom0Configuration(clusterConfiguration, dom0);
            }
            assert index==size : "index!=size: "+index+"!="+size;
            analyzedDom0Configurations = new UnmodifiableArrayList<AnalyzedDom0Configuration>(dom0s);
        }
    }

    /**
     * Gets the cluster configuration that is analyzed.
     */
    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    /**
     * Gets the unmodifiable list of analyzed Dom0 configuration results.
     */
    public List<AnalyzedDom0Configuration> getAnalyzedDom0Configurations() {
        return analyzedDom0Configurations;
    }

    /**
     * This convience method will obtain all the different results.
     * This may be useful by heuristics that weigh the state by
     * all the results.
     * 
     * @return true if more results are wanted, or false to receive no more results.
     */
    public boolean getAllResults(ResultHandler<Object> resultHandler, AlertLevel minimumAlertLevel) {
        for(AnalyzedDom0Configuration dom0 : getAnalyzedDom0Configurations()) {
            if(!dom0.getAllResults(resultHandler, minimumAlertLevel)) return false;
        }
        return true;
    }

    /**
     * Determines if this is optimal, meaning all results have AlertLevel of NONE.
     */
    public boolean isOptimal() {
        final boolean[] isOptimal = {true};
        getAllResults(
            new ResultHandler() {
                public boolean handleResult(Result result) {
                    isOptimal[0] = false;
                    return false;
                }
            },
            AlertLevel.LOW
        );
        return isOptimal[0];
    }

    /**
     * Determines if this has at least one result with AlertLevel of CRITICAL.
     */
    public boolean hasCritical() {
        final boolean[] hasCritical = new boolean[1];
        getAllResults(
            new ResultHandler() {
                public boolean handleResult(Result result) {
                    hasCritical[0] = true;
                    return false;
                }
            },
            AlertLevel.CRITICAL
        );
        return hasCritical[0];
    }
}
