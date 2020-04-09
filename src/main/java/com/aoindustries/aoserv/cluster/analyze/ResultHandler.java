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
public interface ResultHandler<T> {

	/**
	 * Each result is provided as it is generated instead of building into lists.
	 * 
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	boolean handleResult(Result<? extends T> result);
}
