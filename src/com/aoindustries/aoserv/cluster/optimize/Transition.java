/*
 * Copyright 2008-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

/**
 * A transition is one of the possible conversions of clusterConfiguration state.
 * Other transitions could include:
 *     pvmove
 *     vgextend -\
 *                if we allow a mapping where not all extents have been reached,
 *                this may be helpful when growing/shrinking existing VMs.
 *     vgreduce -/
 *     physically moving a hard drive
 *     adding more hardware/servers
 *
 * @author  AO Industries, Inc.
 */
public abstract class Transition {
    
    Transition() {
    }
}
