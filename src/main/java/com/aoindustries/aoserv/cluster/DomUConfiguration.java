/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2007-2011, 2020, 2021, 2022, 2025, 2026  AO Industries, Inc.
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

package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * One Xen domU configuration.
 *
 * @author  AO Industries, Inc.
 */
public class DomUConfiguration implements Comparable<DomUConfiguration>, Serializable {

  private static final long serialVersionUID = 1L;

  final DomU domU;
  final Dom0 primaryDom0;
  final Dom0 secondaryDom0;
  final List<DomUDiskConfiguration> unmodifiableDomUDiskConfigurations;

  /**
   * {@code unmodifiableDomUDiskConfigurations} MUST BE UNMODIFIABLE.
   */
  DomUConfiguration(
      DomU domU,
      Dom0 primaryDom0,
      Dom0 secondaryDom0,
      List<DomUDiskConfiguration> unmodifiableDomUDiskConfigurations
  ) {
    this.domU = domU;

    assert primaryDom0 != null : "primaryDom0 is null";
    assert primaryDom0.clusterName.equals(domU.clusterName) : "primaryDom0.clusterName != domU.clusterName";
    this.primaryDom0 = primaryDom0;

    assert secondaryDom0 != null : "secondaryDom0 is null";
    assert primaryDom0 != secondaryDom0 : "primaryDom0 == secondaryDom0: " + primaryDom0;
    assert secondaryDom0.clusterName.equals(domU.clusterName) : "secondaryDom0.clusterName != domU.clusterName";
    this.secondaryDom0 = secondaryDom0;

    this.unmodifiableDomUDiskConfigurations = unmodifiableDomUDiskConfigurations;
  }

  @Override
  public String toString() {
    return domU.toString();
  }

  public DomU getDomU() {
    return domU;
  }

  /**
   * Gets the current primary Dom0 for this machine.
   */
  public Dom0 getPrimaryDom0() {
    return primaryDom0;
  }

  /**
   * Gets the current secondary Dom0 for this machine.
   */
  public Dom0 getSecondaryDom0() {
    return secondaryDom0;
  }

  /**
   * Gets the unmodifiable list of disk configurations currently set on this domU.
   */
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public List<DomUDiskConfiguration> getDomUDiskConfigurations() {
    return unmodifiableDomUDiskConfigurations;
  }

  /**
   * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
   *
   * @see  DomUConfiguration#equals(DomUConfiguration)
   */
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof DomUConfiguration) && equals((DomUConfiguration) obj);
  }

  /**
   * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
   *
   * @see  DomUConfiguration#equals(Object)
   */
  public boolean equals(DomUConfiguration other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (domU != other.domU) {
      return false;
    }
    if (primaryDom0 != other.primaryDom0) {
      return false;
    }
    if (secondaryDom0 != other.secondaryDom0) {
      return false;
    }
    {
      int size = unmodifiableDomUDiskConfigurations.size();
      if (size != other.unmodifiableDomUDiskConfigurations.size()) {
        return false;
      }
      Iterator<DomUDiskConfiguration> myIter = unmodifiableDomUDiskConfigurations.iterator();
      Iterator<DomUDiskConfiguration> otherIter = other.unmodifiableDomUDiskConfigurations.iterator();
      while (myIter.hasNext()) {
        if (!myIter.next().equals(otherIter.next())) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return
        73 * domU.hashCode()
            + 37 * primaryDom0.hashCode()
            + 31 * secondaryDom0.hashCode()
            + unmodifiableDomUDiskConfigurations.hashCode();
  }

  @Override
  public int compareTo(DomUConfiguration other) {
    if (this == other) {
      return 0;
    }
    return domU.compareTo(other.domU);
  }
}
