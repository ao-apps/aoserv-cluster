/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * One Xen domU disk configuration.
 *
 * @author  AO Industries, Inc.
 */
public class DomUDiskConfiguration implements Comparable<DomUDiskConfiguration>, Serializable {

    private static final long serialVersionUID = 1L;

    final DomUDisk domUDisk;
    final List<PhysicalVolume> unmodifiablePrimaryPhysicalVolumes;
    final List<PhysicalVolume> unmodifiableSecondaryPhysicalVolumes;

    /**
     * unmodifiablePrimaryPhysicalVolumes and unmodifiableSecondaryPhysicalVolumes MUST BE UNMODIFIABLE.
     */
    DomUDiskConfiguration(
        DomUDisk domUDisk,
        List<PhysicalVolume> unmodifiablePrimaryPhysicalVolumes,
        List<PhysicalVolume> unmodifiableSecondaryPhysicalVolumes
    ) {
        this.domUDisk = domUDisk;
        this.unmodifiablePrimaryPhysicalVolumes = unmodifiablePrimaryPhysicalVolumes;
        this.unmodifiableSecondaryPhysicalVolumes = unmodifiableSecondaryPhysicalVolumes;
    }

    @Override
    public String toString() {
        return domUDisk.toString();
    }

    public DomUDisk getDomUDisk() {
        return domUDisk;
    }
    
    /**
     * Gets the unmodifiable set of physical volumes that back this device.
     */
    public List<PhysicalVolume> getPrimaryPhysicalVolumes() {
        return unmodifiablePrimaryPhysicalVolumes;
    }

    /**
     * Gets the unmodifiable set of physical volumes that back this device.
     */
    public List<PhysicalVolume> getSecondaryPhysicalVolumes() {
        return unmodifiableSecondaryPhysicalVolumes;
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
        if(this==other) return true;
        if(other==null) return false;
        if(domUDisk!=other.domUDisk) return false;
        {
            int size = unmodifiablePrimaryPhysicalVolumes.size();
            if(size!=other.unmodifiablePrimaryPhysicalVolumes.size()) return false;
            Iterator<PhysicalVolume> myIter = unmodifiablePrimaryPhysicalVolumes.iterator();
            Iterator<PhysicalVolume> otherIter = other.unmodifiablePrimaryPhysicalVolumes.iterator();
            while(myIter.hasNext()) {
                if(myIter.next()!=otherIter.next()) return false;
            }
        }
        {
            int size = unmodifiableSecondaryPhysicalVolumes.size();
            if(size!=other.unmodifiableSecondaryPhysicalVolumes.size()) return false;
            Iterator<PhysicalVolume> myIter = unmodifiableSecondaryPhysicalVolumes.iterator();
            Iterator<PhysicalVolume> otherIter = other.unmodifiableSecondaryPhysicalVolumes.iterator();
            while(myIter.hasNext()) {
                if(myIter.next()!=otherIter.next()) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return
            + 37*domUDisk.hashCode()
            + 31*unmodifiablePrimaryPhysicalVolumes.hashCode()
            + unmodifiableSecondaryPhysicalVolumes.hashCode()
        ;
    }

    public int compareTo(DomUDiskConfiguration other) {
        if(this==other) return 0;
        return domUDisk.compareTo(other.domUDisk);
        // Include physical volumes?
    }
}
