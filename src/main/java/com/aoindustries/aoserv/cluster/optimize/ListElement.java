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

public class ListElement implements Comparable<ListElement> {

  /**
   * This is null for the first element in the list.
   */
  final ListElement previous;

  /**
   * This is null for the first element in the list.
   */
  final Transition transition;

  final int pathLen;

  /**
   * The configuration after the transition.
   */
  final ClusterConfiguration clusterConfiguration;

  final double heuristic;

  ListElement(
    ListElement previous,
    Transition transition,
    ClusterConfiguration clusterConfiguration,
    double heuristic
  ) {
    this.previous = previous;
    this.transition = transition;
    this.pathLen = previous == null ? 0 : (previous.pathLen+1);
    assert clusterConfiguration != null : "clusterConfiguration is null";
    this.clusterConfiguration = clusterConfiguration;
    this.heuristic = heuristic;
  }

  /**
   * Sorted by heuristic value, lowest to highest.
   */
  @Override
  public int compareTo(ListElement other) {
    double h1 = heuristic;
    double h2 = other.heuristic;
    if (h1<h2) {
      return -1;
    }
    if (h2<h1) {
      return 1;
    }
    return 0;
  }

  public ListElement getPrevious() {
    return previous;
  }

  /**
   * Gets the transition, this is null for the initial state.
   */
  public Transition getTransition() {
    return transition;
  }

  public int getPathLen() {
    return pathLen;
  }

  public ClusterConfiguration getClusterConfiguration() {
    return clusterConfiguration;
  }

  public double getHeuristic() {
    return heuristic;
  }
}
