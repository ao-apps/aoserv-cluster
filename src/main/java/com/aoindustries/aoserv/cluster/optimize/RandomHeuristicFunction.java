/*
 * Copyright 2008-2011, 2019, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.io.IoUtils;
import java.security.SecureRandom;
import java.util.Random;

/**
 * This simply returns a random number between 0 and 1.  The results may be different
 * for each call on the same configuration - this may have unexpected consequences.
 *
 * @author  AO Industries, Inc.
 */
public class RandomHeuristicFunction implements HeuristicFunction {

	private static final Random fastRandom = new Random(IoUtils.bufferToLong(new SecureRandom().generateSeed(8)));

	@Override
	public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
		return fastRandom.nextDouble();
	}
}
