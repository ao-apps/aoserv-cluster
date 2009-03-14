/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;

/**
 * A transition is one of the possible conversions of clusterConfiguration state.
 * TODO: Other transitions could include:
 *     pvmove
 *     vgextend
 *     vgreduce
 *     physically moving a hard drive
 *     adding more hardware/servers
 *
 * @author  AO Industries, Inc.
 */
public abstract class Transition {
    
    private final ClusterConfiguration beforeClusterConfiguration;
    private final ClusterConfiguration afterClusterConfiguration;

    Transition(
        ClusterConfiguration beforeClusterConfiguration,
        ClusterConfiguration afterClusterConfiguration
    ) {
        this.beforeClusterConfiguration = beforeClusterConfiguration;
        this.afterClusterConfiguration = afterClusterConfiguration;
    }

    public ClusterConfiguration getBeforeClusterConfiguration() {
        return beforeClusterConfiguration;
    }

    public ClusterConfiguration getAfterClusterConfiguration() {
        return afterClusterConfiguration;
    }
}
