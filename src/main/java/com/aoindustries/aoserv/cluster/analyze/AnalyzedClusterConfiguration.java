/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.Cluster;
import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.UnmodifiableArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Analyzes the cluster to find anything that is not optimal.  This will be
 * ran periodically from our NOC software in order to identify suboptimal
 * configurations.
 *
 * <p>TODO: Analyze DomUGroups</p>
 *
 * <p>TODO: Add in the concept of node groups (no more than a certain number of Dom0 per group
 *       sharing resources).</p>
 *
 * @author  AO Industries, Inc.
 */
public class AnalyzedClusterConfiguration {

  private final ClusterConfiguration clusterConfiguration;
  private final List<AnalyzedDom0Configuration> analyzedDom0Configurations;

  /**
   * Analyzes the cluster looking for any non-optimal configurations.
   * This will create a snapshot of the cluster results, subsequent changes
   * to the cluster will not effect these values.
   */
  public AnalyzedClusterConfiguration(ClusterConfiguration clusterConfiguration) {
    this.clusterConfiguration = clusterConfiguration;
    // Analyze each Dom0
    Cluster cluster = clusterConfiguration.getCluster();
    Map<String, Dom0> clusterDom0s = cluster.getDom0s();
    int size = clusterDom0s.size();
    if (size == 0) {
      analyzedDom0Configurations = Collections.emptyList();
    } else if (size == 1) {
      analyzedDom0Configurations = Collections.singletonList(
          new AnalyzedDom0Configuration(
              clusterConfiguration,
              clusterDom0s.values().iterator().next()
          )
      );
    } else {
      AnalyzedDom0Configuration[] dom0s = new AnalyzedDom0Configuration[clusterDom0s.size()];
      int index = 0;
      for (Dom0 dom0 : clusterDom0s.values()) {
        dom0s[index++] = new AnalyzedDom0Configuration(clusterConfiguration, dom0);
      }
      assert index == size : "index != size: " + index + " != " + size;
      analyzedDom0Configurations = new UnmodifiableArrayList<>(dom0s);
    }
  }

  /**
   * Gets the cluster configuration that is analyzed.
   */
  public ClusterConfiguration getClusterConfiguration() {
    return clusterConfiguration;
  }

  /**
   * Gets the unmodifiable list of analyzed Dom0 configuration results.
   */
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public List<AnalyzedDom0Configuration> getAnalyzedDom0Configurations() {
    return analyzedDom0Configurations;
  }

  /**
   * This convience method will obtain all the different results.
   * This may be useful by heuristics that weigh the state by
   * all the results.
   *
   * @return true if more results are wanted, or false to receive no more results.
   */
  public boolean getAllResults(ResultHandler<Object> resultHandler, AlertLevel minimumAlertLevel) {
    for (AnalyzedDom0Configuration dom0 : getAnalyzedDom0Configurations()) {
      if (!dom0.getAllResults(resultHandler, minimumAlertLevel)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines if this is optimal, meaning all results have AlertLevel of NONE.
   */
  @SuppressWarnings({"unchecked"})
  public boolean isOptimal() {
    boolean[] isOptimal = {true};
    getAllResults(
        (Result<?> result) -> {
          assert result.getAlertLevel() != AlertLevel.NONE : "result.alertLevel should not be NONE";
          assert isOptimal[0] : "isOptimal[0] is false, handleResult called more than once";
          isOptimal[0] = false;
          return false;
        },
        AlertLevel.LOW
    );
    return isOptimal[0];
  }

  /**
   * Determines if this has at least one result with AlertLevel of CRITICAL.
   */
  @SuppressWarnings({"unchecked"})
  public boolean hasCritical() {
    boolean[] hasCritical = new boolean[1];
    getAllResults(
        (Result<?> result) -> {
          assert result.getAlertLevel() == AlertLevel.CRITICAL : "result.alertLevel should be CRITICAL but it is " + result.getAlertLevel();
          assert !hasCritical[0] : "hasCritical[0] is true, handleResult called more than once";
          hasCritical[0] = true;
          return false;
        },
        AlertLevel.CRITICAL
    );
    return hasCritical[0];
  }
}
