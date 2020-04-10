/*
 * Copyright 2008-2011, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

/**
 * Stores an AlertLevel, a value, and a textual message.
 *
 * @author  AO Industries, Inc.
 */
public class ObjectResult<T> extends Result<T> {

	final private T value;
	final private T maxValue;

	ObjectResult(String label, T value, T maxValue, double deviation, AlertLevel alertLevel) {
		super(label, deviation, alertLevel);
		this.value = value;
		this.maxValue = maxValue;
	}

	/**
	 * Gets the current value for the resource or <code>null</code> if unavailable.
	 */
	@Override
	public T getValue() {
		return value;
	}

	/**
	 * Gets the maximum value for the resource or <code>null</code> if unavailable.
	 */
	@Override
	public T getMaxValue() {
		return maxValue;
	}
}
