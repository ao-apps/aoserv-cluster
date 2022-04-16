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
 * Adds up all the non-optimal states of the analyzed cluster giving more weight
 * to higher level problems.  Adds in <code>g</code> to prefer shorter paths.
 * 
 * This is not thread safe.
 * 
 * The values are:
 * <pre>
 * NONE = 0
 * LOW = 1
 * MEDIUM = 2
 * HIGH = 3
 * CRITICAL = 4
 * </pre>
 *
 * @author  AO Industries, Inc.
 */
public class LinearHeuristicFunction implements HeuristicFunction, ResultHandler<Object> {

	private int total;

	@Override
	public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
		AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

		// Include g to prefer shorter paths
		total = g;

		// Add each result
		analysis.getAllResults(this, AlertLevel.LOW);

		return total;
	}

	@Override
	public boolean handleResult(Result<?> result) {
		AlertLevel alertLevel = result.getAlertLevel();
		switch(alertLevel) {
			case NONE :
				throw new AssertionError("Should only get non-optimal results");
			case LOW :
				total++;
				break;
			case MEDIUM :
				total += 2;
				break;
			case HIGH :
				total += 3;
				break;
			case CRITICAL :
				total += 4;
				break;
			default :
				throw new AssertionError("Unexpected value for alertLevel: "+alertLevel);
		}
		return true;
	}
}
