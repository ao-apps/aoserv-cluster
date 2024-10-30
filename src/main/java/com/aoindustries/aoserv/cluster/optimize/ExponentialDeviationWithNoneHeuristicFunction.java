/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020, 2022, 2024  AO Industries, Inc.
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
 * to higher level problems.  Adds in <code>g*.00001</code> to prefer shorter paths.  Each
 * type of problem is scaled by how far off the state is when possible.
 *
 * <p>The heuristics for "NONE" are also added (in negative form) with a coefficient of .001 (as a tie breaker with more weight than number of moves).</p>
 *
 * <p>This is not thread safe.</p>
 *
 * <p>The values are:</p>
 *
 * <pre>BASE = 1.5
 *
 * NONE = 0.001 * deviation
 * LOW = deviation
 * MEDIUM = BASE * deviation
 * HIGH = BASE*BASE * deviation
 * CRITICAL = 1024 + BASE*BASE*BASE * deviation</pre>
 *
 * @author  AO Industries, Inc.
 */
public class ExponentialDeviationWithNoneHeuristicFunction implements HeuristicFunction, ResultHandler<Object> {

  private static final double BASE = 1.5;

  private double total;

  @Override
  public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
    AnalyzedClusterConfiguration analysis = new AnalyzedClusterConfiguration(clusterConfiguration);

    // Include g to prefer shorter paths - this is meant to be just a tie breaker and to minimally
    // affect the path otherwise
    total = g * .00001;

    // Add each result
    analysis.getAllResults(this, AlertLevel.NONE);

    return total;
  }

  @Override
  public boolean handleResult(Result<?> result) {
    AlertLevel alertLevel = result.getAlertLevel();
    switch (alertLevel) {
      case NONE:
        total += 0.001 * result.getDeviation();
        break;
      case LOW:
        total += result.getDeviation();
        break;
      case MEDIUM:
        total += BASE * result.getDeviation();
        break;
      case HIGH:
        total += BASE * BASE * result.getDeviation();
        break;
      case CRITICAL:
        total += 1024 + BASE * BASE * BASE * result.getDeviation(); // Try to avoid this at all costs
        break;
      default:
        throw new AssertionError("Unexpected value for alertLevel: " + alertLevel);
    }
    return true;
  }
}
