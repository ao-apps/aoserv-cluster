/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

/**
 * For each check, assigns a level associated with any problems.  The recommended
 * uses for each level generally consider reliability problems as more significant
 * than performance problems.
 *
 * @author  AO Industries, Inc.
 */
public enum AlertLevel {

    /**
     * Indicates the resource is optimal.
     */
    NONE,
    
    /**
     * Generally indicates the resource is runnable but nonoptimal in some way that only
     * slightly degrades performance and has no affect on reliability.
     */
    LOW,
    
    /**
     * Generally indicates the resource is either runnable with significantly degraded performance
     * or runnable with slightly degraded reliability.
     */
    MEDIUM,
    
    /**
     * Generally indicates the resources is runnable with significantly degraded reliability.
     */
    HIGH,
    
    /**
     * Indicates the resource is not runnable.
     */
    CRITICAL
}
