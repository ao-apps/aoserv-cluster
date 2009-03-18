/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

/**
 * Stores an AlertLevel, a value, and a textual message.
 *
 * @author  AO Industries, Inc.
 */
public class IntResult extends Result<Integer> {

    final private int value;
    final private int maxValue;

    IntResult(String label, int value, int maxValue, double deviation, AlertLevel alertLevel) {
        super(label, deviation, alertLevel);
        this.value = value;
        this.maxValue = maxValue;
    }

    /**
     * Gets the current value for the resource.
     */
    public Integer getValue() {
        return value;
    }

    /**
     * Gets the maximum value for the resource.
     */
    public Integer getMaxValue() {
        return maxValue;
    }
}
