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
public class Result<T> implements Comparable<Result<T>> {

    final private String label;
    final private T value;
    final private double deviation;
    final private AlertLevel alertLevel;

    Result(String label, T value, double deviation, AlertLevel alertLevel) {
        this.label = label;
        this.value = value;
        this.deviation = deviation;
        this.alertLevel = alertLevel;
    }

    public String getLabel() {
        return label;
    }

    public T getValue() {
        return value;
    }

    /**
     * Gets the relative amount of devation the value is from the expected value.
     * If the deviation is otherwise unknown or doesn't make sense, should be 1.0.
     */
    public double getDeviation() {
        return deviation;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }
    
    /**
     * Sorted by label.
     */
    public int compareTo(Result<T> other) {
        return label.compareTo(other.label);
    }
}
