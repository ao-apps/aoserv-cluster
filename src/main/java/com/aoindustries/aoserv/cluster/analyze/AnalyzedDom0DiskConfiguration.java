/*
 * Copyright 2008-2011, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.Dom0Disk;
import com.aoindustries.aoserv.cluster.DomUConfiguration;
import com.aoindustries.aoserv.cluster.DomUDisk;
import com.aoindustries.aoserv.cluster.DomUDiskConfiguration;
import com.aoindustries.aoserv.cluster.PhysicalVolume;
import com.aoindustries.aoserv.cluster.PhysicalVolumeConfiguration;
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
		assert dom0Disk!=null : "AnalyzedDom0DiskConfiguration.<init>: dom0Disk is null";
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
	 * 
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getAllocatedWeightResult(ResultHandler<? super Integer> resultHandler, AlertLevel minimumAlertLevel) {
		if(minimumAlertLevel.compareTo(AlertLevel.MEDIUM)<=0) {
			// Add up all of the weights on any physical volumes on this drive.
			// Each unique DomUDisk will only be added once.
			int allocatedDiskWeight = 0;

			for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
				// Must be either primary or secondary on this
				if(domUConfiguration.getPrimaryDom0().getHostname().equals(dom0Disk.getDom0Hostname())) {
					assert domUConfiguration.getPrimaryDom0().getClusterName().equals(dom0Disk.getClusterName()) : "primaryDom0.clusterName!=dom0Disk.clusterName";
					// Look only for primary matches
					for(DomUDiskConfiguration domUDiskConfiguration : domUConfiguration.getDomUDiskConfigurations()) {
						for(PhysicalVolumeConfiguration physicalVolumeConfiguration : domUDiskConfiguration.getPrimaryPhysicalVolumeConfigurations()) {
							PhysicalVolume physicalVolume = physicalVolumeConfiguration.getPhysicalVolume();
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
					if(domUConfiguration.getSecondaryDom0().getHostname().equals(dom0Disk.getDom0Hostname())) {
						assert domUConfiguration.getSecondaryDom0().getClusterName().equals(dom0Disk.getClusterName()) : "secondaryDom0.clusterName!=dom0Disk.clusterName";
						// Look only for secondary matches
						for(DomUDiskConfiguration domUDiskConfiguration : domUConfiguration.getDomUDiskConfigurations()) {
							for(PhysicalVolumeConfiguration physicalVolumeConfiguration : domUDiskConfiguration.getSecondaryPhysicalVolumeConfigurations()) {
								PhysicalVolume physicalVolume = physicalVolumeConfiguration.getPhysicalVolume();
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
			int overcommitDiskWeight = allocatedDiskWeight - 1024;
			AlertLevel alertLevel = overcommitDiskWeight>0 ? AlertLevel.MEDIUM : AlertLevel.NONE;
			if(alertLevel.compareTo(minimumAlertLevel)>=0) {
				return resultHandler.handleResult(
					new IntResult(
						"Allocated Weight",
						allocatedDiskWeight,
						1024,
						(double)overcommitDiskWeight / (double)1024,
						alertLevel
					)
				);
			}
		}
		return true;
	}

	/**
	 * Gets the unsorted, unmodifiable list of results per DomUDisk.
	 * 
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getDiskSpeedResults(ResultHandler<? super Integer> resultHandler, AlertLevel minimumAlertLevel) {
		if(minimumAlertLevel.compareTo(AlertLevel.MEDIUM)<=0) {
			List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
			for(int c=0, sizeC=domUConfigurations.size(); c<sizeC; c++) {
				DomUConfiguration domUConfiguration = domUConfigurations.get(c);
				// Must be either primary or secondary on this
				boolean isPrimary;
				if(domUConfiguration.getPrimaryDom0().getHostname().equals(dom0Disk.getDom0Hostname())) {
					assert domUConfiguration.getPrimaryDom0().getClusterName().equals(dom0Disk.getClusterName()) : "primaryDom0.clusterName!=dom0Disk.clusterName";
					isPrimary = true;
				} else if(domUConfiguration.getSecondaryDom0().getHostname().equals(dom0Disk.getDom0Hostname())) {
					assert domUConfiguration.getSecondaryDom0().getClusterName().equals(dom0Disk.getClusterName()) : "secondaryDom0.clusterName!=dom0Disk.clusterName";
					isPrimary = false;
				} else {
					continue;
				}
				List<DomUDiskConfiguration> domUDiskConfigurations = domUConfiguration.getDomUDiskConfigurations();
				for(int d=0, sizeD=domUDiskConfigurations.size(); d<sizeD; d++) {
					DomUDiskConfiguration domUDiskConfiguration = domUDiskConfigurations.get(d);
					DomUDisk domUDisk = domUDiskConfiguration.getDomUDisk();
					long totalExtents = domUDisk.getExtents();
					int minDiskSpeed = domUDisk.getMinimumDiskSpeed();
					long tooSlowExtents = 0;
					long extentsFound = 0;
					List<PhysicalVolumeConfiguration> physicalVolumeConfigurations;
					if(isPrimary) physicalVolumeConfigurations = domUDiskConfiguration.getPrimaryPhysicalVolumeConfigurations();
					else physicalVolumeConfigurations = domUDiskConfiguration.getSecondaryPhysicalVolumeConfigurations();
					for(int e=0, sizeE=physicalVolumeConfigurations.size(); e<sizeE; e++) {
						PhysicalVolumeConfiguration physicalVolumeConfiguration = physicalVolumeConfigurations.get(e);
						PhysicalVolume physicalVolume = physicalVolumeConfiguration.getPhysicalVolume();
						if(physicalVolume.getDevice().equals(dom0Disk.getDevice())) {
							assert physicalVolume.getClusterName().equals(dom0Disk.getClusterName()) : "physicalVolume.clusterName!=dom0Disk.clusterName";
							assert physicalVolume.getDom0Hostname().equals(dom0Disk.getDom0Hostname()) : "physicalVolume.dom0Hostname!=dom0Disk.dom0Hostname";
							// Found a match between DomUDisk and this Dom0Disk
							if(minDiskSpeed==-1) break;
							long pvExtents = physicalVolumeConfiguration.getExtents();
							int diskSpeed = dom0Disk.getDiskSpeed();
							if(diskSpeed<minDiskSpeed) tooSlowExtents += pvExtents;
							extentsFound += pvExtents;
							if(extentsFound>=totalExtents) break; // All extents found, can stop looking for more
						}
					}
					if(extentsFound>0) {
						AlertLevel alertLevel = minDiskSpeed!=-1 && tooSlowExtents>0 ? AlertLevel.MEDIUM : AlertLevel.NONE;
						if(alertLevel.compareTo(minimumAlertLevel)>=0) {
							if(!
								resultHandler.handleResult(
									new ObjectResult<Integer>(
										domUDisk.getDomUHostname() + ":" + domUDisk.getDevice(),
										minDiskSpeed==-1 ? null : Integer.valueOf(minDiskSpeed),
										null,
										(double)tooSlowExtents/(double)totalExtents,
										alertLevel
									)
								)
							) return false;
						}
					}
				}
			}
		}
		return true;
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
	 * @see AnalyzedClusterConfiguration#getAllResults(com.aoindustries.aoserv.cluster.analyze.ResultHandler, com.aoindustries.aoserv.cluster.analyze.AlertLevel)
	 * 
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getAllResults(ResultHandler<Object> resultHandler, AlertLevel minimumAlertLevel) {
		if(!getAllocatedWeightResult(resultHandler, minimumAlertLevel)) return false;
		if(!getDiskSpeedResults(resultHandler, minimumAlertLevel)) return false;
		return true;
	}
}
