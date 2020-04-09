/*
 * Copyright 2007-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

/**
 * Higher level processes may be substituted in place of lower processors
 * without any effecting how optimal the cluster is configured.
 * 
 * @author  AO Industries, Inc.
 */
public enum ProcessorArchitecture {
    I686,
    X86_64
}
