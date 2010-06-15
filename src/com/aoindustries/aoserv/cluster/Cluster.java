/*
 * Copyright 2007-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A cluster contains all of the definitions for virtual and physical resources
 * required and available, but nothing about the current mapping between them.
 * ClusterConfiguration keeps track of the current mappings.
 * 
 * A cluster is immutable.  All setters return a new instance of a cluster.
 *
 * @author  AO Industries, Inc.
 */
public class Cluster implements Comparable<Cluster>, Serializable {

    private static final long serialVersionUID = 2L;

    // These are here just for generic-type-specific versions
    private static final Map<String,Dom0> emptyDom0Map = Collections.emptyMap();
    private static final Map<String,Dom0Disk> emptyDom0DiskMap = Collections.emptyMap();
    private static final Map<Short,PhysicalVolume> emptyPhysicalVolumeMap = Collections.emptyMap();
    private static final Map<String,DomU> emptyDomUMap = Collections.emptyMap();
    private static final Map<String,DomUDisk> emptyDomUDiskMap = Collections.emptyMap();

    /**
     * Gets an unmodifiable map that combines the existing map with the new object
     * If the existing map is empty, will use Collections.singletonMap, otherwise
     * creates a new HashMap and returns it wrapped by Collections.unmodifiableMap.
     */
    private static <K,V> Map<K,V> addToUnmodifiableMap(Map<K,V> existingMap, K newKey, V newValue) {
        if(existingMap.isEmpty()) return Collections.singletonMap(newKey, newValue);
        if(existingMap.containsKey(newKey)) throw new AssertionError("Map already contains key: "+newKey);
        Map<K,V> newMap = new HashMap<K,V>(existingMap);
        newMap.put(newKey, newValue);
        return Collections.unmodifiableMap(newMap);
    }
    
    /**
     * Replaces an existing entry in an unmodifiable map.  If the map only has a single
     * entry, will use Collections.singletonMap, otherwise creates a new HashMap and
     * returns it wrapped by Collections.unmodifiableMap.
     */
    private static <K,V> Map<K,V> replaceInUnmodifiableMap(Map<K,V> existingMap, K key, V newValue) {
        if(!existingMap.containsKey(key)) throw new AssertionError("Map doesn't contain key: "+key);
        if(existingMap.size()==1) return Collections.singletonMap(key, newValue);
        Map<K,V> newMap = new HashMap<K,V>(existingMap);
        newMap.put(key, newValue);
        return Collections.unmodifiableMap(newMap);
    }

    final String name;
    //final SortedSet<Rack> unmodifiableRacks = Collections.unmodifiableSortedSet(racks);
    final Map<String,Dom0> unmodifiableDom0s;
    final Map<String,DomU> unmodifiableDomUs;
    //final Map<String,SortedSet<DomU>> unmodifiableDomUGroups = Collections.unmodifiableMap(domUGroups);

    /**
     * Creates a new, empty cluster.
     */
    public Cluster(String name) {
        this(
            name,
            emptyDom0Map,
            emptyDomUMap
        );
    }

    /**
     * Creates a cluster with the provided details.  No defensive copy of the provided objects
     * is created, and they MUST BE UNMODIFIABLE!
     */
    private Cluster(String name, Map<String,Dom0> unmodifiableDom0s, Map<String,DomU> unmodifiableDomUs) {
        this.name = name;
        this.unmodifiableDom0s = unmodifiableDom0s;
        this.unmodifiableDomUs = unmodifiableDomUs;
    }

    public String getName() {
        return name;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>name</li>
     * </ol>
     */
    @Override
    public int compareTo(Cluster other) {
        if(this==other) return 0;
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return toString(name);
    }
    
    static String toString(String name) {
        return name;
    }

    /**
     * Gets an unmodifiable set of racks.
     */
    /*public SortedSet<Rack> getRacks() {
        return unmodifiableRacks;
    }*/

    /**
     * Adds a rack to the cluster returning the reference to Rack.
     */
    /*public Rack addRack(String id) {
        Rack newRack = new Rack(this, id);
        if(racks.contains(newRack)) throw new IllegalArgumentException(this+": Cluster already contains rack with id="+id);
        racks.add(newRack);
        return newRack;
    }*/

    /**
     * Gets an unmodifiable set of Dom0s.
     */
    public Map<String,Dom0> getDom0s() {
        return unmodifiableDom0s;
    }
    
    /**
     * Gets a specific Dom0 by name or <code>null</code> if not found.
     */
    public Dom0 getDom0(String hostname) {
        return unmodifiableDom0s.get(hostname);
    }

    /**
     * Adds a Dom0 to the cluster returning the reference to the new cluster object.
     */
    public Cluster addDom0(
        String hostname,
        /*Rack rack,*/
        int ram,
        ProcessorType processorType,
        ProcessorArchitecture processorArchitecture,
        int processorSpeed,
        int processorCores,
        boolean supportsHvm
    ) {
        return new Cluster(
            name,
            addToUnmodifiableMap(
                unmodifiableDom0s,
                hostname,
                new Dom0(
                    name,
                    hostname,
                    /*rack,*/
                    ram,
                    processorType,
                    processorArchitecture,
                    processorSpeed,
                    processorCores,
                    supportsHvm,
                    emptyDom0DiskMap
                )
            ),
            unmodifiableDomUs
        );
    }

    /**
     * Gets an unmodifiable set of DomUs.
     */
    public Map<String,DomU> getDomUs() {
        return unmodifiableDomUs;
    }

    /**
     * Gets a specific DomU by name or <code>null</code> if not found.
     */
    public DomU getDomU(String hostname) {
        return unmodifiableDomUs.get(hostname);
    }
    
    /**
     * Adds a DomU to the cluster returning the reference to new cluster.
     */
    public Cluster addDomU(
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
        boolean secondaryDom0Locked
    ) {
        return new Cluster(
            name,
            unmodifiableDom0s,
            addToUnmodifiableMap(
                unmodifiableDomUs,
                hostname,
                new DomU(
                    name,
                    hostname,
                    primaryRam,
                    secondaryRam,
                    minimumProcessorType,
                    minimumProcessorArchitecture,
                    minimumProcessorSpeed,
                    processorCores,
                    processorWeight,
                    requiresHvm,
                    primaryDom0Locked,
                    secondaryDom0Locked,
                    emptyDomUDiskMap
                )
            )
        );
    }
    
    /**
     * Adds a disk to the server with the provided hostname, returns the new cluster.
     */
    public Cluster addDom0Disk(String hostname, String device, int diskSpeed) {
        Dom0 dom0 = unmodifiableDom0s.get(hostname);
        if(dom0==null) throw new IllegalArgumentException(this+": Dom0 not found: "+hostname);
        return new Cluster(
            name,
            replaceInUnmodifiableMap(
                unmodifiableDom0s,
                hostname,
                new Dom0(
                    name,
                    hostname,
                    /*rack,*/
                    dom0.ram,
                    dom0.processorType,
                    dom0.processorArchitecture,
                    dom0.processorSpeed,
                    dom0.processorCores,
                    dom0.supportsHvm,
                    addToUnmodifiableMap(
                        dom0.getDom0Disks(),
                        device,
                        new Dom0Disk(
                            name,
                            hostname,
                            device,
                            diskSpeed,
                            emptyPhysicalVolumeMap
                        )
                    )
                )
            ),
            unmodifiableDomUs
        );
    }

    /**
     * Adds a physical volume to this disk, returns the new cluster.
     */
    public Cluster addPhysicalVolume(String hostname, String device, short partition, long extents) {
        Dom0 dom0 = unmodifiableDom0s.get(hostname);
        if(dom0==null) throw new IllegalArgumentException(this+": Dom0 not found: "+hostname);

        Dom0Disk dom0Disk = dom0.getDom0Disk(device);
        if(dom0Disk==null) throw new IllegalArgumentException(dom0+": Disk not found: "+device);

        return new Cluster(
            name,
            replaceInUnmodifiableMap(
                unmodifiableDom0s,
                hostname,
                new Dom0(
                    name,
                    hostname,
                    /*rack,*/
                    dom0.ram,
                    dom0.processorType,
                    dom0.processorArchitecture,
                    dom0.processorSpeed,
                    dom0.processorCores,
                    dom0.supportsHvm,
                    replaceInUnmodifiableMap(
                        dom0.getDom0Disks(),
                        device,
                        new Dom0Disk(
                            name,
                            hostname,
                            device,
                            dom0Disk.diskSpeed,
                            addToUnmodifiableMap(
                                dom0Disk.getPhysicalVolumes(),
                                partition,
                                new PhysicalVolume(
                                    name,
                                    hostname,
                                    device,
                                    partition,
                                    extents
                                )
                            )
                        )
                    )
                )
            ),
            unmodifiableDomUs
        );
    }

    /**
     * Adds a disk to this virtual server, returns the new cluster.
     */
    public Cluster addDomUDisk(
        String hostname,
        String device,
        int minimumDiskSpeed,
        int extents,
        short weight
    ) {
        DomU domU = unmodifiableDomUs.get(hostname);
        if(domU==null) throw new IllegalArgumentException(this+": DomU not found: "+hostname);
        return new Cluster(
            name,
            unmodifiableDom0s,
            replaceInUnmodifiableMap(
                unmodifiableDomUs,
                hostname,
                new DomU(
                    name,
                    hostname,
                    domU.primaryRam,
                    domU.secondaryRam,
                    domU.minimumProcessorType,
                    domU.minimumProcessorArchitecture,
                    domU.minimumProcessorSpeed,
                    domU.processorCores,
                    domU.processorWeight,
                    domU.requiresHvm,
                    domU.primaryDom0Locked,
                    domU.secondaryDom0Locked,
                    addToUnmodifiableMap(
                        domU.getDomUDisks(),
                        device,
                        new DomUDisk(
                            name,
                            hostname,
                            device,
                            minimumDiskSpeed,
                            extents,
                            weight
                        )
                    )
                )
            )
        );
    }

    /**
     * Adds a group of DomU that should all be on different Dom0 machines.
     */
    /*public void addDomUGroup(String name, SortedSet<DomU> domUGroup) {
        if(domUGroups.containsKey(name)) throw new IllegalArgumentException(this+": DomU group already exists in this cluster: "+name);
        // Each of the DomU must be in this cluster
        for(DomU domU : domUGroup) {
            if(domU.getCluster()!=this) throw new IllegalArgumentException(this+": DomU is not in this cluster: "+domU);
        }
        domUGroups.put(name, Collections.unmodifiableSortedSet(new TreeSet<DomU>(domUGroup)));
    }*/

    /**
     * Gets an unmodifiable view of the DomU groups.
     */
    /*public Map<String,SortedSet<DomU>> unmodifiableDomUGroups() {
        return unmodifiableDomUGroups;
    }*/
}
