/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.DiskType;
import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.Dom0Disk;
import com.aoindustries.aoserv.cluster.DomUConfiguration;
import com.aoindustries.aoserv.cluster.DomUDisk;
import com.aoindustries.aoserv.cluster.DomUDiskConfiguration;
import com.aoindustries.aoserv.cluster.PhysicalVolume;
import com.aoindustries.aoserv.cluster.RaidType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Analyzes a single Dom0Disk to find anything that is not optimal.
 *
 * @author  AO Industries, Inc.
 */
public class AnalyzedDom0DiskConfiguration implements Comparable<AnalyzedDom0DiskConfiguration> {

    private final ClusterConfiguration clusterConfiguration;
    private final Dom0Disk dom0Disk;

    public AnalyzedDom0DiskConfiguration(ClusterConfiguration clusterConfiguration, Dom0Disk dom0Disk) {
        this.clusterConfiguration = clusterConfiguration;
        this.dom0Disk = dom0Disk;
    }

    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public Dom0Disk getDom0Disk() {
        return dom0Disk;
    }

    /**
     * Gets the free allocation disk weight.
     */
    public Result<Integer> getAvailableWeightResult() {
        // Add up all of the weights on any physical volumes on this drive.
        // Each unique DomUDisk will only be added once.
        int allocatedDiskWeight = 0;

        for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
            // Must be either primary or secondary on this
            Dom0 primaryDom0 = domUConfiguration.getPrimaryDom0();
            if(primaryDom0.getHostname().equals(dom0Disk.getDom0Hostname())) {
                assert primaryDom0.getClusterName().equals(dom0Disk.getClusterName()) : "primaryDom0.clusterName!=dom0Disk.clusterName";
                // Look only for primary matches
                for(DomUDiskConfiguration domUDiskConfiguration : domUConfiguration.getDomUDiskConfigurations()) {
                    for(PhysicalVolume physicalVolume : domUDiskConfiguration.getPrimaryPhysicalVolumes()) {
                        if(physicalVolume.getDevice().equals(dom0Disk.getDevice())) {
                            assert physicalVolume.getClusterName().equals(dom0Disk.getClusterName()) : "physicalVolume.clusterName!=dom0Disk.clusterName";
                            assert physicalVolume.getDom0Hostname().equals(dom0Disk.getDom0Hostname()) : "physicalVolume.dom0Hostname!=dom0Disk.dom0Hostname";
                            // Found a match between DomUDisk and this Dom0Disk
                            allocatedDiskWeight += domUDiskConfiguration.getDomUDisk().getWeight();
                            break;
                        }
                    }
                }
            } else {
                Dom0 secondaryDom0 = domUConfiguration.getSecondaryDom0();
                if(secondaryDom0.getHostname().equals(dom0Disk.getDom0Hostname())) {
                    assert secondaryDom0.getClusterName().equals(dom0Disk.getClusterName()) : "secondaryDom0.clusterName!=dom0Disk.clusterName";
                    // Look only for secondary matches
                    for(DomUDiskConfiguration domUDiskConfiguration : domUConfiguration.getDomUDiskConfigurations()) {
                        for(PhysicalVolume physicalVolume : domUDiskConfiguration.getSecondaryPhysicalVolumes()) {
                            if(physicalVolume.getDevice().equals(dom0Disk.getDevice())) {
                                assert physicalVolume.getClusterName().equals(dom0Disk.getClusterName()) : "physicalVolume.clusterName!=dom0Disk.clusterName";
                                assert physicalVolume.getDom0Hostname().equals(dom0Disk.getDom0Hostname()) : "physicalVolume.dom0Hostname!=dom0Disk.dom0Hostname";
                                // Found a match between DomUDisk and this Dom0Disk
                                allocatedDiskWeight += domUDiskConfiguration.getDomUDisk().getWeight();
                                break;
                            }
                        }
                    }
                }
            }
        }
        int freeDiskWeight = 1024 - allocatedDiskWeight;
        return new Result<Integer>(
            "Available Weight",
            freeDiskWeight,
            -((double)freeDiskWeight / (double)1024),
            freeDiskWeight<0 ? AlertLevel.MEDIUM : AlertLevel.NONE
        );
    }

    /**
     * Gets the unsorted, unmodifiable list of results per DomUDisk.
     */
    public List<AnalyzedDomUDiskResults> getDomUDiskResults() {
        return Collections.unmodifiableList(getModifiableDomUDiskResults());
    }
    List<AnalyzedDomUDiskResults> getModifiableDomUDiskResults() {
        List<AnalyzedDomUDiskResults> results = new ArrayList<AnalyzedDomUDiskResults>();
        for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
            // Must be either primary or secondary on this
            Dom0 primaryDom0 = domUConfiguration.getPrimaryDom0();
            if(primaryDom0.getHostname().equals(dom0Disk.getDom0Hostname())) {
                assert primaryDom0.getClusterName().equals(dom0Disk.getClusterName()) : "primaryDom0.clusterName!=dom0Disk.clusterName";
                // Look only for primary matches
                for(DomUDiskConfiguration domUDiskConfiguration : domUConfiguration.getDomUDiskConfigurations()) {
                    for(PhysicalVolume physicalVolume : domUDiskConfiguration.getPrimaryPhysicalVolumes()) {
                        if(physicalVolume.getDevice().equals(dom0Disk.getDevice())) {
                            assert physicalVolume.getClusterName().equals(dom0Disk.getClusterName()) : "physicalVolume.clusterName!=dom0Disk.clusterName";
                            assert physicalVolume.getDom0Hostname().equals(dom0Disk.getDom0Hostname()) : "physicalVolume.dom0Hostname!=dom0Disk.dom0Hostname";
                            // Found a match between DomUDisk and this Dom0Disk
                            DomUDisk domUDisk = domUDiskConfiguration.getDomUDisk();
                            RaidType minRaidType = domUDisk.getMinimumRaidType();
                            DiskType minDiskType = domUDisk.getMinimumDiskType();
                            int minDiskSpeed = domUDisk.getMinimumDiskSpeed();
                            int diskSpeed = dom0Disk.getDiskSpeed();
                            results.add(
                                new AnalyzedDomUDiskResults(
                                    domUDisk,
                                    new Result<RaidType>(
                                        "Minimum RAID Type",
                                        minRaidType,
                                        1,
                                        minRaidType!=null && dom0Disk.getRaidType().compareTo(minRaidType) < 0 ? AlertLevel.HIGH : AlertLevel.NONE
                                    ),
                                    new Result<DiskType>(
                                        "Minimum Disk Type",
                                        minDiskType,
                                        1,
                                        minDiskType!=null && dom0Disk.getDiskType().compareTo(minDiskType) < 0 ? AlertLevel.LOW : AlertLevel.NONE
                                    ),
                                    new Result<Integer>(
                                        "Minimum Disk Speed",
                                        minDiskSpeed==-1 ? null : Integer.valueOf(minDiskSpeed),
                                        (double)(minDiskSpeed-diskSpeed)/(double)minDiskSpeed,
                                        minDiskSpeed!=-1 && diskSpeed < minDiskSpeed ? AlertLevel.MEDIUM : AlertLevel.NONE
                                    )
                                )
                            );
                            break;
                        }
                    }
                }
            } else {
                Dom0 secondaryDom0 = domUConfiguration.getSecondaryDom0();
                if(secondaryDom0.getHostname().equals(dom0Disk.getDom0Hostname())) {
                    assert secondaryDom0.getClusterName().equals(dom0Disk.getClusterName()) : "secondaryDom0.clusterName!=dom0Disk.clusterName";
                    // Look only for secondary matches
                    for(DomUDiskConfiguration domUDiskConfiguration : domUConfiguration.getDomUDiskConfigurations()) {
                        for(PhysicalVolume physicalVolume : domUDiskConfiguration.getSecondaryPhysicalVolumes()) {
                            if(physicalVolume.getDevice().equals(dom0Disk.getDevice())) {
                                assert physicalVolume.getClusterName().equals(dom0Disk.getClusterName()) : "physicalVolume.clusterName!=dom0Disk.clusterName";
                                assert physicalVolume.getDom0Hostname().equals(dom0Disk.getDom0Hostname()) : "physicalVolume.dom0Hostname!=dom0Disk.dom0Hostname";
                                // Found a match between DomUDisk and this Dom0Disk
                                DomUDisk domUDisk = domUDiskConfiguration.getDomUDisk();
                                RaidType minRaidType = domUDisk.getMinimumRaidType();
                                DiskType minDiskType = domUDisk.getMinimumDiskType();
                                int minDiskSpeed = domUDisk.getMinimumDiskSpeed();
                                int diskSpeed = dom0Disk.getDiskSpeed();
                                results.add(
                                    new AnalyzedDomUDiskResults(
                                        domUDisk,
                                        new Result<RaidType>(
                                            "Minimum RAID Type",
                                            minRaidType,
                                            1,
                                            minRaidType!=null && dom0Disk.getRaidType().compareTo(minRaidType) < 0 ? AlertLevel.HIGH : AlertLevel.NONE
                                        ),
                                        new Result<DiskType>(
                                            "Minimum Disk Type",
                                            minDiskType,
                                            1,
                                            minDiskType!=null && dom0Disk.getDiskType().compareTo(minDiskType) < 0 ? AlertLevel.LOW : AlertLevel.NONE
                                        ),
                                        new Result<Integer>(
                                            "Minimum Disk Speed",
                                            minDiskSpeed==-1 ? null : Integer.valueOf(minDiskSpeed),
                                            (double)(minDiskSpeed-diskSpeed)/(double)minDiskSpeed,
                                            minDiskSpeed!=-1 && diskSpeed < minDiskSpeed ? AlertLevel.MEDIUM : AlertLevel.NONE
                                        )
                                    )
                                );
                                break;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>clusterName</li>
     *   <li>dom0Hostname</li>
     *   <li>device</li>
     * </ol>
     */
    /**
     * Sorts by cluster name, dom0 hostname, device, identifyHashCode.
     */
    public int compareTo(AnalyzedDom0DiskConfiguration other) {
        if(this==other) return 0;

        int diff = dom0Disk.getClusterName().compareTo(other.dom0Disk.getClusterName());
        if(diff!=0) return diff;
        
        diff = dom0Disk.getDom0Hostname().compareTo(other.dom0Disk.getDom0Hostname());
        if(diff!=0) return diff;

        return dom0Disk.getDevice().compareTo(other.dom0Disk.getDevice());
    }

    /**
     * @see AnalyzedCluster#getAllResults()
     */
    public void addAllResults(Collection<Result> allResults, boolean nonOptimalOnly) {
        Result availableWeightResult = getAvailableWeightResult();
        if(!nonOptimalOnly || availableWeightResult.getAlertLevel()!=AlertLevel.NONE) allResults.add(availableWeightResult);
        for(AnalyzedDomUDiskResults domUDisk : getModifiableDomUDiskResults()) domUDisk.addAllResults(allResults, nonOptimalOnly);
    }

    /**
     * Determines if this is optimal, meaning all results have AlertLevel of NONE.
     */
    public boolean isOptimal() {
        if(getAvailableWeightResult().getAlertLevel()!=AlertLevel.NONE) return false;
        for(AnalyzedDomUDiskResults domUDisk : getModifiableDomUDiskResults()) if(!domUDisk.isOptimal()) return false;
        return true;
    }

    /**
     * Determines if this has at least one result with AlertLevel of CRITICAL.
     */
    public boolean hasCritical() {
        if(getAvailableWeightResult().getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(AnalyzedDomUDiskResults domUDisk : getModifiableDomUDiskResults()) if(domUDisk.hasCritical()) return true;
        return false;
    }
}
