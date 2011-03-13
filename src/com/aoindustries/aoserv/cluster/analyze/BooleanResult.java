/*
 * Copyright 2008-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

/**
 * Stores an AlertLevel, a value, and a textual message.
 *
 * @author  AO Industries, Inc.
 */
public class BooleanResult extends Result<Boolean> {

    final private boolean value;
    final private boolean maxValue;

    BooleanResult(String label, boolean value, boolean maxValue, double deviation, AlertLevel alertLevel) {
        super(label, deviation, alertLevel);
        this.value = value;
        this.maxValue = maxValue;
    }

    /**
     * Gets the current value for the resource.
     */
    public Boolean getValue() {
        return value;
    }

    /**
     * Gets the maximum value for the resource.
     */
    public Boolean getMaxValue() {
        return maxValue;
    }
}
