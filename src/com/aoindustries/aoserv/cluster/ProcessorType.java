/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

/**
 * Higher level processors may be substituded without effecting
 * how optimal the cluster is.
 *
 * @author  AO Industries, Inc.
 */
public enum ProcessorType {
    PIII,
    P4,
    P4_XEON,
    CORE,
    CORE2,
    XEON_LV
}
