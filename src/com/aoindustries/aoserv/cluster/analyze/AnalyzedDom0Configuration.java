/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.Dom0Disk;
import com.aoindustries.aoserv.cluster.DomU;
import com.aoindustries.aoserv.cluster.DomUConfiguration;
import com.aoindustries.aoserv.cluster.ProcessorArchitecture;
import com.aoindustries.aoserv.cluster.ProcessorType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes a single Dom0 to find anything that is not optimal.
 * 
 * @author  AO Industries, Inc.
 */
public class AnalyzedDom0Configuration {

    private final ClusterConfiguration clusterConfiguration;
    private final Dom0 dom0;

    public AnalyzedDom0Configuration(ClusterConfiguration clusterConfiguration, Dom0 dom0) {
        this.clusterConfiguration = clusterConfiguration;
        this.dom0 = dom0;
    }

    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public Dom0 getDom0() {
        return dom0;
    }

    /**
     * Gets the results for primary RAM
     */
    public Result<Integer> getAvailableRamResult() {
        int allocatedPrimaryRam = 0;
        for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
            if(domUConfiguration.getPrimaryDom0()==dom0) allocatedPrimaryRam+=domUConfiguration.getDomU().getPrimaryRam();
        }
        int totalRam = dom0.getRam();
        int freePrimaryRam = totalRam - allocatedPrimaryRam;
        return new Result<Integer>(
            "Available RAM",
            freePrimaryRam,
            -((double)freePrimaryRam / (double)totalRam),
            freePrimaryRam<0 ? AlertLevel.CRITICAL : AlertLevel.NONE
        );
    }

    /**
     * Gets an unmodifiable set of secondary RAM allocation results.  It has a separate
     * entry for each Dom0 that has any secondary resource on this dom0.
     */
    public List<Result<Integer>> getAllocatedSecondaryRamResults(boolean nonOptimalOnly) {
        return Collections.unmodifiableList(getModifiableAllocatedSecondaryRamResults(nonOptimalOnly));
    }
    List<Result<Integer>> getModifiableAllocatedSecondaryRamResults(boolean nonOptimalOnly) {
        int allocatedPrimaryRam = 0;
        Map<String,Integer> allocatedSecondaryRams = new HashMap<String,Integer>();
        for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
            if(domUConfiguration.getPrimaryDom0()==dom0) {
                allocatedPrimaryRam+=domUConfiguration.getDomU().getPrimaryRam();
            } else if(domUConfiguration.getSecondaryDom0()==dom0) {
                int secondaryRam = domUConfiguration.getDomU().getSecondaryRam();
                if(secondaryRam!=-1) {
                    String failedHostname = domUConfiguration.getPrimaryDom0().getHostname();
                    Integer totalSecondary = allocatedSecondaryRams.get(failedHostname);
                    allocatedSecondaryRams.put(
                        failedHostname,
                        totalSecondary==null ? secondaryRam : (totalSecondary+secondaryRam)
                    );
                }
            }
        }
        int totalRam = dom0.getRam();
        int freePrimaryRam = totalRam - allocatedPrimaryRam;

        List<Result<Integer>> results = new ArrayList<Result<Integer>>(allocatedSecondaryRams.size());
        for(Map.Entry<String,Integer> entry : allocatedSecondaryRams.entrySet()) {
            String failedHostname = entry.getKey();
            int allocatedSecondary = entry.getValue();
            AlertLevel alertLevel = allocatedSecondary>freePrimaryRam ? AlertLevel.HIGH : AlertLevel.NONE;
            if(!nonOptimalOnly || alertLevel!=AlertLevel.NONE) {
                results.add(
                    new Result<Integer>(
                        failedHostname,
                        allocatedSecondary,
                        (double)(allocatedSecondary-freePrimaryRam)/(double)totalRam,
                        alertLevel
                    )
                );
            }
        }
        return results;
    }

    /**
     * Gets the unmodifiable set of specific processor type results.  It has a
     * separate entry for each DomU that is either primary or secondary (with RAM)
     * on this dom0.
     */
    public List<Result<ProcessorType>> getProcessorTypeResults(boolean nonOptimalOnly) {
        return Collections.unmodifiableList(getModifiableProcessorTypeResults(nonOptimalOnly));
    }
    List<Result<ProcessorType>> getModifiableProcessorTypeResults(boolean nonOptimalOnly) {
        ProcessorType processorType = dom0.getProcessorType();
        
        List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
        List<Result<ProcessorType>> results = new ArrayList<Result<ProcessorType>>(domUConfigurations.size());
        for(DomUConfiguration domUConfiguration : domUConfigurations) {
            DomU domU = domUConfiguration.getDomU();
            if(
                domUConfiguration.getPrimaryDom0()==dom0
                || (
                    domUConfiguration.getSecondaryDom0()==dom0
                    && domU.getSecondaryRam()!=-1
                )
            ) {
                ProcessorType minProcessorType = domU.getMinimumProcessorType();
                AlertLevel alertLevel = minProcessorType!=null && processorType.compareTo(minProcessorType)<0 ? AlertLevel.LOW : AlertLevel.NONE;
                if(!nonOptimalOnly || alertLevel!=AlertLevel.NONE) {
                    results.add(
                        new Result<ProcessorType>(
                            domU.getHostname(),
                            minProcessorType,
                            1,
                            alertLevel
                        )
                    );
                }
            }
        }
        return results;
    }
    
    /**
     * Gets the unmodifiable set of specific processor architecture results.  It has a
     * separate entry for each DomU that is either primary or secondary (with RAM)
     * on this dom0.
     */
    public List<Result<ProcessorArchitecture>> getProcessorArchitectureResults(boolean nonOptimalOnly) {
        return Collections.unmodifiableList(getModifiableProcessorArchitectureResults(nonOptimalOnly));
    }
    List<Result<ProcessorArchitecture>> getModifiableProcessorArchitectureResults(boolean nonOptimalOnly) {
        ProcessorArchitecture processorArchitecture = dom0.getProcessorArchitecture();
        
        List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
        List<Result<ProcessorArchitecture>> results = new ArrayList<Result<ProcessorArchitecture>>(domUConfigurations.size());
        for(DomUConfiguration domUConfiguration : domUConfigurations) {
            DomU domU = domUConfiguration.getDomU();
            if(domUConfiguration.getPrimaryDom0()==dom0) {
                // Primary is CRITICAL
                ProcessorArchitecture minProcessorArchitecture = domU.getMinimumProcessorArchitecture();
                AlertLevel alertLevel = processorArchitecture.compareTo(minProcessorArchitecture)<0 ? AlertLevel.CRITICAL : AlertLevel.NONE;
                if(!nonOptimalOnly || alertLevel!=AlertLevel.NONE) {
                    results.add(
                        new Result<ProcessorArchitecture>(
                            domU.getHostname(),
                            minProcessorArchitecture,
                            1,
                            alertLevel
                        )
                    );
                }
            } else if(
                domUConfiguration.getSecondaryDom0()==dom0
                && domU.getSecondaryRam()!=-1
            ) {
                // Secondary is HIGH
                ProcessorArchitecture minProcessorArchitecture = domU.getMinimumProcessorArchitecture();
                AlertLevel alertLevel = processorArchitecture.compareTo(minProcessorArchitecture)<0 ? AlertLevel.HIGH : AlertLevel.NONE;
                if(!nonOptimalOnly || alertLevel!=AlertLevel.NONE) {
                    results.add(
                        new Result<ProcessorArchitecture>(
                            domU.getHostname(),
                            minProcessorArchitecture,
                            1,
                            alertLevel
                        )
                    );
                }
            }
        }
        return results;
    }

    /**
     * Gets the unmodifiable set of specific processor speed results.  It has a
     * separate entry for each DomU that is either primary or secondary (with RAM)
     * on this dom0.
     */
    public List<Result<Integer>> getProcessorSpeedResults(boolean nonOptimalOnly) {
        return Collections.unmodifiableList(getModifiableProcessorSpeedResults(nonOptimalOnly));
    }
    List<Result<Integer>> getModifiableProcessorSpeedResults(boolean nonOptimalOnly) {
        int processorSpeed = dom0.getProcessorSpeed();
        
        List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
        List<Result<Integer>> results = new ArrayList<Result<Integer>>(domUConfigurations.size());
        for(DomUConfiguration domUConfiguration : domUConfigurations) {
            DomU domU = domUConfiguration.getDomU();
            if(
                domUConfiguration.getPrimaryDom0()==dom0
                || (
                    domUConfiguration.getSecondaryDom0()==dom0
                    && domU.getSecondaryRam()!=-1
                )
            ) {
                int minSpeed = domU.getMinimumProcessorSpeed();
                AlertLevel alertLevel = minSpeed!=-1 && processorSpeed<minSpeed ? AlertLevel.LOW : AlertLevel.NONE;
                if(!nonOptimalOnly || alertLevel!=AlertLevel.NONE) {
                    results.add(
                        new Result<Integer>(
                            domU.getHostname(),
                            minSpeed==-1 ? null : Integer.valueOf(minSpeed),
                            (double)(minSpeed-processorSpeed)/(double)minSpeed,
                            alertLevel
                        )
                    );
                }
            }
        }
        return results;
    }

    /**
     * Gets the unmodifiable set of specific processor cores results.  It has a
     * separate entry for each DomU that is either primary or secondary (with RAM)
     * on this dom0.
     */
    public List<Result<Integer>> getProcessorCoresResults(boolean nonOptimalOnly) {
        return Collections.unmodifiableList(getModifiableProcessorCoresResults(nonOptimalOnly));
    }
    List<Result<Integer>> getModifiableProcessorCoresResults(boolean nonOptimalOnly) {
        int processorCores = dom0.getProcessorCores();
        
        List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
        List<Result<Integer>> results = new ArrayList<Result<Integer>>(domUConfigurations.size());
        for(DomUConfiguration domUConfiguration : domUConfigurations) {
            DomU domU = domUConfiguration.getDomU();
            if(
                domUConfiguration.getPrimaryDom0()==dom0
                || (
                    domUConfiguration.getSecondaryDom0()==dom0
                    && domU.getSecondaryRam()!=-1
                )
            ) {
                int minCores = domU.getProcessorCores();
                AlertLevel alertLevel = minCores!=-1 && processorCores<minCores ? AlertLevel.MEDIUM : AlertLevel.NONE;
                if(!nonOptimalOnly || alertLevel!=AlertLevel.NONE) {
                    results.add(
                        new Result<Integer>(
                            domU.getHostname(),
                            minCores==-1 ? null : Integer.valueOf(minCores),
                            (double)(minCores-processorCores)/(double)minCores,
                            alertLevel
                        )
                    );
                }
            }
        }
        return results;
    }

    /**
     * Gets the free primary processor weight.
     */
    public Result<Integer> getAvailableProcessorWeightResult() {
        int allocatedPrimaryWeight = 0;
        for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
            if(domUConfiguration.getPrimaryDom0()==dom0) {
                DomU domU = domUConfiguration.getDomU();
                allocatedPrimaryWeight += (int)domU.getProcessorCores() * (int)domU.getProcessorWeight();
            }
        }
        int totalWeight = dom0.getProcessorCores() * 1024;
        int freePrimaryWeight = totalWeight - allocatedPrimaryWeight;
        return new Result<Integer>(
            "Available Processor Weight",
            freePrimaryWeight,
            -((double)freePrimaryWeight / (double)totalWeight),
            freePrimaryWeight<0 ? AlertLevel.MEDIUM : AlertLevel.NONE
        );
    }

    /**
     * Gets an unmodifiable set of secondary processor weight allocation results.  It has a
     * separate entry for each DomU that is either primary or secondary (with RAM)
     * on this dom0.
     */
    /*
     * No longer worried about secondary processor allocation - just use as needed, don't need to keep reserved.
     *
    public List<Result<Integer>> getAllocatedSecondaryProcessorWeightResults() {
        return Collections.unmodifiableList(getModifiableAllocatedSecondaryProcessorWeightResults());
    }
    List<Result<Integer>> getModifiableAllocatedSecondaryProcessorWeightResults() {
        int allocatedPrimaryWeight = 0;
        Map<String,Integer> allocatedSecondaryWeights = new HashMap<String,Integer>();
        for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
            DomU domU = domUConfiguration.getDomU();
            if(domUConfiguration.getPrimaryDom0()==dom0) {
                allocatedPrimaryWeight += (int)domU.getProcessorCores() * (int)domU.getProcessorWeight();
            } else if(
                domUConfiguration.getSecondaryDom0()==dom0
                && domU.getSecondaryRam()!=-1
            ) {
                int secondaryWeight = (int)domU.getProcessorCores() * (int)domU.getProcessorWeight();
                String failedHostname = domUConfiguration.getPrimaryDom0().getHostname();
                Integer totalSecondary = allocatedSecondaryWeights.get(failedHostname);
                allocatedSecondaryWeights.put(
                    failedHostname,
                    totalSecondary==null ? secondaryWeight : (totalSecondary+secondaryWeight)
                );
            }
        }
        int freePrimaryWeight = dom0.getProcessorCores() * 1024 - allocatedPrimaryWeight;

        List<Result<Integer>> results = new ArrayList<Result<Integer>>(allocatedSecondaryWeights.size());
        for(Map.Entry<String,Integer> entry : allocatedSecondaryWeights.entrySet()) {
            String failedHostname = entry.getKey();
            int allocatedSecondary = entry.getValue();
            results.add(
                new Result<Integer>(
                    failedHostname,
                    allocatedSecondary,
                    allocatedSecondary>freePrimaryWeight ? AlertLevel.LOW : AlertLevel.NONE
                )
            );
        }
        return results;
    }*/

    /**
     * Gets the unmodifiable list of specific requires HVM results.  It has a
     * separate entry for each DomU that is either primary or secondary (with RAM)
     * on this dom0.
     */
    public List<Result<Boolean>> getRequiresHvmResults(boolean nonOptimalOnly) {
        return Collections.unmodifiableList(getModifiableRequiresHvmResults(nonOptimalOnly));
    }
    List<Result<Boolean>> getModifiableRequiresHvmResults(boolean nonOptimalOnly) {
        boolean supportsHvm = dom0.getSupportsHvm();
        
        List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
        List<Result<Boolean>> results = new ArrayList<Result<Boolean>>(domUConfigurations.size());
        for(DomUConfiguration domUConfiguration : domUConfigurations) {
            DomU domU = domUConfiguration.getDomU();
            if(
                domUConfiguration.getPrimaryDom0()==dom0
                || (
                    domUConfiguration.getSecondaryDom0()==dom0
                    && domU.getSecondaryRam()!=-1
                )
            ) {
                boolean requiresHvm = domU.getRequiresHvm();
                AlertLevel alertLevel = requiresHvm && !supportsHvm ? AlertLevel.CRITICAL : AlertLevel.NONE;
                if(!nonOptimalOnly || alertLevel!=AlertLevel.NONE) {
                    results.add(
                        new Result<Boolean>(
                            domU.getHostname(),
                            requiresHvm,
                            1,
                            alertLevel
                        )
                    );
                }
            }
        }
        return results;
    }

    /**
     * Gets the unsorted, unmodifable list of results for each disk.
     */
    public List<AnalyzedDom0DiskConfiguration> getDom0Disks() {
        return Collections.unmodifiableList(getModifiableDom0Disks());
    }
    List<AnalyzedDom0DiskConfiguration> getModifiableDom0Disks() {
        Map<String,Dom0Disk> clusterDom0Disks = dom0.getDom0Disks();
        List<AnalyzedDom0DiskConfiguration> dom0Disks = new ArrayList<AnalyzedDom0DiskConfiguration>(clusterDom0Disks.size());
        for(Dom0Disk dom0Disk : clusterDom0Disks.values()) {
            dom0Disks.add(new AnalyzedDom0DiskConfiguration(clusterConfiguration, dom0Disk));
        }
        return dom0Disks;
    }

    /**
     * @see AnalyzedClusterConfiguration#getAllResults()
     */
    public void addAllResults(Collection<Result> allResults, boolean nonOptimalOnly) {
        Result availableRamResult = getAvailableRamResult();
        if(!nonOptimalOnly || availableRamResult.getAlertLevel()!=AlertLevel.NONE) allResults.add(availableRamResult);
        allResults.addAll(getModifiableAllocatedSecondaryRamResults(nonOptimalOnly));
        allResults.addAll(getModifiableProcessorTypeResults(nonOptimalOnly));
        allResults.addAll(getModifiableProcessorArchitectureResults(nonOptimalOnly));
        allResults.addAll(getModifiableProcessorSpeedResults(nonOptimalOnly));
        allResults.addAll(getModifiableProcessorCoresResults(nonOptimalOnly));
        Result availableWeightResult = getAvailableProcessorWeightResult();
        if(!nonOptimalOnly || availableWeightResult.getAlertLevel()!=AlertLevel.NONE) allResults.add(availableWeightResult);
        allResults.addAll(getModifiableRequiresHvmResults(nonOptimalOnly));

        for(AnalyzedDom0DiskConfiguration dom0Disk : getModifiableDom0Disks()) dom0Disk.addAllResults(allResults, nonOptimalOnly);
    }

    /**
     * Determines if this is optimal, meaning all results have AlertLevel of NONE.
     */
    public boolean isOptimal() {
        if(getAvailableRamResult().getAlertLevel()!=AlertLevel.NONE) return false;
        for(Result result : getModifiableAllocatedSecondaryRamResults(true)) if(result.getAlertLevel()!=AlertLevel.NONE) return false;
        for(Result result : getModifiableProcessorTypeResults(true)) if(result.getAlertLevel()!=AlertLevel.NONE) return false;
        for(Result result : getModifiableProcessorArchitectureResults(true)) if(result.getAlertLevel()!=AlertLevel.NONE) return false;
        for(Result result : getModifiableProcessorSpeedResults(true)) if(result.getAlertLevel()!=AlertLevel.NONE) return false;
        for(Result result : getModifiableProcessorCoresResults(true)) if(result.getAlertLevel()!=AlertLevel.NONE) return false;
        if(getAvailableProcessorWeightResult().getAlertLevel()!=AlertLevel.NONE) return false;
        for(Result result : getModifiableRequiresHvmResults(true)) if(result.getAlertLevel()!=AlertLevel.NONE) return false;
        for(AnalyzedDom0DiskConfiguration dom0Disk : getModifiableDom0Disks()) if(!dom0Disk.isOptimal()) return false;
        return true;
    }

    /**
     * Determines if this has at least one result with AlertLevel of CRITICAL.
     */
    public boolean hasCritical() {
        if(getAvailableRamResult().getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(Result result : getModifiableAllocatedSecondaryRamResults(true)) if(result.getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(Result result : getModifiableProcessorTypeResults(true)) if(result.getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(Result result : getModifiableProcessorArchitectureResults(true)) if(result.getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(Result result : getModifiableProcessorSpeedResults(true)) if(result.getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(Result result : getModifiableProcessorCoresResults(true)) if(result.getAlertLevel()==AlertLevel.CRITICAL) return true;
        if(getAvailableProcessorWeightResult().getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(Result result : getModifiableRequiresHvmResults(true)) if(result.getAlertLevel()==AlertLevel.CRITICAL) return true;
        for(AnalyzedDom0DiskConfiguration dom0Disk : getModifiableDom0Disks()) if(dom0Disk.hasCritical()) return true;
        return false;
    }
}
