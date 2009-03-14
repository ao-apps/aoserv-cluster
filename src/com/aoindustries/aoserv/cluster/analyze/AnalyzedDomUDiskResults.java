/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.DiskType;
import com.aoindustries.aoserv.cluster.DomUDisk;
import com.aoindustries.aoserv.cluster.RaidType;
import java.util.Collection;

/**
 * Stores the results of the analysis of a single DomUDisk->(one or more physical volumes)->Dom0Disk mapping.
 *
 * @author  AO Industries, Inc.
 */
public class AnalyzedDomUDiskResults implements Comparable<AnalyzedDomUDiskResults> {

    private final DomUDisk domUDisk;
    private final Result<RaidType> raidTypeResult;
    private final Result<DiskType> diskTypeResult;
    private final Result<Integer> diskSpeedResult;

    AnalyzedDomUDiskResults(
        DomUDisk domUDisk,
        Result<RaidType> raidTypeResult,
        Result<DiskType> diskTypeResult,
        Result<Integer> diskSpeedResult
    ) {
        this.domUDisk = domUDisk;
        this.raidTypeResult = raidTypeResult;
        this.diskTypeResult = diskTypeResult;
        this.diskSpeedResult = diskSpeedResult;
    }

    public DomUDisk getDomUDisk() {
        return domUDisk;
    }

    public Result<RaidType> getRaidTypeResult() {
        return raidTypeResult;
    }

    public Result<DiskType> getDiskTypeResult() {
        return diskTypeResult;
    }

    public Result<Integer> getDiskSpeedResult() {
        return diskSpeedResult;
    }

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>clusterName</li>
     *   <li>domUHostname</li>
     *   <li>device</li>
     * </ol>
     */
    public int compareTo(AnalyzedDomUDiskResults other) {
        int diff = domUDisk.getClusterName().compareTo(other.domUDisk.getClusterName());
        if(diff!=0) return diff;
        
        diff = domUDisk.getDomUHostname().compareTo(other.domUDisk.getDomUHostname());
        if(diff!=0) return diff;
        
        return domUDisk.getDevice().compareTo(other.domUDisk.getDevice());
    }

    /**
     * @see AnalyzedCluster#getAllResults()
     */
    public void addAllResults(Collection<Result> allResults, boolean nonOptimalOnly) {
        if(!nonOptimalOnly || raidTypeResult.getAlertLevel()!=AlertLevel.NONE) allResults.add(raidTypeResult);
        if(!nonOptimalOnly || diskTypeResult.getAlertLevel()!=AlertLevel.NONE) allResults.add(diskTypeResult);
        if(!nonOptimalOnly || diskSpeedResult.getAlertLevel()!=AlertLevel.NONE) allResults.add(diskSpeedResult);
    }

    /**
     * Determines if this is optimal, meaning all results have AlertLevel of NONE.
     */
    public boolean isOptimal() {
        return
            raidTypeResult.getAlertLevel()==AlertLevel.NONE
            && diskTypeResult.getAlertLevel()==AlertLevel.NONE
            && diskSpeedResult.getAlertLevel()==AlertLevel.NONE
        ;
    }

    /**
     * Determines if this has at least one result with AlertLevel of CRITICAL.
     */
    public boolean hasCritical() {
        return
            raidTypeResult.getAlertLevel()==AlertLevel.CRITICAL
            || diskTypeResult.getAlertLevel()==AlertLevel.CRITICAL
            || diskSpeedResult.getAlertLevel()==AlertLevel.CRITICAL
        ;
    }
}
