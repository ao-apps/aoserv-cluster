/*
 * Copyright 2009-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

/**
 * Accepts results from the optimized cluster
 */
public interface OptimizedClusterConfigurationHandler {

    /**
     * Handles one result.  Returns true if the optimization should continue.
     */
    boolean handleOptimizedClusterConfiguration(ListElement path, long loopCount);
}
