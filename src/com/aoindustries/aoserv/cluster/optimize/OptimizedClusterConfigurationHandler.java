/*
 * 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import java.util.List;

/**
 * Accepts results from the optimized cluster
 */
public interface OptimizedClusterConfigurationHandler {

    /**
     * Handles one result.  Returns true if the optimization should continue.
     */
    boolean handleOptimizedClusterConfiguration(List<Transition> path, long loopCount);
}
