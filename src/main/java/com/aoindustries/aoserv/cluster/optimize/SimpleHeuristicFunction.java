/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020, 2022  AO Industries, Inc.
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
import com.aoindustries.aoserv.cluster.analyze.AlertLevel;
import com.aoindustries.aoserv.cluster.analyze.AnalyzedClusterConfiguration;
import com.aoindustries.aoserv.cluster.analyze.Result;
import com.aoindustries.aoserv.cluster.analyze.ResultHandler;

/**
 * Simply counts the non-optimal nodes, adds <code>g</code> to prefer shorter paths.
 *
 * This is not thread safe.
 * 
 * @author  AO Industries, Inc.
 */
public class SimpleHeuristicFunction implements HeuristicFunction, ResultHandler<Object> {

	private int count;

	@Override
	public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
		AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

		count = g;

		// Add each result
		analysis.getAllResults(this, AlertLevel.LOW);

		return count;
	}

	@Override
	public boolean handleResult(Result<?> result) {
		assert result.getAlertLevel().compareTo(AlertLevel.NONE)>0 : "Should only get non-optimal results, got "+result.getAlertLevel();
		count++;
		return true;
	}
}
