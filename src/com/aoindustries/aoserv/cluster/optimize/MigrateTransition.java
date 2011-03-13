/*
 * Copyright 2008-2011 by AO Industries, Inc.,
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
public class MigrateTransition extends Transition {

    private final DomU domU;
    private final Dom0 oldPrimaryDom0;
    private final Dom0 oldSecondaryDom0;

    MigrateTransition(
        DomU domU,
        Dom0 oldPrimaryDom0,
        Dom0 oldSecondaryDom0
    ) {
        this.domU = domU;
        this.oldPrimaryDom0 = oldPrimaryDom0;
        this.oldSecondaryDom0 = oldSecondaryDom0;
    }

    public DomU getDomU() {
        return domU;
    }
    
    public Dom0 getOldPrimaryDom0() {
        return oldPrimaryDom0;
    }

    public Dom0 getOldSecondaryDom0() {
        return oldSecondaryDom0;
    }

    @Override
    public String toString() {
        return "Migrate "+domU.getHostname()+" from "+oldPrimaryDom0.getHostname()+" to "+oldSecondaryDom0.getHostname();
    }
}
