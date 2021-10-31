/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-cluster.
 *
 * aoserv-cluster is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-cluster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-cluster.  If not, see <https://www.gnu.org/licenses/>.
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
	 * @param  clusterConfiguration   The <code>ClusterConfiguration</code> representing the current state.
	 * @param  g         The number of moves already made.
	 *
	 * @return  The estimated number of moves to an optimal state
	 */
	double getHeuristic(ClusterConfiguration clusterConfiguration, int g);
}
