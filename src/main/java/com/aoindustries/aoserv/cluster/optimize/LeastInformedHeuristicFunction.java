/*
 * Copyright 2008-2011, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.analyze.AnalyzedClusterConfiguration;

/**
 * This simply returns g if the cluster is optimal or g+1 if it is optimal.
 *
 * @author  AO Industries, Inc.
 */
public class LeastInformedHeuristicFunction implements HeuristicFunction {

	@Override
	public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
		AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

		return analysis.isOptimal() ? g : (g+1);
	}
}
