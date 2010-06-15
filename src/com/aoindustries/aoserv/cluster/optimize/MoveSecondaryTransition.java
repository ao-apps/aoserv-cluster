/*
 * Copyright 2008-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.DomU;

/**
 * A swap between primary and secondary.
 *
 * @author  AO Industries, Inc.
 */
public class MoveSecondaryTransition extends Transition {

    private final DomU domU;
    private final Dom0 oldSecondaryDom0;
    private final Dom0 newSecondaryDom0;

    MoveSecondaryTransition(
        DomU domU,
        Dom0 oldSecondaryDom0,
        Dom0 newSecondaryDom0
    ) {
        this.domU = domU;
        this.oldSecondaryDom0 = oldSecondaryDom0;
        this.newSecondaryDom0 = newSecondaryDom0;
    }

    public DomU getDomU() {
        return domU;
    }
    
    public Dom0 getOldSecondaryDom0() {
        return oldSecondaryDom0;
    }

    public Dom0 getNewSecondaryDom0() {
        return newSecondaryDom0;
    }

    @Override
    public String toString() {
        return "Move "+domU.getHostname()+" secondary from "+oldSecondaryDom0.getHostname()+" to "+newSecondaryDom0.getHostname();
    }
}
