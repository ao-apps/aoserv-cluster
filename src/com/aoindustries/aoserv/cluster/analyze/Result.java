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
abstract public class Result<T> implements Comparable<Result> {

    final private String label;
    final private double deviation;
    final private AlertLevel alertLevel;

    Result(String label, double deviation, AlertLevel alertLevel) {
        assert !(alertLevel!=AlertLevel.NONE && deviation<=0) : "Any result with an alert level > NONE should have a positive, non-zero deviation";
        assert !(alertLevel==AlertLevel.NONE && deviation>0) : "Any result with an alert level = NONE should have a negative or zero deviation";
        this.label = label;
        this.deviation = deviation;
        this.alertLevel = alertLevel;
    }

    final public String getLabel() {
        return label;
    }

    /**
     * Gets the current value for the resource or <code>null</code> if unavailable.
     */
    abstract public T getValue();
    
    /**
     * Gets the maximum value for the resource or <code>null</code> if unavailable.
     */
    abstract public T getMaxValue();

    /**
     * Gets the relative amount of devation the value is from the expected/maximum value.
     * If the deviation is otherwise unknown or doesn't make sense for the type of resource,
     * should be 1.0.
     */
    final public double getDeviation() {
        return deviation;
    }

    final public AlertLevel getAlertLevel() {
        return alertLevel;
    }
    
    /**
     * Sorted by label.
     */
    final public int compareTo(Result other) {
        return label.compareTo(other.label);
    }
    
    @Override
    final public String toString() {
        T value = getValue();
        T maxValue = getMaxValue();
        return
            alertLevel
            + ": " + label
            + " "  + (value==null ? "NA" : value)
            + "/"  + (maxValue==null ? "NA" : maxValue)
            + " "  + deviation
        ;
    }
}
