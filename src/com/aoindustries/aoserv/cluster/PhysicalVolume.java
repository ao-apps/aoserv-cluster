/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * @author  AO Industries, Inc.
 */
public class PhysicalVolume implements Comparable<PhysicalVolume>, Serializable {

    private static final long serialVersionUID = 2L;

    final String clusterName;
    final String dom0Hostname;
    final String device;
    final short partition;
    final long extents;

    /**
     * @see Dom0Disk#addPhysicalVolume
     */
    PhysicalVolume(String clusterName, String dom0Hostname, String device, short partition, long extents) {
        assert extents>0 : "extents<=0: "+extents;
        this.clusterName = clusterName;
        this.dom0Hostname = dom0Hostname;
        this.device = device;
        this.partition = partition;
        this.extents = extents;
    }

    public String getClusterName() {
        return clusterName;
    }
    
    public String getDom0Hostname() {
        return dom0Hostname;
    }
    
    public String getDevice() {
        return device;
    }

    public short getPartition() {
        return partition;
    }

    public long getExtents() {
        return extents;
    }

    @Override
    public String toString() {
        return toString(clusterName, dom0Hostname, device, partition);
    }

    static String toString(String clusterName, String dom0Hostname, String device, short partition) {
        return Dom0Disk.toString(clusterName, dom0Hostname, device)+partition;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>clusterName</li>
     *   <li>dom0Hostname</li>
     *   <li>device</li>
     *   <li>partition</li>
     * </ol>
     */
    @Override
    public int compareTo(PhysicalVolume other) {
        if(this==other) return 0;

        int diff = clusterName.compareTo(other.clusterName);
        if(diff!=0) return diff;
        
        diff = dom0Hostname.compareTo(other.dom0Hostname);
        if(diff!=0) return diff;

        diff = device.compareTo(other.device);
        if(diff!=0) return diff;
        
        return other.partition - partition;
    }
}
