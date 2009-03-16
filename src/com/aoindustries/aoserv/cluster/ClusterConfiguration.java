/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A ClusterConfiguration contains one possible configuration of a cluster.  The configuration
 * consists of a mapping between virtual resources and the physical layer.  This includes
 * the following:
 *     DomU onto primary Dom0
 *     DomU onto secondary Dom0
 *     DomUDisk onto a set of primary physical volumes
 *     DomUDisk onto a set of secondary physical volumes
 *
 * The heap space used should be as small as possible to allow the maximum number of possible configurations
 * to be explored.
 *
 * Everything in ClusterConfiguration is not thread-safe, if using from multiple
 * threads, external synchronization is required.
 *
 * DomU VMs may only be allocated to Dom0 machines in the same cluster.
 *
 * @author  AO Industries, Inc.
 */
public class ClusterConfiguration implements Comparable<ClusterConfiguration>, Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Gets an unmodifiable list that combines the existing list with the new object
     * If the existing list is empty, will use Collections.singletonList, otherwise
     * creates a new UnmodifiableArrayList.
     */
    static <V extends Comparable<V>> List<V> addToUnmodifiableList(Class<V> clazz, List<V> existingList, V newValue) {
        int size = existingList.size();
        if(size==0) return Collections.singletonList(newValue);
        V[] newArray = (V[])Array.newInstance(clazz, size+1);
        newArray = existingList.toArray(newArray);
        newArray[size] = newValue;
        //Arrays.sort(newArray);
        return new UnmodifiableArrayList<V>(newArray);
    }

    /**
     * Replaces an existing entry in an unmodifiable list.  If the list only has a single
     * entry, will use Collections.singletonList, otherwise creates a new UnmodifiableArrayList.
     */
    static <V extends Comparable<V>> List<V> replaceInUnmodifiableList(Class<V> clazz, List<V> existingList, int index, V newValue) {
        int size = existingList.size();
        assert size!=0 : "List is empty";
        if(size==1) {
            assert index==0 : "List size is 1 but index!=0: "+index;
            return Collections.singletonList(newValue);
        }
        assert index>=0 && index<size : "Index out of range: "+index;
        V[] newArray = (V[])Array.newInstance(clazz, size);
        newArray = existingList.toArray(newArray);
        newArray[index] = newValue;
        //Arrays.sort(newArray);
        return new UnmodifiableArrayList<V>(newArray);
    }

    private static int computeHashCode(Cluster cluster, List<DomUConfiguration> unmodifiableDomUConfigurations) {
        return 31*cluster.hashCode() + unmodifiableDomUConfigurations.hashCode();
    }

    // These are here just for generic-type-specific versions
    private static final List<DomUConfiguration> emptyDomUConfigurationList = Collections.emptyList();
    private static final List<DomUDiskConfiguration> emptyDomUDiskConfigurationList = Collections.emptyList();

    final Cluster cluster;
    final List<DomUConfiguration> unmodifiableDomUConfigurations;
    transient private int hashCode;

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
    public List<DomUConfiguration> getDomUConfigurations() {
        return unmodifiableDomUConfigurations;
    }

    private static boolean contains(List<DomUConfiguration> domUConfigurations, DomU domU) {
        for(DomUConfiguration domUConfiguration : domUConfigurations) {
            if(domUConfiguration.domU==domU) return true;
        }
        return false;
    }

    public ClusterConfiguration addDomUConfiguration(DomU domU, Dom0 primaryDom0, Dom0 secondaryDom0) {
        // Make sure DomU not already added
        assert !contains(unmodifiableDomUConfigurations, domU) : this+": DomU already exists in this configuration: "+domU;

        assert domU.clusterName.equals(cluster.name) : this+": DomU is not part of this cluster: "+domU;
        assert primaryDom0.clusterName.equals(cluster.name) : this+": primaryDom0 is not part of this cluster: "+primaryDom0;
        assert secondaryDom0.clusterName.equals(cluster.name) : this+": secondaryDom0 is not part of this cluster: "+secondaryDom0;
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
        for(DomUDiskConfiguration domUDiskConfiguration : domUDiskConfigurations) {
            if(domUDiskConfiguration.domUDisk==domUDisk) return true;
        }
        return false;
    }

    private static boolean allDom0Match(List<PhysicalVolumeConfiguration> physicalVolumeConfigurations, Dom0 dom0) {
        for(PhysicalVolumeConfiguration pvc : physicalVolumeConfigurations) {
            if(
                !pvc.physicalVolume.clusterName.equals(dom0.clusterName)
                || !pvc.physicalVolume.dom0Hostname.equals(dom0.hostname)
            ) return false;
        }
        return true;
    }

    /**
     * @param unmodifiablePrimaryPhysicalVolumeConfigurations MUST BE UNMODIFIABLE
     * @param unmodifiableSecondaryPhysicalVolumeConfigurations MUST BE UNMODIFIABLE
     */
    public ClusterConfiguration addDomUDiskConfiguration(
        DomU domU,
        DomUDisk domUDisk,
        List<PhysicalVolumeConfiguration> unmodifiablePrimaryPhysicalVolumeConfigurations,
        List<PhysicalVolumeConfiguration> unmodifiableSecondaryPhysicalVolumeConfigurations
    ) {
        assert domUDisk.clusterName.equals(domU.clusterName) : this+": DomUDisk.clusterName!=DomU.clusterName: "+domUDisk.clusterName+"!="+domU.clusterName;
        assert domUDisk.domUHostname.equals(domU.hostname) : this+": DomUDisk.domUHostname!=DomU.hostname: "+domUDisk.domUHostname+"!="+domU.hostname;

        DomUConfiguration domUConfiguration = null;
        int unmodifiableDomUConfigurationsIndex = 0;
        for(int len=unmodifiableDomUConfigurations.size(); unmodifiableDomUConfigurationsIndex<len; unmodifiableDomUConfigurationsIndex++) {
            DomUConfiguration tDomUConfiguration = unmodifiableDomUConfigurations.get(unmodifiableDomUConfigurationsIndex);
            if(tDomUConfiguration.domU==domU) {
                domUConfiguration = tDomUConfiguration;
                break;
            }
        }
        assert domUConfiguration!=null : this+": DomUConfiguration not found: "+domU;

        // Make sure DomUDisk not already added
        assert !contains(domUConfiguration.unmodifiableDomUDiskConfigurations, domUDisk) : domUConfiguration+": DomUDisk already exists in this configuration: "+domUDisk;

        // Make sure all physical volumes belong to the proper Dom0
        assert allDom0Match(unmodifiablePrimaryPhysicalVolumeConfigurations, domUConfiguration.primaryDom0);
        assert allDom0Match(unmodifiableSecondaryPhysicalVolumeConfigurations, domUConfiguration.secondaryDom0);

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
                            unmodifiablePrimaryPhysicalVolumeConfigurations,
                            unmodifiableSecondaryPhysicalVolumeConfigurations
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
        for(int len=unmodifiableDomUConfigurations.size(); unmodifiableDomUConfigurationsIndex<len; unmodifiableDomUConfigurationsIndex++) {
            DomUConfiguration tDomUConfiguration = unmodifiableDomUConfigurations.get(unmodifiableDomUConfigurationsIndex);
            if(tDomUConfiguration.domU==domU) {
                domUConfiguration = tDomUConfiguration;
                break;
            }
        }
        assert domUConfiguration!=null : this+": DomUConfiguration not found: "+domU;

        List<DomUDiskConfiguration> oldDomUDiskConfigurations = domUConfiguration.unmodifiableDomUDiskConfigurations;
        List<DomUDiskConfiguration> newDomUDiskConfigurations;
        int size = oldDomUDiskConfigurations.size();
        if(size==0) newDomUDiskConfigurations = oldDomUDiskConfigurations;
        else if(size==1) {
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
            for(int c=0;c<size;c++) {
                DomUDiskConfiguration oldDomUDiskConfiguration = oldDomUDiskConfigurations.get(c);
                array[c] = new DomUDiskConfiguration(
                    oldDomUDiskConfiguration.domUDisk,
                    oldDomUDiskConfiguration.secondaryPhysicalVolumeConfigurations,
                    oldDomUDiskConfiguration.primaryPhysicalVolumeConfigurations
                );
            }
            newDomUDiskConfigurations = new UnmodifiableArrayList<DomUDiskConfiguration>(array);
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
     * Moves the secondary to another machine and returns the new ClusterConfiguration.
     */
    public ClusterConfiguration moveSecondary(DomU domU, Dom0 newSecondaryDom0) {
        // Find existing configuration
        DomUConfiguration domUConfiguration = null;
        int unmodifiableDomUConfigurationsIndex = 0;
        for(int len=unmodifiableDomUConfigurations.size(); unmodifiableDomUConfigurationsIndex<len; unmodifiableDomUConfigurationsIndex++) {
            DomUConfiguration tDomUConfiguration = unmodifiableDomUConfigurations.get(unmodifiableDomUConfigurationsIndex);
            if(tDomUConfiguration.domU==domU) {
                domUConfiguration = tDomUConfiguration;
                break;
            }
        }
        assert domUConfiguration!=null : this+": DomUConfiguration not found: "+domU;

        int size = domUConfiguration.unmodifiableDomUDiskConfigurations.size();
        List<DomUDiskConfiguration> newDomUDiskConfigurations;
        if(size==0) {
            newDomUDiskConfigurations = Collections.emptyList();
        } else if(size==1) {
            throw new RuntimeException("TODO: Finish method");
        } else {
            throw new RuntimeException("TODO: Finish method");
        }
        return new ClusterConfiguration(
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
        );
    }

    /**
     * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
     * 
     * @see  #equals(ClusterConfiguration)
     */
    @Override
    public boolean equals(Object O) {
        return O!=null && (O instanceof ClusterConfiguration) && equals((ClusterConfiguration)O);
    }

    /**
     * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
     * 
     * @see  #equals(Object)
     */
    public boolean equals(ClusterConfiguration other) {
        if(this==other) return true;
        if(other==null) return false;
        if(hashCode!=other.hashCode) return false; // hashCode is precomputed so this is a quick check
        if(cluster!=other.cluster) return false;
        {
            int size = unmodifiableDomUConfigurations.size();
            if(size!=other.unmodifiableDomUConfigurations.size()) return false;
            Iterator<DomUConfiguration> myIter = unmodifiableDomUConfigurations.iterator();
            Iterator<DomUConfiguration> otherIter = other.unmodifiableDomUConfigurations.iterator();
            while(myIter.hasNext()) {
                if(!myIter.next().equals(otherIter.next())) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>cluster</li>
     * </ol>
     */
    public int compareTo(ClusterConfiguration other) {
        if(this==other) return 0;
        return cluster.compareTo(other.cluster);
    }
}
