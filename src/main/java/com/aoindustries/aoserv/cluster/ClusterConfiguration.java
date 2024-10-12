/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2007-2011, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A ClusterConfiguration contains one possible configuration of a cluster.  The configuration
 * consists of a mapping between virtual resources and the physical layer.  This includes
 * the following:
 * <ul>
 * <li>DomU onto primary Dom0</li>
 * <li>DomU onto secondary Dom0</li>
 * <li>DomUDisk onto a set of primary physical volumes</li>
 * <li>DomUDisk onto a set of secondary physical volumes</li>
 * </ul>
 * <p>
 * The heap space used should be as small as possible to allow the maximum number of possible configurations
 * to be explored.
 * </p>
 * <p>
 * Everything in ClusterConfiguration is not thread-safe, if using from multiple
 * threads, external synchronization is required.
 * </p>
 * <p>
 * DomU VMs may only be allocated to Dom0 machines in the same cluster.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class ClusterConfiguration implements Comparable<ClusterConfiguration>, Serializable {

  private static final long serialVersionUID = 1L;

  private static final boolean USE_ALREADY_CONTAINS = false;

  /**
   * Gets an unmodifiable list that combines the existing list with the new object
   * If the existing list is empty, will use Collections.singletonList, otherwise
   * creates a new UnmodifiableArrayList.
   */
  @SuppressWarnings({"unchecked"})
  static <V extends Comparable<V>> List<V> addToUnmodifiableList(Class<V> clazz, List<V> existingList, V newValue) {
    int size = existingList.size();
    if (size == 0) {
      return Collections.singletonList(newValue);
    }
    V[] newArray = (V[]) Array.newInstance(clazz, size + 1);
    newArray = existingList.toArray(newArray);
    newArray[size] = newValue;
    //Arrays.sort(newArray);
    return new UnmodifiableArrayList<>(newArray);
  }

  /**
   * Replaces an existing entry in an unmodifiable list.  If the list only has a single
   * entry, will use Collections.singletonList, otherwise creates a new UnmodifiableArrayList.
   */
  @SuppressWarnings({"unchecked"})
  static <V extends Comparable<V>> List<V> replaceInUnmodifiableList(Class<V> clazz, List<V> existingList, int index, V newValue) {
    int size = existingList.size();
    assert size != 0 : "List is empty";
    if (size == 1) {
      assert index == 0 : "List size is 1 but index != 0: " + index;
      return Collections.singletonList(newValue);
    }
    assert index >= 0 && index < size : "Index out of range: " + index;
    V[] newArray = (V[]) Array.newInstance(clazz, size);
    newArray = existingList.toArray(newArray);
    newArray[index] = newValue;
    //Arrays.sort(newArray);
    return new UnmodifiableArrayList<>(newArray);
  }

  /**
   * Gets the smallest possible List container to hold the provided collection.
   * It sorts the list and ensures it is unmodifiable.
   */
  @SuppressWarnings({"unchecked"})
  static <V extends Comparable<V>> List<V> getSortedUnmodifiableCopy(Class<V> clazz, List<V> original) {
    int size = original.size();
    if (size == 0) {
      return Collections.emptyList();
    }
    if (size == 1) {
      return Collections.singletonList(original.get(0));
    }
    V[] newArray = (V[]) Array.newInstance(clazz, size);
    newArray = original.toArray(newArray);
    Arrays.sort(newArray);
    return new UnmodifiableArrayList<>(newArray);
  }

  /**
   * Gets the smallest possible List container to hold the provided collection.
   * It ensures it is unmodifiable.
   */
  @SuppressWarnings({"unchecked"})
  static <V extends Comparable<V>> List<V> getUnmodifiableCopy(Class<V> clazz, List<V> original) {
    int size = original.size();
    if (size == 0) {
      return Collections.emptyList();
    }
    if (size == 1) {
      return Collections.singletonList(original.get(0));
    }
    V[] newArray = (V[]) Array.newInstance(clazz, size);
    newArray = original.toArray(newArray);
    return new UnmodifiableArrayList<>(newArray);
  }

  private static int computeHashCode(Cluster cluster, List<DomUConfiguration> unmodifiableDomUConfigurations) {
    return 31 * cluster.hashCode() + unmodifiableDomUConfigurations.hashCode();
  }

  // These are here just for generic-type-specific versions
  private static final List<DomUConfiguration> emptyDomUConfigurationList = Collections.emptyList();
  private static final List<DomUDiskConfiguration> emptyDomUDiskConfigurationList = Collections.emptyList();

  final Cluster cluster;
  final List<DomUConfiguration> unmodifiableDomUConfigurations;
  private transient int hashCode;

  /**
   * Creates a new {@link ClusterConfiguration}.
   */
  public ClusterConfiguration(Cluster cluster) {
    this(cluster, emptyDomUConfigurationList);
  }

  /**
   * unmodifiableDomUConfigurations must be unmodifiable.
   */
  private ClusterConfiguration(Cluster cluster, List<DomUConfiguration> unmodifiableDomUConfigurations) {
    this.cluster = cluster;
    this.unmodifiableDomUConfigurations = unmodifiableDomUConfigurations;
    this.hashCode = computeHashCode(cluster, unmodifiableDomUConfigurations);
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    this.hashCode = computeHashCode(cluster, unmodifiableDomUConfigurations);
  }

  @Override
  public String toString() {
    return cluster.toString();
  }

  public Cluster getCluster() {
    return cluster;
  }

  /**
   * Gets an unmodifiable list of all configured DomUs.
   */
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public List<DomUConfiguration> getDomUConfigurations() {
    return unmodifiableDomUConfigurations;
  }

  /**
   * Gets the cluster configuration for the provided DomU.  To conserve
   * heap space at the expense of more time, this runs in O(n).
   *
   * @return  the DomUConfiguration or null if not found
   */
  public DomUConfiguration getDomUConfiguration(DomU domU) {
    for (DomUConfiguration domUConfiguration : unmodifiableDomUConfigurations) {
      if (domUConfiguration.domU == domU) {
        return domUConfiguration;
      }
    }
    return null;
  }

  private static boolean contains(List<DomUConfiguration> domUConfigurations, DomU domU) {
    for (DomUConfiguration domUConfiguration : domUConfigurations) {
      if (domUConfiguration.domU == domU) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a domU to the configuration.
   */
  public ClusterConfiguration addDomUConfiguration(DomU domU, Dom0 primaryDom0, Dom0 secondaryDom0) {
    // Make sure DomU not already added
    assert !contains(unmodifiableDomUConfigurations, domU) : this + ": DomU already exists in this configuration: " + domU;

    assert domU.clusterName.equals(cluster.name) : this + ": DomU is not part of this cluster: " + domU;
    assert primaryDom0.clusterName.equals(cluster.name) : this + ": primaryDom0 is not part of this cluster: " + primaryDom0;
    assert secondaryDom0.clusterName.equals(cluster.name) : this + ": secondaryDom0 is not part of this cluster: " + secondaryDom0;
    return new ClusterConfiguration(
        cluster,
        addToUnmodifiableList(
            DomUConfiguration.class,
            unmodifiableDomUConfigurations,
            new DomUConfiguration(
                domU,
                primaryDom0,
                secondaryDom0,
                emptyDomUDiskConfigurationList
            )
        )
    );
  }

  private static boolean contains(List<DomUDiskConfiguration> domUDiskConfigurations, DomUDisk domUDisk) {
    for (DomUDiskConfiguration domUDiskConfiguration : domUDiskConfigurations) {
      if (domUDiskConfiguration.domUDisk == domUDisk) {
        return true;
      }
    }
    return false;
  }

  private static boolean allDom0Match(List<PhysicalVolumeConfiguration> physicalVolumeConfigurations, Dom0 dom0) {
    for (PhysicalVolumeConfiguration pvc : physicalVolumeConfigurations) {
      if (
          !pvc.physicalVolume.clusterName.equals(dom0.clusterName)
              || !pvc.physicalVolume.dom0Hostname.equals(dom0.hostname)
      ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Adds a domU disk to the configuration.
   */
  public ClusterConfiguration addDomUDiskConfiguration(
      DomU domU,
      DomUDisk domUDisk,
      List<PhysicalVolumeConfiguration> primaryPhysicalVolumeConfigurations,
      List<PhysicalVolumeConfiguration> secondaryPhysicalVolumeConfigurations
  ) {
    assert domUDisk.clusterName.equals(domU.clusterName) : this + ": DomUDisk.clusterName != DomU.clusterName: " + domUDisk.clusterName + " != " + domU.clusterName;
    assert domUDisk.domUHostname.equals(domU.hostname) : this + ": DomUDisk.domUHostname != DomU.hostname: " + domUDisk.domUHostname + " != " + domU.hostname;

    DomUConfiguration domUConfiguration = null;
    int unmodifiableDomUConfigurationsIndex = 0;
    for (int len = unmodifiableDomUConfigurations.size(); unmodifiableDomUConfigurationsIndex < len; unmodifiableDomUConfigurationsIndex++) {
      DomUConfiguration domUConfigurationTmp = unmodifiableDomUConfigurations.get(unmodifiableDomUConfigurationsIndex);
      if (domUConfigurationTmp.domU == domU) {
        domUConfiguration = domUConfigurationTmp;
        break;
      }
    }
    assert domUConfiguration != null : this + ": DomUConfiguration not found: " + domU;

    // Make sure DomUDisk not already added
    assert !contains(domUConfiguration.unmodifiableDomUDiskConfigurations, domUDisk) : domUConfiguration + ": DomUDisk already exists in this configuration: " + domUDisk;

    // Make a sorted, unmodifiable, defensive copy of the inputs
    List<PhysicalVolumeConfiguration> primaryPvCopy = getSortedUnmodifiableCopy(PhysicalVolumeConfiguration.class, primaryPhysicalVolumeConfigurations);
    List<PhysicalVolumeConfiguration> secondaryPvCopy = getSortedUnmodifiableCopy(PhysicalVolumeConfiguration.class, secondaryPhysicalVolumeConfigurations);

    // Make sure all physical volumes belong to the proper Dom0
    assert allDom0Match(primaryPvCopy, domUConfiguration.primaryDom0);
    assert allDom0Match(secondaryPvCopy, domUConfiguration.secondaryDom0);

    return new ClusterConfiguration(
        cluster,
        replaceInUnmodifiableList(
            DomUConfiguration.class,
            unmodifiableDomUConfigurations,
            unmodifiableDomUConfigurationsIndex,
            new DomUConfiguration(
                domUConfiguration.domU,
                domUConfiguration.primaryDom0,
                domUConfiguration.secondaryDom0,
                addToUnmodifiableList(
                    DomUDiskConfiguration.class,
                    domUConfiguration.unmodifiableDomUDiskConfigurations,
                    new DomUDiskConfiguration(
                        domUDisk,
                        primaryPvCopy,
                        secondaryPvCopy
                    )
                )
            )
        )
    );
  }

  /**
   * Swaps the primary and secondary for the provided DomU and returns the new cluster configuration.
   */
  public ClusterConfiguration liveMigrate(DomU domU) {
    // Find existing configuration
    DomUConfiguration domUConfiguration = null;
    int unmodifiableDomUConfigurationsIndex = 0;
    for (int len = unmodifiableDomUConfigurations.size(); unmodifiableDomUConfigurationsIndex < len; unmodifiableDomUConfigurationsIndex++) {
      DomUConfiguration domUConfigurationTmp = unmodifiableDomUConfigurations.get(unmodifiableDomUConfigurationsIndex);
      if (domUConfigurationTmp.domU == domU) {
        domUConfiguration = domUConfigurationTmp;
        break;
      }
    }
    assert domUConfiguration != null : this + ": DomUConfiguration not found: " + domU;

    List<DomUDiskConfiguration> oldDomUDiskConfigurations = domUConfiguration.unmodifiableDomUDiskConfigurations;
    List<DomUDiskConfiguration> newDomUDiskConfigurations;
    int size = oldDomUDiskConfigurations.size();
    if (size == 0) {
      newDomUDiskConfigurations = oldDomUDiskConfigurations;
    } else if (size == 1) {
      // Swap single
      DomUDiskConfiguration oldDomUDiskConfiguration = oldDomUDiskConfigurations.get(0);
      newDomUDiskConfigurations = Collections.singletonList(
          new DomUDiskConfiguration(
              oldDomUDiskConfiguration.domUDisk,
              oldDomUDiskConfiguration.secondaryPhysicalVolumeConfigurations,
              oldDomUDiskConfiguration.primaryPhysicalVolumeConfigurations
          )
      );
    } else {
      // Build new ArrayList
      DomUDiskConfiguration[] array = new DomUDiskConfiguration[size];
      for (int c = 0; c < size; c++) {
        DomUDiskConfiguration oldDomUDiskConfiguration = oldDomUDiskConfigurations.get(c);
        array[c] = new DomUDiskConfiguration(
            oldDomUDiskConfiguration.domUDisk,
            oldDomUDiskConfiguration.secondaryPhysicalVolumeConfigurations,
            oldDomUDiskConfiguration.primaryPhysicalVolumeConfigurations
        );
      }
      newDomUDiskConfigurations = new UnmodifiableArrayList<>(array);
    }
    return new ClusterConfiguration(
        cluster,
        replaceInUnmodifiableList(
            DomUConfiguration.class,
            unmodifiableDomUConfigurations,
            unmodifiableDomUConfigurationsIndex,
            new DomUConfiguration(
                domU,
                domUConfiguration.secondaryDom0,
                domUConfiguration.primaryDom0,
                newDomUDiskConfigurations
            )
        )
    );
  }

  /**
   * Moves the secondary to another machine if it is possible to map all of the extents for the DomUDisks onto free physical
   * volumes in Dom0.
   * <p>
   * Because there can be many mappings between DomUDisk and PhysicalVolumes, in factorial combinations,
   * this method has a large impact on the branch factor for the cluster optimizer.  However, it also
   * affects which solutions may be found or transitioned through in the path to a solution.
   * </p>
   * <p>
   * This implementation is meant to be as simple as possible.  It focuses on reducing the search space
   * while possibly missing some valid configurations.  It works as follows:
   * </p>
   * <ol>
   *   <li>Make sure all DomUDisk have the same minspeed - error otherwise.</li>
   *   <li>Find all unallocated physical volumes, sort by speed, device, partition</li>
   *   <li>Work through each Dom0Disk as a starting point
   *     <ol type="a">
   *       <li>Allocate all the extents of the free physical volumes in order on each Dom0Disk in order until VM mapped</li>
   *       <li>If mapping complete add to results (avoid allocation to exactly equal resources in exactly equal ways)</li>
   *       <li>If mapping incomplete return results found</li>
   *     </ol>
   *   </li>
   * </ol>
   * <p>
   * In the future, a more advanced configuration could try to reduce the combinations while (hopefully) not losing any possible solution by:
   * </p>
   * <ol>
   *   <li>Always allocating DomUDisks of the same min speed, extents, and weight in order by device</li>
   *   <li>Always allocating to the physical volumes in order by speed, device, partition</li>
   *   <li>Always allocating from the first unused physical volumes on a single Dom0Disk</li>
   *   <li>Always consuming as many of the physical volumes in a single Dom0Disk as possible</li>
   *   <li>Not returning multiple results onto different Dom0Disk that have the same speed, size, and overall allocation</li>
   * </ol>
   *
   * @return  the new configuration(s)
   */
  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public Iterable<ClusterConfiguration> moveSecondary(DomU domU, Dom0 newSecondaryDom0) {
    // Find existing configuration
    DomUConfiguration domUConfiguration = null;
    int unmodifiableDomUConfigurationsIndex = 0;
    for (int len = unmodifiableDomUConfigurations.size(); unmodifiableDomUConfigurationsIndex < len; unmodifiableDomUConfigurationsIndex++) {
      DomUConfiguration domUConfigurationTmp = unmodifiableDomUConfigurations.get(unmodifiableDomUConfigurationsIndex);
      if (domUConfigurationTmp.domU == domU) {
        domUConfiguration = domUConfigurationTmp;
        break;
      }
    }
    assert domUConfiguration != null : this + ": DomUConfiguration not found: " + domU;

    Map<String, DomUDisk> domUDisks = domU.getDomUDisks();
    Iterator<Map.Entry<String, DomUDisk>> domUDisksIter = domUDisks.entrySet().iterator();
    if (!domUDisksIter.hasNext()) {
      // Short-cut if domU has no disks
      List<DomUDiskConfiguration> newDomUDiskConfigurations = Collections.emptyList();
      return Collections.singletonList(
          new ClusterConfiguration(
              cluster,
              replaceInUnmodifiableList(
                  DomUConfiguration.class,
                  unmodifiableDomUConfigurations,
                  unmodifiableDomUConfigurationsIndex,
                  new DomUConfiguration(
                      domU,
                      domUConfiguration.primaryDom0,
                      newSecondaryDom0,
                      newDomUDiskConfigurations
                  )
              )
          )
      );
    }

    // Make sure all DomUDisk have the same minspeed
    DomUDisk firstDomUDisk = domUDisksIter.next().getValue();
    int firstMinSpeed = firstDomUDisk.minimumDiskSpeed;
    while (domUDisksIter.hasNext()) {
      DomUDisk nextDomUDisk = domUDisksIter.next().getValue();
      int nextMinSpeed = nextDomUDisk.minimumDiskSpeed;
      if (nextMinSpeed != firstMinSpeed) {
        throw new AssertionError("DomUDisks have different minimum speeds: " + firstDomUDisk + "=" + firstMinSpeed + " while " + nextDomUDisk + "=" + nextMinSpeed);
      }
    }

    // Find all unallocated physical volumes
    SortedMap<Dom0Disk, List<PhysicalVolume>> unallocatedDom0Disks = new TreeMap<>(); // Natural sort of Dom0Disk is by speed then device
    for (Dom0Disk dom0Disk : newSecondaryDom0.unmodifiableDom0Disks.values()) {
      for (PhysicalVolume physicalVolume : dom0Disk.unmodifiablePhysicalVolumes.values()) {
        // Find if allocated
        boolean allocated = false;
        ALLOCATED: for (DomUConfiguration duc : unmodifiableDomUConfigurations) {
          if (duc.primaryDom0 == newSecondaryDom0) {
            // Primary matches
            for (DomUDiskConfiguration dudc : duc.unmodifiableDomUDiskConfigurations) {
              for (PhysicalVolumeConfiguration pvc : dudc.primaryPhysicalVolumeConfigurations) {
                if (pvc.physicalVolume == physicalVolume) {
                  allocated = true;
                  break ALLOCATED;
                }
              }
            }
          } else if (duc.secondaryDom0 == newSecondaryDom0) {
            // Secondary matches
            for (DomUDiskConfiguration dudc : duc.unmodifiableDomUDiskConfigurations) {
              for (PhysicalVolumeConfiguration pvc : dudc.secondaryPhysicalVolumeConfigurations) {
                if (pvc.physicalVolume == physicalVolume) {
                  allocated = true;
                  break ALLOCATED;
                }
              }
            }
          }
        }
        if (!allocated) {
          //SortedMap<Dom0Disk, SortedSet<PhysicalVolume>> unallocatedPhysicalVolumes = new TreeMap<>(); // Natural sort of Dom0Disk is by speed then device
          List<PhysicalVolume> unallocatedPhysicalVolumes = unallocatedDom0Disks.get(dom0Disk);
          if (unallocatedPhysicalVolumes == null) {
            unallocatedDom0Disks.put(dom0Disk, unallocatedPhysicalVolumes = new ArrayList<>());
          }
          unallocatedPhysicalVolumes.add(physicalVolume);
        }
      }
    }
    // Sort by partition number
    for (List<PhysicalVolume> sortMe : unallocatedDom0Disks.values()) {
      Collections.sort(sortMe);
    }

    int size = unallocatedDom0Disks.size();
    if (size == 0) {
      // No free physical volumes
      return Collections.emptyList();
    }
    List<ClusterConfiguration> mappedConfigurations = new ArrayList<>();
    int alreadyContainsCount = 0;
    // Reused on inner loop
    List<DomUDiskConfiguration> newDomUDiskConfigurations = new ArrayList<>();
    List<PhysicalVolumeConfiguration> secondaryPhysicalVolumeConfigurations = new ArrayList<>();
    // Work through each Dom0Disk as a starting point
    List<Dom0Disk> unallocatedDom0DisksList = new ArrayList<>(unallocatedDom0Disks.keySet());
    START_DISK:
    for (int startDiskIndex = 0; startDiskIndex < size; startDiskIndex++) {
      // These are all used to iterate through the physical volumes during allocation
      int currentDiskIndex = startDiskIndex;
      Dom0Disk currentDom0Disk = unallocatedDom0DisksList.get(currentDiskIndex);
      assert currentDom0Disk != null : "dom0Disk is null";
      List<PhysicalVolume> currentPhysicalVolumes = unallocatedDom0Disks.get(currentDom0Disk);
      assert currentPhysicalVolumes != null : "physicalVolumes is null";
      int currentPhysicalVolumeIndex = 0;
      PhysicalVolume currentPhysicalVolume = currentPhysicalVolumes.get(currentPhysicalVolumeIndex);
      long currentPhysicalVolumeExtentsRemaing = currentPhysicalVolume.extents;

      // Allocate all the extents of the free physical volumes in order on each Dom0Disk in order until VM mapped
      newDomUDiskConfigurations.clear();
      List<DomUDiskConfiguration> domUDiskConfigurations = domUConfiguration.unmodifiableDomUDiskConfigurations;
      DOMU_DISK:
      for (int domUDiskConfigurationsIndex = 0, domUDiskConfigurationsSize = domUDiskConfigurations.size();
          domUDiskConfigurationsIndex < domUDiskConfigurationsSize;
          domUDiskConfigurationsIndex++
      ) {
        DomUDiskConfiguration domUDiskConfiguration = domUDiskConfigurations.get(domUDiskConfigurationsIndex);
        DomUDisk domUDisk = domUDiskConfiguration.domUDisk;
        secondaryPhysicalVolumeConfigurations.clear();
        long domUDiskAllocationRemaining = domUDisk.extents;

        while (true) {
          // Add to domUDisk
          long allocatingExtents = currentPhysicalVolumeExtentsRemaing < domUDiskAllocationRemaining ? currentPhysicalVolumeExtentsRemaing : domUDiskAllocationRemaining;
          secondaryPhysicalVolumeConfigurations.add(
              PhysicalVolumeConfiguration.newInstance(
                  currentPhysicalVolume,
                  domUDisk.extents - domUDiskAllocationRemaining,
                  currentPhysicalVolume.extents - currentPhysicalVolumeExtentsRemaing,
                  allocatingExtents
              )
          );
          domUDiskAllocationRemaining -= allocatingExtents;
          assert domUDiskAllocationRemaining >= 0 : "domUDiskAllocationRemaining<0: " + domUDiskAllocationRemaining;

          // Update iteration of physical volumes
          currentPhysicalVolumeExtentsRemaing -= allocatingExtents;
          assert currentPhysicalVolumeExtentsRemaing >= 0 : "currentPhysicalVolumeExtentsRemaing<0: " + currentPhysicalVolumeExtentsRemaing;
          boolean hasMorePhysicalExtents;
          if (currentPhysicalVolumeExtentsRemaing == 0) {
            // Update to point to the next physical volume
            currentPhysicalVolumeIndex++;
            if (currentPhysicalVolumeIndex < currentPhysicalVolumes.size()) {
              currentPhysicalVolume = currentPhysicalVolumes.get(currentPhysicalVolumeIndex);
              currentPhysicalVolumeExtentsRemaing = currentPhysicalVolume.extents;
              hasMorePhysicalExtents = true;
            } else {
              currentDiskIndex++;
              if (currentDiskIndex < unallocatedDom0DisksList.size()) {
                currentDom0Disk = unallocatedDom0DisksList.get(currentDiskIndex);
                assert currentDom0Disk != null : "dom0Disk is null";
                currentPhysicalVolumes = unallocatedDom0Disks.get(currentDom0Disk);
                assert currentPhysicalVolumes != null : "physicalVolumes is null";
                currentPhysicalVolumeIndex = 0;
                currentPhysicalVolume = currentPhysicalVolumes.get(currentPhysicalVolumeIndex);
                currentPhysicalVolumeExtentsRemaing = currentPhysicalVolume.extents;
                hasMorePhysicalExtents = true;
              } else {
                hasMorePhysicalExtents = false;
              }
            }
          } else {
            hasMorePhysicalExtents = true;
          }

          // If mapping complete add to domUConfigurations
          if (domUDiskAllocationRemaining <= 0) {
            // This must be the last DomUDisk to be accepted
            if (!hasMorePhysicalExtents && domUDiskConfigurationsIndex < (domUDiskConfigurationsSize - 1)) {
              // More disks but no room left, can't allocate any more
              break START_DISK;
            }
            newDomUDiskConfigurations.add(
                new DomUDiskConfiguration(
                    domUDisk,
                    domUDiskConfiguration.primaryPhysicalVolumeConfigurations,
                    getSortedUnmodifiableCopy(PhysicalVolumeConfiguration.class, secondaryPhysicalVolumeConfigurations)
                )
            );
            continue DOMU_DISK;
          }

          // If mapping incomplete return results found
          if (!hasMorePhysicalExtents) {
            break START_DISK;
          }
        }
      }
      // avoid allocation to exactly equal resources in exactly equal ways
      boolean alreadyContains = false;
      if (USE_ALREADY_CONTAINS) {
        for (ClusterConfiguration alreadyMappedConfig : mappedConfigurations) {
          DomUConfiguration alreadyMappedDomUConfig = alreadyMappedConfig.getDomUConfigurations().get(unmodifiableDomUConfigurationsIndex);
          assert alreadyMappedDomUConfig.getDomU() == domU : "alreadyMappedDomUConfig.domU != domU";
          List<DomUDiskConfiguration> alreadyMappedDiskConfigs = alreadyMappedDomUConfig.getDomUDiskConfigurations();
          int mappedSize = alreadyMappedDiskConfigs.size();
          if (mappedSize == newDomUDiskConfigurations.size()) {
            alreadyContains = true;
            for (int c = 0; c < mappedSize; c++) {
              DomUDiskConfiguration alreadyMappedDiskConfig = alreadyMappedDiskConfigs.get(c);
              DomUDiskConfiguration newDiskConfig = newDomUDiskConfigurations.get(c);
              assert alreadyMappedDiskConfig.domUDisk == newDiskConfig.domUDisk : "alreadyMappedDiskConfig.domUDisk != newDiskConfig.domUDisk";
              List<PhysicalVolumeConfiguration> alreadyMappedPvConfigs = alreadyMappedDiskConfig.secondaryPhysicalVolumeConfigurations;
              List<PhysicalVolumeConfiguration> newPvConfigs = newDiskConfig.secondaryPhysicalVolumeConfigurations;
              int pvSize = alreadyMappedPvConfigs.size();
              if (pvSize == newPvConfigs.size()) {
                for (int pvIndex = 0; pvIndex < pvSize; pvIndex++) {
                  // TODO: Also consider total extents mapped (interaction with other VMs)?
                  // TODO: Consider match by total extents and those of other VMs?
                  // TODO: Or, just randomize the order???
                  PhysicalVolumeConfiguration alreadyMappedPvConfig = alreadyMappedPvConfigs.get(pvIndex);
                  PhysicalVolumeConfiguration newPvConfig = newPvConfigs.get(pvIndex);
                  if (
                      alreadyMappedPvConfig.getFirstLogicalExtent() != newPvConfig.getFirstLogicalExtent()
                          || alreadyMappedPvConfig.getFirstPhysicalExtent() != newPvConfig.getFirstPhysicalExtent()
                          || alreadyMappedPvConfig.getExtents() != newPvConfig.getExtents()
                  ) {
                    alreadyContains = false;
                    break;
                  }
                }
              } else {
                alreadyContains = false;
                break;
              }
            }
            if (alreadyContains) {
              break;
            }
          }
        }
      }
      // If mapping complete add to results
      if (alreadyContains) {
        alreadyContainsCount++;
      } else {
        mappedConfigurations.add(
            new ClusterConfiguration(
                cluster,
                replaceInUnmodifiableList(
                    DomUConfiguration.class,
                    unmodifiableDomUConfigurations,
                    unmodifiableDomUConfigurationsIndex,
                    new DomUConfiguration(
                        domU,
                        domUConfiguration.primaryDom0,
                        newSecondaryDom0,
                        getUnmodifiableCopy(DomUDiskConfiguration.class, newDomUDiskConfigurations)
                    )
                )
            )
        );
      }
    }

    if (USE_ALREADY_CONTAINS) {
      synchronized (mappedConfigurationsStatsLock) {
        totalMapped += mappedConfigurations.size();
        totalAlreadyContains += alreadyContainsCount;
        mappedCount++;
        long currentTime = System.currentTimeMillis();
        long timeSince = currentTime - lastDisplayTime;
        if (timeSince < 0 || timeSince >= 60000) {
          System.out.println(
              "        totalMapped:" + totalMapped
                  + " totalAlreadyContains:" + totalAlreadyContains
                  + " mappedCount:" + mappedCount
                  + " averageMapped:" + ((float) totalMapped / (float) mappedCount)
                  + " averageAlreadyContains:" + ((float) totalAlreadyContains / (float) mappedCount)
          );
          //mappedCount = 0;
          //totalMapped = 0;
          //totalAlreadyContains = 0;
          lastDisplayTime = currentTime;
        }
      }
    }
    return mappedConfigurations;
  }

  private static final Object mappedConfigurationsStatsLock = new Object();
  private static long mappedCount;
  private static long totalMapped;
  private static long totalAlreadyContains;
  private static long lastDisplayTime = System.currentTimeMillis();

  /**
   * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
   *
   * @see  #equals(ClusterConfiguration)
   */
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof ClusterConfiguration) && equals((ClusterConfiguration) obj);
  }

  /**
   * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
   *
   * @see  #equals(Object)
   */
  public boolean equals(ClusterConfiguration other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (hashCode != other.hashCode) {
      // hashCode is precomputed so this is a quick check
      return false;
    }
    if (cluster != other.cluster) {
      return false;
    }
      {
        int size = unmodifiableDomUConfigurations.size();
        if (size != other.unmodifiableDomUConfigurations.size()) {
          return false;
        }
        Iterator<DomUConfiguration> myIter = unmodifiableDomUConfigurations.iterator();
        Iterator<DomUConfiguration> otherIter = other.unmodifiableDomUConfigurations.iterator();
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
    return hashCode;
  }

  /**
   * Sorted ascending.  By:
   * <ol>
   *   <li>cluster</li>
   * </ol>
   */
  @Override
  public int compareTo(ClusterConfiguration other) {
    if (this == other) {
      return 0;
    }
    return cluster.compareTo(other.cluster);
  }
}
