/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.DiskType;
import com.aoindustries.aoserv.cluster.DomUDisk;
import com.aoindustries.aoserv.cluster.RaidType;

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
     *
     * @return true if more results are wanted, or false to receive no more results.
     */
    public boolean getAllResults(ResultHandler resultHandler, AlertLevel minimumAlertLevel) {
        if(raidTypeResult.getAlertLevel().compareTo(minimumAlertLevel)>=0) {
            if(!resultHandler.handleResult(raidTypeResult)) return false;
        }
        if(diskTypeResult.getAlertLevel().compareTo(minimumAlertLevel)>=0) {
            if(!resultHandler.handleResult(diskTypeResult)) return false;
        }
        if(diskSpeedResult.getAlertLevel().compareTo(minimumAlertLevel)>=0) {
            if(!resultHandler.handleResult(diskSpeedResult)) return false;
        }
        return true;
    }
}
