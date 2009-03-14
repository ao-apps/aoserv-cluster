/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.Map;

/**
 * A physical disk that is used for LVM.  It is split into
 * partitions, each of which is a physical volume.
 *
 * @author  AO Industries, Inc.
 */
public class Dom0Disk implements Comparable<Dom0Disk>, Serializable {

    private static final long serialVersionUID = 2L;

    final String clusterName;
    final String dom0Hostname;
    final String device;
    final RaidType raidType;
    final DiskType diskType;
    final int diskSpeed;
    final Map<Integer,PhysicalVolume> unmodifiablePhysicalVolumes;

    /**
     * unmodifiablePhysicalVolumes MUST BE UNMODIFIABLE
     *
     * @see Cluster#addDom0Disk
     */
    Dom0Disk(
        String clusterName,
        String dom0Hostname,
        String device,
        RaidType raidType,
        DiskType diskType,
        int diskSpeed,
        Map<Integer,PhysicalVolume> unmodifiablePhysicalVolumes
    ) {
        this.clusterName = clusterName;
        this.dom0Hostname = dom0Hostname;
        this.device = device;
        this.raidType = raidType;
        this.diskType = diskType;
        this.diskSpeed = diskSpeed;
        this.unmodifiablePhysicalVolumes = unmodifiablePhysicalVolumes;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getDom0Hostname() {
        return dom0Hostname;
    }

    /**
     * Gets the per-Dom0 unique device name.
     */
    public String getDevice() {
        return device;
    }
    
    public RaidType getRaidType() {
        return raidType;
    }
    
    public DiskType getDiskType() {
        return diskType;
    }

    public int getDiskSpeed() {
        return diskSpeed;
    }

    /**
     * Gets the unmodifable set of physical volumes for this disk.
     */
    public Map<Integer,PhysicalVolume> getPhysicalVolumes() {
        return unmodifiablePhysicalVolumes;
    }

    /**
     * Gets the physical volume for the specified partition number of <code>null</code> if not found
     */
    public PhysicalVolume getPhysicalVolume(Integer partition) {
        return unmodifiablePhysicalVolumes.get(partition);
    }

    @Override
    public String toString() {
        return toString(clusterName, dom0Hostname, device);
    }

    static String toString(String clusterName, String dom0Hostname, String device) {
        return Dom0.toString(clusterName, dom0Hostname)+':'+device;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>clusterName</li>
     *   <li>dom0Hostname</li>
     *   <li>diskSpeed</li>
     *   <li>raidType</li>
     *   <li>diskType</li>
     *   <li>device</li>
     * </ol>
     */
    @Override
    public int compareTo(Dom0Disk other) {
        if(this==other) return 0;

        int diff = clusterName.compareTo(other.clusterName);
        if(diff!=0) return diff;
        
        diff = dom0Hostname.compareTo(other.dom0Hostname);
        if(diff!=0) return diff;

        diff = diskSpeed - other.diskSpeed;
        if(diff!=0) return diff;

        diff = raidType.compareTo(other.raidType);
        if(diff!=0) return diff;

        diff = diskType.compareTo(other.diskType);
        if(diff!=0) return diff;
        
        return device.compareTo(other.device);
    }
}
