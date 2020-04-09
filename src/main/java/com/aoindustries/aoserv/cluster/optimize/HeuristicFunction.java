/*
 * Copyright 2008-2011, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;

/**
 * A <code>HeuristicAlgorithm</code> generates a heuristic value for a provided
 * <code>AnalyzedCluster</code>.
 *
 * @author  AO Industries, Inc.
 */
public interface HeuristicFunction {

	/**
	 * Estimates the number of moves to an optimal state.
	 * If it uses the provided <code>g</code> g(n) it will result in Algorithm A.
	 * If it also always uses h(n) &lt;= h*(n) it will result in Algorithm A*.
	 * 
	 * @param  analysis  The <code>AnalyzedCluster</code> representing the current state.
	 * @param  g         The number of moves already made.
	 *
	 * @return  The estimated number of moves to an optimal state
	 */
	double getHeuristic(ClusterConfiguration clusterConfiguration, int g);
}
