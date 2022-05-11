/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020, 2021, 2022  AO Industries, Inc.
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

/**
 * Stores an AlertLevel, a value, and a textual message.
 *
 * @author  AO Industries, Inc.
 */
public abstract class Result<T> implements Comparable<Result<?>> {

  private final String label;
  private final double deviation;
  private final AlertLevel alertLevel;

  Result(String label, double deviation, AlertLevel alertLevel) {
    assert !(alertLevel != AlertLevel.NONE && deviation <= 0) : "Any result with an alert level > NONE should have a positive, non-zero deviation";
    assert !(alertLevel == AlertLevel.NONE && deviation > 0) : "Any result with an alert level = NONE should have a negative or zero deviation";
    this.label = label;
    this.deviation = deviation;
    this.alertLevel = alertLevel;
  }

  public final String getLabel() {
    return label;
  }

  /**
   * Gets the current value for the resource or <code>null</code> if unavailable.
   */
  public abstract T getValue();

  /**
   * Gets the maximum value for the resource or <code>null</code> if unavailable.
   */
  public abstract T getMaxValue();

  /**
   * Gets the relative amount of devation the value is from the expected/maximum value.
   * If the deviation is otherwise unknown or doesn't make sense for the type of resource,
   * should be 1.0.
   */
  public final double getDeviation() {
    return deviation;
  }

  public final AlertLevel getAlertLevel() {
    return alertLevel;
  }

  /**
   * Sorted by label.
   */
  @Override
  public final int compareTo(Result<?> other) {
    return label.compareTo(other.label);
  }

  @Override
  public final String toString() {
    T value = getValue();
    T maxValue = getMaxValue();
    return
        alertLevel
            + ": " + label
            + " "  + (value == null ? "NA" : value)
            + "/"  + (maxValue == null ? "NA" : maxValue)
            + " "  + deviation;
  }
}
