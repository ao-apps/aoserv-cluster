/*
 * Copyright 2007-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * One Xen dom0 cluster member.
 *
 * @author  AO Industries, Inc.
 */
public class Dom0 implements Comparable<Dom0>, Serializable {

    private static final long serialVersionUID = 2L;

    final String clusterName;
    final String hostname;
    //final Rack rack;
    final int ram;
    final ProcessorType processorType;
    final ProcessorArchitecture processorArchitecture;
    final int processorSpeed;
    final int processorCores;
    final boolean supportsHvm;
    final Map<String,Dom0Disk> unmodifiableDom0Disks;

    private static final boolean hasNull(Collection C) {
        for(Object O : C) if(O==null) return true;
        return false;
    }

    /**
     * The list of dom0disks MUST BE UNMODIFIABLE - no defensive copy is made.
     *
     * @see Cluster#addDom0
     */
    Dom0(
        String clusterName,
        String hostname,
        /*Rack rack,*/
        int ram,
        ProcessorType processorType,
        ProcessorArchitecture processorArchitecture,
        int processorSpeed,
        int processorCores,
        boolean supportsHvm,
        Map<String,Dom0Disk> unmodifiableDom0Disks
    ) {
        assert clusterName!=null : "clusterName is null";
        assert hostname!=null : "hostname is null";
        assert !hasNull(unmodifiableDom0Disks.values()) : "null value in unmodifiableDom0Disks";
        this.clusterName = clusterName;
        this.hostname = hostname;
        //if(rack.getCluster()!=cluster) throw new IllegalArgumentException(this+": cluster!=rack.cluster");
        //this.rack = rack;
        this.ram = ram;
        this.processorType = processorType;
        this.processorArchitecture = processorArchitecture;
        this.processorSpeed = processorSpeed;
        this.processorCores = processorCores;
        this.supportsHvm = supportsHvm;
        this.unmodifiableDom0Disks = unmodifiableDom0Disks;
    }

    public String getClusterName() {
        return clusterName;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    /*public Rack getRack() {
        return rack;
    }*/

    /**
     * Gets the physical amount of RAM in megabytes.
     */
    public int getRam() {
        return ram;
    }
    
    public ProcessorType getProcessorType() {
        return processorType;
    }
    
    public ProcessorArchitecture getProcessorArchitecture() {
        return processorArchitecture;
    }
    
    /**
     * Gets the processor speed in megahertz.
     */
    public int getProcessorSpeed() {
        return processorSpeed;
    }
    
    /**
     * Gets the number of processor cores, hyperthreaded CPUs count as two.
     */
    public int getProcessorCores() {
        return processorCores;
    }
    
    /**
     * Gets if the system supports full hardware virtualization.
     */
    public boolean getSupportsHvm() {
        return supportsHvm;
    }

    /**
     * Gets the unmodifiable list of disks.
     */
    public Map<String,Dom0Disk> getDom0Disks() {
        return unmodifiableDom0Disks;
    }
    
    /**
     * Gets a specific disk by its device name or <code>null</code> if not found.
     */
    public Dom0Disk getDom0Disk(String device) {
        return unmodifiableDom0Disks.get(device);
    }

    @Override
    public String toString() {
        return toString(clusterName, hostname);
    }

    static String toString(String clusterName, String hostname) {
        return Cluster.toString(clusterName) /*rack.toString()*/+'/'+hostname;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>clusterName</li>
     *   <li>hostname</li>
     * </ol>
     */
    @Override
    public int compareTo(Dom0 other) {
        if(this==other) return 0;

        int diff = clusterName.compareTo(other.clusterName);
        if(diff!=0) return diff;
        
        return hostname.compareTo(other.hostname);
    }
}
