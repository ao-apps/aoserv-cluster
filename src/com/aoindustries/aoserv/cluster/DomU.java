package com.aoindustries.aoserv.cluster;

/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.Serializable;
import java.util.Map;

/**
 * Represents one virtual server and its required resources.
 *
 * @author  AO Industries, Inc.
 */
public class DomU implements Comparable<DomU>, Serializable {

    private static final long serialVersionUID = 2L;

    final String clusterName;
    final String hostname;
    final int primaryRam;
    final int secondaryRam;
    final ProcessorType minimumProcessorType;
    final ProcessorArchitecture minimumProcessorArchitecture;
    final int minimumProcessorSpeed;
    final short processorCores;
    final short processorWeight;
    final boolean requiresHvm;
    final Map<String,DomUDisk> unmodifiableDomUDisks;
    final boolean primaryDom0Locked;
    final boolean secondaryDom0Locked;

    /**
     * unmodifiableDomUDisks MUST BE UNMODIFIABLE
     * 
     * @see Cluster#addDomU
     */
    DomU(
        String clusterName,
        String hostname,
        int primaryRam,
        int secondaryRam,
        ProcessorType minimumProcessorType,
        ProcessorArchitecture minimumProcessorArchitecture,
        int minimumProcessorSpeed,
        short processorCores,
        short processorWeight,
        boolean requiresHvm,
        boolean primaryDom0Locked,
        boolean secondaryDom0Locked,
        Map<String,DomUDisk> unmodifiableDomUDisks
    ) {
        if(primaryRam<1) throw new IllegalArgumentException(this+": primaryRam should be >=1: "+primaryRam);
        if(secondaryRam!=-1 && secondaryRam<1) throw new IllegalArgumentException(this+": secondaryRam should be -1 or >=1: "+secondaryRam);
        if(minimumProcessorArchitecture==null) throw new IllegalArgumentException(this+": minimumProcessorArchitecture is null");
        if(minimumProcessorSpeed!=-1 && minimumProcessorSpeed<=0) throw new IllegalArgumentException(this+": Invalid value for minimumProcessorSpeed: "+minimumProcessorSpeed);
        if(processorCores<1) throw new IllegalArgumentException(this+": processorCores should be >=1: "+processorCores);
        if(processorWeight<1 || processorWeight>1024) throw new IllegalArgumentException(this+": Invalid value for processorWeight, should be in range 1-1024: "+processorWeight);

        this.clusterName = clusterName;
        this.hostname = hostname;
        this.primaryRam = primaryRam;
        this.secondaryRam = secondaryRam;
        this.minimumProcessorType = minimumProcessorType;
        this.minimumProcessorArchitecture = minimumProcessorArchitecture;
        this.minimumProcessorSpeed = minimumProcessorSpeed;
        this.processorCores = processorCores;
        this.processorWeight = processorWeight;
        this.requiresHvm = requiresHvm;
        this.primaryDom0Locked = primaryDom0Locked;
        this.secondaryDom0Locked = secondaryDom0Locked;
        this.unmodifiableDomUDisks = unmodifiableDomUDisks;
    }

    public String getClusterName() {
        return clusterName;
    }

    /**
     * Gets the cluster-wide unique name.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the amount of RAM needed in primary mode (in MB)
     */
    public int getPrimaryRam() {
        return primaryRam;
    }
    
    /**
     * Gets the amount of RAM needed in secondary mode (in MB) or -1 for
     * no secondary mode required.
     */
    public int getSecondaryRam() {
        return secondaryRam;
    }
    
    /**
     * Gets the minimum processor type for the VM or <code>null</code> if
     * there are no special requirements.
     */
    public ProcessorType getMinimumProcessorType() {
        return minimumProcessorType;
    }

    /**
     * Gets the minimum processor architecture.
     */
    public ProcessorArchitecture getMinimumProcessorArchitecture() {
        return minimumProcessorArchitecture;
    }

    /**
     * Gets the minimum processor speed in MHz or <code>-1</code> for no special
     * requirements.
     */
    public int getMinimumProcessorSpeed() {
        return minimumProcessorSpeed;
    }

    /**
     * Gets the number of processor cores to allocate.
     */
    public short getProcessorCores() {
        return processorCores;
    }
    
    /**
     * Gets the CPU weight (on a scale of 1-1024).
     */
    public short getProcessorWeight() {
        return processorWeight;
    }
    
    /**
     * Gets if this DomU requires full hardware virtualization support.
     */
    public boolean getRequiresHvm() {
        return requiresHvm;
    }

    /**
     * Gets the unmodifiable set of virtual disks.
     */
    public Map<String,DomUDisk> getDomUDisks() {
        return unmodifiableDomUDisks;
    }
    
    /**
     * Gets the disk with the provided device name or <code>null</code> if not found.
     */
    public DomUDisk getDomUDisk(String device) {
        return unmodifiableDomUDisks.get(device);
    }
    
    /**
     * Gets if this VM is manually locked to this Dom0.
     */
    public boolean isPrimaryDom0Locked() {
        return primaryDom0Locked;
    }

    /**
     * Gets if this VM is manually locked to this Dom0.
     */
    public boolean isSecondaryDom0Locked() {
        return secondaryDom0Locked;
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
    public int compareTo(DomU other) {
        if(this==other) return 0;

        int diff = clusterName.compareTo(other.clusterName);
        if(diff!=0) return diff;
        
        return hostname.compareTo(other.hostname);
    }
}
