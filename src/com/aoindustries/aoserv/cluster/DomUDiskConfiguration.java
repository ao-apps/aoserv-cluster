/*
 * Copyright 2007-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.List;

/**
 * One Xen domU disk configuration.
 *
 * @author  AO Industries, Inc.
 */
public class DomUDiskConfiguration implements Comparable<DomUDiskConfiguration>, Serializable {

    private static final long serialVersionUID = 1L;

    final DomUDisk domUDisk;
    final List<PhysicalVolumeConfiguration> primaryPhysicalVolumeConfigurations;
    final List<PhysicalVolumeConfiguration> secondaryPhysicalVolumeConfigurations;

    /**
     * Used by assertions.
     */
    private static boolean isSorted(List<PhysicalVolumeConfiguration> physicalVolumeConfigurations) {
        int size = physicalVolumeConfigurations.size();
        for(int c=1; c<size; c++) {
            if(physicalVolumeConfigurations.get(c-1).compareTo(physicalVolumeConfigurations.get(c))>0) return false;
        }
        return true;
    }

    /**
     * Used by assertions.
     */
    private static boolean totalExtentsMatch(long requiredExtents, List<PhysicalVolumeConfiguration> physicalVolumeConfigurations) {
        long totalExtents = 0;
        for(PhysicalVolumeConfiguration physicalVolumeConfiguration : physicalVolumeConfigurations) {
            totalExtents += physicalVolumeConfiguration.getExtents();
        }
        return requiredExtents==totalExtents;
    }

    /**
     * Used by assertions.
     */
    private static boolean overlaps(List<PhysicalVolumeConfiguration> physicalVolumeConfigurations) {
        int size = physicalVolumeConfigurations.size();
        for(int c=1;c<size;c++) {
            PhysicalVolumeConfiguration pv1 = physicalVolumeConfigurations.get(c-1);
            for(int d=c; d<size; d++) {
                PhysicalVolumeConfiguration pv2 = physicalVolumeConfigurations.get(d);
                if(pv1.overlaps(pv2)) {
                    System.out.println(pv1+" overlaps "+pv2);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Used by assertions.
     */
    private static boolean allSameDom0(List<PhysicalVolumeConfiguration> physicalVolumeConfigurations) {
        int size = physicalVolumeConfigurations.size();
        if(size>1) {
            PhysicalVolume pv = physicalVolumeConfigurations.get(0).physicalVolume;
            String clusterName = pv.clusterName;
            String dom0Hostname = pv.dom0Hostname;
            for(int c=1; c<size; c++) {
                pv = physicalVolumeConfigurations.get(1).physicalVolume;
                if(
                    !clusterName.equals(pv.clusterName)
                    || !dom0Hostname.equals(pv.dom0Hostname)
                ) return false;
            }
        }
        return true;
    }

    /**
     * unmodifiablePrimaryPhysicalVolumes and unmodifiableSecondaryPhysicalVolumes MUST BE UNMODIFIABLE.
     * They must also both be sorted to ensure proper results from hashCode and equals.
     */
    DomUDiskConfiguration(
        DomUDisk domUDisk,
        List<PhysicalVolumeConfiguration> primaryPhysicalVolumeConfigurations,
        List<PhysicalVolumeConfiguration> secondaryPhysicalVolumeConfigurations
    ) {
        assert isSorted(primaryPhysicalVolumeConfigurations) : "primaryPhysicalVolumeConfigurations not sorted";
        assert isSorted(secondaryPhysicalVolumeConfigurations) : "primaryPhysicalVolumeConfigurations not sorted";
        assert totalExtentsMatch(domUDisk.extents, primaryPhysicalVolumeConfigurations) : "primaryPhysicalVolumeConfigurations total extents doesn't match the domUDisk extents: domUDisk="+domUDisk+", domUDisk.extents="+domUDisk.extents+", primaryPhysicalVolumeConfigurations="+primaryPhysicalVolumeConfigurations;
        assert totalExtentsMatch(domUDisk.extents, secondaryPhysicalVolumeConfigurations) : "secondaryPhysicalVolumeConfigurations total extents doesn't match the domUDisk extents: domUDisk="+domUDisk+", domUDisk.extents="+domUDisk.extents+", secondaryPhysicalVolumeConfigurations="+secondaryPhysicalVolumeConfigurations;
        assert !overlaps(primaryPhysicalVolumeConfigurations) : "primaryPhysicalVolumeConfigurations contains overlapping segments";
        assert !overlaps(secondaryPhysicalVolumeConfigurations) : "secondaryPhysicalVolumeConfigurations contains overlapping segments";
        assert allSameDom0(primaryPhysicalVolumeConfigurations) : "not all primaryPhysicalVolumeConfigurations are on the same Dom0";
        assert allSameDom0(secondaryPhysicalVolumeConfigurations) : "not all secondaryPhysicalVolumeConfigurations are on the same Dom0";
        this.domUDisk = domUDisk;
        this.primaryPhysicalVolumeConfigurations = primaryPhysicalVolumeConfigurations;
        this.secondaryPhysicalVolumeConfigurations = secondaryPhysicalVolumeConfigurations;
    }

    @Override
    public String toString() {
        return domUDisk.toString();
    }

    public DomUDisk getDomUDisk() {
        return domUDisk;
    }
    
    /**
     * Gets the unmodifiable sorted list of physical volumes that back this device.
     */
    public List<PhysicalVolumeConfiguration> getPrimaryPhysicalVolumeConfigurations() {
        return primaryPhysicalVolumeConfigurations;
    }

    /**
     * Gets the unmodifiable sorted list of physical volumes that back this device.
     */
    public List<PhysicalVolumeConfiguration> getSecondaryPhysicalVolumeConfigurations() {
        return secondaryPhysicalVolumeConfigurations;
    }

    /**
     * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
     * 
     * @see  #equals(DomUDiskConfiguration)
     */
    @Override
    public boolean equals(Object O) {
        return O!=null && (O instanceof DomUDiskConfiguration) && equals((DomUDiskConfiguration)O);
    }

    /**
     * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
     * 
     * @see  #equals(Object)
     */
    public boolean equals(DomUDiskConfiguration other) {
        return
            this==other
            || (
                other!=null
                && domUDisk==other.domUDisk
                && primaryPhysicalVolumeConfigurations.equals(other.primaryPhysicalVolumeConfigurations)
                && secondaryPhysicalVolumeConfigurations.equals(other.secondaryPhysicalVolumeConfigurations)
            )
        ;
    }

    @Override
    public int hashCode() {
        return
            + 127*domUDisk.hashCode()
            + 31*primaryPhysicalVolumeConfigurations.hashCode()
            + secondaryPhysicalVolumeConfigurations.hashCode()
        ;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>domUDisk</li>
     * </ol>
     */
    @Override
    public int compareTo(DomUDiskConfiguration other) {
        if(this==other) return 0;
        return domUDisk.compareTo(other.domUDisk);
    }
}
